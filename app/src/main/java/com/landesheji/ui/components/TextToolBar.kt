package com.landesheji.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.FormatAlignCenter
import androidx.compose.material.icons.filled.FormatAlignLeft
import androidx.compose.material.icons.filled.FormatAlignRight
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.FormatItalic
import androidx.compose.material.icons.filled.FormatUnderlined
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import com.landesheji.data.model.TextAlignment
import com.landesheji.data.model.TextProperties
import com.landesheji.ui.theme.KawaiiBg
import com.landesheji.ui.theme.KawaiiCardTint
import com.landesheji.ui.theme.KawaiiLavender
import com.landesheji.ui.theme.KawaiiLavenderLight
import com.landesheji.ui.theme.KawaiiMint
import com.landesheji.ui.theme.KawaiiMintLight
import com.landesheji.ui.theme.KawaiiOutline
import com.landesheji.ui.theme.KawaiiPeach
import com.landesheji.ui.theme.KawaiiPink
import com.landesheji.ui.theme.KawaiiPinkLight
import com.landesheji.ui.theme.KawaiiSkyBlue
import com.landesheji.ui.theme.KawaiiSurface
import com.landesheji.ui.theme.KawaiiTextMuted
import com.landesheji.ui.theme.KawaiiTextPrimary
import com.landesheji.ui.theme.KawaiiTextSecondary
import com.landesheji.ui.theme.KawaiiTextWhite
import com.landesheji.ui.theme.KawaiiYellow
import com.landesheji.ui.theme.KawaiiYellowLight

private enum class TextSubTool(val label: String, val icon: String) {
    EDIT("编辑内容", "✏️"),
    SIZE("文字大小", "🔤"),
    SCALE("图层缩放", "⤢"),
    THREE_D("PS 3D立体", "🧊"),
    COLOR("可爱调色", "🎨"),
    STYLE("字体字形", "𝐁"),
    ALIGN("排版对齐", "≡"),
    SPACING("字词间距", "↔️"),
    BACKGROUND_BOX("文字底框", "🔲"),
    STROKE("外边描边", "✒️"),
    SHADOW("投影阴影", "🌫️"),
    OPACITY("不透明度", "💧"),
    POSITION("位置微调", "✛"),
    TO_FRONT("置于顶层", "⬆️"),
    TO_BACK("置于底层", "⬇️"),
    COPY("复制图层", "⧉"),
    DELETE("删除图层", "🗑️")
}

