package com.example.data.ai

import android.util.Log
import com.example.data.model.JarvisMessage
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.Content
import com.google.firebase.ai.type.content
import com.google.firebase.ai.type.generationConfig
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

private const val TAG = "JarvisService"

data class JarvisResponse(
    val replyText: String,
    val pythonCode: String? = null,
    val suggestedCategory: String? = null,
    val noteTitle: String? = null,
    val tags: List<String> = emptyList()
)

data class AutoCategoryResult(
    val category: String,
    val tags: List<String>,
    val summary: String
)

class JarvisService {

    private val okHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(45, TimeUnit.SECONDS)
            .build()
    }

    private val firebaseModel by lazy {
        try {
            Firebase.ai.generativeModel(
                modelName = "gemini-3.8-flash",
                generationConfig = generationConfig {
                    temperature = 0.7f
                }
            )
        } catch (e: Exception) {
            Log.w(TAG, "Firebase AI init deferred or failed: ${e.message}")
            null
        }
    }

    private val systemPrompt = """
        You are "Jarvis", an elite hacker, developer, and productivity AI assistant integrated into "Anmol Notes" crafted specifically for your operator "Anamol".
        Capabilities:
        1. Language: You are fully bilingual. You fluently understand and speak English, pure Hindi (हिंदी), and conversational Hinglish based on how Anamol speaks to you.
        2. Problem Solving: When Anamol describes a problem or request, provide a concise, sharp technical explanation, and ALWAYS provide clean, production-ready, fully commented Python code commands or shell snippets where appropriate.
        3. Note Generation: When asked to draft, explain, or code, format code cleanly in ```python ... ``` blocks.
        4. Auto Categorization: Suggest smart folders (e.g., Python Scripts, Web Dev, Security & Ciphers, Algorithms, Keep Backup, General Notes) and relevant tags.
        Address the user respectfully as "Anamol" or "Operator Anamol".
    """.trimIndent()

    /**
     * Test a user-provided Gemini API Key
     */
    suspend fun testApiKey(apiKey: String): Pair<Boolean, String> = withContext(Dispatchers.IO) {
        if (apiKey.isBlank()) {
            return@withContext Pair(false, "API Key is empty. Please enter a valid Gemini API Key.")
        }
        try {
            val payload = JSONObject().apply {
                val contentsArr = JSONArray().apply {
                    put(JSONObject().apply {
                        put("role", "user")
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply { put("text", "Ping. Confirm connection for Operator Anamol in 5 words.") })
                        })
                    })
                }
                put("contents", contentsArr)
            }

            val request = Request.Builder()
                .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=${apiKey.trim()}")
                .post(payload.toString().toRequestBody("application/json".toMediaType()))
                .build()

            val response = okHttpClient.newCall(request).execute()
            val responseBody = response.body?.string().orEmpty()

            if (response.isSuccessful) {
                Pair(true, "API Key verified! Connected to Gemini Neural Engine.")
            } else {
                val errorMsg = try {
                    val json = JSONObject(responseBody)
                    json.optJSONObject("error")?.optString("message") ?: "HTTP ${response.code}"
                } catch (_: Exception) {
                    "HTTP ${response.code}"
                }
                Pair(false, "Verification failed: $errorMsg")
            }
        } catch (e: Exception) {
            Pair(false, "Connection error: ${e.localizedMessage ?: "Unknown error"}")
        }
    }

    suspend fun chat(
        userMessage: String,
        history: List<JarvisMessage>,
        apiKey: String? = null
    ): JarvisResponse = withTimeout(60_000L) {
        val trimmedKey = apiKey?.trim().orEmpty()

        // 1. If user provided a custom Gemini API key, use direct REST API
        if (trimmedKey.isNotBlank()) {
            val directResult = callGeminiDirectRest(userMessage, history, trimmedKey)
            if (directResult != null) return@withTimeout directResult
        }

        // 2. Otherwise try Firebase AI
        try {
            val model = firebaseModel
            if (model != null) {
                val contentList = mutableListOf<Content>()
                contentList.add(content("user") { text(systemPrompt) })
                contentList.add(content("model") { text("Understood, Operator Anamol. Jarvis is online and ready. How may I assist your systems today?") })

                val recentHistory = history.takeLast(8)
                for (msg in recentHistory) {
                    if (msg.sender == "user") {
                        contentList.add(content("user") { text(msg.message) })
                    } else {
                        val fullModelText = buildString {
                            append(msg.message)
                            if (!msg.pythonCode.isNullOrBlank()) {
                                append("\n\n```python\n${msg.pythonCode}\n```")
                            }
                        }
                        contentList.add(content("model") { text(fullModelText) })
                    }
                }

                contentList.add(content("user") { text(userMessage) })

                val response = model.generateContent(contentList)
                val rawText = response.text ?: "Jarvis received your prompt, Operator Anamol."

                val pythonCode = extractPythonBlock(rawText)
                val suggestedCategory = detectCategoryFromText(rawText + " " + userMessage)

                return@withTimeout JarvisResponse(
                    replyText = rawText.trim(),
                    pythonCode = pythonCode,
                    suggestedCategory = suggestedCategory,
                    noteTitle = deriveTitle(userMessage),
                    tags = listOf("JarvisAI", suggestedCategory.lowercase().replace(" ", "_"))
                )
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.w(TAG, "Firebase AI chat failed: ${e.message}")
        }

        // 3. Fallback message if neither succeeded
        JarvisResponse(
            replyText = "Jarvis Notice: Operator Anamol, unable to reach the Gemini AI model. Please verify your Gemini API Key in 'Settings > API Key Configuration' or check your network connection."
        )
    }

    private suspend fun callGeminiDirectRest(
        userMessage: String,
        history: List<JarvisMessage>,
        apiKey: String
    ): JarvisResponse? = withContext(Dispatchers.IO) {
        try {
            val contentsArr = JSONArray()

            // System prompt as first turn or system_instruction
            val recentHistory = history.takeLast(8)
            for (msg in recentHistory) {
                val role = if (msg.sender == "user") "user" else "model"
                val text = buildString {
                    append(msg.message)
                    if (!msg.pythonCode.isNullOrBlank()) {
                        append("\n\n```python\n${msg.pythonCode}\n```")
                    }
                }
                contentsArr.put(JSONObject().apply {
                    put("role", role)
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply { put("text", text) })
                    })
                })
            }

            // Current message
            contentsArr.put(JSONObject().apply {
                put("role", "user")
                put("parts", JSONArray().apply {
                    put(JSONObject().apply { put("text", userMessage) })
                })
            })

            val payload = JSONObject().apply {
                put("system_instruction", JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply { put("text", systemPrompt) })
                    })
                })
                put("contents", contentsArr)
                put("generationConfig", JSONObject().apply {
                    put("temperature", 0.7)
                })
            }

            val request = Request.Builder()
                .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=$apiKey")
                .post(payload.toString().toRequestBody("application/json".toMediaType()))
                .build()

            val response = okHttpClient.newCall(request).execute()
            val responseBody = response.body?.string().orEmpty()

            if (response.isSuccessful) {
                val json = JSONObject(responseBody)
                val candidates = json.optJSONArray("candidates")
                if (candidates != null && candidates.length() > 0) {
                    val candidate = candidates.getJSONObject(0)
                    val contentObj = candidate.optJSONObject("content")
                    val parts = contentObj?.optJSONArray("parts")
                    if (parts != null && parts.length() > 0) {
                        val rawText = parts.getJSONObject(0).optString("text")
                        val pythonCode = extractPythonBlock(rawText)
                        val suggestedCategory = detectCategoryFromText(rawText + " " + userMessage)
                        return@withContext JarvisResponse(
                            replyText = rawText.trim(),
                            pythonCode = pythonCode,
                            suggestedCategory = suggestedCategory,
                            noteTitle = deriveTitle(userMessage),
                            tags = listOf("JarvisAI", suggestedCategory.lowercase().replace(" ", "_"))
                        )
                    }
                }
            } else {
                Log.w(TAG, "Gemini REST error: HTTP ${response.code} $responseBody")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Gemini direct REST call failed", e)
        }
        null
    }

    suspend fun autoCategorizeAndTag(
        title: String,
        content: String,
        apiKey: String? = null
    ): AutoCategoryResult = withTimeout(30_000L) {
        val prompt = """
            Analyze this note for Operator Anamol in Anmol Notes:
            Title: $title
            Content snippet: ${content.take(500)}

            Respond in valid JSON only with keys:
            "category": (string e.g. "Python Scripts", "Security & Ciphers", "Algorithms", "General Notes", "Personal", "Research")
            "tags": (array of 2 to 4 string tags)
            "summary": (short one sentence summary)
        """.trimIndent()

        val trimmedKey = apiKey?.trim().orEmpty()
        if (trimmedKey.isNotBlank()) {
            val restResult = callGeminiCategorizeDirect(prompt, trimmedKey)
            if (restResult != null) return@withTimeout restResult
        }

        try {
            val model = firebaseModel
            if (model != null) {
                val response = model.generateContent(prompt)
                val text = response.text.orEmpty()
                val parsed = parseCategoryJson(text)
                if (parsed != null) return@withTimeout parsed
            }
        } catch (e: Exception) {
            Log.w(TAG, "Auto-categorize fallback: ${e.message}")
        }

        // Fallback rule-based
        val fallbackCat = if (content.contains("def ") || content.contains("import ") || title.endsWith(".py")) {
            "Python Scripts"
        } else if (content.contains("cipher") || content.contains("morse") || content.contains("base64")) {
            "Security & Ciphers"
        } else {
            "General Notes"
        }
        AutoCategoryResult(category = fallbackCat, tags = listOf("AnamolNote", "Organized"), summary = "Smart organized note")
    }

    private suspend fun callGeminiCategorizeDirect(prompt: String, apiKey: String): AutoCategoryResult? = withContext(Dispatchers.IO) {
        try {
            val payload = JSONObject().apply {
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("role", "user")
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply { put("text", prompt) })
                        })
                    })
                })
            }
            val request = Request.Builder()
                .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=$apiKey")
                .post(payload.toString().toRequestBody("application/json".toMediaType()))
                .build()

            val response = okHttpClient.newCall(request).execute()
            val responseBody = response.body?.string().orEmpty()
            if (response.isSuccessful) {
                val json = JSONObject(responseBody)
                val candidates = json.optJSONArray("candidates")
                if (candidates != null && candidates.length() > 0) {
                    val rawText = candidates.getJSONObject(0).optJSONObject("content")?.optJSONArray("parts")?.getJSONObject(0)?.optString("text").orEmpty()
                    return@withContext parseCategoryJson(rawText)
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Direct categorize failed: ${e.message}")
        }
        null
    }

    private fun parseCategoryJson(text: String): AutoCategoryResult? {
        val jsonStart = text.indexOf('{')
        val jsonEnd = text.lastIndexOf('}')
        if (jsonStart != -1 && jsonEnd != -1 && jsonEnd > jsonStart) {
            try {
                val json = JSONObject(text.substring(jsonStart, jsonEnd + 1))
                val cat = json.optString("category", "General Notes")
                val tagsArr = json.optJSONArray("tags")
                val tags = mutableListOf<String>()
                if (tagsArr != null) {
                    for (i in 0 until tagsArr.length()) {
                        tags.add(tagsArr.getString(i))
                    }
                }
                val summary = json.optString("summary", "Processed note")
                return AutoCategoryResult(category = cat, tags = tags, summary = summary)
            } catch (_: Exception) {}
        }
        return null
    }

    private fun extractPythonBlock(text: String): String? {
        val pattern = Regex("```python\\s*([\\s\\S]*?)```", RegexOption.IGNORE_CASE)
        val match = pattern.find(text)
        if (match != null) {
            return match.groupValues[1].trim()
        }
        val genericPattern = Regex("```\\s*([\\s\\S]*?)```")
        val genericMatch = genericPattern.find(text)
        if (genericMatch != null) {
            val code = genericMatch.groupValues[1].trim()
            if (code.contains("def ") || code.contains("import ") || code.contains("print(") || code.contains("class ")) {
                return code
            }
        }
        return null
    }

    private fun detectCategoryFromText(text: String): String {
        val lower = text.lowercase()
        return when {
            lower.contains("python") || lower.contains("script") || lower.contains("def ") -> "Python Scripts"
            lower.contains("crypto") || lower.contains("cipher") || lower.contains("encrypt") || lower.contains("morse") -> "Security & Ciphers"
            lower.contains("algorithm") || lower.contains("math") || lower.contains("sorting") -> "Algorithms"
            lower.contains("keep") || lower.contains("backup") || lower.contains("import") -> "Keep Backup"
            lower.contains("web") || lower.contains("api") || lower.contains("http") -> "Web & APIs"
            else -> "General Notes"
        }
    }

    private fun deriveTitle(prompt: String): String {
        val words = prompt.trim().split(" ")
        return words.take(5).joinToString(" ").capitalizeWords()
    }

    private fun String.capitalizeWords(): String {
        return split(" ").joinToString(" ") { it.replaceFirstChar { char -> char.uppercase() } }
    }
}
