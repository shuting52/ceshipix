package com.landesheji.data.model

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import java.util.UUID

enum class LayerType {
    TEXT,
    SHAPE,
    STICKER,
    IMAGE,
    DRAW
}

enum class ShapeType {
    RECTANGLE,
    ROUNDED_RECTANGLE,
    CIRCLE,
    STAR,
    HEART,
    TRIANGLE,
    HEXAGON,
    ARROW
}

enum class BackgroundType {
    COLOR,
    GRADIENT,
    TRANSPARENT,
    PRESET_TEXTURE,
    IMAGE_URI,
    VIDEO_URI
}

enum class TextAlignment {
    LEFT,
    CENTER,
    RIGHT
}

/** v2.2：描边位置（类似 PS 描边位置：内/居中/外） */
enum class StrokePosition {
    INNER,
    CENTER,
    OUTER
}

data class DrawPoint(
    val x: Float,
    val y: Float
)

data class DrawStroke(
    val points: List<DrawPoint> = emptyList(),
    val colorArgb: Int = Color.White.toArgb(),
    val strokeWidth: Float = 6f,
    val isEraser: Boolean = false
)

data class TextProperties(
    val text: String = "新文字",
    val fontSize: Float = 36f,
    val colorArgb: Int = 0xFFFFFFFF.toInt(),
    val isGradient: Boolean = false,
    val gradientColor2Argb: Int = 0xFF00ADB5.toInt(),
    val gradientAngle: Float = 45f,
    val isBold: Boolean = false,
    val isItalic: Boolean = false,
    val isUnderline: Boolean = false,
    val fontStyleName: String = "默认",
    val alignment: TextAlignment = TextAlignment.CENTER,
    val letterSpacing: Float = 0f,
    val lineSpacing: Float = 1f,
    val curveAngle: Float = 0f, // -100 to 100 degrees
    val hasBackgroundBox: Boolean = false,
    val backgroundBoxColorArgb: Int = 0x88000000.toInt(),
    val backgroundBoxRadius: Float = 8f,
    val backgroundBoxPadding: Float = 8f,
    // 描边 (Stroke)
    val hasStroke: Boolean = false,
    val strokeColorArgb: Int = 0xFF000000.toInt(),
    val strokeWidth: Float = 4f,
    // 外阴影 (Shadow)
    val hasShadow: Boolean = false,
    val shadowColorArgb: Int = 0xAA000000.toInt(),
    val shadowRadius: Float = 10f,
    val shadowDx: Float = 4f,
    val shadowDy: Float = 4f,
    // Adobe Photoshop 3D 立体字全功能 (PS 3D Text Engine)
    val has3D: Boolean = false,
    val depth3D: Float = 16f, // 挤出厚度 (Extrusion Depth 1..80)
    val angle3D: Float = 45f, // 挤出投影角度 (Extrusion Angle 0..360°)
    val isPerspective3D: Boolean = false, // 是否开启透视灭点 (Perspective vs Isometric)
    val perspectiveAmount3D: Float = 0.5f, // 透视收缩程度 (0.1..1.0)
    val darken3D: Float = 0.55f, // 侧面阴影明暗衰减 (0.1..0.9)
    val customExtrusionColor: Boolean = false, // 是否使用自定侧面颜色
    val extrusionColorArgb: Int = 0xFF555555.toInt(), // 自定义侧面纯色
    val gradientExtrusion: Boolean = false, // 侧面是否启用双色渐变
    val gradientExtrusionColor2Argb: Int = 0xFF222222.toInt(), // 侧面渐变色2
    // PS 3D 倒角与斜面 (Bevel & Emboss)
    val hasBevel: Boolean = false,
    val bevelWidth: Float = 4f, // 倒角斜面宽度 (1..16 px)
    val bevelAngle: Float = 120f, // 倒角光照入射角 (0..360°)
    val bevelHighlightColorArgb: Int = 0xFFFFFFFF.toInt(),
    val bevelShadowColorArgb: Int = 0xFF000000.toInt(),
    // PS 3D 光源与材质质感 (Lighting & Materials)
    val lightAngle3D: Float = 135f, // 主光源照射角 (0..360°)
    val lightIntensity3D: Float = 1.0f, // 光源高光强度 (0.2..2.0)
    val material3D: String = "默认", // 默认, 哑光, 高光光泽, 金属质感, 可爱果冻, 充气气球, 赛博霓虹, 复古街机
    // PS 3D 空间立体俯仰倾斜 (3D Tilt)
    val rotateX3D: Float = 0f, // X轴空间倾斜 (-45°..+45°)
    val rotateY3D: Float = 0f, // Y轴空间倾斜 (-45°..+45°)
    // PS 3D 地面投影 (Ground Cast Shadow)
    val hasGroundShadow: Boolean = false,
    val groundShadowBlur: Float = 14f,
    val groundShadowDistance: Float = 18f,
    val groundShadowOpacity: Float = 0.4f,
    // 倒影 (Reflection)
    val hasReflection: Boolean = false,
    val reflectionDistance: Float = 4f,
    val reflectionAlpha: Float = 0.35f,
    // 浮雕 (Emboss)
    val hasEmboss: Boolean = false,
    val embossIntensity: Float = 0.6f,
    // PS FX 全面化：外发光 (Glow) —— 类似 PS 图层样式的外发光
    val hasGlow: Boolean = false,
    val glowColorArgb: Int = 0xFF00D4FF.toInt(),
    val glowRadius: Float = 18f,
    // v2.2：外发光强度/不透明度
    val glowOpacity: Float = 0.8f,
    // PS FX：纹理填充 (Texture Fill) —— 斜纹/点阵/棋盘/波浪等填充在文字表面
    val hasTextureFill: Boolean = false,
    val textureIndex: Int = 0,
    val textureColorArgb: Int = 0xFFFFFFFF.toInt(),
    // PS FX：渐变叠加 (Gradient Overlay)
    val hasGradientOverlay: Boolean = false,
    val gradientOverlayColor2Argb: Int = 0xFFFF6B9D.toInt(),
    val gradientOverlayAngle: Float = 45f,
    // ===== 需求重造3：文字模板系统（10 组特效模板支持） =====
    // templateEffect 非空时，文字由 TemplateEffectsRenderer 按效果名 + 参数本地绘制，
    // 修改文字内容后效果保持不变（模板=数据、效果=绘制函数、文字=可修改参数）
    val templateEffect: String = "",
    val templateEffectParamsJson: String = "{}",
    // ===== v2.2：PS 图层样式增强 =====
    // 描边位置（内/居中/外）
    val strokePosition: StrokePosition = StrokePosition.OUTER,
    // 阴影扩展范围（0~100，模拟 PS 投影扩展）
    val shadowSpread: Float = 0f,
    // 内阴影（文字内部偏移暗影）
    val hasInnerShadow: Boolean = false,
    val innerShadowColorArgb: Int = 0xCC000000.toInt(),
    val innerShadowRadius: Float = 8f,
    val innerShadowDx: Float = 3f,
    val innerShadowDy: Float = 3f,
    val innerShadowOpacity: Float = 0.7f,
    // 内发光（文字内部边缘发光）
    val hasInnerGlow: Boolean = false,
    val innerGlowColorArgb: Int = 0xFFFF8C00.toInt(),
    val innerGlowRadius: Float = 10f,
    val innerGlowOpacity: Float = 0.8f,
    // 颜色叠加（纯色覆盖层）
    val hasColorOverlay: Boolean = false,
    val colorOverlayColorArgb: Int = 0xFF00ADB5.toInt(),
    val colorOverlayOpacity: Float = 1f
)

