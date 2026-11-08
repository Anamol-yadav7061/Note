package com.example.ui.tools

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Transform
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CodeBackground
import com.example.ui.theme.CyberBlack
import com.example.ui.theme.CyberCardBorder
import com.example.ui.theme.CyberDark
import com.example.ui.theme.CyberSurface
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonPink
import com.example.ui.theme.NeonYellow
import com.example.ui.theme.TextBright
import com.example.ui.theme.TextDim
import com.example.util.HackerTools

@Composable
fun HackerToolsScreen() {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabTitles = listOf("Morse Code", "Encoders", "Ciphers", "Hashes")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CyberBlack)
    ) {
        // Tab Header
        TabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = CyberDark,
            contentColor = NeonGreen,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                    color = NeonGreen
                )
            }
        ) {
            tabTitles.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = {
                        Text(
                            text = title,
                            fontSize = 12.sp,
                            fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedTabIndex == index) NeonGreen else TextDim
                        )
                    }
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            when (selectedTabIndex) {
                0 -> MorseCodeSection(clipboardManager, context)
                1 -> EncodersSection(clipboardManager, context)
                2 -> CipherSection(clipboardManager, context)
                3 -> HashSection(clipboardManager, context)
            }
        }
    }
}

@Composable
private fun MorseCodeSection(clipboardManager: androidx.compose.ui.platform.ClipboardManager, context: android.content.Context) {
    var textInput by remember { mutableStateOf("ANMOL NOTES BY ANAMOL") }
    var isReverse by remember { mutableStateOf(false) }

    val output = if (!isReverse) HackerTools.textToMorse(textInput) else HackerTools.morseToText(textInput)

    Text(
        text = "BIDIRECTIONAL MORSE CODE TRANSLATOR",
        style = MaterialTheme.typography.titleMedium,
        color = NeonGreen,
        fontWeight = FontWeight.Bold
    )
    Text(
        text = "Convert plaintext to Morse and vice-versa in real time",
        style = MaterialTheme.typography.labelSmall,
        color = TextDim
    )

    Spacer(modifier = Modifier.height(14.dp))

    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        FilterChip(
            selected = !isReverse,
            onClick = { isReverse = false },
            label = { Text("Text to Morse (.-)") },
            colors = FilterChipDefaults.filterChipColors(containerColor = CyberDark)
        )
        FilterChip(
            selected = isReverse,
            onClick = { isReverse = true },
            label = { Text("Morse to Text") },
            colors = FilterChipDefaults.filterChipColors(containerColor = CyberDark)
        )
    }

    Spacer(modifier = Modifier.height(12.dp))

    OutlinedTextField(
        value = textInput,
        onValueChange = { textInput = it },
        label = { Text(if (!isReverse) "Enter Plaintext" else "Enter Morse (dots and dashes)") },
        modifier = Modifier.fillMaxWidth().height(120.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = NeonGreen,
            unfocusedBorderColor = CyberCardBorder,
            focusedTextColor = TextBright,
            unfocusedTextColor = TextBright,
            focusedContainerColor = CyberDark,
            unfocusedContainerColor = CyberDark
        )
    )

    Spacer(modifier = Modifier.height(14.dp))

    // Output Box
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, NeonCyan.copy(alpha = 0.5f), RoundedCornerShape(8.dp)),
        color = CodeBackground,
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "TRANSLATED RESULT", style = MaterialTheme.typography.labelSmall, color = NeonCyan)
                IconButton(
                    onClick = {
                        clipboardManager.setText(AnnotatedString(output))
                        Toast.makeText(context, "Copied Morse", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(imageVector = Icons.Default.ContentCopy, contentDescription = "Copy", tint = NeonCyan, modifier = Modifier.size(16.dp))
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = output,
                color = NeonGreen,
                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                style = MaterialTheme.typography.bodyLarge,
                fontSize = 16.sp,
                lineHeight = 24.sp
            )
        }
    }
}

