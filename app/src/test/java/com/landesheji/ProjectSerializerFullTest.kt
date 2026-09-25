package com.landesheji

import com.landesheji.data.db.ProjectSerializer
import com.landesheji.data.model.DrawPoint
import com.landesheji.data.model.DrawStroke
import com.landesheji.data.model.ImageLayerProperties
import com.landesheji.data.model.LayerItem
import com.landesheji.data.model.LayerType
import com.landesheji.data.model.ShapeProperties
import com.landesheji.data.model.ShapeType
import com.landesheji.data.model.StickerProperties
import com.landesheji.data.model.TextProperties
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * 工程序列化完整往返测试：覆盖全部 5 种图层类型与 PS FX 新字段，
 * 以及空输入 / 损坏输入的容错。
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ProjectSerializerFullTest {

    @Test
    fun `all layer types roundtrip`() {
        val layers = listOf(
            // 文字图层（含 PS FX / PS3D 全字段）
            LayerItem(
                name = "3D 标题",
                type = LayerType.TEXT,
                x = 12f,
                y = -8f,
                scaleX = 1.5f,
                scaleY = 1.5f,
                rotation = 30f,
                opacity = 0.8f,
                isVisible = true,
                isLocked = false,
                zIndex = 3,
                textProps = TextProperties(
                    text = "立体大标题",
                    fontSize = 64f,
                    colorArgb = 0xFF00ADB5.toInt(),
                    isGradient = true,
                    isBold = true,
                    has3D = true,
                    depth3D = 24f,
                    angle3D = 210f,
                    isPerspective3D = true,
                    perspectiveAmount3D = 0.7f,
                    darken3D = 0.5f,
                    customExtrusionColor = true,
                    extrusionColorArgb = 0xFF333333.toInt(),
                    gradientExtrusion = true,
                    hasBevel = true,
                    bevelWidth = 6f,
                    bevelAngle = 90f,
                    lightAngle3D = 45f,
                    lightIntensity3D = 1.4f,
                    material3D = "金属质感",
                    rotateX3D = 15f,
                    rotateY3D = -10f,
                    hasGroundShadow = true,
                    groundShadowBlur = 20f,
                    groundShadowDistance = 25f,
                    groundShadowOpacity = 0.5f,
                    hasReflection = true,
                    reflectionDistance = 6f,
                    reflectionAlpha = 0.4f,
                    hasEmboss = true,
                    embossIntensity = 0.8f,
                    hasGlow = true,
                    glowColorArgb = 0xFFFF00AA.toInt(),
                    glowRadius = 30f,
                    hasTextureFill = true,
                    textureIndex = 3,
                    textureColorArgb = 0xFFFFDD00.toInt(),
                    hasGradientOverlay = true,
                    gradientOverlayColor2Argb = 0xFFFF6B9D.toInt(),
                    gradientOverlayAngle = 120f
                )
            ),
            // 形状图层
            LayerItem(
                name = "圆角矩形",
                type = LayerType.SHAPE,
                shapeProps = ShapeProperties(
                    shapeType = ShapeType.ROUNDED_RECTANGLE,
                    fillColorArgb = 0xFF00ADB5.toInt(),
                    isGradient = true,
                    cornerRadius = 24f,
                    hasStroke = true,
                    strokeWidth = 6f,
                    hasShadow = true,
                    shadowRadius = 12f
                )
            ),
            // 贴纸图层
            LayerItem(
                name = "星星贴纸",
                type = LayerType.STICKER,
                stickerProps = StickerProperties(
                    stickerId = "star",
                    stickerName = "星星",
                    hasTint = true,
                    tintArgb = 0xFFFF8800.toInt()
                )
            ),
            // 图片图层
            LayerItem(
                name = "照片",
                type = LayerType.IMAGE,
                imageProps = ImageLayerProperties(
                    uriString = "content://media/external/images/1",
                    aspectRatio = 1.33f,
                    cornerRadius = 16f,
                    borderStrokeWidth = 4f,
                    borderColorArgb = 0xFFFFFFFF.toInt()
                )
            ),
            // 涂鸦图层（多笔画+橡皮擦）
            LayerItem(
                name = "手绘涂鸦",
                type = LayerType.DRAW,
                drawStrokes = listOf(
                    DrawStroke(
                        points = listOf(DrawPoint(1f, 2f), DrawPoint(3f, 4f)),
                        colorArgb = 0xFF00FF00.toInt(),
                        strokeWidth = 12f,
                        isEraser = false
                    ),
                    DrawStroke(
                        points = listOf(DrawPoint(10f, 20f)),
                        colorArgb = 0xFF000000.toInt(),
                        strokeWidth = 30f,
                        isEraser = true
                    )
                )
            )
        )

        val json = ProjectSerializer.serializeLayers(layers)
        val restored = ProjectSerializer.deserializeLayers(json)

        assertEquals("图层数量应保持一致", layers.size, restored.size)

        // 文字图层全字段
        val text = restored[0].textProps
        assertEquals("立体大标题", text.text)
        assertEquals(64f, text.fontSize, 0.01f)
        assertTrue(text.isGradient)
        assertTrue(text.isBold)
        assertTrue(text.has3D)
        assertEquals(24f, text.depth3D, 0.01f)
        assertEquals(210f, text.angle3D, 0.01f)
        assertTrue(text.isPerspective3D)
        assertEquals(0.7f, text.perspectiveAmount3D, 0.01f)
        assertTrue(text.customExtrusionColor)
        assertTrue(text.gradientExtrusion)
        assertTrue(text.hasBevel)
        assertEquals(6f, text.bevelWidth, 0.01f)
        assertEquals(1.4f, text.lightIntensity3D, 0.01f)
        assertEquals("金属质感", text.material3D)
        assertTrue(text.hasGroundShadow)
        assertTrue(text.hasReflection)
        assertTrue(text.hasEmboss)
        assertTrue(text.hasGlow)
        assertEquals(0xFFFF00AA.toInt(), text.glowColorArgb)
        assertEquals(30f, text.glowRadius, 0.01f)
        assertTrue(text.hasTextureFill)
        assertEquals(3, text.textureIndex)
        assertTrue(text.hasGradientOverlay)
        assertEquals(120f, text.gradientOverlayAngle, 0.01f)
        assertEquals(30f, restored[0].rotation, 0.01f)
        assertEquals(0.8f, restored[0].opacity, 0.01f)
        assertEquals(3, restored[0].zIndex)

        // 形状图层
        assertEquals(LayerType.SHAPE, restored[1].type)
        assertEquals(ShapeType.ROUNDED_RECTANGLE, restored[1].shapeProps.shapeType)
        assertEquals(24f, restored[1].shapeProps.cornerRadius, 0.01f)
        assertTrue(restored[1].shapeProps.hasStroke)

        // 贴纸图层
        assertEquals("星星贴纸", restored[2].name)
        assertTrue(restored[2].stickerProps.hasTint)
        assertEquals(0xFFFF8800.toInt(), restored[2].stickerProps.tintArgb)

        // 图片图层
        assertEquals("content://media/external/images/1", restored[3].imageProps.uriString)
        assertEquals(1.33f, restored[3].imageProps.aspectRatio, 0.01f)

        // 涂鸦图层
        assertEquals(2, restored[4].drawStrokes.size)
        assertEquals(2, restored[4].drawStrokes[0].points.size)
        assertTrue(restored[4].drawStrokes[1].isEraser)
        assertEquals(30f, restored[4].drawStrokes[1].strokeWidth, 0.01f)
    }

    @Test
    fun `blank input returns empty`() {
        assertTrue(ProjectSerializer.deserializeLayers("").isEmpty())
        assertTrue(ProjectSerializer.deserializeLayers("not json at all").isEmpty())
        // 损坏 JSON 不应抛出异常
        ProjectSerializer.deserializeLayers("[{\"type\":\"TEXT\"}]")
    }

    @Test
    fun `canvas blank input falls back to defaults`() {
        val cfg = ProjectSerializer.deserializeCanvas("")
        assertEquals(1080, cfg.width)
        assertEquals(1080, cfg.height)
        // 损坏 JSON 不应抛出异常
        val cfg2 = ProjectSerializer.deserializeCanvas("{{{broken")
        assertEquals(1080, cfg2.width)
    }
}