data class ShapeProperties(
    val shapeType: ShapeType = ShapeType.ROUNDED_RECTANGLE,
    val fillColorArgb: Int = 0xFF00ADB5.toInt(),
    val isGradient: Boolean = false,
    val gradientColor2Argb: Int = 0xFF393E46.toInt(),
    val cornerRadius: Float = 16f,
    val hasStroke: Boolean = false,
    val strokeColorArgb: Int = 0xFFFFFFFF.toInt(),
    val strokeWidth: Float = 3f,
    val hasShadow: Boolean = true,
    val shadowColorArgb: Int = 0x88000000.toInt(),
    val shadowRadius: Float = 8f
)

data class StickerProperties(
    val stickerId: String = "star",
    val stickerName: String = "星星",
    val tintArgb: Int = 0xFFFFFFFF.toInt(),
    val hasTint: Boolean = false
)

data class ImageLayerProperties(
    val uriString: String = "",
    val aspectRatio: Float = 1f,
    val cornerRadius: Float = 0f,
    val borderStrokeWidth: Float = 0f,
    val borderColorArgb: Int = 0xFFFFFFFF.toInt(),
    val filterColorFilterArgb: Int = 0x00000000
)

data class LayerItem(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "图层",
    val type: LayerType = LayerType.TEXT,
    val x: Float = 0f, // Center offset X relative to canvas center
    val y: Float = 0f, // Center offset Y relative to canvas center
    val scaleX: Float = 1f,
    val scaleY: Float = 1f,
    val rotation: Float = 0f,
    val opacity: Float = 1f,
    val isVisible: Boolean = true,
    val isLocked: Boolean = false,
    val zIndex: Int = 0,
    // Type specific configs
    val textProps: TextProperties = TextProperties(),
    val shapeProps: ShapeProperties = ShapeProperties(),
    val stickerProps: StickerProperties = StickerProperties(),
    val imageProps: ImageLayerProperties = ImageLayerProperties(),
    val drawStrokes: List<DrawStroke> = emptyList()
)

data class CanvasConfig(
    val width: Int = 1080,
    val height: Int = 1080,
    val ratioName: String = "1:1 正方形",
    val backgroundType: BackgroundType = BackgroundType.COLOR,
    val backgroundColorArgb: Int = 0xFF222831.toInt(),
    val backgroundGradientColor2Argb: Int = 0xFF393E46.toInt(),
    val backgroundGradientAngle: Float = 45f,
    val backgroundImageUri: String = "",
    val presetTextureIndex: Int = 0,
    // 需求6：本地视频作为全局背景（首帧可作为静态背景预览，导出时拍电影渲染）
    val backgroundVideoUri: String = "",
    val backgroundVideoMuted: Boolean = true,
    val backgroundVideoLoop: Boolean = true,
    // 需求6：毛玻璃、霓虹、反色等设计实用滤镜与背景增强开关
    val backgroundBlur: Float = 0f, // 0f ~ 1f
    val backgroundNeonGlow: Float = 0f, // 0f ~ 1f
    val backgroundInvert: Boolean = false,
    // 全图画面特效
    val vignette: Float = 0f, // 0f ~ 1f
    val noise: Float = 0f, // 0f ~ 1f
    val stripes: Float = 0f, // 0f ~ 1f
    val brightness: Float = 1f, // 0.5f ~ 1.5f
    val contrast: Float = 1f // 0.5f ~ 1.5f
)