@Composable
private fun EncodersSection(clipboardManager: androidx.compose.ui.platform.ClipboardManager, context: android.content.Context) {
    var rawInput by remember { mutableStateOf("Anamol Notes Protocol 2026") }
    var selectedFormat by remember { mutableStateOf("Base64") }
    var isDecodeMode by remember { mutableStateOf(false) }

    val output = when (selectedFormat) {
        "Base64" -> if (!isDecodeMode) HackerTools.base64Encode(rawInput) else HackerTools.base64Decode(rawInput)
        "Hexadecimal" -> if (!isDecodeMode) HackerTools.hexEncode(rawInput) else HackerTools.hexDecode(rawInput)
        "Binary" -> if (!isDecodeMode) HackerTools.binaryEncode(rawInput) else HackerTools.binaryDecode(rawInput)
        "URL" -> if (!isDecodeMode) HackerTools.urlEncode(rawInput) else HackerTools.urlDecode(rawInput)
        else -> ""
    }

    Text(
        text = "DEVELOPER ENCODERS & DECODERS",
        style = MaterialTheme.typography.titleMedium,
        color = NeonGreen,
        fontWeight = FontWeight.Bold
    )

    Spacer(modifier = Modifier.height(10.dp))

    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        listOf("Base64", "Hexadecimal", "Binary", "URL").forEach { fmt ->
            FilterChip(
                selected = selectedFormat == fmt,
                onClick = { selectedFormat = fmt },
                label = { Text(fmt, fontSize = 11.sp) },
                colors = FilterChipDefaults.filterChipColors(containerColor = CyberDark)
            )
        }
    }

    Spacer(modifier = Modifier.height(8.dp))

    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        FilterChip(
            selected = !isDecodeMode,
            onClick = { isDecodeMode = false },
            label = { Text("Encode") },
            colors = FilterChipDefaults.filterChipColors(containerColor = CyberDark)
        )
        FilterChip(
            selected = isDecodeMode,
            onClick = { isDecodeMode = true },
            label = { Text("Decode") },
            colors = FilterChipDefaults.filterChipColors(containerColor = CyberDark)
        )
    }

    Spacer(modifier = Modifier.height(12.dp))

    OutlinedTextField(
        value = rawInput,
        onValueChange = { rawInput = it },
        label = { Text(if (!isDecodeMode) "Input Text to Encode" else "Encoded Text to Decode") },
        modifier = Modifier.fillMaxWidth().height(110.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = NeonGreen,
            unfocusedBorderColor = CyberCardBorder,
            focusedTextColor = TextBright,
            unfocusedTextColor = TextBright,
            focusedContainerColor = CyberDark,
            unfocusedContainerColor = CyberDark
        )
    )

    Spacer(modifier = Modifier.height(14.dp))

    Surface(
        modifier = Modifier.fillMaxWidth().border(1.dp, NeonCyan.copy(alpha = 0.5f), RoundedCornerShape(8.dp)),
        color = CodeBackground,
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "$selectedFormat OUTPUT", style = MaterialTheme.typography.labelSmall, color = NeonCyan)
                IconButton(
                    onClick = {
                        clipboardManager.setText(AnnotatedString(output))
                        Toast.makeText(context, "Copied output", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(imageVector = Icons.Default.ContentCopy, contentDescription = "Copy", tint = NeonCyan, modifier = Modifier.size(16.dp))
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = output,
                color = NeonGreen,
                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                style = MaterialTheme.typography.bodySmall,
                fontSize = 13.sp,
                lineHeight = 18.sp
            )
        }
    }
}

