package com.landesheji.ui.components

import android.graphics.Bitmap
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.nativeCanvas
import com.landesheji.data.model.BackgroundType
import com.landesheji.data.model.CanvasConfig
import com.landesheji.data.model.DrawStroke
import com.landesheji.data.model.LayerItem
import com.landesheji.data.model.LayerType
import com.landesheji.data.model.ShapeProperties
import com.landesheji.data.model.ShapeType
import com.landesheji.data.model.TextAlignment
import com.landesheji.data.model.TextProperties
import com.landesheji.util.TemplateEffectsRenderer
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

object CanvasRenderer {

    fun drawCanvasBackground(
        drawScope: DrawScope,
        config: CanvasConfig,
        canvasSize: Size
    ) {
        when (config.backgroundType) {
            BackgroundType.TRANSPARENT -> {
                // Draw checkerboard
                val squareSize = 24f
                var x = 0f
                var row = 0
                while (x < canvasSize.width) {
                    var y = 0f
                    var col = row
                    while (y < canvasSize.height) {
                        val color = if (col % 2 == 0) Color(0xFF2E333D) else Color(0xFF22262E)
                        drawScope.drawRect(
                            color = color,
                            topLeft = Offset(x, y),
                            size = Size(
                                squareSize.coerceAtMost(canvasSize.width - x),
                                squareSize.coerceAtMost(canvasSize.height - y)
                            )
                        )
                        y += squareSize
                        col++
                    }
                    x += squareSize
                    row++
                }
            }
            BackgroundType.COLOR -> {
                drawScope.drawRect(
                    color = Color(config.backgroundColorArgb),
                    topLeft = Offset.Zero,
                    size = canvasSize
                )
            }
            BackgroundType.GRADIENT -> {
                val rad = Math.toRadians(config.backgroundGradientAngle.toDouble())
                val start = Offset(
                    (canvasSize.width / 2 * (1 - cos(rad))).toFloat(),
                    (canvasSize.height / 2 * (1 - sin(rad))).toFloat()
                )
                val end = Offset(
                    (canvasSize.width / 2 * (1 + cos(rad))).toFloat(),
                    (canvasSize.height / 2 * (1 + sin(rad))).toFloat()
                )
                drawScope.drawRect(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(config.backgroundColorArgb),
                            Color(config.backgroundGradientColor2Argb)
                        ),
                        start = start,
                        end = end
                    ),
                    topLeft = Offset.Zero,
                    size = canvasSize
                )
            }
            BackgroundType.PRESET_TEXTURE -> {
                val base = Color(config.backgroundColorArgb)
                drawScope.drawRect(color = base, topLeft = Offset.Zero, size = canvasSize)
                // Draw geometric pattern texture
                val step = 40f
                var px = 0f
                while (px < canvasSize.width) {
                    drawScope.drawLine(
                        color = Color.White.copy(alpha = 0.04f),
                        start = Offset(px, 0f),
                        end = Offset(px, canvasSize.height),
                        strokeWidth = 1f
                    )
                    px += step
                }
                var py = 0f
                while (py < canvasSize.height) {
                    drawScope.drawLine(
                        color = Color.White.copy(alpha = 0.04f),
                        start = Offset(0f, py),
                        end = Offset(canvasSize.width, py),
                        strokeWidth = 1f
                    )
                    py += step
                }
            }
            BackgroundType.IMAGE_URI -> {
                // If no image loaded, draw fallback solid
                drawScope.drawRect(
                    color = Color(config.backgroundColorArgb),
                    topLeft = Offset.Zero,
                    size = canvasSize
                )
            }
            BackgroundType.VIDEO_URI -> {
                // 需求6：本地视频背景 —— 首帧无法实时获取时，退化为默认背景色 + 一个【视频背景】标记图标。
                drawScope.drawRect(
                    color = Color(config.backgroundColorArgb),
                    topLeft = Offset.Zero,
                    size = canvasSize
                )
                val center = Offset(canvasSize.width / 2f, canvasSize.height / 2f)
                drawScope.drawCircle(
                    color = Color(0x55FFFFFF),
                    radius = canvasSize.width.coerceAtMost(canvasSize.height) * 0.18f,
                    center = center
                )
            }
        }

        // 需求6：背景反色滤镜
        if (config.backgroundInvert) {
            drawScope.drawRect(
                color = Color(0x44FFFFFF),
                topLeft = Offset.Zero,
                size = canvasSize
            )
        }
        // 需求6：霓虹光晕背景
        if (config.backgroundNeonGlow > 0f) {
            val center = Offset(canvasSize.width / 2f, canvasSize.height / 2f)
            drawScope.drawRect(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFFFF1493).copy(alpha = (config.backgroundNeonGlow * 0.45f).coerceIn(0f, 0.7f)),
                        Color(0xFF9D4EDD).copy(alpha = (config.backgroundNeonGlow * 0.25f).coerceIn(0f, 0.5f)),
                        Color.Transparent
                    ),
                    center = center,
                    radius = canvasSize.width.coerceAtLeast(canvasSize.height) * 0.7f
                ),
                topLeft = Offset.Zero,
                size = canvasSize
            )
        }
        // 需求6：背景毛玻璃可叠多层透明背景制造磨砂感
        if (config.backgroundBlur > 0f) {
            drawScope.drawRect(
                color = Color.White.copy(alpha = (config.backgroundBlur * 0.18f).coerceIn(0f, 0.4f)),
                topLeft = Offset.Zero,
                size = canvasSize
            )
        }

        // Apply Vignette if enabled
        if (config.vignette > 0f) {
            val center = Offset(canvasSize.width / 2f, canvasSize.height / 2f)
            val radius = (canvasSize.width.coerceAtLeast(canvasSize.height) / 1.4f)
            drawScope.drawRect(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color.Black.copy(alpha = (config.vignette * 0.85f).coerceIn(0f, 1f))
                    ),
                    center = center,
                    radius = radius
                ),
                topLeft = Offset.Zero,
                size = canvasSize
            )
        }

        // Apply Stripes if enabled
        if (config.stripes > 0f) {
            val stripeWidth = 6f
            val stripeGap = 12f
            var sy = 0f
            while (sy < canvasSize.height) {
                drawScope.drawRect(
                    color = Color.Black.copy(alpha = config.stripes * 0.35f),
                    topLeft = Offset(0f, sy),
                    size = Size(canvasSize.width, stripeWidth)
                )
                sy += stripeWidth + stripeGap
            }
        }
    }

    fun drawLayer(
        drawScope: DrawScope,
        layer: LayerItem,
        canvasCenter: Offset,
        imageBitmaps: Map<String, Bitmap> = emptyMap()
    ) {
        if (!layer.isVisible) return

        val layerCenter = Offset(canvasCenter.x + layer.x, canvasCenter.y + layer.y)

        drawScope.translate(layerCenter.x, layerCenter.y) {
            drawScope.rotate(layer.rotation, Offset.Zero) {
                drawScope.scale(layer.scaleX, layer.scaleY, Offset.Zero) {
                    when (layer.type) {
                        LayerType.TEXT -> drawTextLayer(drawScope, layer.textProps, layer.opacity)
                        LayerType.SHAPE -> drawShapeLayer(drawScope, layer.shapeProps, layer.opacity)
                        LayerType.STICKER -> drawStickerLayer(drawScope, layer, layer.opacity)
                        LayerType.IMAGE -> drawImageLayer(drawScope, layer, layer.opacity, imageBitmaps[layer.imageProps.uriString])
                        LayerType.DRAW -> drawDrawLayer(drawScope, layer.drawStrokes, layer.opacity)
                    }
                }
            }
        }
    }

    fun drawTextLayerPreview(
        drawScope: DrawScope,
        props: TextProperties,
        opacity: Float,
        previewScale: Float = 0.4f
    ) {
        // 生成按比例缩放的 props，复用 drawTextLayer 实现实时预览
        val scaled = props.copy(
            fontSize = props.fontSize * previewScale,
            depth3D = props.depth3D * previewScale,
            bevelWidth = props.bevelWidth * previewScale,
            strokeWidth = props.strokeWidth * previewScale,
            shadowRadius = props.shadowRadius * previewScale,
            groundShadowBlur = props.groundShadowBlur * previewScale,
            letterSpacing = props.letterSpacing * previewScale
        )
        drawTextLayer(drawScope, scaled, opacity)
    }

    private fun drawTextLayer(
        drawScope: DrawScope,
        props: TextProperties,
        opacity: Float
    ) {
        // 需求重造3：模板特效文字 —— 优先走本地特效渲染器（水晶/玻璃/刺绣/3D/霓虹/金属/火焰/冰霜/故障/赛博）
        if (props.templateEffect.isNotEmpty()) {
            drawScope.drawIntoCanvas { canvas ->
                val native = canvas.nativeCanvas
                try {
                    val params = org.json.JSONObject(
                        if (props.templateEffectParamsJson.isBlank()) "{}" else props.templateEffectParamsJson
                    )
                    // 画布尺寸 = maxW（模板按 1080 逻辑画布设计，这里直接用当前画布宽度）
                    val canvasW = drawScope.size.width
                    TemplateEffectsRenderer.drawEffect(
                        canvas = native,
                        text = props.text,
                        fontSizePx = props.fontSize,
                        effect = props.templateEffect,
                        params = params,
                        x = 0f,
                        y = 0f,
                        fontFamily = props.fontStyleName.ifBlank { "sans-serif" },
                        fontWeight = if (props.isBold) "bold" else "normal",
                        opacity = opacity,
                        maxWidth = canvasW * 2.5f
                    )
                } catch (_: Exception) {
                    // 效果失败时回退到常规文字
                }
            }
            return
        }

        drawScope.drawIntoCanvas { canvas ->
            val native = canvas.nativeCanvas
            val lines = props.text.split("\n")
            val baseFontSize = props.fontSize * 1.5f

            val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                textSize = baseFontSize
                textAlign = when (props.alignment) {
                    TextAlignment.LEFT -> Paint.Align.LEFT
                    TextAlignment.CENTER -> Paint.Align.CENTER
                    TextAlignment.RIGHT -> Paint.Align.RIGHT
                }
                typeface = when {
                    props.isBold && props.isItalic -> Typeface.create(Typeface.DEFAULT, Typeface.BOLD_ITALIC)
                    props.isBold -> Typeface.DEFAULT_BOLD
                    props.isItalic -> Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
                    else -> Typeface.DEFAULT
                }
                isUnderlineText = props.isUnderline
                letterSpacing = props.letterSpacing / 10f
            }

            val fontMetrics = paint.fontMetrics
            val lineHeight = (fontMetrics.descent - fontMetrics.ascent) * props.lineSpacing
            val totalHeight = lines.size * lineHeight
            var startY = -totalHeight / 2f + (-fontMetrics.ascent)

            // Optional background box
            if (props.hasBackgroundBox) {
                var maxLineWidth = 0f
                for (line in lines) {
                    val w = paint.measureText(line)
                    if (w > maxLineWidth) maxLineWidth = w
                }
                val pad = props.backgroundBoxPadding * 2f
                val boxWidth = maxLineWidth + pad * 2f
                val boxHeight = totalHeight + pad
                val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = props.backgroundBoxColorArgb
                    alpha = ((Color(props.backgroundBoxColorArgb).alpha * opacity) * 255).toInt()
                }
                native.drawRoundRect(
                    -boxWidth / 2f,
                    -boxHeight / 2f,
                    boxWidth / 2f,
                    boxHeight / 2f,
                    props.backgroundBoxRadius,
                    props.backgroundBoxRadius,
                    bgPaint
                )
            }

            // 1. PS 3D 空间立体俯仰倾斜 (3D Spatial Tilt)
            val hasTilt = props.rotateX3D != 0f || props.rotateY3D != 0f
            if (hasTilt) {
                val camera = android.graphics.Camera()
                val matrix = android.graphics.Matrix()
                camera.save()
                camera.rotateX(-props.rotateX3D)
                camera.rotateY(props.rotateY3D)
                camera.getMatrix(matrix)
                camera.restore()
                native.save()
                native.concat(matrix)
            }

            // 2. PS 3D 地面投影 (Ground Cast Shadow)
            if (props.hasGroundShadow) {
                val groundAlpha = ((props.groundShadowOpacity * opacity) * 255).toInt().coerceIn(0, 255)
                val groundPaint = Paint(paint).apply {
                    color = android.graphics.Color.BLACK
                    alpha = groundAlpha
                    clearShadowLayer()
                    if (props.groundShadowBlur > 1f) {
                        maskFilter = android.graphics.BlurMaskFilter(props.groundShadowBlur, android.graphics.BlurMaskFilter.Blur.NORMAL)
                    }
                }
                native.save()
                val groundY = (totalHeight / 2f) + props.groundShadowDistance
                native.translate(0f, groundY)
                native.scale(1f, 0.24f)
                native.skew(-0.35f, 0f)
                var gY = -totalHeight / 2f
                for (line in lines) {
                    native.drawText(line, 0f, gY, groundPaint)
                    gY += lineHeight
                }
                native.restore()
            }

            // 3. PS 3D 挤出立体引擎 (Full Adobe Photoshop 3D Extrusion Engine)
            if (props.has3D) {
                val depth = props.depth3D.coerceIn(2f, 80f)
                val steps = depth.toInt()
                val angleRad = Math.toRadians(props.angle3D.toDouble())
                // 3D Direction Vector
                val dirX = Math.cos(angleRad).toFloat()
                val dirY = -Math.sin(angleRad).toFloat()
                val stepDist = 1.3f

                val baseCol = Color(props.colorArgb)
                val isNeon = props.material3D == "赛博霓虹"
                val isArcade = props.material3D == "复古街机"

                // Extrude from back layer to front
                for (d in steps downTo 1) {
                    val t = d.toFloat() / steps.toFloat() // 1.0 (furthest) -> 0.0 (near front)
                    val offX = dirX * d * stepDist
                    val offY = dirY * d * stepDist

                    // Extrusion Side Color blending
                    val stepCol = when {
                        isArcade -> {
                            // Alternating retro 3D bands
                            if ((d / 4) % 2 == 0) Color(0xFFFF2A6D) else Color(0xFF05FFA1)
                        }
                        isNeon -> {
                            Color(props.colorArgb).copy(alpha = (0.7f - t * 0.4f).coerceIn(0.1f, 1f))
                        }
                        props.gradientExtrusion -> {
                            val c1 = Color(props.colorArgb)
                            val c2 = Color(props.gradientExtrusionColor2Argb)
                            Color(
                                red = c1.red + (c2.red - c1.red) * t,
                                green = c1.green + (c2.green - c1.green) * t,
                                blue = c1.blue + (c2.blue - c1.blue) * t,
                                alpha = c1.alpha * opacity
                            )
                        }
                        props.customExtrusionColor -> {
                            val cSide = Color(props.extrusionColorArgb)
                            val shade = (1f - (props.darken3D * 0.7f) * t).coerceIn(0.15f, 1f)
                            Color(
                                red = cSide.red * shade,
                                green = cSide.green * shade,
                                blue = cSide.blue * shade,
                                alpha = cSide.alpha * opacity
                            )
                        }
                        else -> {
                            val darkFactor = (props.darken3D * t).coerceIn(0.1f, 0.9f)
                            Color(
                                red = baseCol.red * (1f - darkFactor),
                                green = baseCol.green * (1f - darkFactor),
                                blue = baseCol.blue * (1f - darkFactor),
                                alpha = baseCol.alpha * opacity
                            )
                        }
                    }

                    val stepPaint = Paint(paint).apply {
                        color = stepCol.hashCode()
                        clearShadowLayer()
                    }

                    if (props.isPerspective3D) {
                        val scaleD = 1f - t * (props.perspectiveAmount3D * 0.35f)
                        native.save()
                        native.translate(offX, offY)
                        native.scale(scaleD, scaleD)
                        var curY = startY
                        for (line in lines) {
                            native.drawText(line, 0f, curY, stepPaint)
                            curY += lineHeight
                        }
                        native.restore()
                    } else {
                        var curY = startY + offY
                        for (line in lines) {
                            native.drawText(line, offX, curY, stepPaint)
                            curY += lineHeight
                        }
                    }
                }
            }

            // 4. PS 3D 倒角与浮雕斜面 (Bevel & Emboss)
            if (props.hasBevel) {
                val bevelRad = Math.toRadians(props.bevelAngle.toDouble())
                val bhX = Math.cos(bevelRad).toFloat() * props.bevelWidth
                val bhY = -Math.sin(bevelRad).toFloat() * props.bevelWidth

                // Shadow bevel edge
                val bevelShadowPaint = Paint(paint).apply {
                    style = Paint.Style.STROKE
                    strokeWidth = props.bevelWidth * 1.5f
                    color = props.bevelShadowColorArgb
                    alpha = (160 * opacity).toInt()
                    clearShadowLayer()
                }
                var curY = startY - bhY
                for (line in lines) {
                    native.drawText(line, -bhX, curY, bevelShadowPaint)
                    curY += lineHeight
                }

                // Highlight bevel edge
                val bevelHlPaint = Paint(paint).apply {
                    style = Paint.Style.STROKE
                    strokeWidth = props.bevelWidth * 1.2f
                    color = props.bevelHighlightColorArgb
                    alpha = (210 * opacity).toInt()
                    clearShadowLayer()
                }
                curY = startY + bhY
                for (line in lines) {
                    native.drawText(line, bhX, curY, bevelHlPaint)
                    curY += lineHeight
                }
            }

            // Drop Shadow（v2.2：支持扩展范围 shadowSpread）
            if (props.hasShadow) {
                val spread = props.shadowSpread.coerceIn(0f, 100f)
                val spreadOffset = props.shadowRadius * (spread / 100f) * 0.6f
                // 扩展层：先画一层更柔和的外圈阴影（随后主 fill 覆盖中心）
                if (spread > 5f) {
                    val spreadPaint = Paint(paint).apply {
                        style = Paint.Style.FILL
                        color = props.colorArgb
                        alpha = 255
                        clearShadowLayer()
                        setShadowLayer(
                            props.shadowRadius * (1f + spread / 80f),
                            props.shadowDx,
                            props.shadowDy,
                            props.shadowColorArgb
                        )
                    }
                    var curY = startY
                    for (line in lines) { native.drawText(line, 0f, curY, spreadPaint); curY += lineHeight }
                }
                // 主阴影：设置在 paint 上，随 fill 一起绘制（保持透明度正确）
                paint.setShadowLayer(
                    props.shadowRadius,
                    props.shadowDx + spreadOffset,
                    props.shadowDy + spreadOffset,
                    props.shadowColorArgb
                )
            } else {
                paint.clearShadowLayer()
            }

            // Text Stroke（v2.2：支持内/居中/外三种位置，类似 PS 描边位置）
            if (props.hasStroke) {
                val strokeW = props.strokeWidth * 2f
                if (props.strokePosition == com.landesheji.data.model.StrokePosition.INNER) {
                    // 内描边：先用 fill 作 mask，再用 SRC_IN 把描边限制在文字内部
                    val bounds = android.graphics.RectF(
                        -baseFontSize * 1.5f, startY - baseFontSize,
                        baseFontSize * 1.5f, startY + totalHeight + baseFontSize
                    )
                    native.saveLayer(bounds, null, android.graphics.Canvas.ALL_SAVE_FLAG)
                    // mask：纯白 fill
                    val maskPaint = Paint(paint).apply {
                        style = Paint.Style.FILL
                        color = android.graphics.Color.WHITE
                        alpha = 255
                        clearShadowLayer()
                        shader = null
                    }
                    var curY = startY
                    for (line in lines) { native.drawText(line, 0f, curY, maskPaint); curY += lineHeight }
                    // SRC_IN 内描边
                    val innerStrokePaint = Paint(paint).apply {
                        style = Paint.Style.STROKE
                        strokeWidth = strokeW
                        color = props.strokeColorArgb
                        alpha = ((Color(props.strokeColorArgb).alpha * opacity) * 255).toInt()
                        clearShadowLayer()
                        xfermode = android.graphics.PorterDuffXfermode(android.graphics.PorterDuff.Mode.SRC_IN)
                    }
                    curY = startY
                    for (line in lines) { native.drawText(line, 0f, curY, innerStrokePaint); curY += lineHeight }
                    native.restore()
                } else if (props.strokePosition == com.landesheji.data.model.StrokePosition.CENTER) {
                    // 居中描边：先 fill 再 stroke（一半在内一半在外）
                    val centerStrokePaint = Paint(paint).apply {
                        style = Paint.Style.STROKE
                        strokeWidth = strokeW
                        color = props.strokeColorArgb
                        alpha = ((Color(props.strokeColorArgb).alpha * opacity) * 255).toInt()
                        clearShadowLayer()
                    }
                    var curY = startY
                    for (line in lines) {
                        native.drawText(line, 0f, curY, centerStrokePaint)
                        curY += lineHeight
                    }
                } else {
                    // 外描边：先描边后填充（现有逻辑）
                    val strokePaint = Paint(paint).apply {
                        style = Paint.Style.STROKE
                        strokeWidth = strokeW
                        color = props.strokeColorArgb
                        alpha = ((Color(props.strokeColorArgb).alpha * opacity) * 255).toInt()
                        clearShadowLayer()
                    }
                    var curY = startY
                    for (line in lines) {
                        native.drawText(line, 0f, curY, strokePaint)
                        curY += lineHeight
                    }
                }
            }

            // PS FX：外发光 (Glow) —— 在文字下方叠多层模糊光晕（v2.2：支持强度 glowOpacity）
            if (props.hasGlow) {
                val glowAlphaMul = (props.glowOpacity * opacity).coerceIn(0f, 1f)
                val glowPaint = Paint(paint).apply {
                    style = Paint.Style.FILL
                    color = props.glowColorArgb
                    clearShadowLayer()
                    maskFilter = android.graphics.BlurMaskFilter(
                        props.glowRadius * 0.8f,
                        android.graphics.BlurMaskFilter.Blur.NORMAL
                    )
                    alpha = (glowAlphaMul * 255).toInt()
                }
                var curY = startY
                for (line in lines) {
                    native.drawText(line, 0f, curY, glowPaint)
                    curY += lineHeight
                }
                // 更强光晕第二层
                val glowPaint2 = Paint(glowPaint).apply {
                    alpha = (150 * glowAlphaMul).toInt().coerceIn(0, 255)
                    maskFilter = android.graphics.BlurMaskFilter(
                        props.glowRadius * 1.6f,
                        android.graphics.BlurMaskFilter.Blur.NORMAL
                    )
                }
                curY = startY
                for (line in lines) {
                    native.drawText(line, 0f, curY, glowPaint2)
                    curY += lineHeight
                }
            }

            // Foreground Text Fill（PS FX：渐变叠加支持）
            paint.style = Paint.Style.FILL
            paint.color = props.colorArgb
            paint.alpha = ((Color(props.colorArgb).alpha * opacity) * 255).toInt()

            // PS FX：渐变叠加 (Gradient Overlay) —— 用 LinearGradient Shader 覆盖文字填充
            var restoreShaderAfter: Paint? = null
            if (props.hasGradientOverlay) {
                val gradSh = android.graphics.LinearGradient(
                    0f, -baseFontSize * 0.6f, 0f, baseFontSize * 1.2f,
                    props.colorArgb,
                    props.gradientOverlayColor2Argb,
                    android.graphics.Shader.TileMode.CLAMP
                )
                paint.shader = gradSh
            } else if (props.hasTextureFill) {
                // PS FX：纹理填充 —— 依据 textureIndex 生成纹理 Shader
                val tex = createTextureShader(
                    baseFontSize = baseFontSize,
                    index = props.textureIndex,
                    tintArgb = props.textureColorArgb
                )
                if (tex != null) paint.shader = tex
                restoreShaderAfter = paint
            }

            var curY = startY
            for (line in lines) {
                native.drawText(line, 0f, curY, paint)
                curY += lineHeight
            }
            paint.shader = null

            // ===== v2.2：PS 图层样式增强（内阴影/内发光/颜色叠加，用 mask+SRC_IN 实现） =====
            val textBounds = android.graphics.RectF(
                -baseFontSize * 1.6f, startY - baseFontSize,
                baseFontSize * 1.6f, startY + totalHeight + baseFontSize
            )
            if (props.hasInnerShadow || props.hasInnerGlow || props.hasColorOverlay) {
                native.saveLayer(textBounds, null, android.graphics.Canvas.ALL_SAVE_FLAG)
                // 文字本体作 mask（纯白填充）
                val maskPaint = Paint(paint).apply {
                    style = Paint.Style.FILL
                    color = android.graphics.Color.WHITE
                    alpha = 255
                    clearShadowLayer()
                    shader = null
                    xfermode = null
                }
                var mY = startY
                for (line in lines) { native.drawText(line, 0f, mY, maskPaint); mY += lineHeight }

                // 内阴影：偏移 + 模糊，SRC_IN 只显示在文字内部
                if (props.hasInnerShadow) {
                    val isp = Paint(paint).apply {
                        style = Paint.Style.FILL
                        color = props.innerShadowColorArgb
                        alpha = (props.innerShadowOpacity * 255).toInt().coerceIn(0, 255)
                        clearShadowLayer()
                        maskFilter = android.graphics.BlurMaskFilter(
                            props.innerShadowRadius,
                            android.graphics.BlurMaskFilter.Blur.NORMAL
                        )
                        shader = null
                        xfermode = android.graphics.PorterDuffXfermode(android.graphics.PorterDuff.Mode.SRC_IN)
                    }
                    var iY = startY + props.innerShadowDy
                    for (line in lines) {
                        native.drawText(line, props.innerShadowDx, iY, isp)
                        iY += lineHeight
                    }
                }

                // 内发光：模糊亮色，SRC_IN 只显示在文字内部边缘
                if (props.hasInnerGlow) {
                    val igp = Paint(paint).apply {
                        style = Paint.Style.FILL
                        color = props.innerGlowColorArgb
                        alpha = (props.innerGlowOpacity * 255).toInt().coerceIn(0, 255)
                        clearShadowLayer()
                        maskFilter = android.graphics.BlurMaskFilter(
                            props.innerGlowRadius,
                            android.graphics.BlurMaskFilter.Blur.NORMAL
                        )
                        shader = null
                        xfermode = android.graphics.PorterDuffXfermode(android.graphics.PorterDuff.Mode.SRC_IN)
                    }
                    var gY = startY
                    for (line in lines) { native.drawText(line, 0f, gY, igp); gY += lineHeight }
                }

                // 颜色叠加：纯色覆盖（SRC_IN 限制在文字内）
                if (props.hasColorOverlay) {
                    val cop = Paint(paint).apply {
                        style = Paint.Style.FILL
                        color = props.colorOverlayColorArgb
                        alpha = (props.colorOverlayOpacity * 255).toInt().coerceIn(0, 255)
                        clearShadowLayer()
                        shader = null
                        xfermode = android.graphics.PorterDuffXfermode(android.graphics.PorterDuff.Mode.SRC_IN)
                    }
                    var cY = startY
                    for (line in lines) { native.drawText(line, 0f, cY, cop); cY += lineHeight }
                }
                native.restore()
            }

            // 5. PS 3D 材质质感效果 (Surface Material Shaders)
            when (props.material3D) {
                "高光光泽", "GLOSS" -> {
                    // Curved gloss reflection arc across top of letters
                    val glossPaint = Paint(paint).apply {
                        style = Paint.Style.STROKE
                        strokeWidth = (baseFontSize * 0.09f).coerceAtLeast(2f)
                        color = android.graphics.Color.WHITE
                        alpha = (180 * opacity).toInt()
                        clearShadowLayer()
                    }
                    var gY = startY - baseFontSize * 0.12f
                    for (line in lines) {
                        native.drawText(line, -2f, gY, glossPaint)
                        gY += lineHeight
                    }
                }
                "可爱果冻", "JELLY" -> {
                    // Kawaii sweet candy gloss bubble
                    val jellyPaint = Paint(paint).apply {
                        style = Paint.Style.FILL
                        color = android.graphics.Color.WHITE
                        alpha = (220 * opacity).toInt()
                        clearShadowLayer()
                    }
                    var jY = startY - baseFontSize * 0.18f
                    for (line in lines) {
                        native.drawText(line, -3f, jY, jellyPaint)
                        jY += lineHeight
                    }
                }
                "充气气球", "INFLATABLE" -> {
                    val balloonPaint = Paint(paint).apply {
                        style = Paint.Style.STROKE
                        strokeWidth = (baseFontSize * 0.15f).coerceAtLeast(3f)
                        color = android.graphics.Color.WHITE
                        alpha = (140 * opacity).toInt()
                        clearShadowLayer()
                    }
                    var bY = startY - baseFontSize * 0.06f
                    for (line in lines) {
                        native.drawText(line, -1f, bY, balloonPaint)
                        bY += lineHeight
                    }
                }
                // ====== 以下为需求2补充的 PS 风格立体字效 ======
                "金属质感" -> {
                    // Chrome 镜面高光：上下两道亮线
                    val chromeHi = Paint(paint).apply {
                        style = Paint.Style.STROKE
                        strokeWidth = (baseFontSize * 0.06f).coerceAtLeast(2f)
                        color = android.graphics.Color.WHITE
                        alpha = (230 * opacity).toInt()
                        clearShadowLayer()
                    }
                    val chromeLo = Paint(paint).apply {
                        style = Paint.Style.STROKE
                        strokeWidth = (baseFontSize * 0.04f).coerceAtLeast(1.5f)
                        color = android.graphics.Color.WHITE
                        alpha = (120 * opacity).toInt()
                        clearShadowLayer()
                    }
                    var mY = startY - baseFontSize * 0.22f
                    for (line in lines) {
                        native.drawText(line, 0f, mY, chromeHi)
                        mY += lineHeight
                    }
                    mY = startY + baseFontSize * 0.18f
                    for (line in lines) {
                        native.drawText(line, 0f, mY, chromeLo)
                        mY += lineHeight
                    }
                }
                "水晶冰凌" -> {
                    // Crystal：冷白描边 + 双层亮边
                    val crystalOuter = Paint(paint).apply {
                        style = Paint.Style.STROKE
                        strokeWidth = (baseFontSize * 0.10f).coerceAtLeast(3f)
                        color = android.graphics.Color.WHITE
                        alpha = (200 * opacity).toInt()
                        clearShadowLayer()
                    }
                    var cY = startY - baseFontSize * 0.05f
                    for (line in lines) {
                        native.drawText(line, 2f, cY, crystalOuter)
                        cY += lineHeight
                    }
                }
                "糖果彩虹" -> {
                    // 多次错位上色产生彩色描边
                    val candyColors = listOf(
                        0xFFFF6B9D.toInt() to -2f,
                        0xFFFFC857.toInt() to -1f,
                        0xFF7BD389.toInt() to 1f,
                        0xFF4FC3F7.toInt() to 2f
                    )
                    for ((cc, dx) in candyColors) {
                        val cp = Paint(paint).apply {
                            style = Paint.Style.STROKE
                            strokeWidth = (baseFontSize * 0.05f).coerceAtLeast(1.5f)
                            color = cc
                            alpha = (180 * opacity).toInt()
                            clearShadowLayer()
                        }
                        var y = startY
                        for (line in lines) {
                            native.drawText(line, dx, y, cp)
                            y += lineHeight
                        }
                    }
                }
                "火焰光焰" -> {
                    // 火焰：上下红橙双层亮边
                    val fireHot = Paint(paint).apply {
                        style = Paint.Style.STROKE
                        strokeWidth = (baseFontSize * 0.12f).coerceAtLeast(4f)
                        color = 0xFFFF4500.toInt()
                        alpha = (160 * opacity).toInt()
                        clearShadowLayer()
                    }
                    val fireWarm = Paint(paint).apply {
                        style = Paint.Style.STROKE
                        strokeWidth = (baseFontSize * 0.07f).coerceAtLeast(2.5f)
                        color = 0xFFFFD700.toInt()
                        alpha = (200 * opacity).toInt()
                        clearShadowLayer()
                    }
                    var fY = startY
                    for (line in lines) {
                        native.drawText(line, -1f, fY, fireHot)
                        fY += lineHeight
                    }
                    fY = startY
                    for (line in lines) {
                        native.drawText(line, 1f, fY + 1f, fireWarm)
                        fY += lineHeight
                    }
                }
                "糖果霓虹" -> {
                    // 双色错位描边，粉紫渐变
                    val neonPink = Paint(paint).apply {
                        style = Paint.Style.STROKE
                        strokeWidth = (baseFontSize * 0.08f).coerceAtLeast(2.5f)
                        color = 0xFFFF1493.toInt()
                        alpha = (200 * opacity).toInt()
                        clearShadowLayer()
                    }
                    val neonPurple = Paint(paint).apply {
                        style = Paint.Style.STROKE
                        strokeWidth = (baseFontSize * 0.08f).coerceAtLeast(2.5f)
                        color = 0xFF9D4EDD.toInt()
                        alpha = (200 * opacity).toInt()
                        clearShadowLayer()
                    }
                    var nY = startY
                    for (line in lines) {
                        native.drawText(line, -1.5f, nY, neonPink)
                        native.drawText(line, 1.5f, nY, neonPurple)
                        nY += lineHeight
                    }
                }
                "玻璃琉璃" -> {
                    // Glass：多重透明叠加产生玻璃质感
                    val glassTop = Paint(paint).apply {
                        style = Paint.Style.STROKE
                        strokeWidth = (baseFontSize * 0.14f).coerceAtLeast(4f)
                        color = 0xFFFFFFFF.toInt()
                        alpha = (110 * opacity).toInt()
                        clearShadowLayer()
                    }
                    var gY = startY - baseFontSize * 0.05f
                    for (line in lines) {
                        native.drawText(line, 0f, gY, glassTop)
                        gY += lineHeight
                    }
                }
                "卡通厚涂" -> {
                    // 卡通描边 + 高亮填充
                    val cartoonOuter = Paint(paint).apply {
                        style = Paint.Style.STROKE
                        strokeWidth = (baseFontSize * 0.18f).coerceAtLeast(5f)
                        color = props.colorArgb
                        alpha = ((Color(props.colorArgb).alpha * opacity) * 255).toInt()
                        clearShadowLayer()
                    }
                    var oY = startY - baseFontSize * 0.02f
                    for (line in lines) {
                        native.drawText(line, 0f, oY, cartoonOuter)
                        oY += lineHeight
                    }
                }
            }

            // 6. 3D Tilt Restore
            if (hasTilt) {
                native.restore()
            }

            // Reflection Effect
            if (props.hasReflection) {
                val refPaint = Paint(paint).apply {
                    clearShadowLayer()
                    alpha = (props.reflectionAlpha * opacity * 255).toInt().coerceIn(0, 255)
                }
                val refStartY = (totalHeight / 2f) + props.reflectionDistance * 2f
                native.save()
                native.scale(1f, -0.6f, 0f, refStartY)
                var refY = refStartY
                for (line in lines) {
                    native.drawText(line, 0f, refY, refPaint)
                    refY += lineHeight
                }
                native.restore()
            }
        }
    }

    private fun drawShapeLayer(
        drawScope: DrawScope,
        props: ShapeProperties,
        opacity: Float
    ) {
        val size = 160f
        val half = size / 2f
        val fillColor = Color(props.fillColorArgb).copy(alpha = Color(props.fillColorArgb).alpha * opacity)
        val strokeColor = Color(props.strokeColorArgb).copy(alpha = Color(props.strokeColorArgb).alpha * opacity)

        when (props.shapeType) {
            ShapeType.RECTANGLE -> {
                drawScope.drawRect(
                    color = fillColor,
                    topLeft = Offset(-half, -half),
                    size = Size(size, size)
                )
                if (props.hasStroke) {
                    drawScope.drawRect(
                        color = strokeColor,
                        topLeft = Offset(-half, -half),
                        size = Size(size, size),
                        style = Stroke(width = props.strokeWidth * 2f)
                    )
                }
            }
            ShapeType.ROUNDED_RECTANGLE -> {
                val r = androidx.compose.ui.geometry.CornerRadius(props.cornerRadius * 1.5f)
                drawScope.drawRoundRect(
                    color = fillColor,
                    topLeft = Offset(-half, -half),
                    size = Size(size, size),
                    cornerRadius = r
                )
                if (props.hasStroke) {
                    drawScope.drawRoundRect(
                        color = strokeColor,
                        topLeft = Offset(-half, -half),
                        size = Size(size, size),
                        cornerRadius = r,
                        style = Stroke(width = props.strokeWidth * 2f)
                    )
                }
            }
            ShapeType.CIRCLE -> {
                drawScope.drawCircle(
                    color = fillColor,
                    radius = half,
                    center = Offset.Zero
                )
                if (props.hasStroke) {
                    drawScope.drawCircle(
                        color = strokeColor,
                        radius = half,
                        center = Offset.Zero,
                        style = Stroke(width = props.strokeWidth * 2f)
                    )
                }
            }
            ShapeType.STAR -> {
                val path = createStarPath(half, half * 0.45f, 5)
                drawScope.drawPath(path, color = fillColor, style = Fill)
                if (props.hasStroke) {
                    drawScope.drawPath(path, color = strokeColor, style = Stroke(width = props.strokeWidth * 2f))
                }
            }
            ShapeType.HEART -> {
                val path = createHeartPath(size)
                drawScope.drawPath(path, color = fillColor, style = Fill)
                if (props.hasStroke) {
                    drawScope.drawPath(path, color = strokeColor, style = Stroke(width = props.strokeWidth * 2f))
                }
            }
            ShapeType.TRIANGLE -> {
                val path = Path().apply {
                    moveTo(0f, -half)
                    lineTo(half, half)
                    lineTo(-half, half)
                    close()
                }
                drawScope.drawPath(path, color = fillColor, style = Fill)
                if (props.hasStroke) {
                    drawScope.drawPath(path, color = strokeColor, style = Stroke(width = props.strokeWidth * 2f))
                }
            }
            ShapeType.HEXAGON -> {
                val path = createPolygonPath(half, 6)
                drawScope.drawPath(path, color = fillColor, style = Fill)
                if (props.hasStroke) {
                    drawScope.drawPath(path, color = strokeColor, style = Stroke(width = props.strokeWidth * 2f))
                }
            }
            ShapeType.ARROW -> {
                val path = createArrowPath(half)
                drawScope.drawPath(path, color = fillColor, style = Fill)
                if (props.hasStroke) {
                    drawScope.drawPath(path, color = strokeColor, style = Stroke(width = props.strokeWidth * 2f))
                }
            }
        }
    }

    private fun drawStickerLayer(
        drawScope: DrawScope,
        layer: LayerItem,
        opacity: Float
    ) {
        drawScope.drawIntoCanvas { canvas ->
            val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                textSize = 90f
                textAlign = Paint.Align.CENTER
                alpha = (opacity * 255).toInt()
                // v2.2：贴纸染色
                if (layer.stickerProps.hasTint) {
                    colorFilter = android.graphics.PorterDuffColorFilter(
                        layer.stickerProps.tintArgb,
                        android.graphics.PorterDuff.Mode.SRC_IN
                    )
                }
            }
            val text = layer.stickerProps.stickerName
            val bounds = Rect()
            paint.getTextBounds(text, 0, text.length, bounds)
            val yOffset = bounds.height() / 2f
            canvas.nativeCanvas.drawText(text, 0f, yOffset, paint)
        }
    }

    private fun drawImageLayer(
        drawScope: DrawScope,
        layer: LayerItem,
        opacity: Float,
        bitmap: Bitmap? = null
    ) {
        // 需求6：优先绘制真实位图（本地导入的图片/自定义贴纸）
        if (bitmap != null) {
            val width = 160f
            val aspect = layer.imageProps.aspectRatio.coerceAtLeast(0.1f)
            val height = width / aspect
            drawScope.drawRoundRect(
                color = Color(0xFF334155).copy(alpha = opacity),
                topLeft = Offset(-width / 2f, -height / 2f),
                size = Size(width, height),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(layer.imageProps.cornerRadius)
            )
            drawScope.drawIntoCanvas { canvas ->
                val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    alpha = (opacity * 255).toInt().coerceIn(0, 255)
                }
                // 按目标区域绘制，保持图片长宽比
                val bmpW = bitmap.width.toFloat()
                val bmpH = bitmap.height.toFloat()
                val scale = kotlin.math.min(width / bmpW, height / bmpH)
                val drawW = bmpW * scale
                val drawH = bmpH * scale
                val left = -drawW / 2f
                val top = -drawH / 2f
                val src = Rect(0, 0, bitmap.width, bitmap.height)
                val dst = Rect(
                    left.toInt(), top.toInt(),
                    (left + drawW).toInt(), (top + drawH).toInt()
                )
                canvas.nativeCanvas.drawBitmap(bitmap, src, dst, paint)
            }
            // 边框
            drawScope.drawRoundRect(
                color = Color(layer.imageProps.borderColorArgb).copy(alpha = opacity),
                topLeft = Offset(-width / 2f, -height / 2f),
                size = Size(width, height),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(layer.imageProps.cornerRadius),
                style = Stroke(width = 3f)
            )
            return
        }
        // Fallback placeholder card for images if not loaded via bitmap
        val width = 160f
        val height = 160f / layer.imageProps.aspectRatio.coerceAtLeast(0.1f)
        drawScope.drawRoundRect(
            color = Color(0xFF334155).copy(alpha = opacity),
            topLeft = Offset(-width / 2f, -height / 2f),
            size = Size(width, height),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(layer.imageProps.cornerRadius)
        )
        // Draw frame border
        drawScope.drawRoundRect(
            color = Color(layer.imageProps.borderColorArgb).copy(alpha = opacity),
            topLeft = Offset(-width / 2f, -height / 2f),
            size = Size(width, height),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(layer.imageProps.cornerRadius),
            style = Stroke(width = 3f)
        )
        // Icon in center
        drawScope.drawIntoCanvas { canvas ->
            val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                textSize = 44f
                textAlign = Paint.Align.CENTER
                alpha = (opacity * 255).toInt()
            }
            canvas.nativeCanvas.drawText("🖼️", 0f, 15f, paint)
        }
    }

    private fun drawDrawLayer(
        drawScope: DrawScope,
        strokes: List<DrawStroke>,
        opacity: Float
    ) {
        for (stroke in strokes) {
            if (stroke.points.size < 2) continue
            val path = Path().apply {
                moveTo(stroke.points[0].x, stroke.points[0].y)
                for (i in 1 until stroke.points.size) {
                    val p = stroke.points[i]
                    lineTo(p.x, p.y)
                }
            }
            drawScope.drawPath(
                path = path,
                color = if (stroke.isEraser) Color.Transparent else Color(stroke.colorArgb).copy(alpha = opacity),
                style = Stroke(width = stroke.strokeWidth, miter = 4f)
            )
        }
    }

    // PS FX：纹理填充 Shader（斜纹/点阵/棋盘/波浪）
    private fun createTextureShader(
        baseFontSize: Float,
        index: Int,
        tintArgb: Int
    ): android.graphics.Shader? {
        val cell = (baseFontSize * 0.18f).coerceAtLeast(12f)
        val bmp = android.graphics.Bitmap.createBitmap(
            cell.toInt(), cell.toInt(),
            android.graphics.Bitmap.Config.ARGB_8888
        )
        val c = android.graphics.Canvas(bmp)
        val fill = android.graphics.Paint().apply { color = tintArgb }
        val bg = android.graphics.Paint().apply { color = applyAlpha(tintArgb, 0x33) }
        c.drawRect(0f, 0f, cell, cell, bg)
        when (index % 4) {
            0 -> { // 斜纹
                val line = android.graphics.Paint().apply { color = tintArgb; strokeWidth = cell * 0.22f }
                c.drawLine(0f, cell, cell, 0f, line)
            }
            1 -> { // 点阵
                c.drawCircle(cell * 0.5f, cell * 0.5f, cell * 0.3f, fill)
            }
            2 -> { // 棋盘
                c.drawRect(0f, 0f, cell * 0.5f, cell * 0.5f, fill)
                c.drawRect(cell * 0.5f, cell * 0.5f, cell, cell, fill)
            }
            else -> { // 波浪圆环
                c.drawCircle(cell * 0.5f, cell * 0.5f, cell * 0.42f, android.graphics.Paint().apply {
                    color = tintArgb
                    style = android.graphics.Paint.Style.STROKE
                    strokeWidth = cell * 0.18f
                })
            }
        }
        return android.graphics.BitmapShader(bmp, android.graphics.Shader.TileMode.REPEAT, android.graphics.Shader.TileMode.REPEAT)
    }

    private fun applyAlpha(argb: Int, alphaFactor: Int): Int {
        val a = (android.graphics.Color.alpha(argb) * alphaFactor) / 0xFF
        return (a shl 24) or (argb and 0x00FFFFFF)
    }

    private fun createStarPath(outerRadius: Float, innerRadius: Float, points: Int): Path {
        val path = Path()
        val step = PI / points
        var angle = -PI / 2.0
        path.moveTo(
            (outerRadius * cos(angle)).toFloat(),
            (outerRadius * sin(angle)).toFloat()
        )
        for (i in 0 until points * 2) {
            val r = if (i % 2 == 0) innerRadius else outerRadius
            angle += step
            path.lineTo(
                (r * cos(angle)).toFloat(),
                (r * sin(angle)).toFloat()
            )
        }
        path.close()
        return path
    }

    private fun createHeartPath(size: Float): Path {
        val path = Path()
        val s = size / 2f
        path.moveTo(0f, s * 0.7f)
        path.cubicTo(-s * 1.1f, -s * 0.1f, -s * 0.9f, -s * 0.9f, 0f, -s * 0.35f)
        path.cubicTo(s * 0.9f, -s * 0.9f, s * 1.1f, -s * 0.1f, 0f, s * 0.7f)
        path.close()
        return path
    }

    private fun createPolygonPath(radius: Float, sides: Int): Path {
        val path = Path()
        val angleStep = 2 * PI / sides
        var angle = -PI / 2.0
        path.moveTo((radius * cos(angle)).toFloat(), (radius * sin(angle)).toFloat())
        for (i in 1 until sides) {
            angle += angleStep
            path.lineTo((radius * cos(angle)).toFloat(), (radius * sin(angle)).toFloat())
        }
        path.close()
        return path
    }

    private fun createArrowPath(size: Float): Path {
        val path = Path()
        val s = size
        path.moveTo(-s * 0.8f, -s * 0.3f)
        path.lineTo(0f, -s * 0.3f)
        path.lineTo(0f, -s * 0.6f)
        path.lineTo(s * 0.8f, 0f)
        path.lineTo(0f, s * 0.6f)
        path.lineTo(0f, s * 0.3f)
        path.lineTo(-s * 0.8f, s * 0.3f)
        path.close()
        return path
    }
}
