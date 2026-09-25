package com.landesheji.data.model

data class PresetTemplate(
    val id: String,
    val name: String,
    val previewBgColorArgb: Int,
    val previewTextColorArgb: Int,
    val description: String,
    val canvasConfig: CanvasConfig,
    val defaultLayers: List<LayerItem>
)

object PresetsData {
    val templates: List<PresetTemplate> = listOf(
        PresetTemplate(
            id = "default",
            name = "默认经典",
            previewBgColorArgb = 0xFF222831.toInt(),
            previewTextColorArgb = 0xFF00ADB5.toInt(),
            description = "PixelLab 经典暗色画布配青色高光",
            canvasConfig = CanvasConfig(
                backgroundColorArgb = 0xFF222831.toInt(),
                backgroundType = BackgroundType.COLOR
            ),
            defaultLayers = listOf(
                LayerItem(
                    name = "新文字",
                    type = LayerType.TEXT,
                    textProps = TextProperties(
                        text = "新文字",
                        fontSize = 42f,
                        colorArgb = 0xFF00ADB5.toInt(),
                        hasStroke = false,
                        hasShadow = true,
                        shadowRadius = 8f
                    )
                )
            )
        ),
        PresetTemplate(
            id = "neon_3d",
            name = "3D 霓虹国潮",
            previewBgColorArgb = 0xFF141324.toInt(),
            previewTextColorArgb = 0xFFFF2E93.toInt(),
            description = "潮酷立体字与发光霓虹双色",
            canvasConfig = CanvasConfig(
                backgroundColorArgb = 0xFF141324.toInt(),
                backgroundType = BackgroundType.GRADIENT,
                backgroundGradientColor2Argb = 0xFF2E124D.toInt()
            ),
            defaultLayers = listOf(
                LayerItem(
                    name = "3D 霓虹文字",
                    type = LayerType.TEXT,
                    textProps = TextProperties(
                        text = "PIXEL LAB",
                        fontSize = 48f,
                        colorArgb = 0xFFFF2E93.toInt(),
                        isBold = true,
                        hasStroke = true,
                        strokeColorArgb = 0xFFFFFFFF.toInt(),
                        strokeWidth = 3f,
                        hasShadow = true,
                        shadowColorArgb = 0xFFFF2E93.toInt(),
                        shadowRadius = 24f,
                        has3D = true,
                        depth3D = 16f,
                        darken3D = 0.6f
                    )
                ),
                LayerItem(
                    name = "副标题",
                    type = LayerType.TEXT,
                    y = 70f,
                    textProps = TextProperties(
                        text = "图文设计工坊",
                        fontSize = 22f,
                        colorArgb = 0xFF00FFF0.toInt(),
                        letterSpacing = 4f,
                        hasShadow = true,
                        shadowColorArgb = 0xFF00FFF0.toInt(),
                        shadowRadius = 12f
                    )
                )
            )
        ),
        PresetTemplate(
            id = "golden_luxury",
            name = "黑金尊享",
            previewBgColorArgb = 0xFF111111.toInt(),
            previewTextColorArgb = 0xFFFFD700.toInt(),
            description = "奢华黑底金字与立体倒影",
            canvasConfig = CanvasConfig(
                backgroundColorArgb = 0xFF111111.toInt(),
                backgroundType = BackgroundType.COLOR
            ),
            defaultLayers = listOf(
                LayerItem(
                    name = "金色主题",
                    type = LayerType.TEXT,
                    textProps = TextProperties(
                        text = "极致奢华",
                        fontSize = 46f,
                        colorArgb = 0xFFFFD700.toInt(),
                        isBold = true,
                        hasStroke = true,
                        strokeColorArgb = 0xFFFFA000.toInt(),
                        strokeWidth = 2f,
                        hasShadow = true,
                        shadowColorArgb = 0x99FFD700.toInt(),
                        shadowRadius = 16f,
                        hasReflection = true,
                        reflectionDistance = 6f,
                        reflectionAlpha = 0.4f
                    )
                )
            )
        ),
        PresetTemplate(
            id = "minimal_badge",
            name = "极简徽章",
            previewBgColorArgb = 0xFFECEFF1.toInt(),
            previewTextColorArgb = 0xFF263238.toInt(),
            description = "清爽质感浅色徽章排版",
            canvasConfig = CanvasConfig(
                backgroundColorArgb = 0xFFECEFF1.toInt(),
                backgroundType = BackgroundType.COLOR
            ),
            defaultLayers = listOf(
                LayerItem(
                    name = "圆形边框",
                    type = LayerType.SHAPE,
                    scaleX = 1.3f,
                    scaleY = 1.3f,
                    shapeProps = ShapeProperties(
                        shapeType = ShapeType.CIRCLE,
                        fillColorArgb = 0xFFFFFFFF.toInt(),
                        hasStroke = true,
                        strokeColorArgb = 0xFF263238.toInt(),
                        strokeWidth = 4f
                    )
                ),
                LayerItem(
                    name = "徽章文字",
                    type = LayerType.TEXT,
                    textProps = TextProperties(
                        text = "DESIGN STUDIO",
                        fontSize = 24f,
                        colorArgb = 0xFF263238.toInt(),
                        isBold = true,
                        letterSpacing = 3f
                    )
                )
            )
        ),
        PresetTemplate(
            id = "transparent_png",
            name = "透明底出图",
            previewBgColorArgb = 0x33888888.toInt(),
            previewTextColorArgb = 0xFF00ADB5.toInt(),
            description = "透明网格背景，一键制作免抠图/表情包",
            canvasConfig = CanvasConfig(
                backgroundColorArgb = 0x00000000,
                backgroundType = BackgroundType.TRANSPARENT
            ),
            defaultLayers = listOf(
                LayerItem(
                    name = "免抠文字",
                    type = LayerType.TEXT,
                    textProps = TextProperties(
                        text = "透明背景 PNG",
                        fontSize = 38f,
                        colorArgb = 0xFF00ADB5.toInt(),
                        hasStroke = true,
                        strokeColorArgb = 0xFFFFFFFF.toInt(),
                        strokeWidth = 4f
                    )
                )
            )
        ),
        // ====== 以下为需求5：可编辑的 PS 3D 立体字效预设 ======
        PresetTemplate(
            id = "ps_balloon",
            name = "PS 气球立体字",
            previewBgColorArgb = 0xFFFFE9F1.toInt(),
            previewTextColorArgb = 0xFFFF6FA0.toInt(),
            description = "气球感 PS 立体字（可编辑修改文本）",
            canvasConfig = CanvasConfig(
                backgroundColorArgb = 0xFFFFE9F1.toInt(),
                backgroundType = BackgroundType.COLOR
            ),
            defaultLayers = listOf(
                LayerItem(
                    name = "气球字",
                    type = LayerType.TEXT,
                    textProps = TextProperties(
                        text = "BABY",
                        fontSize = 64f,
                        colorArgb = 0xFFFF6FA0.toInt(),
                        isBold = true,
                        has3D = true,
                        depth3D = 22f,
                        angle3D = 45f,
                        darken3D = 0.35f,
                        material3D = "充气气球",
                        hasBevel = true,
                        bevelWidth = 5f,
                        hasGroundShadow = true
                    )
                )
            )
        ),
        PresetTemplate(
            id = "ps_metal_chrome",
            name = "PS 镜面金属字",
            previewBgColorArgb = 0xFF1B1B1B.toInt(),
            previewTextColorArgb = 0xFFC0C0C0.toInt(),
            description = "质感金属立体字（字可改）",
            canvasConfig = CanvasConfig(
                backgroundColorArgb = 0xFF1B1B1B.toInt(),
                backgroundType = BackgroundType.COLOR
            ),
            defaultLayers = listOf(
                LayerItem(
                    name = "金属字",
                    type = LayerType.TEXT,
                    textProps = TextProperties(
                        text = "METAL",
                        fontSize = 56f,
                        colorArgb = 0xFFC0C0C0.toInt(),
                        isBold = true,
                        has3D = true,
                        depth3D = 36f,
                        angle3D = 45f,
                        darken3D = 0.65f,
                        material3D = "金属质感",
                        customExtrusionColor = true,
                        extrusionColorArgb = 0xFF606060.toInt(),
                        hasBevel = true,
                        bevelWidth = 6f,
                        hasShadow = true,
                        shadowColorArgb = 0x80000000.toInt(),
                        shadowRadius = 18f
                    )
                )
            )
        ),
        PresetTemplate(
            id = "ps_crystal",
            name = "PS 水晶冰凌字",
            previewBgColorArgb = 0xFF0F2E55.toInt(),
            previewTextColorArgb = 0xFFBEEAFF.toInt(),
            description = "水晶质感立体字（可编辑）",
            canvasConfig = CanvasConfig(
                backgroundColorArgb = 0xFF0F2E55.toInt(),
                backgroundType = BackgroundType.COLOR
            ),
            defaultLayers = listOf(
                LayerItem(
                    name = "水晶字",
                    type = LayerType.TEXT,
                    textProps = TextProperties(
                        text = "冰凌",
                        fontSize = 64f,
                        colorArgb = 0xFFBEEAFF.toInt(),
                        isBold = true,
                        has3D = true,
                        depth3D = 30f,
                        angle3D = 45f,
                        darken3D = 0.5f,
                        material3D = "水晶冰凌",
                        hasBevel = true,
                        bevelWidth = 6f,
                        hasGroundShadow = true
                    )
                )
            )
        ),
        PresetTemplate(
            id = "ps_fire",
            name = "PS 火焰烈焰字",
            previewBgColorArgb = 0xFF1A0A0A.toInt(),
            previewTextColorArgb = 0xFFFF6A00.toInt(),
            description = "燃烧火焰立体字（可编辑）",
            canvasConfig = CanvasConfig(
                backgroundColorArgb = 0xFF1A0A0A.toInt(),
                backgroundType = BackgroundType.COLOR
            ),
            defaultLayers = listOf(
                LayerItem(
                    name = "火焰字",
                    type = LayerType.TEXT,
                    textProps = TextProperties(
                        text = "燃",
                        fontSize = 88f,
                        colorArgb = 0xFFFF6A00.toInt(),
                        isBold = true,
                        has3D = true,
                        depth3D = 26f,
                        angle3D = 315f,
                        darken3D = 0.4f,
                        material3D = "火焰光焰",
                        customExtrusionColor = true,
                        extrusionColorArgb = 0xFF8B0000.toInt(),
                        hasShadow = true,
                        shadowColorArgb = 0xFFFF6A00.toInt(),
                        shadowRadius = 24f
                    )
                )
            )
        ),
        PresetTemplate(
            id = "ps_rainbow_candy",
            name = "PS 糖果彩虹字",
            previewBgColorArgb = 0xFFFFF0F5.toInt(),
            previewTextColorArgb = 0xFFFF6B9D.toInt(),
            description = "糖果彩虹感立体字（可编辑）",
            canvasConfig = CanvasConfig(
                backgroundColorArgb = 0xFFFFF0F5.toInt(),
                backgroundType = BackgroundType.COLOR
            ),
            defaultLayers = listOf(
                LayerItem(
                    name = "彩虹字",
                    type = LayerType.TEXT,
                    textProps = TextProperties(
                        text = "SWEET",
                        fontSize = 54f,
                        colorArgb = 0xFFFF6B9D.toInt(),
                        isBold = true,
                        has3D = true,
                        depth3D = 22f,
                        angle3D = 225f,
                        darken3D = 0.35f,
                        material3D = "糖果彩虹",
                        hasGroundShadow = true
                    )
                )
            )
        ),
        PresetTemplate(
            id = "ps_glass",
            name = "PS 玻璃琉璃字",
            previewBgColorArgb = 0xFF1A4D6E.toInt(),
            previewTextColorArgb = 0xFF7CD3F7.toInt(),
            description = "通透玻璃琉璃立体字（可编辑）",
            canvasConfig = CanvasConfig(
                backgroundColorArgb = 0xFF1A4D6E.toInt(),
                backgroundType = BackgroundType.COLOR
            ),
            defaultLayers = listOf(
                LayerItem(
                    name = "玻璃字",
                    type = LayerType.TEXT,
                    textProps = TextProperties(
                        text = "GLASS",
                        fontSize = 52f,
                        colorArgb = 0xFF7CD3F7.toInt(),
                        isBold = true,
                        has3D = true,
                        depth3D = 32f,
                        angle3D = 90f,
                        darken3D = 0.3f,
                        material3D = "玻璃琉璃",
                        hasBevel = true,
                        bevelWidth = 4f,
                        hasShadow = true,
                        shadowColorArgb = 0x80000050.toInt(),
                        shadowRadius = 16f
                    )
                )
            )
        )
    )
}
