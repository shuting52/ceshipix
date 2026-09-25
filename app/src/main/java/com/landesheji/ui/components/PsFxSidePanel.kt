package com.landesheji.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.landesheji.data.model.TextProperties
import com.landesheji.ui.theme.KawaiiCardTint
import com.landesheji.ui.theme.KawaiiLavender
import com.landesheji.ui.theme.KawaiiMint
import com.landesheji.ui.theme.KawaiiOutline
import com.landesheji.ui.theme.KawaiiPink
import com.landesheji.ui.theme.KawaiiPinkLight
import com.landesheji.ui.theme.KawaiiSkyBlue
import com.landesheji.ui.theme.KawaiiSurface
import com.landesheji.ui.theme.KawaiiTextPrimary
import com.landesheji.ui.theme.KawaiiTextSecondary
import com.landesheji.ui.theme.KawaiiTextWhite
import com.landesheji.ui.theme.KawaiiYellow

/**
 * 需求3：PS FX 全面化侧边面板
 *
 * 以"侧边栏"而非全屏形式呈现，右侧悬浮面板 + 顶部实时预览，
 * 可一边调节一边看效果。包含与电脑 PS 图层样式相仿的 FX：
 * 3D 立体、斜面浮雕、光影特效、材质质感、高级样式（描边/纹理/渐变/投影/发光等）。
 */
@Composable
fun PsFxSidePanel(
    props: TextProperties,
    opacity: Float,
    onUpdateProps: ((TextProperties) -> TextProperties) -> Unit,
    onClose: () -> Unit,
    onUpdateOpacity: (Float) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var activeFx by remember { mutableStateOf("3D立体") }
    val fxTabs = listOf(
        "3D立体" to "🧊",
        "斜面浮雕" to "✨",
        "光影特效" to "💡",
        "材质质感" to "🎨",
        "高级样式" to "🪄"
    )

    Column(
        modifier = modifier
            .fillMaxHeight()
            .width(232.dp)
            .background(KawaiiSurface)
            .border(2.dp, KawaiiOutline, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .padding(horizontal = 10.dp, vertical = 8.dp)
    ) {
        // 面板头
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = "🪄 PS FX 特效", fontSize = 13.sp, fontWeight = FontWeight.Black, color = KawaiiTextPrimary)
                Text(text = "实时预览·边调边看", fontSize = 9.sp, color = KawaiiTextSecondary)
            }
            IconButton(onClick = onClose, modifier = Modifier.size(26.dp)) {
                Icon(Icons.Default.Close, contentDescription = "关闭 FX 面板", tint = KawaiiPink, modifier = Modifier.size(16.dp))
            }
        }

        // 顶部实时预览
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(KawaiiCardTint)
                .border(1.5.dp, KawaiiPinkLight, RoundedCornerShape(12.dp))
        ) {
            Canvas(modifier = Modifier.fillMaxWidth().height(72.dp)) {
                CanvasRenderer.drawTextLayerPreview(
                    drawScope = this,
                    props = props,
                    opacity = opacity,
                    previewScale = 0.30f
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 分类页签
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            fxTabs.forEach { (tab, icon) ->
                val selected = activeFx == tab
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (selected) KawaiiPink else KawaiiCardTint)
                        .border(1.dp, if (selected) KawaiiPink else KawaiiOutline.copy(alpha = 0.2f), RoundedCornerShape(10.dp))
                        .jellyClickable { activeFx = tab }
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = "$icon $tab",
                        fontSize = 11.sp,
                        fontWeight = if (selected) FontWeight.Black else FontWeight.Bold,
                        color = if (selected) KawaiiTextWhite else KawaiiTextPrimary
                    )
                }
            }
        }

        HorizontalDivider(color = KawaiiOutline.copy(alpha = 0.2f), modifier = Modifier.padding(vertical = 8.dp))

        // FX 内容（可滚动）
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            when (activeFx) {
                "3D立体" -> Fx3DSection(props, onUpdateProps)
                "斜面浮雕" -> FxBevelSection(props, onUpdateProps)
                "光影特效" -> FxLightingSection(props, onUpdateProps)
                "材质质感" -> FxMaterialSection(props, onUpdateProps)
                "高级样式" -> FxAdvancedSection(props, onUpdateProps, opacity, onUpdateOpacity)
            }
        }
    }
}

