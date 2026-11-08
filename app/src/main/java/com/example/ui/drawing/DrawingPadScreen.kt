package com.example.ui.drawing

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Note
import com.example.data.repository.NotesRepository
import com.example.ui.theme.CyberBlack
import com.example.ui.theme.CyberCardBorder
import com.example.ui.theme.CyberDark
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonPink
import com.example.ui.theme.NeonYellow
import com.example.ui.theme.TextBright
import com.example.ui.theme.TextDim
import kotlinx.coroutines.launch

data class StrokePath(
    val points: List<Offset>,
    val color: Color,
    val strokeWidth: Float,
    val isHighlighter: Boolean = false
)

@Composable
fun DrawingPadScreen(
    repository: NotesRepository,
    currentUserId: String,
    onBack: () -> Unit
) {
    BackHandler { onBack() }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val paths = remember { mutableStateListOf<StrokePath>() }
    var currentPath by remember { mutableStateOf<List<Offset>>(emptyList()) }

    var selectedColor by remember { mutableStateOf(NeonGreen) }
    var strokeWidth by remember { mutableFloatStateOf(6f) }
    var selectedBrushStyle by remember { mutableStateOf("Neon Pen") } // "Neon Pen", "Pencil", "Highlighter"
    var isSaving by remember { mutableStateOf(false) }

    val colors = listOf(
        NeonGreen to "Neon Green",
        NeonCyan to "Cyber Cyan",
        NeonPink to "Neon Pink",
        NeonYellow to "Neon Yellow",
        Color.White to "Pure White",
        Color(0xFFFF9900) to "Amber",
        Color(0xFFB026FF) to "Neon Purple"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CyberBlack)
    ) {
        // Top Toolbar
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = CyberDark,
            shadowElevation = 4.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
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
                    text = "FREEHAND DRAWING PAD",
                    style = MaterialTheme.typography.titleMedium,
                    color = NeonGreen,
                    fontWeight = FontWeight.Bold
                )

                Row {
                    // Undo
                    IconButton(
                        onClick = {
                            if (paths.isNotEmpty()) paths.removeAt(paths.size - 1)
                        },
                        enabled = paths.isNotEmpty()
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Undo,
                            contentDescription = "Undo",
                            tint = if (paths.isNotEmpty()) NeonCyan else TextDim
                        )
                    }

                    // Clear
                    IconButton(
                        onClick = { paths.clear() },
                        enabled = paths.isNotEmpty()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Clear Canvas",
                            tint = if (paths.isNotEmpty()) NeonPink else TextDim
                        )
                    }

                    // Save as Note
                    Button(
                        onClick = {
                            if (paths.isEmpty()) {
                                Toast.makeText(context, "Drawing canvas is blank", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            isSaving = true
                            scope.launch {
                                val strokeCount = paths.size
                                val note = Note(
                                    userId = currentUserId,
                                    title = "Drawing Note #${(100..999).random()}",
                                    content = "[Visual Drawing Note crafted by Operator Anamol containing $strokeCount freehand strokes]",
                                    category = "Drawings",
                                    tags = listOf("Drawing", "Canvas", "AnamolArtwork"),
                                    colorTheme = "#00F0FF",
                                    drawingData = "strokes:$strokeCount",
                                    noteType = "drawing"
                                )
                                repository.saveNote(note)
                                isSaving = false
                                Toast.makeText(context, "Drawing saved to Cloud Notes!", Toast.LENGTH_SHORT).show()
                                onBack()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonGreen, contentColor = CyberBlack),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("save_drawing_button")
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
            }
        }

        // Secondary controls: brush styles, thickness, colors
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(CyberDark)
                .padding(horizontal = 14.dp, vertical = 6.dp)
        ) {
            // Brush Styles
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("Neon Pen", "Pencil", "Highlighter").forEach { style ->
                    val isSelected = selectedBrushStyle == style
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedBrushStyle = style },
                        label = { Text(style, fontSize = 11.sp, color = if (isSelected) NeonGreen else TextDim) },
                        colors = FilterChipDefaults.filterChipColors(containerColor = CyberBlack),
                        border = FilterChipDefaults.filterChipBorder(borderColor = if (isSelected) NeonGreen else CyberCardBorder, enabled = true, selected = isSelected)
                    )
                }
            }

            // Thickness Slider
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "THICKNESS: ${strokeWidth.toInt()}px", style = MaterialTheme.typography.labelSmall, color = TextDim, fontSize = 10.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Slider(
                    value = strokeWidth,
                    onValueChange = { strokeWidth = it },
                    valueRange = 2f..32f,
                    colors = SliderDefaults.colors(thumbColor = NeonGreen, activeTrackColor = NeonGreen),
                    modifier = Modifier.weight(1f)
                )
            }

            // Colors Palette
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                colors.forEach { (color, _) ->
                    val isSelected = selectedColor == color
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .background(color, CircleShape)
                            .border(
                                width = if (isSelected) 2.dp else 0.5.dp,
                                color = if (isSelected) TextBright else CyberCardBorder,
                                shape = CircleShape
                            )
                            .clickable { selectedColor = color }
                    )
                }
            }
        }

        // Canvas Area
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(CyberBlack)
                .pointerInput(selectedColor, strokeWidth, selectedBrushStyle) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            currentPath = listOf(offset)
                        },
                        onDrag = { change, _ ->
                            change.consume()
                            currentPath = currentPath + change.position
                        },
                        onDragEnd = {
                            if (currentPath.isNotEmpty()) {
                                paths.add(
                                    StrokePath(
                                        points = currentPath,
                                        color = if (selectedBrushStyle == "Highlighter") selectedColor.copy(alpha = 0.35f) else selectedColor,
                                        strokeWidth = if (selectedBrushStyle == "Highlighter") strokeWidth * 2.2f else strokeWidth,
                                        isHighlighter = selectedBrushStyle == "Highlighter"
                                    )
                                )
                                currentPath = emptyList()
                            }
                        }
                    )
                }
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                // Draw completed paths
                for (stroke in paths) {
                    if (stroke.points.size > 1) {
                        val path = Path().apply {
                            moveTo(stroke.points.first().x, stroke.points.first().y)
                            for (i in 1 until stroke.points.size) {
                                lineTo(stroke.points[i].x, stroke.points[i].y)
                            }
                        }
                        drawPath(
                            path = path,
                            color = stroke.color,
                            style = Stroke(
                                width = stroke.strokeWidth,
                                cap = StrokeCap.Round,
                                join = StrokeJoin.Round
                            )
                        )
                    }
                }

                // Draw active path
                if (currentPath.size > 1) {
                    val activePath = Path().apply {
                        moveTo(currentPath.first().x, currentPath.first().y)
                        for (i in 1 until currentPath.size) {
                            lineTo(currentPath[i].x, currentPath[i].y)
                        }
                    }
                    drawPath(
                        path = activePath,
                        color = if (selectedBrushStyle == "Highlighter") selectedColor.copy(alpha = 0.35f) else selectedColor,
                        style = Stroke(
                            width = if (selectedBrushStyle == "Highlighter") strokeWidth * 2.2f else strokeWidth,
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round
                        )
                    )
                }
            }
        }
    }
}
