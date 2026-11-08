package com.example.ui.notes

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ai.JarvisService
import com.example.data.model.Note
import com.example.data.repository.NotesRepository
import com.example.ui.theme.CyberBlack
import com.example.ui.theme.CyberCardBorder
import com.example.ui.theme.CyberDark
import com.example.ui.theme.CyberSurface
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonPink
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.NeonYellow
import com.example.ui.theme.TextBright
import com.example.ui.theme.TextDim
import com.example.ui.theme.VaultRed
import com.example.util.ExternalNotesBackup
import kotlinx.coroutines.launch

@Composable
fun NoteEditorScreen(
    note: Note?,
    repository: NotesRepository,
    currentUserId: String,
    onBack: () -> Unit
) {
    BackHandler { onBack() }

    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val scope = rememberCoroutineScope()
    val jarvisService = remember { JarvisService() }

    var title by remember { mutableStateOf(note?.title ?: "") }
    var content by remember { mutableStateOf(note?.content ?: "") }
    var category by remember { mutableStateOf(note?.category ?: "General") }
    var tagsInput by remember { mutableStateOf(note?.tags?.joinToString(", ") ?: "AnamolNotes") }
    var colorTheme by remember { mutableStateOf(note?.colorTheme ?: "#00FF66") }
    var isVault by remember { mutableStateOf(note?.isVault ?: false) }
    var isPinned by remember { mutableStateOf(note?.isPinned ?: false) }
    var noteType by remember { mutableStateOf(note?.noteType ?: "text") }

    var isPreviewMode by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }
    var isJarvisAnalyzing by remember { mutableStateOf(false) }
    var exportDialogText by remember { mutableStateOf<String?>(null) }

    val colorPresets = listOf(
        "#00FF66" to "Matrix Green",
        "#00F0FF" to "Cyber Cyan",
        "#FF0055" to "Neon Red",
        "#FF9900" to "Deep Amber",
        "#B026FF" to "Neon Purple",
        "#111417" to "Dark Slate"
    )

    val categories = listOf("General", "Python", "Security", "Algorithms", "Keep Backup", "Work", "Vault")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CyberBlack)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Top Action Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = NeonGreen
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Markdown Preview Toggle
                IconButton(onClick = { isPreviewMode = !isPreviewMode }) {
                    Icon(
                        imageVector = if (isPreviewMode) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = "Toggle Preview",
                        tint = if (isPreviewMode) NeonCyan else TextDim
                    )
                }

                // Export Note Button
                IconButton(onClick = {
                    val currentNote = Note(
                        id = note?.id ?: "",
                        userId = currentUserId,
                        title = title,
                        content = content,
                        category = category,
                        tags = tagsInput.split(",").map { it.trim() }.filter { it.isNotBlank() },
                        colorTheme = colorTheme,
                        noteType = noteType
                    )
                    exportDialogText = ExternalNotesBackup.exportNoteToMarkdown(currentNote)
                }) {
                    Icon(
                        imageVector = Icons.Default.FileDownload,
                        contentDescription = "Export Note",
                        tint = NeonAmber
                    )
                }

                // Save Note Button
                Button(
                    onClick = {
                        if (title.isBlank() && content.isBlank()) {
                            Toast.makeText(context, "Note is empty", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        isSaving = true
                        scope.launch {
                            val tagsList = tagsInput.split(",").map { it.trim() }.filter { it.isNotBlank() }
                            val noteToSave = (note ?: Note()).copy(
                                userId = currentUserId,
                                title = title.ifBlank { "Untitled Note" },
                                content = content,
                                category = category,
                                tags = if (tagsList.isEmpty()) listOf("AnamolNotes") else tagsList,
                                colorTheme = colorTheme,
                                isVault = isVault,
                                isPinned = isPinned,
                                noteType = noteType
                            )
                            repository.saveNote(noteToSave)
                            isSaving = false
                            Toast.makeText(context, "Saved & Synced to Cloud!", Toast.LENGTH_SHORT).show()
                            onBack()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonGreen, contentColor = CyberBlack),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("save_note_action_button")
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = CyberBlack, strokeWidth = 2.dp)
                    } else {
                        Icon(imageVector = Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("SAVE", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Title Input
        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Note Title", color = TextDim) },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("note_title_input"),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = NeonGreen,
                unfocusedBorderColor = CyberCardBorder,
                focusedTextColor = TextBright,
                unfocusedTextColor = TextBright,
                focusedContainerColor = CyberDark,
                unfocusedContainerColor = CyberDark
            ),
            shape = RoundedCornerShape(8.dp)
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Category Selector Chips
        Text(text = "FOLDER / CATEGORY:", style = MaterialTheme.typography.labelSmall, color = NeonCyan, fontSize = 10.sp)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            categories.forEach { cat ->
                val isSelected = category == cat
                FilterChip(
                    selected = isSelected,
                    onClick = { category = cat },
                    label = { Text(cat, fontSize = 11.sp, color = if (isSelected) NeonGreen else TextDim) },
                    colors = FilterChipDefaults.filterChipColors(containerColor = CyberDark),
                    border = FilterChipDefaults.filterChipBorder(borderColor = if (isSelected) NeonGreen else CyberCardBorder, enabled = true, selected = isSelected)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Tags Input & Ask Jarvis to Auto-Categorize
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = tagsInput,
                onValueChange = { tagsInput = it },
                label = { Text("Tags (comma separated)", color = TextDim) },
                singleLine = true,
                modifier = Modifier.weight(1f),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NeonGreen,
                    unfocusedBorderColor = CyberCardBorder,
                    focusedTextColor = TextBright,
                    unfocusedTextColor = TextBright,
                    focusedContainerColor = CyberDark,
                    unfocusedContainerColor = CyberDark
                ),
                shape = RoundedCornerShape(8.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            // Jarvis Smart Organize Button
            Button(
                onClick = {
                    if (title.isBlank() && content.isBlank()) {
                        Toast.makeText(context, "Write some content first", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    isJarvisAnalyzing = true
                    scope.launch {
                        val result = jarvisService.autoCategorizeAndTag(title, content)
                        category = result.category
                        tagsInput = result.tags.joinToString(", ")
                        isJarvisAnalyzing = false
                        Toast.makeText(context, "Jarvis: Categorized as ${result.category}", Toast.LENGTH_SHORT).show()
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = NeonCyan.copy(alpha = 0.2f),
                    contentColor = NeonCyan
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("jarvis_categorize_button")
            ) {
                if (isJarvisAnalyzing) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), color = NeonCyan, strokeWidth = 2.dp)
                } else {
                    Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("JARVIS TAG", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Color & Security Options
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Color Presets
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                colorPresets.forEach { (hex, _) ->
                    val color = Color(android.graphics.Color.parseColor(hex))
                    val isSelected = colorTheme == hex
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .background(color, CircleShape)
                            .border(
                                width = if (isSelected) 2.dp else 0.5.dp,
                                color = if (isSelected) TextBright else CyberCardBorder,
                                shape = CircleShape
                            )
                            .clickable { colorTheme = hex }
                    )
                }
            }

            // Pin & Vault Toggles
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { isPinned = !isPinned }) {
                    Icon(
                        imageVector = Icons.Default.PushPin,
                        contentDescription = "Pin Note",
                        tint = if (isPinned) NeonYellow else TextDim
                    )
                }

                IconButton(onClick = { isVault = !isVault }) {
                    Icon(
                        imageVector = if (isVault) Icons.Default.Lock else Icons.Default.LockOpen,
                        contentDescription = "Secure Vault",
                        tint = if (isVault) VaultRed else TextDim
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Content Area or Markdown Preview
        if (isPreviewMode) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(350.dp)
                    .border(1.dp, NeonCyan.copy(alpha = 0.4f), RoundedCornerShape(8.dp)),
                color = CyberDark,
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp).verticalScroll(rememberScrollState())) {
                    Text(text = "MARKDOWN PREVIEW", style = MaterialTheme.typography.labelSmall, color = NeonCyan)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = content.ifBlank { "(Empty note)" },
                        color = TextBright,
                        style = MaterialTheme.typography.bodyMedium,
                        lineHeight = 22.sp
                    )
                }
            }
        } else {
            OutlinedTextField(
                value = content,
                onValueChange = { content = it },
                label = { Text("Note Content (Markdown supported)", color = TextDim) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(350.dp)
                    .testTag("note_content_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NeonGreen,
                    unfocusedBorderColor = CyberCardBorder,
                    focusedTextColor = TextBright,
                    unfocusedTextColor = TextBright,
                    focusedContainerColor = CyberDark,
                    unfocusedContainerColor = CyberDark
                ),
                shape = RoundedCornerShape(8.dp)
            )
        }
    }

    // Export Dialog
    if (exportDialogText != null) {
        AlertDialog(
            onDismissRequest = { exportDialogText = null },
            confirmButton = {
                Button(
                    onClick = {
                        clipboardManager.setText(AnnotatedString(exportDialogText!!))
                        Toast.makeText(context, "Export copied to clipboard!", Toast.LENGTH_SHORT).show()
                        exportDialogText = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonGreen, contentColor = CyberBlack)
                ) {
                    Text("COPY MARKDOWN")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { exportDialogText = null }) {
                    Text("CLOSE", color = TextDim)
                }
            },
            containerColor = CyberDark,
            title = { Text("EXPORT NOTE", color = NeonGreen, fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = exportDialogText!!,
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth().height(220.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonGreen,
                        unfocusedBorderColor = CyberCardBorder,
                        focusedTextColor = TextBright,
                        unfocusedTextColor = TextBright,
                        focusedContainerColor = CyberBlack,
                        unfocusedContainerColor = CyberBlack
                    )
                )
            }
        )
    }
}