// ======================== 3D 立体 ========================
@Composable
private fun Fx3DSection(
    props: TextProperties,
    onUpdateProps: ((TextProperties) -> TextProperties) -> Unit
) {
    FxSwitchRow("启用 3D 立体挤出", checked = props.has3D) { on ->
        onUpdateProps { it.copy(has3D = on) }
    }
    if (props.has3D) {
        FxSlider("挤出深度", "${props.depth3D.toInt()} px", props.depth3D, 2f..70f) { v -> onUpdateProps { it.copy(depth3D = v) } }
        FxSlider("投射角度", "${props.angle3D.toInt()}°", props.angle3D, 0f..360f) { v -> onUpdateProps { it.copy(angle3D = v) } }

        // 方向快捷
        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            listOf("↗45°" to 45f, "↘315°" to 315f, "↙225°" to 225f, "↖135°" to 135f, "↑90°" to 90f, "↓270°" to 270f)
                .forEach { (lbl, deg) ->
                    KawaiiChipFx(
                        text = lbl,
                        isSelected = props.angle3D.toInt() == deg.toInt(),
                        onClick = { onUpdateProps { it.copy(angle3D = deg) } }
                    )
                }
        }

        FxSwitchRow("透视灭点收缩", checked = props.isPerspective3D) { on ->
            onUpdateProps { it.copy(isPerspective3D = on) }
        }
        if (props.isPerspective3D) {
            FxSlider("透视收缩程度", "%.2f".format(props.perspectiveAmount3D), props.perspectiveAmount3D, 0.1f..1f) { v ->
                onUpdateProps { it.copy(perspectiveAmount3D = v) }
            }
        }
        FxSlider("侧面明暗衰减", "%.2f".format(props.darken3D), props.darken3D, 0.1f..0.9f) { v -> onUpdateProps { it.copy(darken3D = v) } }

        FxSwitchRow("自定义侧面颜色", checked = props.customExtrusionColor) { on ->
            onUpdateProps { it.copy(customExtrusionColor = on) }
        }
        if (props.customExtrusionColor) {
            FxColorPickRow(
                label = "侧面颜色",
                current = props.extrusionColorArgb
            ) { c -> onUpdateProps { it.copy(extrusionColorArgb = c) } }
        }

        // 空间倾斜
        FxSlider("X 轴立体仰角", "${props.rotateX3D.toInt()}°", props.rotateX3D, -40f..40f) { v -> onUpdateProps { it.copy(rotateX3D = v) } }
        FxSlider("Y 轴立体侧角", "${props.rotateY3D.toInt()}°", props.rotateY3D, -40f..40f) { v -> onUpdateProps { it.copy(rotateY3D = v) } }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            KawaiiButtonFx("↺ 归零俯仰角", onClick = { onUpdateProps { it.copy(rotateX3D = 0f, rotateY3D = 0f) } })
        }

        // 一键预设
        Spacer(modifier = Modifier.height(6.dp))
        Text("⚡ 一键 3D 预设：", fontWeight = FontWeight.Black, fontSize = 12.sp, color = KawaiiTextPrimary)
        Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            KawaiiButtonFx("🎈 气球", onClick = { onUpdateProps { it.copy(has3D = true, depth3D = 22f, angle3D = 45f, darken3D = 0.35f, material3D = "充气气球", hasBevel = true, bevelWidth = 5f, hasGroundShadow = true) } })
            KawaiiButtonFx("🥇 金辉", onClick = { onUpdateProps { it.copy(has3D = true, depth3D = 24f, angle3D = 45f, darken3D = 0.55f, colorArgb = 0xFFFFD700.toInt(), customExtrusionColor = true, extrusionColorArgb = 0xFF996515.toInt(), hasBevel = true, bevelWidth = 5f) } })
            KawaiiButtonFx("⚡ 霓虹", onClick = { onUpdateProps { it.copy(has3D = true, depth3D = 20f, angle3D = 225f, darken3D = 0.3f, material3D = "赛博霓虹", hasBevel = true, bevelWidth = 3f) } })
            KawaiiButtonFx("❄️ 水晶", onClick = { onUpdateProps { it.copy(has3D = true, depth3D = 30f, angle3D = 45f, darken3D = 0.5f, material3D = "水晶冰凌", hasBevel = true, bevelWidth = 6f) } })
        }
    }
}

