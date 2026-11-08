package com.example.ui.photo

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Crop
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Security
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.model.Note
import com.example.data.repository.NotesRepository
import com.example.ui.theme.CyberBlack
import com.example.ui.theme.CyberCardBorder
import com.example.ui.theme.CyberDark
import com.example.ui.theme.CyberSurface
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonPink
import com.example.ui.theme.NeonYellow
import com.example.ui.theme.TextBright
import com.example.ui.theme.TextDim
import kotlinx.coroutines.launch

@Composable
fun PhotoEditorScreen(
    repository: NotesRepository,
    currentUserId: String,
    onBack: () -> Unit
) {
    BackHandler { onBack() }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var selectedFilter by remember { mutableStateOf("Normal") }
    var photoTitle by remember { mutableStateOf("Encrypted Photo Note") }
    var photoCaption by remember { mutableStateOf("") }
    var addWatermark by remember { mutableStateOf(true) }
    var isSaving by remember { mutableStateOf(false) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            imageUri = uri
        }
    }

    val filters = listOf(
        "Normal" to null,
        "Matrix Green" to ColorFilter.colorMatrix(ColorMatrix().apply {
            setToScale(0.2f, 1.8f, 0.4f, 1f)
        }),
        "Night Vision" to ColorFilter.colorMatrix(ColorMatrix().apply {
            setToScale(0.1f, 1.4f, 1.6f, 1f)
        }),
        "High Contrast B&W" to ColorFilter.colorMatrix(ColorMatrix().apply {
            setToSaturation(0f)
        }),
        "Cyber Invert" to ColorFilter.colorMatrix(ColorMatrix(floatArrayOf(
            -1f, 0f, 0f, 0f, 255f,
            0f, -1f, 0f, 0f, 255f,
            0f, 0f, -1f, 0f, 255f,
            0f, 0f, 0f, 1f, 0f
        )))
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CyberBlack)
            .padding(14.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Top Bar
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

            Text(
                text = "IN-APP PHOTO EDITOR",
                style = MaterialTheme.typography.titleMedium,
                color = NeonGreen,
                fontWeight = FontWeight.Bold
            )

            Button(
                onClick = {
                    if (imageUri == null && photoCaption.isBlank()) {
                        Toast.makeText(context, "Select an image or write a caption", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    isSaving = true
                    scope.launch {
                        val note = Note(
                            userId = currentUserId,
                            title = photoTitle.ifBlank { "Photo Note" },
                            content = buildString {
                                append(photoCaption)
                                if (addWatermark) {
                                    append("\n\n[CERTIFIED SECURE // ANAMOL NOTES]")
                                }
                            },
                            category = "Photos",
                            tags = listOf("Photo", "Media", "AnamolArchive"),
                            imageUrl = imageUri?.toString(),
                            colorTheme = "#00F0FF",
                            noteType = "photo"
                        )
                        repository.saveNote(note)
                        isSaving = false
                        Toast.makeText(context, "Photo note saved to cloud!", Toast.LENGTH_SHORT).show()
                        onBack()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = NeonGreen, contentColor = CyberBlack),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("save_photo_note_button")
            ) {
                if (isSaving) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), color = CyberBlack, strokeWidth = 2.dp)
                } else {
                    Icon(imageVector = Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("SAVE", fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Image Preview or Pick Button
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
                .border(1.dp, NeonCyan.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                .clickable {
                    photoPickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
            color = CyberDark,
            shape = RoundedCornerShape(12.dp)
        ) {
            if (imageUri != null) {
                Box(modifier = Modifier.fillMaxSize()) {
                    val activeColorFilter = filters.firstOrNull { it.first == selectedFilter }?.second
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(imageUri)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Selected Photo",
                        contentScale = ContentScale.Crop,
                        colorFilter = activeColorFilter,
                        modifier = Modifier.fillMaxSize()
                    )

                    // Cyber Security Watermark Overlay
                    if (addWatermark) {
                        Surface(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(10.dp),
                            color = CyberBlack.copy(alpha = 0.75f),
                            shape = RoundedCornerShape(4.dp),
                            border = androidx.compose.foundation.BorderStroke(0.5.dp, NeonGreen)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Security,
                                    contentDescription = null,
                                    tint = NeonGreen,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "ANMOL SECURE // ANAMOL",
                                    color = NeonGreen,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            } else {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.AddPhotoAlternate,
                            contentDescription = null,
                            tint = NeonCyan,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "TAP TO SELECT PHOTO FROM GALLERY",
                            style = MaterialTheme.typography.labelMedium,
                            color = NeonCyan,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Zero-permission Android Photo Picker",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextDim,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Cyberpunk Filter Chips
        Text(
            text = "CYBERPUNK FILTERS:",
            style = MaterialTheme.typography.labelSmall,
            color = NeonGreen,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(6.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            filters.forEach { (filterName, _) ->
                val isSelected = selectedFilter == filterName
                FilterChip(
                    selected = isSelected,
                    onClick = { selectedFilter = filterName },
                    label = {
                        Text(
                            text = filterName,
                            fontSize = 11.sp,
                            color = if (isSelected) NeonGreen else TextDim
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(containerColor = CyberDark),
                    border = FilterChipDefaults.filterChipBorder(borderColor = if (isSelected) NeonGreen else CyberCardBorder, enabled = true, selected = isSelected)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Note Title
        OutlinedTextField(
            value = photoTitle,
            onValueChange = { photoTitle = it },
            label = { Text("Photo Title", color = TextDim) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
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

        // Photo Caption / Annotation
        OutlinedTextField(
            value = photoCaption,
            onValueChange = { photoCaption = it },
            label = { Text("Caption & Cyber Annotations", color = TextDim) },
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
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
