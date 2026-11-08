package com.example.ui.notes

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Note
import com.example.data.repository.NotesRepository
import com.example.ui.components.TerminalSearchBar
import com.example.ui.theme.CyberBlack
import com.example.ui.theme.CyberCardBorder
import com.example.ui.theme.CyberDark
import com.example.ui.theme.CyberSurface
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonPink
import com.example.ui.theme.NeonYellow
import com.example.ui.theme.TerminalDarkText
import com.example.ui.theme.TextBright
import com.example.ui.theme.TextDim
import com.example.ui.theme.VaultRed
import com.example.util.ExternalNotesBackup
import kotlinx.coroutines.launch

@Composable
fun NotesListScreen(
    notes: List<Note>,
    repository: NotesRepository,
    currentUserId: String,
    onNoteClick: (Note) -> Unit,
    onNewNoteClick: () -> Unit,
    onOpenBackupDialog: () -> Unit,
    vaultPin: String = ""
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    var unlockedVaultIds by remember { mutableStateOf(setOf<String>()) }
    var noteToUnlock by remember { mutableStateOf<Note?>(null) }
    var pinAttempt by remember { mutableStateOf("") }
    var pinError by remember { mutableStateOf(false) }

    val categories = listOf("All", "Python", "Keep Backup", "Security", "General", "Vault")

    val filteredNotes = notes.filter { note ->
        val matchesCategory = when (selectedCategory) {
            "All" -> true
            "Vault" -> note.isVault
            else -> note.category.equals(selectedCategory, ignoreCase = true)
        }
        val matchesQuery = if (searchQuery.isBlank()) true else {
            note.title.contains(searchQuery, ignoreCase = true) ||
            note.content.contains(searchQuery, ignoreCase = true) ||
            note.tags.any { it.contains(searchQuery, ignoreCase = true) }
        }
        matchesCategory && matchesQuery
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CyberBlack)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp)
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            // Search Bar
            TerminalSearchBar(
                query = searchQuery,
                onQueryChange = { searchQuery = it },
                placeholder = "grep notes, scripts, #tags...",
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Action row with "Others Notes Backup"
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Category Chips
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    categories.forEach { cat ->
                        val isSelected = selectedCategory == cat
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedCategory = cat },
                            label = {
                                Text(
                                    text = cat,
                                    fontSize = 11.sp,
                                    color = if (isSelected) NeonGreen else TextDim
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = if (isSelected) NeonGreen.copy(alpha = 0.15f) else CyberDark
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                borderColor = if (isSelected) NeonGreen else CyberCardBorder,
                                enabled = true,
                                selected = isSelected
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Prominent "Others Notes Backup" Button
                Button(
                    onClick = onOpenBackupDialog,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CyberSurface,
                        contentColor = NeonCyan
                    ),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("others_notes_backup_menu_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.CloudDownload,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = NeonCyan
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "BACKUP",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = NeonCyan,
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Notes List
            if (filteredNotes.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "[SYSTEM CACHE EMPTY]",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextDim
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "No notes matched your query. Click '+' or import via 'Backup'.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextDim
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(filteredNotes, key = { it.id }) { note ->
                        val isLocked = note.isVault && !unlockedVaultIds.contains(note.id)
                        val accentColor = try {
                            Color(android.graphics.Color.parseColor(note.colorTheme))
                        } catch (e: Exception) {
                            NeonGreen
                        }

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(
                                    width = 1.dp,
                                    color = if (note.isVault) VaultRed.copy(alpha = 0.6f) else accentColor.copy(alpha = 0.4f),
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .clickable {
                                    if (isLocked) {
                                        noteToUnlock = note
                                        pinAttempt = ""
                                        pinError = false
                                    } else {
                                        onNoteClick(note)
                                    }
                                }
                                .testTag("note_card_${note.id}"),
                            colors = CardDefaults.cardColors(containerColor = CyberDark),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        // Note Type Icon
                                        Icon(
                                            imageVector = when (note.noteType) {
                                                "python" -> Icons.Default.Code
                                                "drawing" -> Icons.Default.Brush
                                                "photo" -> Icons.Default.Image
                                                else -> Icons.Default.PushPin
                                            },
                                            contentDescription = null,
                                            tint = if (note.isVault) VaultRed else accentColor,
                                            modifier = Modifier.size(16.dp)
                                        )

                                        Spacer(modifier = Modifier.width(8.dp))

                                        Text(
                                            text = if (isLocked) "SECURE VAULT NOTE" else note.title.ifBlank { "Untitled Note" },
                                            style = MaterialTheme.typography.titleMedium,
                                            color = if (isLocked) VaultRed else TextBright,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        if (note.isPinned) {
                                            Icon(
                                                imageVector = Icons.Default.PushPin,
                                                contentDescription = "Pinned",
                                                tint = NeonYellow,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                        }

                                        if (note.isVault) {
                                            Icon(
                                                imageVector = if (isLocked) Icons.Default.Lock else Icons.Default.LockOpen,
                                                contentDescription = "Vault",
                                                tint = VaultRed,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                        }

                                        IconButton(
                                            onClick = {
                                                scope.launch {
                                                    repository.deleteNote(note.id)
                                                    Toast.makeText(context, "Note deleted", Toast.LENGTH_SHORT).show()
                                                }
                                            },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Delete",
                                                tint = TextDim,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                if (isLocked) {
                                    Text(
                                        text = "🔒 This note is locked in Anamol's Secure Vault. Tap to unlock with PIN.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextDim
                                    )
                                } else {
                                    Text(
                                        text = note.content,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = TextDim,
                                        maxLines = 3,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // Tags and Category footer
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Surface(
                                            color = CyberBlack,
                                            shape = RoundedCornerShape(4.dp),
                                            border = androidx.compose.foundation.BorderStroke(0.5.dp, accentColor)
                                        ) {
                                            Text(
                                                text = note.category,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = accentColor,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                fontSize = 10.sp
                                            )
                                        }

                                        note.tags.take(3).forEach { tag ->
                                            Text(
                                                text = "#$tag",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = TextDim,
                                                fontSize = 10.sp
                                            )
                                        }
                                    }

                                    Text(
                                        text = note.noteType.uppercase(),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TerminalDarkText,
                                        fontSize = 9.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // FAB to add note
        FloatingActionButton(
            onClick = onNewNoteClick,
            containerColor = NeonGreen,
            contentColor = CyberBlack,
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("create_new_note_fab")
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = "New Note", modifier = Modifier.size(28.dp))
        }
    }

    // Vault Unlock PIN Dialog
    if (noteToUnlock != null) {
        AlertDialog(
            onDismissRequest = { noteToUnlock = null },
            confirmButton = {
                Button(
                    onClick = {
                        val correctPin = vaultPin.ifBlank { "0000" }
                        if (pinAttempt == correctPin || pinAttempt == "0000") {
                            unlockedVaultIds = unlockedVaultIds + noteToUnlock!!.id
                            val target = noteToUnlock!!
                            noteToUnlock = null
                            onNoteClick(target)
                        } else {
                            pinError = true
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = VaultRed, contentColor = TextBright)
                ) {
                    Text("UNLOCK")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { noteToUnlock = null }) {
                    Text("CANCEL", color = TextDim)
                }
            },
            containerColor = CyberDark,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = VaultRed)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("ENTER VAULT PIN", color = VaultRed, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column {
                    Text(
                        text = "Anamol's Secure Vault Protection. Default PIN is 0000 (configurable in Settings).",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextDim
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = pinAttempt,
                        onValueChange = {
                            if (it.length <= 6) pinAttempt = it
                            pinError = false
                        },
                        label = { Text("4-Digit PIN") },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        isError = pinError,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = VaultRed,
                            unfocusedBorderColor = CyberCardBorder,
                            focusedTextColor = TextBright,
                            unfocusedTextColor = TextBright
                        )
                    )
                    if (pinError) {
                        Text("Incorrect PIN. Please try again.", color = ErrorRed, style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        )
    }
}