// ======================== 斜面浮雕 ========================
@Composable
private fun FxBevelSection(
    props: TextProperties,
    onUpdateProps: ((TextProperties) -> TextProperties) -> Unit
) {
    FxSwitchRow("斜面与浮雕 (Bevel)", checked = props.hasBevel) { on ->
        onUpdateProps { it.copy(hasBevel = on) }
    }
    if (props.hasBevel) {
        FxSlider("倒角宽度", "${props.bevelWidth.toInt()} px", props.bevelWidth, 1f..16f) { v -> onUpdateProps { it.copy(bevelWidth = v) } }
        FxSlider("光照入射角", "${props.bevelAngle.toInt()}°", props.bevelAngle, 0f..360f) { v -> onUpdateProps { it.copy(bevelAngle = v) } }
        FxColorPickRow("高光颜色", props.bevelHighlightColorArgb) { c -> onUpdateProps { it.copy(bevelHighlightColorArgb = c) } }
        FxColorPickRow("阴影颜色", props.bevelShadowColorArgb) { c -> onUpdateProps { it.copy(bevelShadowColorArgb = c) } }
    }

    HorizontalDivider(color = KawaiiOutline.copy(alpha = 0.15f), modifier = Modifier.padding(vertical = 8.dp))
    FxSwitchRow("浮雕 (Emboss)", checked = props.hasEmboss) { on ->
        onUpdateProps { it.copy(hasEmboss = on) }
    }
    if (props.hasEmboss) {
        FxSlider("浮雕强度", "%.2f".format(props.embossIntensity), props.embossIntensity, 0.1f..1.5f) { v -> onUpdateProps { it.copy(embossIntensity = v) } }
    }
}

