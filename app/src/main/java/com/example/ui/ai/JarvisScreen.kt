package com.example.ui.ai

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ai.JarvisService
import com.example.data.model.JarvisMessage
import com.example.data.model.Note
import com.example.data.repository.NotesRepository
import com.example.ui.theme.CodeBackground
import com.example.ui.theme.CyberBlack
import com.example.ui.theme.CyberCardBorder
import com.example.ui.theme.CyberDark
import com.example.ui.theme.CyberSurface
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonPink
import com.example.ui.theme.TextBright
import com.example.ui.theme.TextDim
import kotlinx.coroutines.launch

@Composable
fun JarvisScreen(
    repository: NotesRepository,
    currentUserId: String,
    onOpenPythonWorkspace: (String, String) -> Unit = { _, _ -> }
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val scope = rememberCoroutineScope()
    val jarvisService = remember { JarvisService() }

    val messages by repository.observeJarvisMessages().collectAsState(initial = emptyList())
    val userSettings by repository.observeSettings().collectAsState(
        initial = com.example.data.model.UserSettings(userId = currentUserId, userName = "Anamol")
    )
    var inputText by remember { mutableStateOf("") }
    var isThinking by remember { mutableStateOf(false) }

    val listState = rememberLazyListState()

    LaunchedEffect(messages.size, isThinking) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CyberBlack)
    ) {
        // Jarvis Header Bar
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = CyberDark,
            shadowElevation = 4.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(
                                Brush.linearGradient(listOf(NeonCyan, NeonGreen)),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.SmartToy,
                            contentDescription = "Jarvis AI",
                            tint = CyberBlack,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "JARVIS // AI ASSISTANT",
                                style = MaterialTheme.typography.titleMedium,
                                color = NeonGreen,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (userSettings.geminiApiKey.isNotBlank()) "[API KEY]" else "[FIREBASE AI]",
                                color = if (userSettings.geminiApiKey.isNotBlank()) NeonGreen else NeonCyan,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = "Bilingual English & Hindi // Operator: Anamol",
                            style = MaterialTheme.typography.labelSmall,
                            color = NeonCyan,
                            fontSize = 10.sp
                        )
                    }
                }

                if (messages.isNotEmpty()) {
                    IconButton(
                        onClick = {
                            scope.launch {
                                repository.clearJarvisHistory()
                                Toast.makeText(context, "Chat history cleared", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Clear Chat",
                            tint = TextDim,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        // Quick prompt chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(CyberBlack)
                .padding(horizontal = 12.dp, vertical = 8.dp)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(
                "Write Python web scraper",
                "Python me factorial script banao",
                "Explain Caesar Cipher in Hindi",
                "Smart organize and tag my notes",
                "Create AES & XOR encryption snippet"
            ).forEach { prompt ->
                FilterChip(
                    selected = false,
                    onClick = { inputText = prompt },
                    label = {
                        Text(
                            text = prompt,
                            fontSize = 11.sp,
                            color = NeonCyan
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(containerColor = CyberDark),
                    border = FilterChipDefaults.filterChipBorder(borderColor = CyberCardBorder, enabled = true, selected = false)
                )
            }
        }

        // Messages list
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (messages.isEmpty() && !isThinking) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = NeonGreen.copy(alpha = 0.6f),
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Jarvis Neural System Online",
                                style = MaterialTheme.typography.titleMedium,
                                color = NeonGreen
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Namaste Anamol! Ask any technical problem, request Python scripts in English or Hindi, or ask me to organize your notes.",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextDim,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 24.dp)
                            )
                        }
                    }
                }
            }

            items(messages) { msg ->
                val isUser = msg.sender == "user"
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
                ) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth(0.92f)
                            .border(
                                width = 1.dp,
                                color = if (isUser) NeonGreen.copy(alpha = 0.5f) else NeonCyan.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(12.dp)
                            ),
                        color = if (isUser) CyberSurface else CyberDark,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (isUser) "OPERATOR ANAMOL" else "JARVIS // AI",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isUser) NeonGreen else NeonCyan,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )

                                if (!isUser) {
                                    Row {
                                        IconButton(
                                            onClick = {
                                                val copyText = buildString {
                                                    append(msg.message)
                                                    if (!msg.pythonCode.isNullOrBlank()) {
                                                        append("\n\n")
                                                        append(msg.pythonCode)
                                                    }
                                                }
                                                clipboardManager.setText(AnnotatedString(copyText))
                                                Toast.makeText(context, "Copied response", Toast.LENGTH_SHORT).show()
                                            },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.ContentCopy,
                                                contentDescription = "Copy",
                                                tint = TextDim,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }

                                        IconButton(
                                            onClick = {
                                                scope.launch {
                                                    val savedTitle = msg.suggestedCategory?.let { "$it - Jarvis Script" } ?: "Jarvis Generated Note"
                                                    val noteContent = buildString {
                                                        append(msg.message)
                                                        if (!msg.pythonCode.isNullOrBlank()) {
                                                            append("\n\n```python\n")
                                                            append(msg.pythonCode)
                                                            append("\n```")
                                                        }
                                                    }
                                                    val note = Note(
                                                        userId = currentUserId,
                                                        title = savedTitle,
                                                        content = noteContent,
                                                        category = msg.suggestedCategory ?: "Python",
                                                        tags = listOf("JarvisAI", "Python"),
                                                        noteType = if (msg.pythonCode != null) "python" else "text"
                                                    )
                                                    repository.saveNote(note)
                                                    Toast.makeText(context, "Saved to Cloud Notes!", Toast.LENGTH_SHORT).show()
                                                }
                                            },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Save,
                                                contentDescription = "Save as Note",
                                                tint = NeonGreen,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = msg.message,
                                color = TextBright,
                                style = MaterialTheme.typography.bodyMedium,
                                lineHeight = 20.sp
                            )

                            // Render Python Code Block if present
                            if (!msg.pythonCode.isNullOrBlank()) {
                                Spacer(modifier = Modifier.height(10.dp))

                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(1.dp, NeonGreen.copy(alpha = 0.4f), RoundedCornerShape(6.dp)),
                                    color = CodeBackground,
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Column(modifier = Modifier.padding(8.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "PYTHON SNIPPET",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = NeonGreen,
                                                fontSize = 10.sp
                                            )

                                            Button(
                                                onClick = {
                                                    onOpenPythonWorkspace("jarvis_script.py", msg.pythonCode)
                                                },
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = NeonGreen.copy(alpha = 0.2f),
                                                    contentColor = NeonGreen
                                                ),
                                                shape = RoundedCornerShape(4.dp),
                                                modifier = Modifier.height(28.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.PlayArrow,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("OPEN IN WORKSPACE", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(6.dp))

                                        Text(
                                            text = msg.pythonCode,
                                            color = NeonGreen,
                                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                            style = MaterialTheme.typography.bodySmall,
                                            fontSize = 12.sp,
                                            lineHeight = 16.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (isThinking) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = NeonCyan,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Jarvis is processing for Operator Anamol...",
                            color = NeonCyan,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        val sendAction = {
            if (inputText.isNotBlank() && !isThinking) {
                val text = inputText
                inputText = ""
                scope.launch {
                    isThinking = true
                    val userMsg = JarvisMessage(
                        userId = currentUserId,
                        sender = "user",
                        message = text
                    )
                    repository.saveJarvisMessage(userMsg)

                    val response = jarvisService.chat(text, messages, userSettings.geminiApiKey)

                    val jarvisMsg = JarvisMessage(
                        userId = currentUserId,
                        sender = "jarvis",
                        message = response.replyText,
                        pythonCode = response.pythonCode,
                        suggestedCategory = response.suggestedCategory
                    )
                    repository.saveJarvisMessage(jarvisMsg)
                    isThinking = false
                }
            }
        }

        // Input text bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(CyberDark)
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                placeholder = { Text("Ask Jarvis (English / Hindi / Hinglish)...", color = TextDim, fontSize = 13.sp) },
                modifier = Modifier
                    .weight(1f)
                    .testTag("jarvis_chat_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NeonGreen,
                    unfocusedBorderColor = CyberCardBorder,
                    focusedTextColor = TextBright,
                    unfocusedTextColor = TextBright,
                    focusedContainerColor = CyberBlack,
                    unfocusedContainerColor = CyberBlack
                ),
                shape = RoundedCornerShape(8.dp),
                maxLines = 3,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = { sendAction() })
            )

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(
                onClick = { sendAction() },
                enabled = inputText.isNotBlank() && !isThinking,
                modifier = Modifier
                    .size(46.dp)
                    .background(
                        if (inputText.isNotBlank() && !isThinking) NeonGreen else CyberSurface,
                        RoundedCornerShape(8.dp)
                    )
                    .testTag("jarvis_send_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Send",
                    tint = if (inputText.isNotBlank() && !isThinking) CyberBlack else TextDim
                )
            }
        }
    }
}
