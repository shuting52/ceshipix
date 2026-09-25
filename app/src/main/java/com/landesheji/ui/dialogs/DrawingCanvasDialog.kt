package com.landesheji.ui.dialogs

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.landesheji.data.model.DrawPoint
import com.landesheji.data.model.DrawStroke
import com.landesheji.ui.components.KawaiiButton
import com.landesheji.ui.components.jellyClickable
import com.landesheji.ui.components.kawaiiShadow
import com.landesheji.ui.theme.KawaiiCardTint
import com.landesheji.ui.theme.KawaiiOutline
import com.landesheji.ui.theme.KawaiiPink
import com.landesheji.ui.theme.KawaiiPinkLight
import com.landesheji.ui.theme.KawaiiSurface
import com.landesheji.ui.theme.KawaiiTextPrimary
import com.landesheji.ui.theme.KawaiiTextSecondary
import com.landesheji.ui.theme.KawaiiYellow

@Composable
fun DrawingCanvasDialog(
    onDismiss: () -> Unit,
    onConfirm: (List<DrawStroke>) -> Unit,
    initialStrokes: List<DrawStroke> = emptyList()
) {
    val strokes = remember { mutableStateListOf<DrawStroke>().apply { addAll(initialStrokes) } }
    var currentPoints = remember { mutableStateListOf<DrawPoint>() }
    var currentColor by remember { mutableStateOf(KawaiiPink) }
    var strokeWidth by remember { mutableStateOf(8f) }
    var isEraser by remember { mutableStateOf(false) }
    // 需求8：撤销栈（回退上一步）
    val undoStack = remember { mutableListOf<DrawStroke>() }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .kawaiiShadow(shadowOffset = 5.dp, cornerRadius = 20.dp)
                .border(2.dp, KawaiiOutline, RoundedCornerShape(20.dp))
                .testTag("drawing_dialog"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = KawaiiSurface)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "✏️ 可爱手绘涂鸦板",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        color = KawaiiTextPrimary
                    )

                    Row {
                        // 需求8：撤销上一步
                        IconButton(
                            onClick = {
                                if (strokes.isNotEmpty()) {
                                    undoStack.add(strokes.removeAt(strokes.size - 1))
                                }
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Undo,
                                contentDescription = "撤销",
                                tint = KawaiiPink
                            )
                        }

                        IconButton(
                            onClick = {
                                strokes.clear()
                                currentPoints.clear()
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "清空画布",
                                tint = KawaiiTextSecondary
                            )
                        }

                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "关闭",
                                tint = KawaiiTextSecondary
                            )
                        }
                    }
                }

                HorizontalDivider(color = KawaiiOutline.copy(alpha = 0.2f))

                // Drawing Pad Canvas
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .background(Color.White)
                        .pointerInput(currentColor, strokeWidth, isEraser) {
                            detectDragGestures(
                                onDragStart = { offset ->
                                    currentPoints.clear()
                                    currentPoints.add(DrawPoint(offset.x, offset.y))
                                },
                                onDrag = { change, _ ->
                                    change.consume()
                                    currentPoints.add(DrawPoint(change.position.x, change.position.y))
                                },
                                onDragEnd = {
                                    if (currentPoints.isNotEmpty()) {
                                        strokes.add(
                                            DrawStroke(
                                                points = currentPoints.toList(),
                                                colorArgb = currentColor.toArgb(),
                                                strokeWidth = strokeWidth,
                                                isEraser = isEraser
                                            )
                                        )
                                        currentPoints.clear()
                                    }
                                }
                            )
                        }
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        strokes.forEach { stroke ->
                            if (stroke.points.size > 1) {
                                val path = Path().apply {
                                    moveTo(stroke.points[0].x, stroke.points[0].y)
                                    for (i in 1 until stroke.points.size) {
                                        lineTo(stroke.points[i].x, stroke.points[i].y)
                                    }
                                }
                                drawPath(
                                    path = path,
                                    color = if (stroke.isEraser) Color.White else Color(stroke.colorArgb),
                                    style = Stroke(width = stroke.strokeWidth)
                                )
                            }
                        }

                        if (currentPoints.size > 1) {
                            val path = Path().apply {
                                moveTo(currentPoints[0].x, currentPoints[0].y)
                                for (i in 1 until currentPoints.size) {
                                    lineTo(currentPoints[i].x, currentPoints[i].y)
                                }
                            }
                            drawPath(
                                path = path,
                                color = if (isEraser) Color.White else currentColor,
                                style = Stroke(width = strokeWidth)
                            )
                        }
                    }
                }

                HorizontalDivider(color = KawaiiOutline.copy(alpha = 0.2f))

                // Bottom Tools Bar
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(KawaiiCardTint)
                        .padding(14.dp)
                ) {
                    val palette = listOf(
                        KawaiiPink,
                        Color(0xFFFF5252),
                        Color(0xFFFF9F1C),
                        Color(0xFFFFD166),
                        Color(0xFF06D6A0),
                        Color(0xFF118AB2),
                        Color(0xFF8338EC),
                        Color(0xFF2A2A38)
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        palette.forEach { c ->
                            val isSel = !isEraser && currentColor == c
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(c)
                                    .border(
                                        width = if (isSel) 3.dp else 1.5.dp,
                                        color = if (isSel) KawaiiOutline else KawaiiOutline.copy(alpha = 0.3f),
                                        shape = CircleShape
                                    )
                                    .jellyClickable {
                                        currentColor = c
                                        isEraser = false
                                    }
                            )
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        // Eraser
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isEraser) KawaiiPinkLight else KawaiiSurface)
                                .border(
                                    width = 1.5.dp,
                                    color = if (isEraser) KawaiiPink else KawaiiOutline.copy(alpha = 0.3f),
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .jellyClickable { isEraser = true }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "🧽 橡皮擦",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isEraser) KawaiiPink else KawaiiTextPrimary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Stroke width slider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "笔触粗细: ${strokeWidth.toInt()}px",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = KawaiiTextSecondary,
                            modifier = Modifier.width(95.dp)
                        )
                        Slider(
                            value = strokeWidth,
                            onValueChange = { strokeWidth = it },
                            valueRange = 2f..40f,
                            colors = SliderDefaults.colors(
                                thumbColor = KawaiiPink,
                                activeTrackColor = KawaiiPink
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = onDismiss) {
                            Text("取消", color = KawaiiTextSecondary, fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        KawaiiButton(
                            text = if (initialStrokes.isEmpty()) "加入画布 ✨" else "更新涂鸦 ✨",
                            onClick = {
                                onConfirm(strokes.toList())
                                onDismiss()
                            },
                            containerColor = KawaiiPink
                        )
                    }
                }
            }
        }
    }
}