// ======================== 光影特效 ========================
@Composable
private fun FxLightingSection(
    props: TextProperties,
    onUpdateProps: ((TextProperties) -> TextProperties) -> Unit
) {
    // 外发光（新增 FX）
    FxSwitchRow("💫 外发光 (Glow)", checked = props.hasGlow) { on ->
        onUpdateProps { it.copy(hasGlow = on) }
    }
    if (props.hasGlow) {
        FxSlider("发光半径", "${props.glowRadius.toInt()} px", props.glowRadius, 4f..60f) { v -> onUpdateProps { it.copy(glowRadius = v) } }
        FxSlider("发光强度", "%.0f%%".format(props.glowOpacity * 100), props.glowOpacity, 0.1f..1f) { v -> onUpdateProps { it.copy(glowOpacity = v) } }
        FxColorPickRow("发光颜色", props.glowColorArgb) { c -> onUpdateProps { it.copy(glowColorArgb = c) } }
    }

    HorizontalDivider(color = KawaiiOutline.copy(alpha = 0.15f), modifier = Modifier.padding(vertical = 8.dp))

    // 投影
    FxSwitchRow("投影阴影", checked = props.hasShadow) { on ->
        onUpdateProps { it.copy(hasShadow = on) }
    }
    if (props.hasShadow) {
        FxSlider("阴影半径", "${props.shadowRadius.toInt()} px", props.shadowRadius, 2f..40f) { v -> onUpdateProps { it.copy(shadowRadius = v) } }
        // v2.2：阴影扩展
        FxSlider("阴影扩展", "${props.shadowSpread.toInt()}", props.shadowSpread, 0f..100f) { v -> onUpdateProps { it.copy(shadowSpread = v) } }
        FxSlider("阴影 X 偏移", "${props.shadowDx.toInt()} px", props.shadowDx, -20f..20f) { v -> onUpdateProps { it.copy(shadowDx = v) } }
        FxSlider("阴影 Y 偏移", "${props.shadowDy.toInt()} px", props.shadowDy, -20f..20f) { v -> onUpdateProps { it.copy(shadowDy = v) } }
        FxColorPickRow("阴影颜色", props.shadowColorArgb) { c -> onUpdateProps { it.copy(shadowColorArgb = c) } }
    }

    HorizontalDivider(color = KawaiiOutline.copy(alpha = 0.15f), modifier = Modifier.padding(vertical = 8.dp))

    // 光源与地面投影
    FxSlider("主光源角度", "${props.lightAngle3D.toInt()}°", props.lightAngle3D, 0f..360f) { v -> onUpdateProps { it.copy(lightAngle3D = v) } }
    FxSlider("光源强度", "%.2f".format(props.lightIntensity3D), props.lightIntensity3D, 0.2f..2f) { v -> onUpdateProps { it.copy(lightIntensity3D = v) } }

    FxSwitchRow("地面透视投影", checked = props.hasGroundShadow) { on ->
        onUpdateProps { it.copy(hasGroundShadow = on) }
    }
    if (props.hasGroundShadow) {
        FxSlider("投影模糊", "${props.groundShadowBlur.toInt()} px", props.groundShadowBlur, 2f..40f) { v -> onUpdateProps { it.copy(groundShadowBlur = v) } }
        FxSlider("投影距离", "${props.groundShadowDistance.toInt()} px", props.groundShadowDistance, 4f..80f) { v -> onUpdateProps { it.copy(groundShadowDistance = v) } }
        FxSlider("投影不透明度", "%.0f%%".format(props.groundShadowOpacity * 100), props.groundShadowOpacity, 0.05f..0.9f) { v -> onUpdateProps { it.copy(groundShadowOpacity = v) } }
    }
}

// ======================== 材质质感 ========================
@Composable
private fun FxMaterialSection(
    props: TextProperties,
    onUpdateProps: ((TextProperties) -> TextProperties) -> Unit
) {
    Text("🎨 表面材质：", fontWeight = FontWeight.Black, fontSize = 12.sp, color = KawaiiTextPrimary)
    Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        listOf(
            "默认" to "常规",
            "高光光泽" to "高光",
            "可爱果冻" to "果冻",
            "充气气球" to "气球",
            "金属质感" to "金属",
            "水晶冰凌" to "水晶",
            "糖果彩虹" to "彩虹",
            "火焰光焰" to "火焰",
            "糖果霓虹" to "霓虹",
            "玻璃琉璃" to "玻璃",
            "卡通厚涂" to "卡通",
            "复古街机" to "街机",
            "赛博霓虹" to "赛博"
        ).forEach { (key, label) ->
            KawaiiChipFx(text = label, isSelected = props.material3D == key, onClick = { onUpdateProps { it.copy(material3D = key) } })
        }
    }

    HorizontalDivider(color = KawaiiOutline.copy(alpha = 0.15f), modifier = Modifier.padding(vertical = 8.dp))
    FxSwitchRow("倒影 (Reflection)", checked = props.hasReflection) { on ->
        onUpdateProps { it.copy(hasReflection = on) }
    }
    if (props.hasReflection) {
        FxSlider("倒影距离", "${props.reflectionDistance.toInt()} px", props.reflectionDistance, 0f..40f) { v -> onUpdateProps { it.copy(reflectionDistance = v) } }
        FxSlider("倒影透明度", "%.0f%%".format(props.reflectionAlpha * 100), props.reflectionAlpha, 0.05f..0.9f) { v -> onUpdateProps { it.copy(reflectionAlpha = v) } }
    }
}