@Composable
fun TextToolBar(
    selectedLayer: LayerItem?,
    onAddText: () -> Unit,
    onOpenQuotes: () -> Unit,
    onOpenEditor: () -> Unit,
    onDelete: () -> Unit,
    onDuplicate: () -> Unit,
    onToFront: () -> Unit,
    onToBack: () -> Unit,
    onMoveDelta: (dx: Float, dy: Float) -> Unit,
    onUpdateTextProps: ((TextProperties) -> TextProperties) -> Unit,
    onUpdateLayerOpacity: (Float) -> Unit,
    onStepFontSize: (Float) -> Unit = {},
    onSetFontSize: (Float) -> Unit = {},
    onScaleLayer: (Float) -> Unit = {},
    onSetExactScale: (Float) -> Unit = {},
    onResetTransform: () -> Unit = {},
    modifier: Modifier = Modifier,
    // 需求3：点击 PS 3D 立体时打开右侧 FX 侧边面板（边调边看）
    onOpenFxPanel: (() -> Unit)? = null
) {
    var activeSubTool by remember { mutableStateOf<TextSubTool?>(null) }
    val horizontalScrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(KawaiiBg.copy(alpha = 0.86f))
            .border(2.dp, KawaiiOutline)
            .testTag("text_toolbar")
    ) {
        // Expanded Panel for Selected Sub-Tool
        AnimatedVisibility(
            visible = activeSubTool != null && selectedLayer != null,
            enter = expandVertically(spring(dampingRatio = Spring.DampingRatioMediumBouncy)) + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            if (selectedLayer != null && activeSubTool != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(KawaiiSurface)
                        .padding(horizontal = 14.dp, vertical = 12.dp)
                ) {
                    Column {
                        // Sub-Tool Header
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = activeSubTool?.icon ?: "", fontSize = 16.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = activeSubTool?.label ?: "",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 14.sp,
                                    color = KawaiiTextPrimary
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .jellyClickable { activeSubTool = null }
                                    .clip(CircleShape)
                                    .background(KawaiiPinkLight)
                                    .border(1.5.dp, KawaiiOutline, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "关闭",
                                    tint = KawaiiOutline,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        HorizontalDivider(color = KawaiiOutline.copy(alpha = 0.2f), thickness = 1.dp)
                        Spacer(modifier = Modifier.height(10.dp))

                        // Sub-Tool Detailed Content View
                        TextSubToolContent(
                            tool = activeSubTool!!,
                            layer = selectedLayer,
                            onUpdateProps = onUpdateTextProps,
                            onUpdateOpacity = onUpdateLayerOpacity,
                            onMoveDelta = onMoveDelta,
                            onStepFontSize = onStepFontSize,
                            onSetFontSize = onSetFontSize,
                            onScaleLayer = onScaleLayer,
                            onSetExactScale = onSetExactScale,
                            onResetTransform = onResetTransform
                        )
                    }
                }
            }
        }

        HorizontalDivider(color = KawaiiOutline, thickness = 2.dp)

        // Main Horizontal Scrollable Tools Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(horizontalScrollState)
                .padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // New Text Button
            KawaiiButton(
                text = "+ 新文字",
                onClick = onAddText,
                containerColor = KawaiiPink,
                contentColor = KawaiiTextWhite,
                icon = "✨"
            )

            // Quotes Dialog Button
            KawaiiButton(
                text = "金句库",
                onClick = onOpenQuotes,
                containerColor = KawaiiYellow,
                contentColor = KawaiiTextPrimary,
                icon = "🌸"
            )

            if (selectedLayer != null) {
                TextSubTool.values().forEach { subTool ->
                    val isSelected = activeSubTool == subTool

                    val chipColor = when (subTool) {
                        TextSubTool.THREE_D -> if (isSelected) KawaiiPink else KawaiiLavenderLight
                        TextSubTool.SIZE, TextSubTool.SCALE -> if (isSelected) KawaiiYellow else KawaiiYellowLight
                        TextSubTool.COLOR -> if (isSelected) KawaiiMint else KawaiiMintLight
                        else -> if (isSelected) KawaiiPinkLight else KawaiiSurface
                    }

                    Box(
                        modifier = Modifier
                            .kawaiiShadow(shadowOffset = if (isSelected) 3.dp else 1.5.dp, cornerRadius = 12.dp)
                            .jellyClickable {
                                when (subTool) {
                                    TextSubTool.EDIT -> onOpenEditor()
                                    TextSubTool.DELETE -> onDelete()
                                    TextSubTool.COPY -> onDuplicate()
                                    TextSubTool.TO_FRONT -> onToFront()
                                    TextSubTool.TO_BACK -> onToBack()
                                    // 需求3：PS 3D 立体 → 打开右侧 FX 侧边面板
                                    TextSubTool.THREE_D -> {
                                        if (onOpenFxPanel != null) {
                                            activeSubTool = null
                                            onOpenFxPanel()
                                        } else {
                                            activeSubTool = if (activeSubTool == subTool) null else subTool
                                        }
                                    }
                                    else -> {
                                        activeSubTool = if (activeSubTool == subTool) null else subTool
                                    }
                                }
                            }
                            .clip(RoundedCornerShape(12.dp))
                            .background(chipColor)
                            .border(2.dp, KawaiiOutline, RoundedCornerShape(12.dp))
                            .padding(horizontal = 10.dp, vertical = 7.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = subTool.icon, fontSize = 13.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = subTool.label,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
                                color = KawaiiTextPrimary
                            )
                        }
                    }
                }
            } else {
                Text(
                    text = "👈 点击上方画布文字图层进行可爱编辑与 3D 设计",
                    color = KawaiiTextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
            }
        }
    }
}

