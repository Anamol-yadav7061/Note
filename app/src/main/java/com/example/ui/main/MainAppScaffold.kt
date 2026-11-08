package com.example.ui.main

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Note
import com.example.data.model.UserSettings
import com.example.data.repository.NotesRepository
import com.example.ui.ai.JarvisScreen
import com.example.ui.backup.OthersNotesBackupDialog
import com.example.ui.components.HackerHeader
import com.example.ui.dev.DevWorkspaceScreen
import com.example.ui.drawing.DrawingPadScreen
import com.example.ui.notes.NoteEditorScreen
import com.example.ui.notes.NotesListScreen
import com.example.ui.photo.PhotoEditorScreen
import com.example.ui.settings.SettingsScreen
import com.example.ui.theme.CyberBlack
import com.example.ui.theme.CyberCardBorder
import com.example.ui.theme.CyberDark
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.TextBright
import com.example.ui.theme.TextDim

import androidx.compose.material.icons.filled.Code
import com.example.ui.terminal.TermuxTerminalScreen

enum class MainTab(val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    NOTES("Notes", Icons.Default.Description),
    TERMINAL("Termux", Icons.Default.Terminal),
    DEV_WORKSPACE("Python", Icons.Default.Code),
    JARVIS("Jarvis", Icons.Default.SmartToy),
    TOOLS("Tools", Icons.Default.Build),
    SETTINGS("Settings", Icons.Default.Settings),
    DRAWING("Draw", Icons.Default.Brush),
    PHOTO_EDIT("Photos", Icons.Default.Image)
}

enum class SubScreen {
    NONE,
    NOTE_EDITOR,
    DRAWING_PAD,
    PHOTO_EDITOR
}