@Composable
private fun CipherSection(clipboardManager: androidx.compose.ui.platform.ClipboardManager, context: android.content.Context) {
    var cipherType by remember { mutableStateOf("Caesar Cipher") }
    var textInput by remember { mutableStateOf("MEET AT THE CIPHER ROOM ANAMOL") }
    var caesarShift by remember { mutableFloatStateOf(7f) }
    var xorKey by remember { mutableStateOf("SECRET_KEY_ANAMOL") }
    var isXorDecrypt by remember { mutableStateOf(false) }

    val output = if (cipherType == "Caesar Cipher") {
        HackerTools.caesarCipher(textInput, caesarShift.toInt())
    } else {
        if (!isXorDecrypt) HackerTools.xorCipher(textInput, xorKey) else HackerTools.xorDecrypt(textInput, xorKey)
    }

    Text(
        text = "CRYPTOGRAPHY & CIPHER SUITE",
        style = MaterialTheme.typography.titleMedium,
        color = NeonGreen,
        fontWeight = FontWeight.Bold
    )

    Spacer(modifier = Modifier.height(8.dp))

    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        FilterChip(
            selected = cipherType == "Caesar Cipher",
            onClick = { cipherType = "Caesar Cipher" },
            label = { Text("Caesar Cipher") },
            colors = FilterChipDefaults.filterChipColors(containerColor = CyberDark)
        )
        FilterChip(
            selected = cipherType == "XOR Cipher",
            onClick = { cipherType = "XOR Cipher" },
            label = { Text("XOR Cipher") },
            colors = FilterChipDefaults.filterChipColors(containerColor = CyberDark)
        )
    }

    Spacer(modifier = Modifier.height(10.dp))

    if (cipherType == "Caesar Cipher") {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = "SHIFT: +${caesarShift.toInt()}", style = MaterialTheme.typography.labelSmall, color = NeonGreen)
            Spacer(modifier = Modifier.width(8.dp))
            Slider(
                value = caesarShift,
                onValueChange = { caesarShift = it },
                valueRange = 1f..25f,
                colors = SliderDefaults.colors(thumbColor = NeonGreen, activeTrackColor = NeonGreen),
                modifier = Modifier.weight(1f)
            )
        }
    } else {
        OutlinedTextField(
            value = xorKey,
            onValueChange = { xorKey = it },
            label = { Text("XOR Secret Key") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = NeonGreen,
                unfocusedBorderColor = CyberCardBorder,
                focusedTextColor = TextBright,
                unfocusedTextColor = TextBright,
                focusedContainerColor = CyberDark,
                unfocusedContainerColor = CyberDark
            )
        )
        Spacer(modifier = Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(selected = !isXorDecrypt, onClick = { isXorDecrypt = false }, label = { Text("Encrypt") })
            FilterChip(selected = isXorDecrypt, onClick = { isXorDecrypt = true }, label = { Text("Decrypt") })
        }
    }

    Spacer(modifier = Modifier.height(10.dp))

    OutlinedTextField(
        value = textInput,
        onValueChange = { textInput = it },
        label = { Text("Text Payload") },
        modifier = Modifier.fillMaxWidth().height(100.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = NeonGreen,
            unfocusedBorderColor = CyberCardBorder,
            focusedTextColor = TextBright,
            unfocusedTextColor = TextBright,
            focusedContainerColor = CyberDark,
            unfocusedContainerColor = CyberDark
        )
    )

    Spacer(modifier = Modifier.height(12.dp))

    Surface(
        modifier = Modifier.fillMaxWidth().border(1.dp, NeonCyan.copy(alpha = 0.5f), RoundedCornerShape(8.dp)),
        color = CodeBackground,
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "CIPHERTEXT", style = MaterialTheme.typography.labelSmall, color = NeonCyan)
                IconButton(
                    onClick = {
                        clipboardManager.setText(AnnotatedString(output))
                        Toast.makeText(context, "Copied ciphertext", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(imageVector = Icons.Default.ContentCopy, contentDescription = "Copy", tint = NeonCyan, modifier = Modifier.size(16.dp))
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = output,
                color = NeonGreen,
                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                style = MaterialTheme.typography.bodySmall,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
private fun HashSection(clipboardManager: androidx.compose.ui.platform.ClipboardManager, context: android.content.Context) {
    var hashInput by remember { mutableStateOf("Anamol Notes Secure Hash") }
    val sha256 = HackerTools.hashSha256(hashInput)
    val md5 = HackerTools.hashMd5(hashInput)

    Text(
        text = "CRYPTOGRAPHIC HASH DIGESTS",
        style = MaterialTheme.typography.titleMedium,
        color = NeonGreen,
        fontWeight = FontWeight.Bold
    )

    Spacer(modifier = Modifier.height(12.dp))

    OutlinedTextField(
        value = hashInput,
        onValueChange = { hashInput = it },
        label = { Text("Input String") },
        modifier = Modifier.fillMaxWidth(),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = NeonGreen,
            unfocusedBorderColor = CyberCardBorder,
            focusedTextColor = TextBright,
            unfocusedTextColor = TextBright,
            focusedContainerColor = CyberDark,
            unfocusedContainerColor = CyberDark
        )
    )

    Spacer(modifier = Modifier.height(14.dp))

    HashCard(title = "SHA-256 DIGEST", hash = sha256, clipboardManager = clipboardManager, context = context)
    Spacer(modifier = Modifier.height(10.dp))
    HashCard(title = "MD5 DIGEST", hash = md5, clipboardManager = clipboardManager, context = context)
}

@Composable
private fun HashCard(title: String, hash: String, clipboardManager: androidx.compose.ui.platform.ClipboardManager, context: android.content.Context) {
    Surface(
        modifier = Modifier.fillMaxWidth().border(1.dp, NeonCyan.copy(alpha = 0.3f), RoundedCornerShape(8.dp)),
        color = CodeBackground,
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = title, style = MaterialTheme.typography.labelSmall, color = NeonCyan)
                IconButton(
                    onClick = {
                        clipboardManager.setText(AnnotatedString(hash))
                        Toast.makeText(context, "Copied hash", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(imageVector = Icons.Default.ContentCopy, contentDescription = "Copy", tint = NeonCyan, modifier = Modifier.size(14.dp))
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = hash,
                color = NeonGreen,
                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                style = MaterialTheme.typography.bodySmall,
                fontSize = 11.sp
            )
        }
    }
}
