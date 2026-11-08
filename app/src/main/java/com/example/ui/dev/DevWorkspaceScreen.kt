package com.example.ui.dev

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.PlayArrow
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Note
import com.example.data.repository.NotesRepository
import com.example.ui.theme.CodeBackground
import com.example.ui.theme.CyberBlack
import com.example.ui.theme.CyberCardBorder
import com.example.ui.theme.CyberDark
import com.example.ui.theme.CyberSurface
import com.example.ui.theme.MatrixGreen
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonPink
import com.example.ui.theme.NeonYellow
import com.example.ui.theme.TerminalDarkText
import com.example.ui.theme.TerminalGray
import com.example.ui.theme.TextBright
import com.example.ui.theme.TextDim
import com.example.util.PythonSimulator
import kotlinx.coroutines.launch

@Composable
fun DevWorkspaceScreen(
    repository: NotesRepository,
    currentUserId: String,
    initialCode: String = "",
    initialTitle: String = "script.py",
    onCodeSavedAsNote: () -> Unit = {}
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val scope = rememberCoroutineScope()

    var scriptTitle by remember { mutableStateOf(initialTitle.ifBlank { "quick_snippet.py" }) }
    var codeText by remember {
        mutableStateOf(
            if (initialCode.isNotBlank()) initialCode
            else """
# Anmol Notes - Python Dev Suite
# Operator: Anamol
import datetime

def system_status():
    user = "Anamol"
    time_str = datetime.datetime.now()
    print(f"[STATUS] Workspace: Anmol Notes | User: {user}")
    print(f"[ONLINE] Timestamp: {time_str}")
    for i in range(3):
        print(f"Ping probe cluster {i+1} => SUCCESS (0.2ms)")

system_status()
            """.trimIndent()
        )
    }

    var executionOutput by remember { mutableStateOf<String?>(null) }
    var isRunning by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CyberBlack)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Workspace Header Banner
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "PYTHON COMMAND SUITE",
                    style = MaterialTheme.typography.titleLarge,
                    color = NeonGreen,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Operator: Anamol // High-Speed Simulation & Workspace",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextDim
                )
            }

            Row {
                IconButton(
                    onClick = {
                        clipboardManager.setText(AnnotatedString(codeText))
                        Toast.makeText(context, "Code copied to clipboard!", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.testTag("copy_code_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy Code",
                        tint = NeonCyan,
                        modifier = Modifier.size(20.dp)
                    )
                }

                IconButton(
                    onClick = {
                        scope.launch {
                            val newNote = Note(
                                userId = currentUserId,
                                title = scriptTitle,
                                content = codeText,
                                category = "Python",
                                tags = listOf("Python", "Script", "DevWorkspace"),
                                colorTheme = "#00FF66",
                                noteType = "python"
                            )
                            repository.saveNote(newNote)
                            Toast.makeText(context, "Saved to Cloud Notes!", Toast.LENGTH_SHORT).show()
                            onCodeSavedAsNote()
                        }
                    },
                    modifier = Modifier.testTag("save_code_to_notes_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Save,
                        contentDescription = "Save to Notes",
                        tint = NeonGreen,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Template chips row
        Text(
            text = "LOAD DEV TEMPLATE:",
            style = MaterialTheme.typography.labelSmall,
            color = NeonCyan,
            fontSize = 10.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            PythonSimulator.getTemplates().forEach { (templateName, templateCode) ->
                FilterChip(
                    selected = false,
                    onClick = {
                        scriptTitle = "${templateName.lowercase().replace(" ", "_")}.py"
                        codeText = templateCode
                        executionOutput = null
                    },
                    label = {
                        Text(
                            text = templateName,
                            fontSize = 11.sp,
                            color = NeonGreen
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = CyberDark
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        borderColor = CyberCardBorder,
                        enabled = true,
                        selected = false
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Script File Title input
        OutlinedTextField(
            value = scriptTitle,
            onValueChange = { scriptTitle = it },
            label = { Text("Script Name", color = TextDim) },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("script_name_input"),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = NeonGreen,
                unfocusedBorderColor = CyberCardBorder,
                focusedTextColor = NeonGreen,
                unfocusedTextColor = TextBright,
                focusedContainerColor = CyberDark,
                unfocusedContainerColor = CyberDark
            ),
            shape = RoundedCornerShape(8.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Editor with line numbers and syntax highlighting
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, NeonGreen.copy(alpha = 0.5f), RoundedCornerShape(8.dp)),
            color = CodeBackground,
            shape = RoundedCornerShape(8.dp)
        ) {
            Column {
                // Top terminal title bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(CyberDark)
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(10.dp).background(NeonPink, androidx.compose.foundation.shape.CircleShape))
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(modifier = Modifier.size(10.dp).background(NeonYellow, androidx.compose.foundation.shape.CircleShape))
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(modifier = Modifier.size(10.dp).background(NeonGreen, androidx.compose.foundation.shape.CircleShape))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "terminal://anamol@anmol-notes: ~/$scriptTitle",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextDim,
                            fontSize = 11.sp
                        )
                    }

                    Text(
                        text = "UTF-8",
                        style = MaterialTheme.typography.labelSmall,
                        color = TerminalDarkText
                    )
                }

                // Editor Body with line numbers
                val lineCount = codeText.lines().size.coerceAtLeast(1)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                ) {
                    // Line numbers column
                    val lineNumbersText = (1..lineCount).joinToString("\n") { "%2d".format(it) }
                    Text(
                        text = lineNumbersText,
                        color = TerminalDarkText,
                        style = MaterialTheme.typography.bodyMedium,
                        fontSize = 13.sp,
                        lineHeight = 20.sp,
                        modifier = Modifier.padding(end = 8.dp)
                    )

                    // Code Editor Text Field
                    Box(modifier = Modifier.weight(1f)) {
                        BasicTextField(
                            value = codeText,
                            onValueChange = { codeText = it },
                            textStyle = TextStyle(
                                color = TextBright,
                                fontFamily = MaterialTheme.typography.bodyMedium.fontFamily ?: FontFamily.Monospace,
                                fontSize = 13.sp,
                                lineHeight = 20.sp
                            ),
                            cursorBrush = SolidColor(NeonGreen),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("python_code_editor_field")
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Execute Simulation Button
        Button(
            onClick = {
                isRunning = true
                val result = PythonSimulator.execute(codeText)
                executionOutput = result.output
                isRunning = false
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = NeonGreen,
                contentColor = CyberBlack
            ),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("run_python_simulation_button")
        ) {
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "EXECUTE PYTHON COMMAND / SCRIPT",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.labelLarge
            )
        }

        // Terminal Output Console
        if (executionOutput != null) {
            Spacer(modifier = Modifier.height(16.dp))

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, NeonCyan.copy(alpha = 0.6f), RoundedCornerShape(8.dp)),
                color = CyberDark,
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Terminal,
                                contentDescription = null,
                                tint = NeonCyan,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "TERMINAL STDOUT [SIMULATOR]",
                                style = MaterialTheme.typography.labelSmall,
                                color = NeonCyan,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        IconButton(
                            onClick = { executionOutput = null },
                            modifier = Modifier.size(20.dp)
                        ) {
                            Text(text = "✕", color = TextDim, fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(CodeBackground, RoundedCornerShape(4.dp))
                            .padding(10.dp)
                    ) {
                        Text(
                            text = executionOutput!!,
                            color = NeonGreen,
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 12.sp,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }
    }
}