// ======================== 高级样式 ========================
@Composable
private fun FxAdvancedSection(
    props: TextProperties,
    onUpdateProps: ((TextProperties) -> TextProperties) -> Unit,
    opacity: Float = 1f,
    onUpdateOpacity: (Float) -> Unit = {}
) {
    // 需求6：文字透明度
    FxSlider("文字透明度", "%.0f%%".format(opacity * 100), opacity, 0.1f..1f) { v -> onUpdateOpacity(v) }

    HorizontalDivider(color = KawaiiOutline.copy(alpha = 0.15f), modifier = Modifier.padding(vertical = 6.dp))

    // 需求6：排版增强 —— 字间距 / 行距 / 曲线弧度 / 高亮三种效果开关
    FxSlider("字间距", "%.1f".format(props.letterSpacing), props.letterSpacing, -10f..20f) { v -> onUpdateProps { it.copy(letterSpacing = v) } }
    FxSlider("行距", "%.2f".format(props.lineSpacing), props.lineSpacing, 0.6f..2.5f) { v -> onUpdateProps { it.copy(lineSpacing = v) } }
    FxSlider("曲线弧度", "%.0f°".format(props.curveAngle), props.curveAngle, -100f..100f) { v -> onUpdateProps { it.copy(curveAngle = v) } }

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        KawaiiChipFx(text = "加粗", isSelected = props.isBold, onClick = { onUpdateProps { it.copy(isBold = it.isBold.not()) } })
        KawaiiChipFx(text = "斜体", isSelected = props.isItalic, onClick = { onUpdateProps { it.copy(isItalic = it.isItalic.not()) } })
        KawaiiChipFx(text = "下划线", isSelected = props.isUnderline, onClick = { onUpdateProps { it.copy(isUnderline = it.isUnderline.not()) } })
    }

    HorizontalDivider(color = KawaiiOutline.copy(alpha = 0.15f), modifier = Modifier.padding(vertical = 6.dp))

    // 渐变叠加
    FxSwitchRow("🌈 渐变叠加", checked = props.hasGradientOverlay) { on ->
        onUpdateProps { it.copy(hasGradientOverlay = on) }
    }
    if (props.hasGradientOverlay) {
        FxColorPickRow("渐变副色", props.gradientOverlayColor2Argb) { c -> onUpdateProps { it.copy(gradientOverlayColor2Argb = c) } }
        FxSlider("渐变角度", "${props.gradientOverlayAngle.toInt()}°", props.gradientOverlayAngle, 0f..360f) { v -> onUpdateProps { it.copy(gradientOverlayAngle = v) } }
    }

    HorizontalDivider(color = KawaiiOutline.copy(alpha = 0.15f), modifier = Modifier.padding(vertical = 8.dp))

    // 纹理填充
    FxSwitchRow("🔲 纹理填充", checked = props.hasTextureFill) { on ->
        onUpdateProps { it.copy(hasTextureFill = on) }
    }
    if (props.hasTextureFill) {
        Text("纹理样式：", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = KawaiiTextPrimary)
        Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf(0 to "斜纹", 1 to "点阵", 2 to "棋盘", 3 to "波浪").forEach { (idx, label) ->
                KawaiiChipFx(text = label, isSelected = props.textureIndex % 4 == idx, onClick = { onUpdateProps { it.copy(textureIndex = idx) } })
            }
        }
        FxColorPickRow("纹理颜色", props.textureColorArgb) { c -> onUpdateProps { it.copy(textureColorArgb = c) } }
    }

    HorizontalDivider(color = KawaiiOutline.copy(alpha = 0.15f), modifier = Modifier.padding(vertical = 8.dp))

    // 描边
    FxSwitchRow("✒️ 外边描边", checked = props.hasStroke) { on ->
        onUpdateProps { it.copy(hasStroke = on) }
    }
    if (props.hasStroke) {
        FxSlider("描边粗细", "${props.strokeWidth.toInt()} px", props.strokeWidth, 1f..24f) { v -> onUpdateProps { it.copy(strokeWidth = v) } }
        FxColorPickRow("描边颜色", props.strokeColorArgb) { c -> onUpdateProps { it.copy(strokeColorArgb = c) } }
        // v2.2：描边位置（内/居中/外）
        Text("描边位置：", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = KawaiiTextSecondary)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf(
                com.landesheji.data.model.StrokePosition.INNER to "内描边",
                com.landesheji.data.model.StrokePosition.CENTER to "居中",
                com.landesheji.data.model.StrokePosition.OUTER to "外描边"
            ).forEach { (pos, label) ->
                KawaiiChipFx(
                    text = label,
                    isSelected = props.strokePosition == pos,
                    onClick = { onUpdateProps { it.copy(strokePosition = pos) } }
                )
            }
        }
    }

    HorizontalDivider(color = KawaiiOutline.copy(alpha = 0.15f), modifier = Modifier.padding(vertical = 8.dp))

    // v2.2：内阴影
    FxSwitchRow("🌑 内阴影", checked = props.hasInnerShadow) { on ->
        onUpdateProps { it.copy(hasInnerShadow = on) }
    }
    if (props.hasInnerShadow) {
        FxSlider("模糊半径", "${props.innerShadowRadius.toInt()} px", props.innerShadowRadius, 1f..30f) { v -> onUpdateProps { it.copy(innerShadowRadius = v) } }
        FxSlider("X 偏移", "${props.innerShadowDx.toInt()} px", props.innerShadowDx, -20f..20f) { v -> onUpdateProps { it.copy(innerShadowDx = v) } }
        FxSlider("Y 偏移", "${props.innerShadowDy.toInt()} px", props.innerShadowDy, -20f..20f) { v -> onUpdateProps { it.copy(innerShadowDy = v) } }
        FxSlider("不透明度", "%.0f%%".format(props.innerShadowOpacity * 100), props.innerShadowOpacity, 0.05f..1f) { v -> onUpdateProps { it.copy(innerShadowOpacity = v) } }
        FxColorPickRow("阴影颜色", props.innerShadowColorArgb) { c -> onUpdateProps { it.copy(innerShadowColorArgb = c) } }
    }

    HorizontalDivider(color = KawaiiOutline.copy(alpha = 0.15f), modifier = Modifier.padding(vertical = 8.dp))

    // v2.2：内发光
    FxSwitchRow("🔥 内发光", checked = props.hasInnerGlow) { on ->
        onUpdateProps { it.copy(hasInnerGlow = on) }
    }
    if (props.hasInnerGlow) {
        FxSlider("发光半径", "${props.innerGlowRadius.toInt()} px", props.innerGlowRadius, 1f..40f) { v -> onUpdateProps { it.copy(innerGlowRadius = v) } }
        FxSlider("不透明度", "%.0f%%".format(props.innerGlowOpacity * 100), props.innerGlowOpacity, 0.05f..1f) { v -> onUpdateProps { it.copy(innerGlowOpacity = v) } }
        FxColorPickRow("发光颜色", props.innerGlowColorArgb) { c -> onUpdateProps { it.copy(innerGlowColorArgb = c) } }
    }

    HorizontalDivider(color = KawaiiOutline.copy(alpha = 0.15f), modifier = Modifier.padding(vertical = 8.dp))

    // v2.2：颜色叠加
    FxSwitchRow("🎨 颜色叠加", checked = props.hasColorOverlay) { on ->
        onUpdateProps { it.copy(hasColorOverlay = on) }
    }
    if (props.hasColorOverlay) {
        FxSlider("不透明度", "%.0f%%".format(props.colorOverlayOpacity * 100), props.colorOverlayOpacity, 0.05f..1f) { v -> onUpdateProps { it.copy(colorOverlayOpacity = v) } }
        FxColorPickRow("叠加颜色", props.colorOverlayColorArgb) { c -> onUpdateProps { it.copy(colorOverlayColorArgb = c) } }
    }

    HorizontalDivider(color = KawaiiOutline.copy(alpha = 0.15f), modifier = Modifier.padding(vertical = 8.dp))

    // v2.2：FX 预设
    Text("⚡ FX 预设：", fontSize = 12.sp, fontWeight = FontWeight.Black, color = KawaiiTextPrimary)
    Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        KawaiiChipFx(text = "⚫ 黑描边", isSelected = false, onClick = {
            onUpdateProps { it.copy(hasStroke = true, strokeColorArgb = 0xFF000000.toInt(), strokeWidth = 6f, strokePosition = com.landesheji.data.model.StrokePosition.OUTER, hasShadow = false, hasGlow = false, hasColorOverlay = false, hasInnerShadow = false, hasInnerGlow = false) }
        })
        KawaiiChipFx(text = "⚪ 白描边", isSelected = false, onClick = {
            onUpdateProps { it.copy(hasStroke = true, strokeColorArgb = 0xFFFFFFFF.toInt(), strokeWidth = 6f, strokePosition = com.landesheji.data.model.StrokePosition.OUTER, hasShadow = false, hasGlow = false, hasColorOverlay = false, hasInnerShadow = false, hasInnerGlow = false) }
        })
        KawaiiChipFx(text = "💫 霓虹发光", isSelected = false, onClick = {
            onUpdateProps { it.copy(hasGlow = true, glowColorArgb = 0xFF00D4FF.toInt(), glowRadius = 22f, glowOpacity = 0.9f, hasStroke = true, strokeColorArgb = 0xFFFFFFFF.toInt(), strokeWidth = 2f, hasShadow = true, shadowColorArgb = 0x8800D4FF.toInt(), shadowRadius = 10f, hasColorOverlay = false, hasInnerShadow = false, hasInnerGlow = false) }
        })
        KawaiiChipFx(text = "🥇 金属文字", isSelected = false, onClick = {
            onUpdateProps { it.copy(hasColorOverlay = false, hasGradientOverlay = true, gradientOverlayColor2Argb = 0xFFFFD700.toInt(), hasStroke = true, strokeColorArgb = 0xFF713F08.toInt(), strokeWidth = 5f, hasInnerShadow = true, innerShadowColorArgb = 0x99330000.toInt(), innerShadowDy = 3f, hasGlow = false, material3D = "金属质感") }
        })
        KawaiiChipFx(text = "🧊 立体文字", isSelected = false, onClick = {
            onUpdateProps { it.copy(has3D = true, depth3D = 18f, angle3D = 45f, darken3D = 0.55f, hasBevel = true, bevelWidth = 4f, hasGroundShadow = true, hasShadow = true, shadowRadius = 8f, hasColorOverlay = false, hasInnerShadow = false, hasInnerGlow = false) }
        })
        KawaiiChipFx(text = "🌑 长阴影", isSelected = false, onClick = {
            onUpdateProps { it.copy(hasShadow = true, shadowColorArgb = 0x99000000.toInt(), shadowRadius = 6f, shadowDx = 0f, shadowDy = 14f, shadowSpread = 20f, hasStroke = false, hasGlow = false, hasInnerShadow = false, hasInnerGlow = false, hasColorOverlay = false) }
        })
    }

    HorizontalDivider(color = KawaiiOutline.copy(alpha = 0.15f), modifier = Modifier.padding(vertical = 8.dp))

    // v2.2：一键清除全部效果
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
        KawaiiButtonFx("🗑️ 一键清除全部效果", onClick = {
            onUpdateProps {
                it.copy(
                    hasStroke = false, hasShadow = false, hasGlow = false, has3D = false, hasBevel = false,
                    hasEmboss = false, hasReflection = false, hasGroundShadow = false, hasTextureFill = false,
                    hasGradientOverlay = false, hasInnerShadow = false, hasInnerGlow = false, hasColorOverlay = false
                )
            }
        })
    }

    HorizontalDivider(color = KawaiiOutline.copy(alpha = 0.15f), modifier = Modifier.padding(vertical = 8.dp))

    // 文字底框
    FxSwitchRow("▧ 背景底框", checked = props.hasBackgroundBox) { on ->
        onUpdateProps { it.copy(hasBackgroundBox = on) }
    }
    if (props.hasBackgroundBox) {
        FxSlider("边框半径", "${props.backgroundBoxRadius.toInt()} px", props.backgroundBoxRadius, 0f..32f) { v -> onUpdateProps { it.copy(backgroundBoxRadius = v) } }
        FxSlider("内边距", "${props.backgroundBoxPadding.toInt()} px", props.backgroundBoxPadding, 2f..24f) { v -> onUpdateProps { it.copy(backgroundBoxPadding = v) } }
    }
}

