package com.example.ui.settings

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.FontDownload
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import com.example.data.model.UserSettings
import com.example.data.repository.NotesRepository
import com.example.ui.auth.performSignOut
import com.example.ui.theme.CyberBlack
import com.example.ui.theme.CyberCardBorder
import com.example.ui.theme.CyberDark
import com.example.ui.theme.CyberSurface
import com.example.ui.theme.HackerFont
import com.example.ui.theme.HackerFontSize
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonPink
import com.example.ui.theme.TextBright
import com.example.ui.theme.TextDim
import com.example.ui.theme.VaultRed
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    currentSettings: UserSettings,
    repository: NotesRepository,
    onSettingsUpdated: (UserSettings) -> Unit,
    onSignOut: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember { CredentialManager.create(context) }

    var selectedFontName by remember { mutableStateOf(currentSettings.fontFamily) }
    var selectedSizeName by remember { mutableStateOf(currentSettings.fontSize) }
    var vaultPinInput by remember { mutableStateOf(currentSettings.vaultPin) }
    var geminiApiKeyInput by remember { mutableStateOf(currentSettings.geminiApiKey) }
    var isApiKeyVisible by remember { mutableStateOf(false) }
    var isTestingKey by remember { mutableStateOf(false) }
    var keyTestResult by remember { mutableStateOf<Pair<Boolean, String>?>(null) }
    var isSaving by remember { mutableStateOf(false) }

    val jarvisService = remember { com.example.data.ai.JarvisService() }
    val userEmail = Firebase.auth.currentUser?.email ?: "anamolsir7061@gmail.com"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CyberBlack)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Operator Profile Card
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, NeonGreen.copy(alpha = 0.5f), RoundedCornerShape(12.dp)),
            color = CyberDark,
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(NeonGreen.copy(alpha = 0.2f), CircleShape)
                            .border(1.dp, NeonGreen, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Terminal,
                            contentDescription = null,
                            tint = NeonGreen,
                            modifier = Modifier.size(26.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = "Welcome back, Anamol",
                            style = MaterialTheme.typography.titleMedium,
                            color = NeonGreen,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Anamol's Secure Workspace // Master Admin",
                            style = MaterialTheme.typography.labelSmall,
                            color = NeonCyan,
                            fontSize = 11.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Gmail Account: ",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextDim
                    )
                    Text(
                        text = userEmail,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextBright,
                        fontWeight = FontWeight.Medium
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Cloud Status: ",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextDim
                    )
                    Text(
                        text = "REAL-TIME SYNCHRONIZED (FIREBASE)",
                        style = MaterialTheme.typography.labelSmall,
                        color = NeonGreen,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Gemini API Key Settings Section (Dedicated for Jarvis AI)
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, NeonCyan.copy(alpha = 0.6f), RoundedCornerShape(12.dp)),
            color = CyberDark,
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = androidx.compose.material.icons.Icons.Default.Lock,
                        contentDescription = null,
                        tint = NeonCyan,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "GEMINI API KEY CONFIGURATION (JARVIS AI)",
                        style = MaterialTheme.typography.labelSmall,
                        color = NeonCyan,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Configure your Google Gemini API key to activate Jarvis's bilingual Hindi & English intelligence, smart problem-solving, and Python script generation.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextDim,
                    fontSize = 11.sp,
                    lineHeight = 16.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = geminiApiKeyInput,
                    onValueChange = {
                        geminiApiKeyInput = it
                        keyTestResult = null
                    },
                    label = { Text("Gemini API Key (AIzaSy...)") },
                    placeholder = { Text("Paste your Gemini API Key here") },
                    visualTransformation = if (isApiKeyVisible) androidx.compose.ui.text.input.VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { isApiKeyVisible = !isApiKeyVisible }) {
                            Text(
                                text = if (isApiKeyVisible) "HIDE" else "SHOW",
                                color = NeonCyan,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("gemini_api_key_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonCyan,
                        unfocusedBorderColor = CyberCardBorder,
                        focusedTextColor = TextBright,
                        unfocusedTextColor = TextBright,
                        focusedContainerColor = CyberBlack,
                        unfocusedContainerColor = CyberBlack
                    ),
                    shape = RoundedCornerShape(8.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            isTestingKey = true
                            keyTestResult = null
                            scope.launch {
                                val result = jarvisService.testApiKey(geminiApiKeyInput)
                                keyTestResult = result
                                isTestingKey = false
                            }
                        },
                        enabled = geminiApiKeyInput.isNotBlank() && !isTestingKey,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = NeonCyan),
                        border = androidx.compose.foundation.BorderStroke(1.dp, NeonCyan),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp)
                            .testTag("test_api_key_button")
                    ) {
                        if (isTestingKey) {
                            androidx.compose.material3.CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = NeonCyan,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("TESTING...", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        } else {
                            Text("TEST CONNECTION", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Button(
                        onClick = {
                            scope.launch {
                                isSaving = true
                                val updated = currentSettings.copy(
                                    geminiApiKey = geminiApiKeyInput.trim(),
                                    fontFamily = selectedFontName,
                                    fontSize = selectedSizeName,
                                    vaultPin = vaultPinInput,
                                    userName = "Anamol"
                                )
                                repository.saveSettings(updated)
                                onSettingsUpdated(updated)
                                isSaving = false
                                Toast.makeText(context, "Gemini API Key saved for Operator Anamol!", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonGreen, contentColor = CyberBlack),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp)
                            .testTag("save_api_key_button")
                    ) {
                        Text("SAVE KEY", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                keyTestResult?.let { (success, message) ->
                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                1.dp,
                                if (success) NeonGreen else VaultRed,
                                RoundedCornerShape(6.dp)
                            ),
                        color = CyberBlack,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = message,
                            color = if (success) NeonGreen else VaultRed,
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Font Family Settings
        Text(
            text = "HACKER TYPOGRAPHY & FONT FAMILIES:",
            style = MaterialTheme.typography.labelSmall,
            color = NeonGreen,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Select from bundled developer and terminal monospaced typefaces",
            style = MaterialTheme.typography.bodySmall,
            color = TextDim,
            fontSize = 11.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        HackerFont.entries.forEach { fontOption ->
            val isSelected = selectedFontName == fontOption.displayName
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .border(
                        1.dp,
                        if (isSelected) NeonGreen else CyberCardBorder,
                        RoundedCornerShape(8.dp)
                    ),
                color = if (isSelected) CyberSurface else CyberDark,
                shape = RoundedCornerShape(8.dp),
                onClick = { selectedFontName = fontOption.displayName }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = fontOption.displayName,
                            style = MaterialTheme.typography.bodyLarge,
                            color = if (isSelected) NeonGreen else TextBright,
                            fontFamily = fontOption.fontFamily
                        )
                        Text(
                            text = ">_ def hacker_notes(): return 'Anamol Notes'",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextDim,
                            fontFamily = fontOption.fontFamily,
                            fontSize = 11.sp
                        )
                    }

                    if (isSelected) {
                        Text(text = "ACTIVE", color = NeonGreen, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Font Size Configuration
        Text(
            text = "DISPLAY FONT SCALE:",
            style = MaterialTheme.typography.labelSmall,
            color = NeonCyan,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            HackerFontSize.entries.forEach { sizeOption ->
                val isSelected = selectedSizeName == sizeOption.name
                FilterChip(
                    selected = isSelected,
                    onClick = { selectedSizeName = sizeOption.name },
                    label = { Text(sizeOption.name, fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(containerColor = CyberDark),
                    border = FilterChipDefaults.filterChipBorder(
                        borderColor = if (isSelected) NeonCyan else CyberCardBorder,
                        enabled = true,
                        selected = isSelected
                    ),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Secure Vault PIN Setting
        Text(
            text = "SECURE VAULT PIN SETUP:",
            style = MaterialTheme.typography.labelSmall,
            color = VaultRed,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Protect sensitive encrypted notes with a custom 4-digit numeric PIN",
            style = MaterialTheme.typography.bodySmall,
            color = TextDim,
            fontSize = 11.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = vaultPinInput,
            onValueChange = { if (it.length <= 6) vaultPinInput = it },
            label = { Text("Vault PIN (Default: 0000)") },
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = VaultRed,
                unfocusedBorderColor = CyberCardBorder,
                focusedTextColor = TextBright,
                unfocusedTextColor = TextBright,
                focusedContainerColor = CyberDark,
                unfocusedContainerColor = CyberDark
            ),
            shape = RoundedCornerShape(8.dp)
        )

        Spacer(modifier = Modifier.height(18.dp))

        // Save Settings Button
        Button(
            onClick = {
                isSaving = true
                scope.launch {
                    val updated = currentSettings.copy(
                        fontFamily = selectedFontName,
                        fontSize = selectedSizeName,
                        vaultPin = vaultPinInput,
                        geminiApiKey = geminiApiKeyInput.trim(),
                        userName = "Anamol"
                    )
                    repository.saveSettings(updated)
                    onSettingsUpdated(updated)
                    isSaving = false
                    Toast.makeText(context, "Preferences saved & synchronized!", Toast.LENGTH_SHORT).show()
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = NeonGreen, contentColor = CyberBlack),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("save_settings_button")
        ) {
            Icon(imageVector = Icons.Default.Save, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("SAVE PREFERENCES", fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Developer Credits & System Info
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .border(0.5.dp, CyberCardBorder, RoundedCornerShape(8.dp)),
            color = CyberSurface,
            shape = RoundedCornerShape(8.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "APPLICATION METADATA & DEVELOPER CREDITS",
                    style = MaterialTheme.typography.labelSmall,
                    color = NeonAmber,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "App: Anmol Notes v1.0.0 (Release)\nArchitected & Built for: Operator Anamol\nBackend: Cloud Firestore (Enterprise Edition)\nIntelligence: Google Gemini 3.8 Flash via Firebase AI Logic\nSecurity: AES & Zero-Trust Cloud Firestore Rules",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextDim,
                    lineHeight = 18.sp,
                    fontSize = 11.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Sign Out Button
        OutlinedButton(
            onClick = {
                performSignOut(credentialManager, scope) {
                    onSignOut()
                }
            },
            colors = ButtonDefaults.outlinedButtonColors(contentColor = NeonPink),
            border = androidx.compose.foundation.BorderStroke(1.dp, NeonPink),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("sign_out_button")
        ) {
            Icon(imageVector = Icons.AutoMirrored.Filled.Logout, contentDescription = null, tint = NeonPink)
            Spacer(modifier = Modifier.width(8.dp))
            Text("SIGN OUT / DISCONNECT ACCOUNT", fontWeight = FontWeight.Bold, color = NeonPink)
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
