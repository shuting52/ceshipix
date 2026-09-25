package com.landesheji.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.landesheji.data.model.LayerItem
import com.landesheji.data.model.LayerType
import com.landesheji.data.model.ShapeProperties
import com.landesheji.ui.theme.KawaiiBg
import com.landesheji.ui.theme.KawaiiMint
import com.landesheji.ui.theme.KawaiiOutline
import com.landesheji.ui.theme.KawaiiPink
import com.landesheji.ui.theme.KawaiiPinkLight
import com.landesheji.ui.theme.KawaiiSurface
import com.landesheji.ui.theme.KawaiiTextMuted
import com.landesheji.ui.theme.KawaiiTextPrimary
import com.landesheji.ui.theme.KawaiiTextSecondary
import com.landesheji.ui.theme.KawaiiYellow

@Composable
fun ShapeToolBar(
    selectedLayer: LayerItem?,
    onOpenStickers: () -> Unit,
    onOpenShapes: () -> Unit,
    onImportImage: () -> Unit,
    onStartDraw: () -> Unit,
    onDelete: () -> Unit,
    onDuplicate: () -> Unit,
    onToFront: () -> Unit,
    onToBack: () -> Unit,
    onUpdateShapeProps: ((ShapeProperties) -> ShapeProperties) -> Unit,
    onUpdateOpacity: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    var showColorPicker by remember { mutableStateOf(false) }
    var showStrokePicker by remember { mutableStateOf(false) }
    // 需求7：几何图形调节面板（圆角弧度 / 不透明度）
    var showAdjustPanel by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(KawaiiBg.copy(alpha = 0.86f))
            .border(2.dp, KawaiiOutline)
            .testTag("shape_toolbar")
    ) {
        // Expandable settings if a shape layer is selected
        if (selectedLayer != null && selectedLayer.type == LayerType.SHAPE) {
            if (showColorPicker || showStrokePicker) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(KawaiiSurface)
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (showColorPicker) "🎨 形状填充颜色" else "✒️ 形状边框描边",
                            fontWeight = FontWeight.Black,
                            fontSize = 13.sp,
                            color = KawaiiPink
                        )
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .jellyClickable {
                                    showColorPicker = false
                                    showStrokePicker = false
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "关闭",
                                tint = KawaiiTextPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    if (showColorPicker) {
                        val palette = listOf(
                            Color(0xFFFF6B9D), // Kawaii Pink
                            Color(0xFFFFD166), // Yellow
                            Color(0xFF06D6A0), // Mint
                            Color(0xFF4EA8DE), // Blue
                            Color(0xFFB57EDC), // Lavender
                            Color(0xFFFF9E79), // Peach
                            Color.White,
                            Color(0xFF382933), // Chocolate
                            Color(0xFF8338EC)
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            palette.forEach { c ->
                                val isSel = selectedLayer.shapeProps.fillColorArgb == c.toArgb()
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(c)
                                        .border(
                                            width = if (isSel) 3.dp else 1.5.dp,
                                            color = if (isSel) KawaiiPink else KawaiiOutline,
                                            shape = CircleShape
                                        )
                                        .jellyClickable {
                                            onUpdateShapeProps { it.copy(fillColorArgb = c.toArgb()) }
                                        }
                                )
                            }
                        }
                    }

                    if (showStrokePicker) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "启用边框", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = KawaiiTextPrimary)
                            Switch(
                                checked = selectedLayer.shapeProps.hasStroke,
                                onCheckedChange = { onUpdateShapeProps { p -> p.copy(hasStroke = it) } },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = KawaiiPink,
                                    checkedTrackColor = KawaiiPinkLight
                                )
                            )
                        }
                        if (selectedLayer.shapeProps.hasStroke) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "边框粗细: ${selectedLayer.shapeProps.strokeWidth.toInt()}px",
                                fontSize = 12.sp,
                                color = KawaiiTextSecondary
                            )
                            Slider(
                                value = selectedLayer.shapeProps.strokeWidth,
                                onValueChange = { w -> onUpdateShapeProps { it.copy(strokeWidth = w) } },
                                valueRange = 1f..16f,
                                colors = SliderDefaults.colors(
                                    thumbColor = KawaiiPink,
                                    activeTrackColor = KawaiiPink
                                )
                            )
                        }
                    }
                }
                HorizontalDivider(color = KawaiiOutline, thickness = 1.dp)
            }

            // 需求7：几何图形调节面板（圆角弧度 + 不透明度）
            if (showAdjustPanel) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(KawaiiSurface)
                        .padding(horizontal = 12.dp, vertical = 10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "🔧 图形调节",
                            fontWeight = FontWeight.Black,
                            fontSize = 13.sp,
                            color = KawaiiPink
                        )
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .jellyClickable { showAdjustPanel = false },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "关闭",
                                tint = KawaiiTextPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    // 圆角弧度
                    Text(
                        text = "圆角弧度: ${selectedLayer.shapeProps.cornerRadius.toInt()}px",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = KawaiiTextPrimary,
                        modifier = Modifier.padding(top = 6.dp)
                    )
                    Slider(
                        value = selectedLayer.shapeProps.cornerRadius,
                        onValueChange = { r -> onUpdateShapeProps { it.copy(cornerRadius = r) } },
                        valueRange = 0f..60f,
                        colors = SliderDefaults.colors(thumbColor = KawaiiPink, activeTrackColor = KawaiiPink)
                    )

                    // 不透明度
                    Text(
                        text = "不透明度: ${(selectedLayer.opacity * 100).toInt()}%",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = KawaiiTextPrimary,
                        modifier = Modifier.padding(top = 6.dp)
                    )
                    Slider(
                        value = selectedLayer.opacity,
                        onValueChange = { o -> onUpdateOpacity(o) },
                        valueRange = 0.1f..1f,
                        colors = SliderDefaults.colors(thumbColor = KawaiiMint, activeTrackColor = KawaiiMint)
                    )
                }
                HorizontalDivider(color = KawaiiOutline, thickness = 1.dp)
            }
        }

        // Horizontal tools list
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp)
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Stickers Button
            Column(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .jellyClickable { onOpenStickers() }
                    .padding(horizontal = 10.dp, vertical = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(KawaiiPinkLight, CircleShape)
                        .border(1.5.dp, KawaiiOutline, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "⭐", fontSize = 16.sp)
                }
                Text(
                    text = "贴纸",
                    fontSize = 11.sp,
                    color = KawaiiPink,
                    fontWeight = FontWeight.Bold
                )
            }

            // Shapes Button
            Column(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .jellyClickable { onOpenShapes() }
                    .padding(horizontal = 10.dp, vertical = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(KawaiiSurface, CircleShape)
                        .border(1.5.dp, KawaiiOutline, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "🔷", fontSize = 16.sp)
                }
                Text(
                    text = "几何图形",
                    fontSize = 11.sp,
                    color = KawaiiTextPrimary,
                    fontWeight = FontWeight.Bold
                )
            }

            // Import Image Button
            Column(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .jellyClickable { onImportImage() }
                    .padding(horizontal = 10.dp, vertical = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(KawaiiSurface, CircleShape)
                        .border(1.5.dp, KawaiiOutline, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "🖼️", fontSize = 16.sp)
                }
                Text(
                    text = "导入图片",
                    fontSize = 11.sp,
                    color = KawaiiTextPrimary,
                    fontWeight = FontWeight.Bold
                )
            }

            // Draw Button
            Column(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .jellyClickable { onStartDraw() }
                    .padding(horizontal = 10.dp, vertical = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(KawaiiSurface, CircleShape)
                        .border(1.5.dp, KawaiiOutline, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "✏️", fontSize = 16.sp)
                }
                Text(
                    text = "画笔涂鸦",
                    fontSize = 11.sp,
                    color = KawaiiTextPrimary,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(6.dp))
            Box(
                modifier = Modifier
                    .width(1.5.dp)
                    .height(36.dp)
                    .background(KawaiiOutline)
            )
            Spacer(modifier = Modifier.width(6.dp))

            // If a shape layer is selected, show shape editing tools
            if (selectedLayer != null) {
                // 需求7：图形调节（弧度/不透明度）
                Column(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .jellyClickable { showAdjustPanel = !showAdjustPanel }
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "🔧", fontSize = 18.sp)
                    Text(text = "调节", fontSize = 11.sp, color = KawaiiPink, fontWeight = FontWeight.Bold)
                }

                // 需求7：本地上传自定义图形（作为图片图层）
                Column(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .jellyClickable { onImportImage() }
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "📤", fontSize = 18.sp)
                    Text(text = "上传图形", fontSize = 11.sp, color = KawaiiTextPrimary, fontWeight = FontWeight.Bold)
                }

                // Color
                Column(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .jellyClickable { showColorPicker = !showColorPicker }
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "🎨", fontSize = 18.sp)
                    Text(text = "颜色", fontSize = 11.sp, color = KawaiiTextPrimary, fontWeight = FontWeight.Bold)
                }

                // Stroke
                Column(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .jellyClickable { showStrokePicker = !showStrokePicker }
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "✒️", fontSize = 18.sp)
                    Text(text = "描边", fontSize = 11.sp, color = KawaiiTextPrimary, fontWeight = FontWeight.Bold)
                }

                // Duplicate
                Column(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .jellyClickable { onDuplicate() }
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "⧉", fontSize = 18.sp)
                    Text(text = "复制", fontSize = 11.sp, color = KawaiiTextPrimary, fontWeight = FontWeight.Bold)
                }

                // Delete
                Column(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .jellyClickable { onDelete() }
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "🗑️", fontSize = 18.sp)
                    Text(text = "删除", fontSize = 11.sp, color = KawaiiPink, fontWeight = FontWeight.Bold)
                }
            } else {
                Row(
                    modifier = Modifier.padding(start = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "👈 点击上方贴纸或几何图形添加元素",
                        fontSize = 12.sp,
                        color = KawaiiTextSecondary
                    )
                }
            }
        }
    }
}
