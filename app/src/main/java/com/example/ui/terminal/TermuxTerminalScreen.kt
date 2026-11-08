package com.example.ui.terminal

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ai.JarvisService
import com.example.data.model.Note
import com.example.data.model.UserSettings
import com.example.data.repository.NotesRepository
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
import com.example.ui.theme.VaultRed
import com.example.util.HackerTools
import com.example.util.PythonSimulator
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.random.Random

data class TerminalLine(
    val prompt: String,
    val command: String,
    val output: String,
    val isError: Boolean = false,
    val outputColor: Color = NeonGreen
)

@Composable
fun TermuxTerminalScreen(
    repository: NotesRepository,
    currentUserId: String,
    onOpenPythonWorkspace: (String, String) -> Unit = { _, _ -> }
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val clipboardManager = LocalClipboardManager.current
    val jarvisService = remember { JarvisService() }

    val notes by repository.observeNotes().collectAsState(initial = emptyList())
    val userSettings by repository.observeSettings().collectAsState(
        initial = UserSettings(userId = currentUserId, userName = "Anamol")
    )

    val terminalLines = remember {
        mutableStateListOf<TerminalLine>().apply {
            add(
                TerminalLine(
                    prompt = "",
                    command = "",
                    output = """
                        Welcome to Termux! (Anmol Notes Virtual Terminal)
                        ==================================================
                        OS: AnmolLinux v2.4 (ARM64 Android Environment)
                        Operator: Anamol (UID: 1000, GID: 1000)
                        Session: /dev/pts/0 [ONLINE]
                        Type 'help' for shell commands, 'python' for Python REPL,
                        or 'jarvis <prompt>' for bilingual AI assistance.
                        ==================================================
                    """.trimIndent(),
                    outputColor = NeonGreen
                )
            )
        }
    }

    val commandHistory = remember { mutableStateListOf<String>() }
    var historyIndex by remember { mutableIntStateOf(-1) }

    var inputCommand by remember { mutableStateOf("") }
    var isPythonMode by remember { mutableStateOf(false) }
    var isExecutingAsync by remember { mutableStateOf(false) }

    val pythonVariables = remember {
        mutableMapOf<String, Any>(
            "USER" to "Anamol",
            "APP" to "Anmol Notes",
            "SYS_STATUS" to "ONLINE"
        )
    }

    val listState = rememberLazyListState()

    LaunchedEffect(terminalLines.size, isExecutingAsync) {
        if (terminalLines.isNotEmpty()) {
            listState.animateScrollToItem(terminalLines.size - 1)
        }
    }

    fun executeTerminalCommand(cmd: String) {
        val trimmed = cmd.trim()
        if (trimmed.isEmpty()) return

        commandHistory.add(trimmed)
        historyIndex = -1

        val currentPrompt = if (isPythonMode) ">>> " else "Anamol@AnmolNotes:~$ "

        // Check python mode exit
        if (isPythonMode) {
            if (trimmed == "exit()" || trimmed == "quit()" || trimmed == "exit") {
                isPythonMode = false
                terminalLines.add(
                    TerminalLine(
                        prompt = currentPrompt,
                        command = trimmed,
                        output = "Exited Python 3.12 session. Returned to AnmolNotes bash shell.",
                        outputColor = NeonCyan
                    )
                )
                return
            }

            // Execute line in Python REPL
            try {
                val execResult = PythonSimulator.execute(trimmed)
                terminalLines.add(
                    TerminalLine(
                        prompt = currentPrompt,
                        command = trimmed,
                        output = execResult.output,
                        isError = !execResult.isSuccess,
                        outputColor = if (execResult.isSuccess) NeonGreen else VaultRed
                    )
                )
            } catch (e: Exception) {
                terminalLines.add(
                    TerminalLine(
                        prompt = currentPrompt,
                        command = trimmed,
                        output = "Python Error: ${e.localizedMessage ?: "Unknown syntax error"}",
                        isError = true,
                        outputColor = VaultRed
                    )
                )
            }
            return
        }

        // Standard Shell Commands
        val parts = trimmed.split(Regex("\\s+"))
        val baseCmd = parts[0].lowercase()
        val args = if (parts.size > 1) trimmed.substringAfter(parts[0]).trim() else ""

        when (baseCmd) {
            "clear", "cls" -> {
                terminalLines.clear()
            }

            "help" -> {
                val helpText = """
                    TERMUX COMMAND MANUAL (Anmol Notes Edition):
                    --------------------------------------------------
                    python             : Launch interactive Python 3.12 REPL (>>>)
                    python <code>      : Execute inline Python expression
                    python <file.py>   : Run a Python script from your notes
                    run <script_name>  : Execute saved script from notes
                    jarvis <prompt>    : Ask Jarvis AI (Hindi & English)
                    ls / dir           : List saved notes & Python scripts
                    cat <note_title>   : Display content of note or script
                    neofetch           : Cyberpunk system specs & Operator Anamol badge
                    matrix             : Trigger Matrix green digital rain stream
                    whoami             : Display operator profile info
                    uname -a           : Display kernel & architecture details
                    date               : Show current system timestamp
                    history            : List command execution history
                    ping <host>        : Simulate network latency ping
                    morse <text>       : Convert text to Morse code
                    demorse <dots>     : Convert Morse code to plain text
                    base64 <text>      : Base64 encode string
                    debase64 <data>    : Base64 decode string
                    rot13 <text>       : Caesar ROT13 cipher encode/decode
                    hash <text>        : Generate SHA-256 and MD5 hashes
                    echo <text>        : Print text to terminal
                    clear              : Clear terminal screen buffer
                    exit               : Reset terminal session
                """.trimIndent()
                terminalLines.add(
                    TerminalLine(
                        prompt = currentPrompt,
                        command = trimmed,
                        output = helpText,
                        outputColor = NeonGreen
                    )
                )
            }

            "python", "python3" -> {
                if (args.isEmpty()) {
                    isPythonMode = true
                    terminalLines.add(
                        TerminalLine(
                            prompt = currentPrompt,
                            command = trimmed,
                            output = """
                                Python 3.12.2 (main, AnmolNotes Termux v2.4)
                                [GCC 13.2.0 on aarch64-linux-android]
                                Type "help", "copyright", "credits" or "license" for more information.
                                Type "exit()" or "quit()" to return to terminal shell.
                            """.trimIndent(),
                            outputColor = NeonCyan
                        )
                    )
                } else if (args.startsWith("-c ")) {
                    val code = args.removePrefix("-c ").trim().removeSurrounding("\"").removeSurrounding("'")
                    val result = PythonSimulator.execute(code)
                    terminalLines.add(
                        TerminalLine(
                            prompt = currentPrompt,
                            command = trimmed,
                            output = result.output,
                            isError = !result.isSuccess,
                            outputColor = if (result.isSuccess) NeonGreen else VaultRed
                        )
                    )
                } else {
                    // Try to execute named script from notes
                    val matchingNote = notes.find { it.title.equals(args, ignoreCase = true) || it.title.equals("$args.py", ignoreCase = true) }
                    if (matchingNote != null) {
                        val result = PythonSimulator.execute(matchingNote.content)
                        terminalLines.add(
                            TerminalLine(
                                prompt = currentPrompt,
                                command = trimmed,
                                output = "Executing script: ${matchingNote.title}\n" + result.output,
                                isError = !result.isSuccess,
                                outputColor = if (result.isSuccess) NeonGreen else VaultRed
                            )
                        )
                    } else {
                        // Treat args as raw python code
                        val result = PythonSimulator.execute(args)
                        terminalLines.add(
                            TerminalLine(
                                prompt = currentPrompt,
                                command = trimmed,
                                output = result.output,
                                isError = !result.isSuccess,
                                outputColor = if (result.isSuccess) NeonGreen else VaultRed
                            )
                        )
                    }
                }
            }

            "ls", "dir" -> {
                val out = buildString {
                    appendLine("total ${notes.size}")
                    appendLine("drwxr-xr-x 2 anamol anamol 4096 .")
                    appendLine("drwxr-xr-x 4 root   root   4096 ..")
                    if (notes.isEmpty()) {
                        appendLine("-rw-r--r-- 1 anamol anamol  128 sample_note.txt")
                    } else {
                        notes.forEach { note ->
                            val ext = if (note.noteType == "python") "py" else "txt"
                            val icon = if (note.noteType == "python") "🐍" else "📄"
                            val vaultTag = if (note.isVault) "[VAULT]" else ""
                            appendLine("-rw-r--r-- 1 anamol anamol ${note.content.length.coerceAtLeast(32)} ${note.title.replace(" ", "_")}.$ext $icon $vaultTag")
                        }
                    }
                }
                terminalLines.add(TerminalLine(prompt = currentPrompt, command = trimmed, output = out.trimEnd(), outputColor = NeonCyan))
            }

            "cat" -> {
                if (args.isEmpty()) {
                    terminalLines.add(TerminalLine(prompt = currentPrompt, command = trimmed, output = "usage: cat <note_title_or_script>", isError = true, outputColor = VaultRed))
                } else {
                    val matchingNote = notes.find {
                        it.title.equals(args, ignoreCase = true) ||
                        it.title.replace(" ", "_").equals(args, ignoreCase = true) ||
                        it.title.equals(args.removeSuffix(".py"), ignoreCase = true) ||
                        it.title.equals(args.removeSuffix(".txt"), ignoreCase = true)
                    }
                    if (matchingNote != null) {
                        terminalLines.add(
                            TerminalLine(
                                prompt = currentPrompt,
                                command = trimmed,
                                output = "=== [ ${matchingNote.title} ] ===\n${matchingNote.content}",
                                outputColor = TextBright
                            )
                        )
                    } else {
                        terminalLines.add(TerminalLine(prompt = currentPrompt, command = trimmed, output = "cat: $args: No such file in Anmol Notes", isError = true, outputColor = VaultRed))
                    }
                }
            }

            "run", "exec" -> {
                if (args.isEmpty()) {
                    terminalLines.add(TerminalLine(prompt = currentPrompt, command = trimmed, output = "usage: run <script_name>", isError = true, outputColor = VaultRed))
                } else {
                    val matchingNote = notes.find {
                        it.title.contains(args, ignoreCase = true)
                    }
                    if (matchingNote != null) {
                        val result = PythonSimulator.execute(matchingNote.content)
                        terminalLines.add(
                            TerminalLine(
                                prompt = currentPrompt,
                                command = trimmed,
                                output = ">>> Running ${matchingNote.title}...\n" + result.output,
                                isError = !result.isSuccess,
                                outputColor = if (result.isSuccess) NeonGreen else VaultRed
                            )
                        )
                    } else {
                        terminalLines.add(TerminalLine(prompt = currentPrompt, command = trimmed, output = "run: script '$args' not found. Type 'ls' to see files.", isError = true, outputColor = VaultRed))
                    }
                }
            }

            "jarvis" -> {
                if (args.isEmpty()) {
                    terminalLines.add(TerminalLine(prompt = currentPrompt, command = trimmed, output = "usage: jarvis <question_or_problem_in_english_or_hindi>", isError = true, outputColor = VaultRed))
                } else {
                    terminalLines.add(TerminalLine(prompt = currentPrompt, command = trimmed, output = "[JARVIS] Processing request for Operator Anamol...", outputColor = NeonCyan))
                    isExecutingAsync = true
                    scope.launch {
                        val resp = jarvisService.chat(args, emptyList(), userSettings.geminiApiKey)
                        isExecutingAsync = false
                        val formattedOutput = buildString {
                            appendLine("[JARVIS RESPONSE]")
                            appendLine(resp.replyText)
                            if (!resp.pythonCode.isNullOrBlank()) {
                                appendLine("\n--- Generated Python Script ---")
                                appendLine(resp.pythonCode)
                                appendLine("-------------------------------")
                                appendLine("(Type 'python -c \"...\"' or visit Dev Workspace to run)")
                            }
                        }
                        terminalLines.add(TerminalLine(prompt = "", command = "", output = formattedOutput.trimEnd(), outputColor = NeonGreen))
                    }
                }
            }

            "neofetch", "fastfetch" -> {
                val neofetchArt = """
                       /\         Operator: Anamol (UID: 1000)
                      /  \        Host: CyberTerminal-A100
                     / /\ \       OS: AnmolLinux v2.4 (Android 14)
                    / /  \ \      Kernel: 6.1.0-anmol-cyber-lts
                   / /    \ \     Uptime: 3 days, 14 hours
                  / /  /\  \ \    Shell: termux-bash 5.2.21
                 / /  /  \  \ \   Terminal: AnmolNotes-Termux
                /_/  /____\  \_\  Security: AES-256 Zero-Trust
                                  Cloud: Firebase Synchronized
                                  AI Engine: Jarvis (Gemini 2.5)
                """.trimIndent()
                terminalLines.add(TerminalLine(prompt = currentPrompt, command = trimmed, output = neofetchArt, outputColor = NeonCyan))
            }

            "matrix" -> {
                terminalLines.add(TerminalLine(prompt = currentPrompt, command = trimmed, output = "[SYS] Entering Matrix Digital Stream...", outputColor = NeonGreen))
                scope.launch {
                    val matrixChars = "01アイウエオカキクケコサシスセソタチツテト0123456789ABCDEF"
                    for (i in 1..10) {
                        delay(120)
                        val line = (1..36).map { matrixChars[Random.nextInt(matrixChars.length)] }.joinToString("")
                        terminalLines.add(TerminalLine(prompt = "", command = "", output = line, outputColor = NeonGreen))
                    }
                    terminalLines.add(TerminalLine(prompt = "", command = "", output = "[SYS] Matrix Stream Completed. Operator: Anamol.", outputColor = NeonCyan))
                }
            }

            "whoami" -> {
                val info = "Anamol (UID: 1000, GID: 1000, Groups: root, anmol-operator, developers, wheel)\nWorkspace: Anmol Notes Secure Environment"
                terminalLines.add(TerminalLine(prompt = currentPrompt, command = trimmed, output = info, outputColor = NeonGreen))
            }

            "uname" -> {
                val unameInfo = "Linux anmolnotes 6.1.0-anmol-cyber-arm64 #1 SMP PREEMPT Anamol aarch64 GNU/Linux"
                terminalLines.add(TerminalLine(prompt = currentPrompt, command = trimmed, output = unameInfo, outputColor = TextBright))
            }

            "date" -> {
                val now = SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy", Locale.US).format(Date())
                terminalLines.add(TerminalLine(prompt = currentPrompt, command = trimmed, output = now, outputColor = TextBright))
            }

            "history" -> {
                val hist = buildString {
                    commandHistory.forEachIndexed { idx, h ->
                        appendLine(" ${idx + 1}  $h")
                    }
                }
                terminalLines.add(TerminalLine(prompt = currentPrompt, command = trimmed, output = hist.trimEnd().ifEmpty { "No history recorded." }, outputColor = TextDim))
            }

            "ping" -> {
                val host = if (args.isEmpty()) "google.com" else args
                val pingOut = """
                    PING $host (142.250.190.46) 56(84) bytes of data.
                    64 bytes from $host: icmp_seq=1 ttl=117 time=14.2 ms
                    64 bytes from $host: icmp_seq=2 ttl=117 time=15.1 ms
                    64 bytes from $host: icmp_seq=3 ttl=117 time=13.8 ms
                    --- $host ping statistics ---
                    3 packets transmitted, 3 received, 0% packet loss, time 2003ms
                    rtt min/avg/max = 13.8/14.3/15.1 ms
                """.trimIndent()
                terminalLines.add(TerminalLine(prompt = currentPrompt, command = trimmed, output = pingOut, outputColor = NeonGreen))
            }

            "morse" -> {
                val encoded = HackerTools.textToMorse(args)
                terminalLines.add(TerminalLine(prompt = currentPrompt, command = trimmed, output = "Morse Output:\n$encoded", outputColor = NeonAmber))
            }

            "demorse" -> {
                val decoded = HackerTools.morseToText(args)
                terminalLines.add(TerminalLine(prompt = currentPrompt, command = trimmed, output = "Decoded Text:\n$decoded", outputColor = NeonAmber))
            }

            "base64" -> {
                val encoded = HackerTools.base64Encode(args)
                terminalLines.add(TerminalLine(prompt = currentPrompt, command = trimmed, output = encoded, outputColor = NeonCyan))
            }

            "debase64" -> {
                val decoded = HackerTools.base64Decode(args)
                terminalLines.add(TerminalLine(prompt = currentPrompt, command = trimmed, output = decoded, outputColor = NeonCyan))
            }

            "rot13" -> {
                val rot = HackerTools.caesarCipher(args, 13)
                terminalLines.add(TerminalLine(prompt = currentPrompt, command = trimmed, output = rot, outputColor = NeonPink))
            }

            "hash" -> {
                val sha = HackerTools.hashSha256(args)
                val md5 = HackerTools.hashMd5(args)
                terminalLines.add(
                    TerminalLine(
                        prompt = currentPrompt,
                        command = trimmed,
                        output = "Input: $args\nSHA-256: $sha\nMD5:     $md5",
                        outputColor = NeonGreen
                    )
                )
            }

            "echo" -> {
                terminalLines.add(TerminalLine(prompt = currentPrompt, command = trimmed, output = args, outputColor = TextBright))
            }

            "exit" -> {
                terminalLines.clear()
                terminalLines.add(
                    TerminalLine(
                        prompt = "",
                        command = "",
                        output = "Terminal session reset. Welcome back Operator Anamol.",
                        outputColor = NeonGreen
                    )
                )
            }

            else -> {
                terminalLines.add(
                    TerminalLine(
                        prompt = currentPrompt,
                        command = trimmed,
                        output = "termux: command not found: $baseCmd. Type 'help' for command list.",
                        isError = true,
                        outputColor = VaultRed
                    )
                )
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Termux Top Bar
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = CyberDark,
            shadowElevation = 2.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Terminal,
                        contentDescription = null,
                        tint = NeonGreen,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "TERMUX // ANMOL NOTES",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = NeonGreen,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isPythonMode) "[PYTHON 3.12]" else "[BASH: ~/]",
                        fontFamily = FontFamily.Monospace,
                        color = if (isPythonMode) NeonCyan else TextDim,
                        fontSize = 10.sp
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Button(
                        onClick = {
                            if (isPythonMode) {
                                executeTerminalCommand("exit()")
                            } else {
                                executeTerminalCommand("python")
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isPythonMode) NeonCyan.copy(alpha = 0.2f) else CyberSurface,
                            contentColor = if (isPythonMode) NeonCyan else NeonGreen
                        ),
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier.height(26.dp)
                    ) {
                        Text(if (isPythonMode) "EXIT PY" else "PYTHON", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }

                    IconButton(
                        onClick = { terminalLines.clear() },
                        modifier = Modifier.size(26.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Clear Terminal",
                            tint = TextDim,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }

        // Terminal Buffer Output
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(terminalLines) { item ->
                Column {
                    if (item.command.isNotBlank() || item.prompt.isNotBlank()) {
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = item.prompt,
                                color = if (item.prompt.startsWith(">>>")) NeonCyan else NeonGreen,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = item.command,
                                color = TextBright,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.sp
                            )
                        }
                    }
                    if (item.output.isNotBlank()) {
                        Text(
                            text = item.output,
                            color = item.outputColor,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            if (isExecutingAsync) {
                item {
                    Row(
                        modifier = Modifier.padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(12.dp),
                            color = NeonCyan,
                            strokeWidth = 1.5.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Executing task...",
                            color = NeonCyan,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }

        // Termux Extra-Keys Row (The iconic Termux bar above keyboard)
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = CyberDark,
            border = androidx.compose.foundation.BorderStroke(0.5.dp, CyberCardBorder)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 6.dp, vertical = 4.dp)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Termux standard keys: ESC, TAB, CTRL, ALT, UP, DOWN
                TermuxKeyButton(label = "ESC") {
                    inputCommand = ""
                }
                TermuxKeyButton(label = "TAB") {
                    // Autocomplete command
                    val candidates = listOf("python", "help", "neofetch", "matrix", "clear", "ls", "jarvis", "cat", "whoami", "history", "ping", "morse", "base64", "rot13")
                    val match = candidates.find { it.startsWith(inputCommand.trim().lowercase()) && it != inputCommand.trim().lowercase() }
                    if (match != null) {
                        inputCommand = match + " "
                    }
                }
                TermuxKeyButton(label = "CTRL") {
                    Toast.makeText(context, "CTRL modifier", Toast.LENGTH_SHORT).show()
                }
                TermuxKeyButton(label = "ALT") {
                    Toast.makeText(context, "ALT modifier", Toast.LENGTH_SHORT).show()
                }
                TermuxKeyButton(label = "▲") {
                    // Previous command in history
                    if (commandHistory.isNotEmpty()) {
                        if (historyIndex == -1) {
                            historyIndex = commandHistory.size - 1
                        } else if (historyIndex > 0) {
                            historyIndex--
                        }
                        inputCommand = commandHistory.getOrElse(historyIndex) { "" }
                    }
                }
                TermuxKeyButton(label = "▼") {
                    // Next command in history
                    if (commandHistory.isNotEmpty() && historyIndex != -1) {
                        if (historyIndex < commandHistory.size - 1) {
                            historyIndex++
                            inputCommand = commandHistory[historyIndex]
                        } else {
                            historyIndex = -1
                            inputCommand = ""
                        }
                    }
                }
                TermuxKeyButton(label = "clear") {
                    terminalLines.clear()
                }
                TermuxKeyButton(label = "ls") {
                    executeTerminalCommand("ls")
                }
                TermuxKeyButton(label = "neofetch") {
                    executeTerminalCommand("neofetch")
                }
                TermuxKeyButton(label = "matrix") {
                    executeTerminalCommand("matrix")
                }
                TermuxKeyButton(label = "help") {
                    executeTerminalCommand("help")
                }
            }
        }

        // Active Command Input Line with blinking prompt
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.Black
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isPythonMode) ">>> " else "Anamol@AnmolNotes:~$ ",
                    color = if (isPythonMode) NeonCyan else NeonGreen,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )

                BasicTextField(
                    value = inputCommand,
                    onValueChange = { inputCommand = it },
                    textStyle = TextStyle(
                        color = TextBright,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp
                    ),
                    cursorBrush = SolidColor(NeonGreen),
                    singleLine = true,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("termux_command_input"),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(
                        onSend = {
                            val cmd = inputCommand
                            inputCommand = ""
                            executeTerminalCommand(cmd)
                        }
                    )
                )

                IconButton(
                    onClick = {
                        val cmd = inputCommand
                        inputCommand = ""
                        executeTerminalCommand(cmd)
                    },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Execute Command",
                        tint = NeonGreen,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun TermuxKeyButton(
    label: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        color = CyberSurface,
        shape = RoundedCornerShape(3.dp),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, CyberCardBorder),
        modifier = Modifier.height(28.dp)
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                color = NeonGreen,
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
