package com.example.util

import com.example.data.model.Note
import org.json.JSONArray
import org.json.JSONObject

object ExternalNotesBackup {

    /**
     * Parses Google Keep Takeout JSON or exported notes format.
     * Google Keep Takeout JSON usually contains:
     * - "title": String
     * - "textContent": String (or "text" or "annotations")
     * - "labels": Array of {"name": "label"}
     * - "isPinned": Boolean
     * - "isArchived": Boolean
     */
    fun parseGoogleKeepJson(jsonString: String, currentUserId: String): List<Note> {
        val notes = mutableListOf<Note>()
        try {
            val trimmed = jsonString.trim()
            if (trimmed.startsWith("[")) {
                val array = JSONArray(trimmed)
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    notes.add(extractKeepNote(obj, currentUserId))
                }
            } else if (trimmed.startsWith("{")) {
                val obj = JSONObject(trimmed)
                if (obj.has("notes") && obj.get("notes") is JSONArray) {
                    val array = obj.getJSONArray("notes")
                    for (i in 0 until array.length()) {
                        notes.add(extractKeepNote(array.getJSONObject(i), currentUserId))
                    }
                } else {
                    notes.add(extractKeepNote(obj, currentUserId))
                }
            }
        } catch (e: Exception) {
            // If raw text or markdown is pasted instead of JSON
            notes.add(
                Note(
                    userId = currentUserId,
                    title = "Imported Keep Note",
                    content = jsonString,
                    category = "Keep Backup",
                    tags = listOf("KeepBackup", "Imported"),
                    colorTheme = "#00FF66",
                    noteType = "text"
                )
            )
        }
        return notes
    }

    private fun extractKeepNote(obj: JSONObject, userId: String): Note {
        val title = obj.optString("title", "Imported Note").ifBlank { "Untitled Keep Note" }
        val content = when {
            obj.has("textContent") -> obj.optString("textContent")
            obj.has("text") -> obj.optString("text")
            obj.has("content") -> obj.optString("content")
            obj.has("listContent") -> {
                val items = obj.optJSONArray("listContent")
                if (items != null) {
                    (0 until items.length()).mapNotNull { idx ->
                        val item = items.optJSONObject(idx)
                        val text = item?.optString("text", "") ?: ""
                        val isChecked = item?.optBoolean("isChecked", false) ?: false
                        if (text.isNotBlank()) "- [${if (isChecked) "x" else " "}] $text" else null
                    }.joinToString("\n")
                } else ""
            }
            else -> ""
        }

        val tags = mutableListOf("KeepBackup")
        if (obj.has("labels")) {
            val labels = obj.optJSONArray("labels")
            if (labels != null) {
                for (j in 0 until labels.length()) {
                    val labelObj = labels.optJSONObject(j)
                    val labelName = labelObj?.optString("name") ?: labels.optString(j)
                    if (!labelName.isNullOrBlank()) tags.add(labelName)
                }
            }
        }

        val isPinned = obj.optBoolean("isPinned", false)
        val isArchived = obj.optBoolean("isArchived", false)
        val color = obj.optString("color", "#00FF66")

        return Note(
            userId = userId,
            title = title,
            content = content,
            category = "Keep Backup",
            tags = tags,
            colorTheme = if (color.startsWith("#")) color else "#00FF66",
            isPinned = isPinned,
            isArchived = isArchived,
            noteType = "text"
        )
    }

    /**
     * Generate sample Google Keep notes migration payload
     * so user can immediately test "Others Notes Backup" with realistic data
     */
    fun getSampleKeepBackupData(): String {
        return """
        [
          {
            "title": "Python Automation Routine",
            "textContent": "Daily server health check script:\nimport os, sys\nprint('SYS.CHECK: ALL SYSTEMS OPERATIONAL')\nprint('OPERATOR: ANAMOL')",
            "isPinned": true,
            "labels": [{"name": "DevOps"}, {"name": "Python"}]
          },
          {
            "title": "Quantum Cryptography References",
            "textContent": "Notes on Caesar shift 7, XOR keys, and quantum key distribution channels.",
            "isPinned": false,
            "labels": [{"name": "Security"}, {"name": "Research"}]
          },
          {
            "title": "Personal Goals - Anamol",
            "textContent": "- [x] Master Jetpack Compose\n- [x] Configure Firebase Cloud Sync\n- [ ] Deploy Anmol Notes Suite\n- [ ] Integrate Jarvis Multimodal Intelligence",
            "isPinned": true,
            "labels": [{"name": "Goals"}]
          }
        ]
        """.trimIndent()
    }

    /**
     * Export all notes as clean JSON backup string
     */
    fun exportNotesToJson(notes: List<Note>): String {
        val array = JSONArray()
        for (note in notes) {
            val obj = JSONObject()
            obj.put("title", note.title)
            obj.put("content", note.content)
            obj.put("category", note.category)
            obj.put("tags", JSONArray(note.tags))
            obj.put("colorTheme", note.colorTheme)
            obj.put("noteType", note.noteType)
            obj.put("isVault", note.isVault)
            obj.put("isPinned", note.isPinned)
            obj.put("isArchived", note.isArchived)
            obj.put("createdAt", note.createdAt?.toDate()?.toString())
            array.put(obj)
        }
        return array.toString(2)
    }

    /**
     * Export a single note as Markdown string
     */
    fun exportNoteToMarkdown(note: Note): String {
        return buildString {
            append("# ${note.title}\n\n")
            append("> **Category**: ${note.category} | **Tags**: ${note.tags.joinToString(", ")}\n")
            append("> **Author**: Anamol (Anmol Notes)\n\n")
            append("---\n\n")
            if (note.noteType == "python") {
                append("```python\n")
                append(note.content)
                append("\n```\n")
            } else {
                append(note.content)
            }
        }
    }
}