// ======================== 通用小部件 ========================
@Composable
private fun FxSwitchRow(
    title: String,
    checked: Boolean,
    onChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = title, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = KawaiiTextPrimary)
        Switch(
            checked = checked,
            onCheckedChange = { onChange(it) },
            colors = SwitchDefaults.colors(checkedThumbColor = KawaiiPink, checkedTrackColor = KawaiiPinkLight)
        )
    }
}

@Composable
private fun FxSlider(
    label: String,
    valueText: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    onValue: (Float) -> Unit
) {
    Column(modifier = Modifier.padding(vertical = 3.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = label, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = KawaiiTextPrimary)
            Text(text = valueText, fontSize = 11.sp, fontWeight = FontWeight.Black, color = KawaiiPink)
        }
        Slider(
            value = value,
            onValueChange = { onValue(it) },
            valueRange = range,
            colors = SliderDefaults.colors(thumbColor = KawaiiPink, activeTrackColor = KawaiiPink)
        )
    }
}

@Composable
private fun FxColorPickRow(
    label: String,
    current: Int,
    onPick: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = KawaiiTextPrimary)
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            val palette = listOf(
                Color(0xFFFFFFFF), Color(0xFF000000), KawaiiPink, Color(0xFFFFD166),
                KawaiiMint, KawaiiSkyBlue, KawaiiLavender, Color(0xFFFF6A00),
                Color(0xFFFF1493), Color(0xFF9D4EDD), Color(0xFF00D4FF), Color(0xFF8B0000)
            )
            palette.take(8).forEach { c ->
                val isSel = current == c.toArgb()
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(c)
                        .border(if (isSel) 2.dp else 1.dp, if (isSel) KawaiiPink else KawaiiOutline.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                        .jellyClickable { onPick(c.toArgb()) }
                )
            }
            KawaiiButtonFx("更多", onClick = { /* keep simple */ })
        }
    }
}

@Composable
private fun KawaiiChipFx(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (isSelected) KawaiiPinkLight else KawaiiCardTint)
            .border(1.dp, if (isSelected) KawaiiPink else KawaiiOutline.copy(alpha = 0.2f), RoundedCornerShape(10.dp))
            .jellyClickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 5.dp)
    ) {
        Text(
            text = text,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
            color = if (isSelected) KawaiiPink else KawaiiTextPrimary
        )
    }
}

@Composable
private fun KawaiiButtonFx(
    text: String,
    onClick: () -> Unit,
    containerColor: Color = KawaiiPink
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(containerColor)
            .jellyClickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(text = text, fontSize = 11.sp, fontWeight = FontWeight.Black, color = KawaiiTextWhite)
    }
}