@Composable
fun MainAppScaffold(
    repository: NotesRepository,
    currentUserId: String,
    onSignOut: () -> Unit
) {
    var currentTab by remember { mutableStateOf(MainTab.NOTES) }
    var subScreen by remember { mutableStateOf(SubScreen.NONE) }
    var editingNote by remember { mutableStateOf<Note?>(null) }
    var devInitialCode by remember { mutableStateOf("") }
    var devInitialTitle by remember { mutableStateOf("script.py") }
    var showBackupDialog by remember { mutableStateOf(false) }

    val notes by repository.observeNotes().collectAsState(initial = emptyList())
    val userSettings by repository.observeSettings().collectAsState(
        initial = UserSettings(userId = currentUserId, userName = "Anamol")
    )

    BackHandler(enabled = subScreen != SubScreen.NONE || currentTab != MainTab.NOTES) {
        if (subScreen != SubScreen.NONE) {
            subScreen = SubScreen.NONE
            editingNote = null
        } else if (currentTab != MainTab.NOTES) {
            currentTab = MainTab.NOTES
        }
    }

    Scaffold(
        topBar = {
            if (subScreen == SubScreen.NONE) {
                HackerHeader(
                    operatorName = userSettings.userName.ifBlank { "Anamol" },
                    isSyncActive = true,
                    onVaultClick = {
                        currentTab = MainTab.NOTES
                    }
                )
            }
        },
        bottomBar = {
            if (subScreen == SubScreen.NONE) {
                NavigationBar(
                    containerColor = CyberDark,
                    contentColor = TextBright,
                    modifier = Modifier.border(0.5.dp, CyberCardBorder)
                ) {
                    val tabs = listOf(
                        MainTab.NOTES,
                        MainTab.TERMINAL,
                        MainTab.DEV_WORKSPACE,
                        MainTab.JARVIS,
                        MainTab.TOOLS,
                        MainTab.SETTINGS
                    )
                    tabs.forEach { tab ->
                        val isSelected = currentTab == tab
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = { currentTab = tab },
                            icon = {
                                Icon(
                                    imageVector = tab.icon,
                                    contentDescription = tab.title,
                                    tint = if (isSelected) NeonGreen else TextDim,
                                    modifier = Modifier.size(20.dp)
                                )
                            },
                            label = {
                                Text(
                                    text = tab.title,
                                    fontSize = 10.sp,
                                    color = if (isSelected) NeonGreen else TextDim
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = NeonGreen.copy(alpha = 0.15f)
                            ),
                            modifier = Modifier.testTag("nav_${tab.name.lowercase()}")
                        )
                    }
                }
            }
        },
        containerColor = CyberBlack
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (subScreen) {
                SubScreen.NOTE_EDITOR -> {
                    NoteEditorScreen(
                        note = editingNote,
                        repository = repository,
                        currentUserId = currentUserId,
                        onBack = {
                            subScreen = SubScreen.NONE
                            editingNote = null
                        }
                    )
                }
                SubScreen.DRAWING_PAD -> {
                    DrawingPadScreen(
                        repository = repository,
                        currentUserId = currentUserId,
                        onBack = { subScreen = SubScreen.NONE }
                    )
                }
                SubScreen.PHOTO_EDITOR -> {
                    PhotoEditorScreen(
                        repository = repository,
                        currentUserId = currentUserId,
                        onBack = { subScreen = SubScreen.NONE }
                    )
                }
                SubScreen.NONE -> {
                    when (currentTab) {
                        MainTab.NOTES -> {
                            NotesListScreen(
                                notes = notes,
                                repository = repository,
                                currentUserId = currentUserId,
                                onNoteClick = { note ->
                                    editingNote = note
                                    subScreen = SubScreen.NOTE_EDITOR
                                },
                                onNewNoteClick = {
                                    editingNote = null
                                    subScreen = SubScreen.NOTE_EDITOR
                                },
                                onOpenBackupDialog = {
                                    showBackupDialog = true
                                },
                                vaultPin = userSettings.vaultPin
                            )
                        }
                        MainTab.TERMINAL -> {
                            TermuxTerminalScreen(
                                repository = repository,
                                currentUserId = currentUserId,
                                onOpenPythonWorkspace = { title, code ->
                                    devInitialTitle = title
                                    devInitialCode = code
                                    currentTab = MainTab.DEV_WORKSPACE
                                }
                            )
                        }
                        MainTab.DEV_WORKSPACE -> {
                            DevWorkspaceScreen(
                                repository = repository,
                                currentUserId = currentUserId,
                                initialCode = devInitialCode,
                                initialTitle = devInitialTitle,
                                onCodeSavedAsNote = {
                                    currentTab = MainTab.NOTES
                                }
                            )
                        }
                        MainTab.JARVIS -> {
                            JarvisScreen(
                                repository = repository,
                                currentUserId = currentUserId,
                                onOpenPythonWorkspace = { title, code ->
                                    devInitialTitle = title
                                    devInitialCode = code
                                    currentTab = MainTab.DEV_WORKSPACE
                                }
                            )
                        }
                        MainTab.DRAWING -> {
                            DrawingPadScreen(
                                repository = repository,
                                currentUserId = currentUserId,
                                onBack = { currentTab = MainTab.NOTES }
                            )
                        }
                        MainTab.PHOTO_EDIT -> {
                            PhotoEditorScreen(
                                repository = repository,
                                currentUserId = currentUserId,
                                onBack = { currentTab = MainTab.NOTES }
                            )
                        }
                        MainTab.TOOLS -> {
                            com.example.ui.tools.HackerToolsScreen()
                        }
                        MainTab.SETTINGS -> {
                            SettingsScreen(
                                currentSettings = userSettings,
                                repository = repository,
                                onSettingsUpdated = { /* state handled by flow */ },
                                onSignOut = onSignOut
                            )
                        }
                    }
                }
            }
        }
    }

    if (showBackupDialog) {
        OthersNotesBackupDialog(
            repository = repository,
            currentUserId = currentUserId,
            allNotes = notes,
            onDismiss = { showBackupDialog = false }
        )
    }
}