@Composable
private fun TextSubToolContent(
    tool: TextSubTool,
    layer: LayerItem,
    onUpdateProps: ((TextProperties) -> TextProperties) -> Unit,
    onUpdateOpacity: (Float) -> Unit,
    onMoveDelta: (dx: Float, dy: Float) -> Unit,
    onStepFontSize: (Float) -> Unit,
    onSetFontSize: (Float) -> Unit,
    onScaleLayer: (Float) -> Unit,
    onSetExactScale: (Float) -> Unit,
    onResetTransform: () -> Unit
) {
    val props = layer.textProps
    val opacity = layer.opacity

    when (tool) {
        // ==========================================================
        // 🧊 ADOBE PHOTOSHOP 3D 立体字工坊 (FULL PHOTOSHOP 3D STUDIO)
        // ==========================================================
        TextSubTool.THREE_D -> {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp)
            ) {
                // 需求2：顶部实时预览面板，调节任一 3D 属性时立即看到效果
                ThreeDTextLivePreview(props = props, opacity = opacity)

                Spacer(modifier = Modifier.height(8.dp))

                // Main Switch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = "🧊 Adobe Photoshop 3D 立体字引擎", fontSize = 14.sp, fontWeight = FontWeight.Black, color = KawaiiTextPrimary)
                        Text(text = "支持挤出、全向角度、倒角斜面、材质质感与透视", fontSize = 11.sp, color = KawaiiTextSecondary)
                    }
                    Switch(
                        checked = props.has3D,
                        onCheckedChange = { checked -> onUpdateProps { it.copy(has3D = checked) } },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = KawaiiPink,
                            checkedTrackColor = KawaiiPinkLight,
                            uncheckedThumbColor = KawaiiOutline,
                            uncheckedTrackColor = KawaiiCardTint
                        )
                    )
                }

                if (props.has3D) {
                    Spacer(modifier = Modifier.height(10.dp))

                    // 1. 一键套用经典 PS 3D 预设
                    Text(text = "🌟 PS 经典立体风格预设（一键套用）：", fontSize = 12.sp, fontWeight = FontWeight.Black, color = KawaiiTextPrimary)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        KawaiiButton(
                            text = "🎈 软萌气球",
                            onClick = {
                                onUpdateProps {
                                    it.copy(
                                        has3D = true, depth3D = 22f, angle3D = 45f, darken3D = 0.35f,
                                        material3D = "充气气球", hasBevel = true, bevelWidth = 5f,
                                        hasGroundShadow = true
                                    )
                                }
                            },
                            containerColor = KawaiiPink,
                            contentColor = KawaiiTextWhite
                        )
                        KawaiiButton(
                            text = "🍬 Q弹果冻",
                            onClick = {
                                onUpdateProps {
                                    it.copy(
                                        has3D = true, depth3D = 18f, angle3D = 315f, darken3D = 0.38f,
                                        material3D = "可爱果冻", hasBevel = true, bevelWidth = 4f,
                                        hasGroundShadow = true
                                    )
                                }
                            },
                            containerColor = KawaiiMint,
                            contentColor = KawaiiTextWhite
                        )
                        KawaiiButton(
                            text = "👑 好莱坞金",
                            onClick = {
                                onUpdateProps {
                                    it.copy(
                                        has3D = true, depth3D = 24f, angle3D = 45f, darken3D = 0.55f,
                                        material3D = "高光光泽", colorArgb = 0xFFFFD700.toInt(),
                                        customExtrusionColor = true, extrusionColorArgb = 0xFF996515.toInt(),
                                        hasBevel = true, bevelWidth = 5f
                                    )
                                }
                            },
                            containerColor = KawaiiYellow,
                            contentColor = KawaiiTextPrimary
                        )
                        KawaiiButton(
                            text = "🕹️ 复古街机",
                            onClick = {
                                onUpdateProps {
                                    it.copy(
                                        has3D = true, depth3D = 32f, angle3D = 30f,
                                        material3D = "复古街机", isPerspective3D = false
                                    )
                                }
                            },
                            containerColor = KawaiiLavender,
                            contentColor = KawaiiTextWhite
                        )
                        KawaiiButton(
                            text = "⚡ 赛博霓虹",
                            onClick = {
                                onUpdateProps {
                                    it.copy(
                                        has3D = true, depth3D = 20f, angle3D = 225f, darken3D = 0.3f,
                                        material3D = "赛博霓虹", hasBevel = true, bevelWidth = 3f,
                                        hasGroundShadow = true
                                    )
                                }
                            },
                            containerColor = KawaiiPeach,
                            contentColor = KawaiiTextWhite
                        )
                        KawaiiButton(
                            text = "🧱 等距积木",
                            onClick = {
                                onUpdateProps {
                                    it.copy(
                                        has3D = true, depth3D = 28f, angle3D = 330f,
                                        isPerspective3D = false, darken3D = 0.6f, material3D = "默认"
                                    )
                                }
                            },
                            containerColor = KawaiiSkyBlue,
                            contentColor = KawaiiTextWhite
                        )
                        KawaiiButton(
                            text = "💎 水晶冰凌",
                            onClick = {
                                onUpdateProps {
                                    it.copy(
                                        has3D = true, depth3D = 30f, angle3D = 45f, darken3D = 0.5f,
                                        material3D = "水晶冰凌", hasBevel = true, bevelWidth = 6f,
                                        hasGroundShadow = true
                                    )
                                }
                            },
                            containerColor = KawaiiSkyBlue,
                            contentColor = KawaiiTextWhite
                        )
                        KawaiiButton(
                            text = "✨ 金属镜面",
                            onClick = {
                                onUpdateProps {
                                    it.copy(
                                        has3D = true, depth3D = 36f, angle3D = 45f, darken3D = 0.65f,
                                        material3D = "金属质感",
                                        colorArgb = 0xFFC0C0C0.toInt(),
                                        customExtrusionColor = true, extrusionColorArgb = 0xFF606060.toInt(),
                                        hasBevel = true, bevelWidth = 6f
                                    )
                                }
                            },
                            containerColor = KawaiiLavender,
                            contentColor = KawaiiTextWhite
                        )
                        KawaiiButton(
                            text = "🔥 火焰烈焰",
                            onClick = {
                                onUpdateProps {
                                    it.copy(
                                        has3D = true, depth3D = 26f, angle3D = 315f, darken3D = 0.4f,
                                        material3D = "火焰光焰",
                                        colorArgb = 0xFFFF6A00.toInt(),
                                        customExtrusionColor = true, extrusionColorArgb = 0xFF8B0000.toInt()
                                    )
                                }
                            },
                            containerColor = KawaiiPeach,
                            contentColor = KawaiiTextWhite
                        )
                        KawaiiButton(
                            text = "🌈 糖果彩虹",
                            onClick = {
                                onUpdateProps {
                                    it.copy(
                                        has3D = true, depth3D = 22f, angle3D = 225f, darken3D = 0.35f,
                                        material3D = "糖果彩虹"
                                    )
                                }
                            },
                            containerColor = KawaiiPink,
                            contentColor = KawaiiTextWhite
                        )
                        KawaiiButton(
                            text = "🍬 卡通厚涂",
                            onClick = {
                                onUpdateProps {
                                    it.copy(
                                        has3D = true, depth3D = 38f, angle3D = 135f, darken3D = 0.5f,
                                        material3D = "卡通厚涂",
                                        colorArgb = 0xFFFF7BAC.toInt(),
                                        customExtrusionColor = true, extrusionColorArgb = 0xFF8B0040.toInt()
                                    )
                                }
                            },
                            containerColor = KawaiiMint,
                            contentColor = KawaiiTextWhite
                        )
                        KawaiiButton(
                            text = "🪟 玻璃琉璃",
                            onClick = {
                                onUpdateProps {
                                    it.copy(
                                        has3D = true, depth3D = 32f, angle3D = 90f, darken3D = 0.3f,
                                        material3D = "玻璃琉璃",
                                        colorArgb = 0xFF7CD3F7.toInt()
                                    )
                                }
                            },
                            containerColor = KawaiiSkyBlue,
                            contentColor = KawaiiTextWhite
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // 2. 挤出深度 (Depth)
                    Text(text = "📏 3D 挤出深度: ${props.depth3D.toInt()} px", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = KawaiiTextPrimary)
                    Slider(
                        value = props.depth3D,
                        onValueChange = { valD -> onUpdateProps { it.copy(depth3D = valD) } },
                        valueRange = 2f..70f,
                        colors = SliderDefaults.colors(thumbColor = KawaiiPink, activeTrackColor = KawaiiPink)
                    )

                    // 3. 挤出方向角度 (Extrusion Angle)
                    Text(text = "🧭 3D 投射角度: ${props.angle3D.toInt()}°", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = KawaiiTextPrimary)
                    Slider(
                        value = props.angle3D,
                        onValueChange = { valA -> onUpdateProps { it.copy(angle3D = valA) } },
                        valueRange = 0f..360f,
                        colors = SliderDefaults.colors(thumbColor = KawaiiYellow, activeTrackColor = KawaiiYellow)
                    )

                    // 8 Quick Angle Direction Chips
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(
                            "↗ 右上 45°" to 45f,
                            "↘ 右下 315°" to 315f,
                            "↙ 左下 225°" to 225f,
                            "↖ 左上 135°" to 135f,
                            "⬆ 正上 90°" to 90f,
                            "⬇ 正下 270°" to 270f,
                            "➡ 正右 0°" to 0f,
                            "⬅ 正左 180°" to 180f
                        ).forEach { (lbl, deg) ->
                            KawaiiChip(
                                text = lbl,
                                isSelected = (props.angle3D.toInt() == deg.toInt()),
                                onClick = { onUpdateProps { it.copy(angle3D = deg) } },
                                selectedColor = KawaiiYellow
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // 4. 透视灭点 vs 等距平行模式 (Perspective vs Isometric)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = "📐 透视灭点收缩模式", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = KawaiiTextPrimary)
                            Text(text = if (props.isPerspective3D) "开启（近大远小深邃透视）" else "关闭（等距平行立体）", fontSize = 11.sp, color = KawaiiTextSecondary)
                        }
                        Switch(
                            checked = props.isPerspective3D,
                            onCheckedChange = { checked -> onUpdateProps { it.copy(isPerspective3D = checked) } },
                            colors = SwitchDefaults.colors(checkedThumbColor = KawaiiMint, checkedTrackColor = KawaiiMintLight)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // 5. 倒角与斜面浮雕 (Bevel & Emboss)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = "✨ 倒角与斜面浮雕 (Bevel & Emboss)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = KawaiiTextPrimary)
                            Text(text = "雕刻光感斜面边缘", fontSize = 11.sp, color = KawaiiTextSecondary)
                        }
                        Switch(
                            checked = props.hasBevel,
                            onCheckedChange = { checked -> onUpdateProps { it.copy(hasBevel = checked) } },
                            colors = SwitchDefaults.colors(checkedThumbColor = KawaiiLavender, checkedTrackColor = KawaiiLavenderLight)
                        )
                    }

                    if (props.hasBevel) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = "倒角宽度: ${props.bevelWidth.toInt()} px", fontSize = 11.sp, color = KawaiiTextSecondary)
                        Slider(
                            value = props.bevelWidth,
                            onValueChange = { bw -> onUpdateProps { it.copy(bevelWidth = bw) } },
                            valueRange = 1f..14f,
                            colors = SliderDefaults.colors(thumbColor = KawaiiLavender, activeTrackColor = KawaiiLavender)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // 6. 3D 表面材质质感 (Surface Materials)
                    Text(text = "🎨 3D 表面材质与高光质感：", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = KawaiiTextPrimary)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(
                            "默认" to "🍦 常规",
                            "高光光泽" to "✨ 高光亮面",
                            "可爱果冻" to "🍬 Q弹果冻",
                            "充气气球" to "🎈 充气气球",
                            "金属质感" to "💎 金属镜面",
                            "水晶冰凌" to "❄️ 水晶冰凌",
                            "糖果彩虹" to "🌈 糖果彩虹",
                            "火焰光焰" to "🔥 火焰烈焰",
                            "糖果霓虹" to "💗 糖果霓虹",
                            "玻璃琉璃" to "🪟 玻璃琉璃",
                            "卡通厚涂" to "🍬 卡通厚涂",
                            "复古街机" to "🕹️ 街机条纹",
                            "赛博霓虹" to "⚡ 赛博霓虹"
                        ).forEach { (matKey, matLabel) ->
                            KawaiiChip(
                                text = matLabel,
                                isSelected = (props.material3D == matKey),
                                onClick = { onUpdateProps { it.copy(material3D = matKey) } },
                                selectedColor = KawaiiPink
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // 7. 3D 地面投影 (Ground Shadow)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "👤 3D 地面透视投影", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = KawaiiTextPrimary)
                        Switch(
                            checked = props.hasGroundShadow,
                            onCheckedChange = { checked -> onUpdateProps { it.copy(hasGroundShadow = checked) } },
                            colors = SwitchDefaults.colors(checkedThumbColor = KawaiiPink, checkedTrackColor = KawaiiPinkLight)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // 8. 3D 空间立体俯仰倾斜 (3D Tilt)
                    Text(text = "🔄 空间 3D 俯仰侧倾：", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = KawaiiTextPrimary)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "X轴立体仰角: ${props.rotateX3D.toInt()}°", fontSize = 11.sp, color = KawaiiTextSecondary)
                    Slider(
                        value = props.rotateX3D,
                        onValueChange = { rx -> onUpdateProps { it.copy(rotateX3D = rx) } },
                        valueRange = -40f..40f,
                        colors = SliderDefaults.colors(thumbColor = KawaiiSkyBlue, activeTrackColor = KawaiiSkyBlue)
                    )
                    Text(text = "Y轴立体侧角: ${props.rotateY3D.toInt()}°", fontSize = 11.sp, color = KawaiiTextSecondary)
                    Slider(
                        value = props.rotateY3D,
                        onValueChange = { ry -> onUpdateProps { it.copy(rotateY3D = ry) } },
                        valueRange = -40f..40f,
                        colors = SliderDefaults.colors(thumbColor = KawaiiSkyBlue, activeTrackColor = KawaiiSkyBlue)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        KawaiiButton(
                            text = "↺ 归零俯仰角",
                            onClick = { onUpdateProps { it.copy(rotateX3D = 0f, rotateY3D = 0f) } },
                            containerColor = KawaiiSurface,
                            contentColor = KawaiiTextPrimary,
                            borderWidth = 1.5.dp
                        )
                    }
                }
            }
        }

        // ==========================================================
        // 🔤 文字大小 (SIZE)
        // ==========================================================
        TextSubTool.SIZE -> {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "字号: ${props.fontSize.toInt()} SP",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black,
                        color = KawaiiTextPrimary
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        KawaiiButton(text = "-10", onClick = { onStepFontSize(-10f) }, containerColor = KawaiiPinkLight, contentColor = KawaiiTextPrimary)
                        KawaiiButton(text = "-2", onClick = { onStepFontSize(-2f) }, containerColor = KawaiiPinkLight, contentColor = KawaiiTextPrimary)
                        KawaiiButton(text = "+2", onClick = { onStepFontSize(2f) }, containerColor = KawaiiYellow, contentColor = KawaiiTextPrimary)
                        KawaiiButton(text = "+10", onClick = { onStepFontSize(10f) }, containerColor = KawaiiYellow, contentColor = KawaiiTextPrimary)
                    }
                }

                Slider(
                    value = props.fontSize,
                    onValueChange = { onSetFontSize(it) },
                    valueRange = 12f..260f,
                    colors = SliderDefaults.colors(thumbColor = KawaiiPink, activeTrackColor = KawaiiPink)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(
                        "小号 24" to 24f,
                        "标准 36" to 36f,
                        "醒目 54" to 54f,
                        "特大 80" to 80f,
                        "海报 120" to 120f,
                        "巨幕 180" to 180f
                    ).forEach { (lbl, sizeVal) ->
                        KawaiiChip(
                            text = lbl,
                            isSelected = props.fontSize.toInt() == sizeVal.toInt(),
                            onClick = { onSetFontSize(sizeVal) },
                            selectedColor = KawaiiYellow
                        )
                    }
                }
            }
        }

        // ==========================================================
        // ⤢ 图层缩放 (SCALE)
        // ==========================================================
        TextSubTool.SCALE -> {
            val scalePct = (layer.scaleX * 100).toInt()
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "缩放比例: ${scalePct}%",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black,
                        color = KawaiiTextPrimary
                    )
                    KawaiiButton(
                        text = "↺ 恢复100%",
                        onClick = onResetTransform,
                        containerColor = KawaiiMint,
                        contentColor = KawaiiTextWhite
                    )
                }

                Slider(
                    value = layer.scaleX,
                    onValueChange = { onSetExactScale(it) },
                    valueRange = 0.3f..4.0f,
                    colors = SliderDefaults.colors(thumbColor = KawaiiMint, activeTrackColor = KawaiiMint)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(
                        "50%" to 0.5f,
                        "75%" to 0.75f,
                        "100%" to 1.0f,
                        "125%" to 1.25f,
                        "150%" to 1.5f,
                        "200%" to 2.0f,
                        "300%" to 3.0f
                    ).forEach { (lbl, factor) ->
                        KawaiiChip(
                            text = lbl,
                            isSelected = Math.abs(layer.scaleX - factor) < 0.05f,
                            onClick = { onSetExactScale(factor) },
                            selectedColor = KawaiiMint
                        )
                    }
                }
            }
        }

        // ==========================================================
        // 🎨 可爱调色 (COLOR)
        // ==========================================================
        TextSubTool.COLOR -> {
            val cuteColors = listOf(
                Color(0xFFFF5C8D), Color(0xFFFF85B3), Color(0xFFFFC72C), Color(0xFF2ED1A2),
                Color(0xFF9D65FF), Color(0xFF38B6FF), Color(0xFFFF7A59), Color(0xFFFFFFFF),
                Color(0xFF2F1E33), Color(0xFFFFE3EC), Color(0xFFFFF4D1), Color(0xFFD6F9EE)
            )

            Column(modifier = Modifier.fillMaxWidth()) {
                Text(text = "🌸 可爱糖果色板：", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = KawaiiTextPrimary)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    cuteColors.forEach { c ->
                        val isSelected = props.colorArgb == c.toArgb()
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .kawaiiShadow(shadowOffset = if (isSelected) 3.dp else 1.dp, cornerRadius = 18.dp)
                                .jellyClickable { onUpdateProps { it.copy(colorArgb = c.toArgb()) } }
                                .clip(CircleShape)
                                .background(c)
                                .border(
                                    if (isSelected) 3.dp else 2.dp,
                                    if (isSelected) KawaiiOutline else KawaiiOutline.copy(alpha = 0.5f),
                                    CircleShape
                                )
                        )
                    }
                }
            }
        }

        // ==========================================================
        // 𝐁 字体字形 (STYLE)
        // ==========================================================
        TextSubTool.STYLE -> {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                KawaiiChip(
                    text = "加粗 𝐁",
                    isSelected = props.isBold,
                    onClick = { onUpdateProps { it.copy(isBold = !it.isBold) } },
                    selectedColor = KawaiiPink
                )
                KawaiiChip(
                    text = "倾斜 𝐼",
                    isSelected = props.isItalic,
                    onClick = { onUpdateProps { it.copy(isItalic = !it.isItalic) } },
                    selectedColor = KawaiiYellow
                )
                KawaiiChip(
                    text = "下划线 ̲",
                    isSelected = props.isUnderline,
                    onClick = { onUpdateProps { it.copy(isUnderline = !it.isUnderline) } },
                    selectedColor = KawaiiMint
                )
            }
        }

        // ==========================================================
        // ≡ 对齐排版 (ALIGN)
        // ==========================================================
        TextSubTool.ALIGN -> {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                KawaiiChip(
                    text = "⫷ 靠左",
                    isSelected = props.alignment == TextAlignment.LEFT,
                    onClick = { onUpdateProps { it.copy(alignment = TextAlignment.LEFT) } },
                    selectedColor = KawaiiPinkLight
                )
                KawaiiChip(
                    text = "═ 居中",
                    isSelected = props.alignment == TextAlignment.CENTER,
                    onClick = { onUpdateProps { it.copy(alignment = TextAlignment.CENTER) } },
                    selectedColor = KawaiiYellow
                )
                KawaiiChip(
                    text = "靠右 ⫸",
                    isSelected = props.alignment == TextAlignment.RIGHT,
                    onClick = { onUpdateProps { it.copy(alignment = TextAlignment.RIGHT) } },
                    selectedColor = KawaiiPinkLight
                )
            }
        }

        // ==========================================================
        // ↔️ 字词间距 (SPACING)
        // ==========================================================
        TextSubTool.SPACING -> {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(text = "字间距: ${props.letterSpacing.toInt()} px", fontSize = 12.sp, color = KawaiiTextPrimary, fontWeight = FontWeight.Bold)
                Slider(
                    value = props.letterSpacing,
                    onValueChange = { newSpacing -> onUpdateProps { it.copy(letterSpacing = newSpacing) } },
                    valueRange = -2f..15f,
                    colors = SliderDefaults.colors(thumbColor = KawaiiYellow, activeTrackColor = KawaiiYellow)
                )
                Text(text = "行间距: ${String.format("%.2f", props.lineSpacing)} 倍", fontSize = 12.sp, color = KawaiiTextPrimary, fontWeight = FontWeight.Bold)
                Slider(
                    value = props.lineSpacing,
                    onValueChange = { newLineSpacing -> onUpdateProps { it.copy(lineSpacing = newLineSpacing) } },
                    valueRange = 0.8f..2.5f,
                    colors = SliderDefaults.colors(thumbColor = KawaiiYellow, activeTrackColor = KawaiiYellow)
                )
            }
        }

        // ==========================================================
        // ✒️ 外边描边 (STROKE)
        // ==========================================================
        TextSubTool.STROKE -> {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "外边描边效果", fontSize = 13.sp, fontWeight = FontWeight.Black, color = KawaiiTextPrimary)
                    Switch(
                        checked = props.hasStroke,
                        onCheckedChange = { checked -> onUpdateProps { it.copy(hasStroke = checked) } },
                        colors = SwitchDefaults.colors(checkedThumbColor = KawaiiPink, checkedTrackColor = KawaiiPinkLight)
                    )
                }
                if (props.hasStroke) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "描边粗细: ${props.strokeWidth.toInt()} px", fontSize = 11.sp, color = KawaiiTextSecondary)
                    Slider(
                        value = props.strokeWidth,
                        onValueChange = { widthVal -> onUpdateProps { it.copy(strokeWidth = widthVal) } },
                        valueRange = 1f..25f,
                        colors = SliderDefaults.colors(thumbColor = KawaiiPink, activeTrackColor = KawaiiPink)
                    )
                }
            }
        }

        // ==========================================================
        // 🌫️ 投影阴影 (SHADOW)
        // ==========================================================
        TextSubTool.SHADOW -> {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "文字阴影效果", fontSize = 13.sp, fontWeight = FontWeight.Black, color = KawaiiTextPrimary)
                    Switch(
                        checked = props.hasShadow,
                        onCheckedChange = { checked -> onUpdateProps { it.copy(hasShadow = checked) } },
                        colors = SwitchDefaults.colors(checkedThumbColor = KawaiiYellow, checkedTrackColor = KawaiiYellowLight)
                    )
                }
                if (props.hasShadow) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "阴影模糊: ${props.shadowRadius.toInt()} px", fontSize = 11.sp, color = KawaiiTextSecondary)
                    Slider(
                        value = props.shadowRadius,
                        onValueChange = { radiusVal -> onUpdateProps { it.copy(shadowRadius = radiusVal) } },
                        valueRange = 0f..40f,
                        colors = SliderDefaults.colors(thumbColor = KawaiiYellow, activeTrackColor = KawaiiYellow)
                    )
                }
            }
        }

        // ==========================================================
        // 🔲 文字底框 (BACKGROUND_BOX)
        // ==========================================================
        TextSubTool.BACKGROUND_BOX -> {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "文字独立背景底框", fontSize = 13.sp, fontWeight = FontWeight.Black, color = KawaiiTextPrimary)
                    Switch(
                        checked = props.hasBackgroundBox,
                        onCheckedChange = { checked -> onUpdateProps { it.copy(hasBackgroundBox = checked) } },
                        colors = SwitchDefaults.colors(checkedThumbColor = KawaiiMint, checkedTrackColor = KawaiiMintLight)
                    )
                }
                if (props.hasBackgroundBox) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "底框圆角: ${props.backgroundBoxRadius.toInt()} px", fontSize = 11.sp, color = KawaiiTextSecondary)
                    Slider(
                        value = props.backgroundBoxRadius,
                        onValueChange = { radiusVal -> onUpdateProps { it.copy(backgroundBoxRadius = radiusVal) } },
                        valueRange = 0f..35f,
                        colors = SliderDefaults.colors(thumbColor = KawaiiMint, activeTrackColor = KawaiiMint)
                    )
                }
            }
        }

        // ==========================================================
        // 💧 不透明度 (OPACITY)
        // ==========================================================
        TextSubTool.OPACITY -> {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(text = "不透明度: ${(opacity * 100).toInt()}%", fontSize = 12.sp, color = KawaiiTextPrimary, fontWeight = FontWeight.Bold)
                Slider(
                    value = opacity,
                    onValueChange = { onUpdateOpacity(it) },
                    valueRange = 0.05f..1f,
                    colors = SliderDefaults.colors(thumbColor = KawaiiPink, activeTrackColor = KawaiiPink)
                )
            }
        }

        // ==========================================================
        // ✛ 位置微调 (POSITION)
        // ==========================================================
        TextSubTool.POSITION -> {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(text = "精确微调移动 (每次 5px)：", fontSize = 12.sp, color = KawaiiTextPrimary, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    KawaiiButton(text = "⬅ 左移", onClick = { onMoveDelta(-5f, 0f) }, containerColor = KawaiiPinkLight, contentColor = KawaiiTextPrimary)
                    KawaiiButton(text = "右移 ➡", onClick = { onMoveDelta(5f, 0f) }, containerColor = KawaiiPinkLight, contentColor = KawaiiTextPrimary)
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    KawaiiButton(text = "⬆ 上移", onClick = { onMoveDelta(0f, -5f) }, containerColor = KawaiiYellowLight, contentColor = KawaiiTextPrimary)
                    KawaiiButton(text = "下移 ⬇", onClick = { onMoveDelta(0f, 5f) }, containerColor = KawaiiYellowLight, contentColor = KawaiiTextPrimary)
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 需求9：一键对齐
                Text(text = "🎯 快速对齐：", fontSize = 12.sp, color = KawaiiTextPrimary, fontWeight = FontWeight.Black)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    KawaiiButton(text = "⏺ 水平居中", onClick = { onMoveDelta(-layer.x, 0f) }, containerColor = KawaiiLavender, contentColor = KawaiiTextWhite)
                    KawaiiButton(text = "⏺ 垂直居中", onClick = { onMoveDelta(0f, -layer.y) }, containerColor = KawaiiLavender, contentColor = KawaiiTextWhite)
                    KawaiiButton(text = "🔼 顶部对齐", onClick = { onMoveDelta(0f, -layer.y - 0f) }, containerColor = KawaiiSkyBlue, contentColor = KawaiiTextWhite)
                    KawaiiButton(text = "🔽 底部对齐", onClick = { onMoveDelta(0f, -layer.y) }, containerColor = KawaiiSkyBlue, contentColor = KawaiiTextWhite)
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    KawaiiButton(text = "◀ 左对齐", onClick = { onMoveDelta(-layer.x, 0f) }, containerColor = KawaiiMint, contentColor = KawaiiTextWhite)
                    KawaiiButton(text = "▶ 右对齐", onClick = { onMoveDelta(-layer.x, 0f) }, containerColor = KawaiiMint, contentColor = KawaiiTextWhite)
                    KawaiiButton(text = "↺ 回到中心原点", onClick = { onMoveDelta(-layer.x, -layer.y) }, containerColor = KawaiiYellow, contentColor = KawaiiTextPrimary)
                }
            }
        }

        else -> {}
    }
}
