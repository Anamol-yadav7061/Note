package com.example.ui.backup

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Note
import com.example.data.repository.NotesRepository
import com.example.ui.theme.CyberBlack
import com.example.ui.theme.CyberCardBorder
import com.example.ui.theme.CyberDark
import com.example.ui.theme.CyberSurface
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonYellow
import com.example.ui.theme.TextBright
import com.example.ui.theme.TextDim
import com.example.util.ExternalNotesBackup
import kotlinx.coroutines.launch

@Composable
fun OthersNotesBackupDialog(
    repository: NotesRepository,
    currentUserId: String,
    allNotes: List<Note>,
    onDismiss: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var selectedPlatform by remember { mutableStateOf("Google Keep") }
    var inputPayload by remember { mutableStateOf("") }
    var isImporting by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf<String?>(null) }
    var isExportDialog by remember { mutableStateOf(false) }
    var exportedData by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = CyberDark,
                    contentColor = TextBright
                )
            ) {
                Text("CLOSE")
            }
        },
        containerColor = CyberDark,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.CloudDownload,
                    contentDescription = null,
                    tint = NeonGreen,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "OTHERS NOTES BACKUP",
                    style = MaterialTheme.typography.titleLarge,
                    color = NeonGreen,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Seamless external migration tool: Import notes from Google Keep, Markdown archives, or JSON backups into your cloud database for Anamol.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextDim
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Platform selector chips
                Text(
                    text = "SELECT SOURCE PLATFORM:",
                    style = MaterialTheme.typography.labelSmall,
                    color = NeonCyan,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Google Keep", "JSON Backup", "Markdown").forEach { platform ->
                        val isSelected = selectedPlatform == platform
                        OutlinedButton(
                            onClick = { selectedPlatform = platform },
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (isSelected) NeonGreen.copy(alpha = 0.15f) else CyberSurface,
                                contentColor = if (isSelected) NeonGreen else TextDim
                            ),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isSelected) NeonGreen else CyberCardBorder
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = platform,
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 10.sp,
                                maxLines = 1
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Action helper to load sample Keep data
                if (selectedPlatform == "Google Keep") {
                    Button(
                        onClick = {
                            inputPayload = ExternalNotesBackup.getSampleKeepBackupData()
                            statusMessage = "Loaded sample Google Keep takeout export for Operator Anamol."
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NeonCyan.copy(alpha = 0.2f),
                            contentColor = NeonCyan
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("load_sample_keep_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.SmartToy,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "PRELOAD SAMPLE KEEP NOTES EXPORT",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = inputPayload,
                    onValueChange = { inputPayload = it },
                    label = { Text("Paste $selectedPlatform Payload / Content") },
                    placeholder = {
                        Text(
                            when (selectedPlatform) {
                                "Google Keep" -> "Paste Google Keep Takeout JSON or text..."
                                "JSON Backup" -> "[{\"title\": \"...\", \"content\": \"...\"}]"
                                else -> "# Note Title\nMarkdown content..."
                            }
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .testTag("backup_import_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonGreen,
                        unfocusedBorderColor = CyberCardBorder,
                        focusedTextColor = TextBright,
                        unfocusedTextColor = TextBright,
                        focusedContainerColor = CyberBlack,
                        unfocusedContainerColor = CyberBlack
                    ),
                    shape = RoundedCornerShape(8.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Import Button
                Button(
                    onClick = {
                        if (inputPayload.isBlank()) {
                            statusMessage = "Please enter or paste backup content to scan."
                            return@Button
                        }
                        isImporting = true
                        statusMessage = null
                        scope.launch {
                            try {
                                val notesToImport = when (selectedPlatform) {
                                    "Google Keep" -> ExternalNotesBackup.parseGoogleKeepJson(inputPayload, currentUserId)
                                    "JSON Backup" -> ExternalNotesBackup.parseGoogleKeepJson(inputPayload, currentUserId)
                                    else -> listOf(
                                        Note(
                                            userId = currentUserId,
                                            title = "Imported Markdown",
                                            content = inputPayload,
                                            category = "Keep Backup",
                                            tags = listOf("Markdown", "Imported")
                                        )
                                    )
                                }
                                val count = repository.batchImportNotes(notesToImport)
                                statusMessage = "SUCCESS: Imported and synchronized $count notes to cloud Firestore!"
                                inputPayload = ""
                            } catch (e: Exception) {
                                statusMessage = "ERROR: Import failed: ${e.message}"
                            } finally {
                                isImporting = false
                            }
                        }
                    },
                    enabled = !isImporting,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NeonGreen,
                        contentColor = CyberBlack
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("execute_import_button")
                ) {
                    if (isImporting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = CyberBlack,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("SCANNING & SYNCING...")
                    } else {
                        Icon(
                            imageVector = Icons.Default.FileUpload,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "SCAN & IMPORT TO ANMOL NOTES",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Export Current Notes Button
                Button(
                    onClick = {
                        exportedData = ExternalNotesBackup.exportNotesToJson(allNotes)
                        isExportDialog = true
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CyberSurface,
                        contentColor = NeonCyan
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("export_backup_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.FileDownload,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "EXPORT ENTIRE BACKUP (JSON)",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (statusMessage != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = statusMessage!!,
                        color = if (statusMessage!!.startsWith("SUCCESS")) NeonGreen else NeonYellow,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    )

    if (isExportDialog) {
        AlertDialog(
            onDismissRequest = { isExportDialog = false },
            confirmButton = {
                Button(
                    onClick = { isExportDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonGreen, contentColor = CyberBlack)
                ) {
                    Text("DONE")
                }
            },
            containerColor = CyberDark,
            title = {
                Text(
                    text = "BACKUP EXPORT READY",
                    color = NeonGreen,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text(
                        text = "Complete cloud backup of ${allNotes.size} notes for Operator Anamol:",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextDim
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = exportedData,
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonCyan,
                            unfocusedBorderColor = CyberCardBorder,
                            focusedTextColor = TextBright,
                            unfocusedTextColor = TextBright,
                            focusedContainerColor = CyberBlack,
                            unfocusedContainerColor = CyberBlack
                        )
                    )
                }
            }
        )
    }
}
