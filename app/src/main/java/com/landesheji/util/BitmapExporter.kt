package com.landesheji.util

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.graphics.Shader
import android.graphics.Typeface
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import com.landesheji.data.model.BackgroundType
import com.landesheji.data.model.CanvasConfig
import com.landesheji.data.model.LayerItem
import com.landesheji.data.model.LayerType
import com.landesheji.data.model.ShapeType
import com.landesheji.data.model.TextAlignment
import com.landesheji.ui.dialogs.ExportFormat
import com.landesheji.ui.dialogs.ExportQuality
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

object BitmapExporter {

    suspend fun generateBitmap(
        config: CanvasConfig,
        layers: List<LayerItem>,
        quality: ExportQuality,
        targetWidth: Int = 0,
        targetHeight: Int = 0,
        previewCanvasWidthPx: Float = 380f
    ): Bitmap = withContext(Dispatchers.Default) {
        // 需求5：支持按外部指定比例导出（居中裁剪），否则按画布尺寸
        val width = if (targetWidth > 0) targetWidth else (config.width * quality.multiplier).toInt().coerceIn(200, 4096)
        val height = if (targetHeight > 0) targetHeight else (config.height * quality.multiplier).toInt().coerceIn(200, 4096)

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // 1. Draw Background
        when (config.backgroundType) {
            BackgroundType.TRANSPARENT -> {
                // Keep transparent
            }
            BackgroundType.COLOR, BackgroundType.PRESET_TEXTURE, BackgroundType.IMAGE_URI, BackgroundType.VIDEO_URI -> {
                canvas.drawColor(config.backgroundColorArgb)
            }
            BackgroundType.GRADIENT -> {
                val shader = LinearGradient(
                    0f, 0f, width.toFloat(), height.toFloat(),
                    config.backgroundColorArgb,
                    config.backgroundGradientColor2Argb,
                    Shader.TileMode.CLAMP
                )
                val paint = Paint().apply { this.shader = shader }
                canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
            }
        }

        // Vignette
        if (config.vignette > 0f) {
            val centerPaint = Paint().apply {
                color = Color.BLACK
                alpha = (config.vignette * 180).toInt().coerceIn(0, 255)
            }
            // Simple edge shading
            canvas.drawRect(0f, 0f, width.toFloat(), height * 0.05f, centerPaint)
            canvas.drawRect(0f, height * 0.95f, width.toFloat(), height.toFloat(), centerPaint)
        }

        // 2. Scale factor: 画布等比映射到导出位图（目标比例下以画布中心裁剪，内容不拉伸）
        // 需求4修复：除以“实际预览画布宽度”，而非硬编码 380 —— 否则导出文字会被错误放大导致溢出/显示不全
        val previewCanvasW = previewCanvasWidthPx.coerceAtLeast(1f)
        val previewScale = width.toFloat() / previewCanvasW
        val centerX = width / 2f
        val centerY = height / 2f
        // 注意：ExportDialog 已根据所选比例计算出目标宽高（裁剪模式），
        // 这里以目标宽为基准缩放，中心对齐画布，超出的上下/左右部分自然被裁掉。

        // 3. Draw layers
        layers.forEach { layer ->
            if (!layer.isVisible) return@forEach

            canvas.save()
            val layerX = centerX + (layer.x * previewScale)
            val layerY = centerY + (layer.y * previewScale)
            canvas.translate(layerX, layerY)
            canvas.rotate(layer.rotation)
            canvas.scale(layer.scaleX * previewScale, layer.scaleY * previewScale)

            when (layer.type) {
                LayerType.TEXT -> {
                    val p = layer.textProps
                    val baseFontSize = p.fontSize * 1.5f
                    val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                        textSize = baseFontSize
                        textAlign = when (p.alignment) {
                            TextAlignment.LEFT -> Paint.Align.LEFT
                            TextAlignment.CENTER -> Paint.Align.CENTER
                            TextAlignment.RIGHT -> Paint.Align.RIGHT
                        }
                        typeface = when {
                            p.isBold && p.isItalic -> Typeface.create(Typeface.DEFAULT, Typeface.BOLD_ITALIC)
                            p.isBold -> Typeface.DEFAULT_BOLD
                            p.isItalic -> Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
                            else -> Typeface.DEFAULT
                        }
                        isUnderlineText = p.isUnderline
                        color = p.colorArgb
                        alpha = (androidx.compose.ui.graphics.Color(p.colorArgb).alpha * layer.opacity * 255).toInt()
                    }

                    val lines = p.text.split("\n")
                    val fontMetrics = paint.fontMetrics
                    val lineHeight = (fontMetrics.descent - fontMetrics.ascent) * p.lineSpacing
                    val totalH = lines.size * lineHeight
                    var curY = -totalH / 2f + (-fontMetrics.ascent)

                    // Background Box
                    if (p.hasBackgroundBox) {
                        var maxW = 0f
                        for (l in lines) {
                            val w = paint.measureText(l)
                            if (w > maxW) maxW = w
                        }
                        val pad = p.backgroundBoxPadding * 2f
                        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                            color = p.backgroundBoxColorArgb
                            alpha = (androidx.compose.ui.graphics.Color(p.backgroundBoxColorArgb).alpha * layer.opacity * 255).toInt()
                        }
                        canvas.drawRoundRect(
                            -maxW / 2f - pad,
                            -totalH / 2f - pad / 2f,
                            maxW / 2f + pad,
                            totalH / 2f + pad / 2f,
                            p.backgroundBoxRadius,
                            p.backgroundBoxRadius,
                            bgPaint
                        )
                    }

                    // 1. PS 3D Tilt Spatial
                    val hasTilt = p.rotateX3D != 0f || p.rotateY3D != 0f
                    if (hasTilt) {
                        val camera = android.graphics.Camera()
                        val m = android.graphics.Matrix()
                        camera.save()
                        camera.rotateX(-p.rotateX3D)
                        camera.rotateY(p.rotateY3D)
                        camera.getMatrix(m)
                        camera.restore()
                        canvas.save()
                        canvas.concat(m)
                    }

                    // 2. PS 3D Ground Shadow
                    if (p.hasGroundShadow) {
                        val gAlpha = ((p.groundShadowOpacity * layer.opacity) * 255).toInt().coerceIn(0, 255)
                        val gPaint = Paint(paint).apply {
                            color = Color.BLACK
                            alpha = gAlpha
                            clearShadowLayer()
                            if (p.groundShadowBlur > 1f) {
                                maskFilter = android.graphics.BlurMaskFilter(p.groundShadowBlur * previewScale, android.graphics.BlurMaskFilter.Blur.NORMAL)
                            }
                        }
                        canvas.save()
                        val groundY = (totalH / 2f) + (p.groundShadowDistance * previewScale)
                        canvas.translate(0f, groundY)
                        canvas.scale(1f, 0.24f)
                        canvas.skew(-0.35f, 0f)
                        var gCurY = -totalH / 2f
                        for (line in lines) {
                            canvas.drawText(line, 0f, gCurY, gPaint)
                            gCurY += lineHeight
                        }
                        canvas.restore()
                    }

                    // 3. PS 3D Extrusion
                    if (p.has3D) {
                        val depth = (p.depth3D * previewScale).coerceIn(2f, 120f)
                        val steps = depth.toInt()
                        val angleRad = Math.toRadians(p.angle3D.toDouble())
                        val dirX = Math.cos(angleRad).toFloat()
                        val dirY = -Math.sin(angleRad).toFloat()
                        val stepDist = 1.3f

                        val baseC = androidx.compose.ui.graphics.Color(p.colorArgb)
                        for (d in steps downTo 1) {
                            val t = d.toFloat() / steps.toFloat()
                            val offX = dirX * d * stepDist
                            val offY = dirY * d * stepDist

                            val stepCol = when {
                                p.material3D == "复古街机" -> {
                                    if ((d / 4) % 2 == 0) androidx.compose.ui.graphics.Color(0xFFFF2A6D) else androidx.compose.ui.graphics.Color(0xFF05FFA1)
                                }
                                p.gradientExtrusion -> {
                                    val c1 = androidx.compose.ui.graphics.Color(p.colorArgb)
                                    val c2 = androidx.compose.ui.graphics.Color(p.gradientExtrusionColor2Argb)
                                    androidx.compose.ui.graphics.Color(
                                        red = c1.red + (c2.red - c1.red) * t,
                                        green = c1.green + (c2.green - c1.green) * t,
                                        blue = c1.blue + (c2.blue - c1.blue) * t,
                                        alpha = c1.alpha * layer.opacity
                                    )
                                }
                                p.customExtrusionColor -> {
                                    val cSide = androidx.compose.ui.graphics.Color(p.extrusionColorArgb)
                                    val shade = (1f - (p.darken3D * 0.7f) * t).coerceIn(0.15f, 1f)
                                    androidx.compose.ui.graphics.Color(
                                        red = cSide.red * shade,
                                        green = cSide.green * shade,
                                        blue = cSide.blue * shade,
                                        alpha = cSide.alpha * layer.opacity
                                    )
                                }
                                else -> {
                                    val darkFactor = (p.darken3D * t).coerceIn(0.1f, 0.9f)
                                    androidx.compose.ui.graphics.Color(
                                        red = baseC.red * (1f - darkFactor),
                                        green = baseC.green * (1f - darkFactor),
                                        blue = baseC.blue * (1f - darkFactor),
                                        alpha = baseC.alpha * layer.opacity
                                    )
                                }
                            }

                            val stepPaint = Paint(paint).apply {
                                color = stepCol.hashCode()
                                clearShadowLayer()
                            }

                            if (p.isPerspective3D) {
                                val scaleD = 1f - t * (p.perspectiveAmount3D * 0.35f)
                                canvas.save()
                                canvas.translate(offX, offY)
                                canvas.scale(scaleD, scaleD)
                                var cY = curY
                                for (line in lines) {
                                    canvas.drawText(line, 0f, cY, stepPaint)
                                    cY += lineHeight
                                }
                                canvas.restore()
                            } else {
                                var cY = curY + offY
                                for (line in lines) {
                                    canvas.drawText(line, offX, cY, stepPaint)
                                    cY += lineHeight
                                }
                            }
                        }
                    }

                    // 4. PS 3D Bevel & Emboss
                    if (p.hasBevel) {
                        val bevelRad = Math.toRadians(p.bevelAngle.toDouble())
                        val bhX = Math.cos(bevelRad).toFloat() * p.bevelWidth * previewScale
                        val bhY = -Math.sin(bevelRad).toFloat() * p.bevelWidth * previewScale

                        val bevelShadowPaint = Paint(paint).apply {
                            style = Paint.Style.STROKE
                            strokeWidth = p.bevelWidth * 1.5f * previewScale
                            color = p.bevelShadowColorArgb
                            alpha = (160 * layer.opacity).toInt()
                            clearShadowLayer()
                        }
                        var cY = curY - bhY
                        for (line in lines) {
                            canvas.drawText(line, -bhX, cY, bevelShadowPaint)
                            cY += lineHeight
                        }

                        val bevelHlPaint = Paint(paint).apply {
                            style = Paint.Style.STROKE
                            strokeWidth = p.bevelWidth * 1.2f * previewScale
                            color = p.bevelHighlightColorArgb
                            alpha = (210 * layer.opacity).toInt()
                            clearShadowLayer()
                        }
                        cY = curY + bhY
                        for (line in lines) {
                            canvas.drawText(line, bhX, cY, bevelHlPaint)
                            cY += lineHeight
                        }
                    }

                    // Stroke
                    if (p.hasStroke) {
                        val strokePaint = Paint(paint).apply {
                            style = Paint.Style.STROKE
                            strokeWidth = p.strokeWidth * 2f * previewScale
                            color = p.strokeColorArgb
                            alpha = (androidx.compose.ui.graphics.Color(p.strokeColorArgb).alpha * layer.opacity * 255).toInt()
                        }
                        var sY = curY
                        for (line in lines) {
                            canvas.drawText(line, 0f, sY, strokePaint)
                            sY += lineHeight
                        }
                    }

                    // Text fill
                    for (line in lines) {
                        canvas.drawText(line, 0f, curY, paint)
                        curY += lineHeight
                    }

                    // 5. PS 3D Material Glare
                    if (p.material3D == "高光光泽" || p.material3D == "GLOSS") {
                        val glossPaint = Paint(paint).apply {
                            style = Paint.Style.STROKE
                            strokeWidth = (baseFontSize * 0.09f).coerceAtLeast(2f)
                            color = Color.WHITE
                            alpha = (180 * layer.opacity).toInt()
                            clearShadowLayer()
                        }
                        var gY = curY - baseFontSize * 0.12f
                        for (line in lines) {
                            canvas.drawText(line, -2f, gY, glossPaint)
                            gY += lineHeight
                        }
                    }

                    if (hasTilt) {
                        canvas.restore()
                    }
                }

                LayerType.SHAPE -> {
                    val sp = layer.shapeProps
                    val size = 160f
                    val half = size / 2f
                    val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                        style = Paint.Style.FILL
                        color = sp.fillColorArgb
                        alpha = (androidx.compose.ui.graphics.Color(sp.fillColorArgb).alpha * layer.opacity * 255).toInt()
                    }
                    val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                        style = Paint.Style.STROKE
                        strokeWidth = sp.strokeWidth * 2f
                        color = sp.strokeColorArgb
                        alpha = (androidx.compose.ui.graphics.Color(sp.strokeColorArgb).alpha * layer.opacity * 255).toInt()
                    }

                    when (sp.shapeType) {
                        ShapeType.RECTANGLE -> {
                            canvas.drawRect(-half, -half, half, half, fillPaint)
                            if (sp.hasStroke) canvas.drawRect(-half, -half, half, half, strokePaint)
                        }
                        ShapeType.ROUNDED_RECTANGLE -> {
                            val r = sp.cornerRadius * 1.5f
                            canvas.drawRoundRect(-half, -half, half, half, r, r, fillPaint)
                            if (sp.hasStroke) canvas.drawRoundRect(-half, -half, half, half, r, r, strokePaint)
                        }
                        ShapeType.CIRCLE -> {
                            canvas.drawCircle(0f, 0f, half, fillPaint)
                            if (sp.hasStroke) canvas.drawCircle(0f, 0f, half, strokePaint)
                        }
                        else -> {
                            canvas.drawCircle(0f, 0f, half, fillPaint)
                        }
                    }
                }

                LayerType.STICKER -> {
                    val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                        textSize = 90f
                        textAlign = Paint.Align.CENTER
                        alpha = (layer.opacity * 255).toInt()
                    }
                    val bounds = Rect()
                    paint.getTextBounds(layer.stickerProps.stickerName, 0, layer.stickerProps.stickerName.length, bounds)
                    canvas.drawText(layer.stickerProps.stickerName, 0f, bounds.height() / 2f, paint)
                }

                else -> {}
            }

            canvas.restore()
        }

        bitmap
    }

    // 需求4：同步版渲染（供 SourceCodec 导出使用）
    fun generateBitmapSync(
        config: CanvasConfig,
        layers: List<LayerItem>,
        quality: ExportQuality,
        targetWidth: Int = 0,
        targetHeight: Int = 0,
        previewCanvasWidthPx: Float = 380f
    ): Bitmap {
        val width = if (targetWidth > 0) targetWidth else (config.width * quality.multiplier).toInt().coerceIn(200, 4096)
        val height = if (targetHeight > 0) targetHeight else (config.height * quality.multiplier).toInt().coerceIn(200, 4096)
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // 背景
        when (config.backgroundType) {
            BackgroundType.TRANSPARENT -> { }
            BackgroundType.COLOR, BackgroundType.PRESET_TEXTURE, BackgroundType.IMAGE_URI, BackgroundType.VIDEO_URI -> {
                canvas.drawColor(config.backgroundColorArgb)
            }
            BackgroundType.GRADIENT -> {
                val shader = LinearGradient(
                    0f, 0f, width.toFloat(), height.toFloat(),
                    config.backgroundColorArgb,
                    config.backgroundGradientColor2Argb,
                    Shader.TileMode.CLAMP
                )
                val paint = Paint().apply { this.shader = shader }
                canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
            }
        }

        val previewScale = width.toFloat() / previewCanvasWidthPx.coerceAtLeast(1f)
        val centerX = width / 2f
        val centerY = height / 2f
        layers.forEach { layer ->
            if (!layer.isVisible) return@forEach
            canvas.save()
            val layerX = centerX + (layer.x * previewScale)
            val layerY = centerY + (layer.y * previewScale)
            canvas.translate(layerX, layerY)
            canvas.rotate(layer.rotation)
            canvas.scale(layer.scaleX * previewScale, layer.scaleY * previewScale)
            when (layer.type) {
                LayerType.TEXT -> {
                    val p = layer.textProps
                    val baseFontSize = p.fontSize * 1.5f
                    val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                        textSize = baseFontSize
                        textAlign = when (p.alignment) {
                            TextAlignment.LEFT -> Paint.Align.LEFT
                            TextAlignment.CENTER -> Paint.Align.CENTER
                            TextAlignment.RIGHT -> Paint.Align.RIGHT
                        }
                        typeface = when {
                            p.isBold && p.isItalic -> Typeface.create(Typeface.DEFAULT, Typeface.BOLD_ITALIC)
                            p.isBold -> Typeface.DEFAULT_BOLD
                            p.isItalic -> Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
                            else -> Typeface.DEFAULT
                        }
                    }
                    val fm = paint.fontMetrics
                    val lineHeight = (fm.descent - fm.ascent) * p.lineSpacing
                    val lines = p.text.split("\n")
                    val totalHeight = lines.size * lineHeight
                    var curY = -totalHeight / 2f + (-fm.ascent)
                    for (line in lines) {
                        canvas.drawText(line, 0f, curY, paint)
                        curY += lineHeight
                    }
                }
                else -> {
                    val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                        color = 0xFF00ADB5.toInt()
                        alpha = (layer.opacity * 255).toInt().coerceIn(0, 255)
                    }
                    canvas.drawCircle(0f, 0f, 60f, paint)
                }
            }
            canvas.restore()
        }
        return bitmap
    }

    suspend fun saveBitmapToGallery(
        context: Context,
        bitmap: Bitmap,
        format: ExportFormat,
        title: String = "PixelLab_${System.currentTimeMillis()}"
    ): Uri? = withContext(Dispatchers.IO) {
        val compressFormat = if (format == ExportFormat.PNG) Bitmap.CompressFormat.PNG else Bitmap.CompressFormat.JPEG
        val extension = if (format == ExportFormat.PNG) "png" else "jpg"
        val mimeType = if (format == ExportFormat.PNG) "image/png" else "image/jpeg"
        val filename = "$title.$extension"

        var uri: Uri? = null
        var outputStream: OutputStream? = null

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val values = ContentValues().apply {
                    put(MediaStore.Images.Media.DISPLAY_NAME, filename)
                    put(MediaStore.Images.Media.MIME_TYPE, mimeType)
                    put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/PixelLab")
                }
                val resolver = context.contentResolver
                uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
                if (uri != null) {
                    outputStream = resolver.openOutputStream(uri)
                }
            } else {
                val imagesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                val pixelLabDir = File(imagesDir, "PixelLab")
                if (!pixelLabDir.exists()) pixelLabDir.mkdirs()
                val imageFile = File(pixelLabDir, filename)
                outputStream = FileOutputStream(imageFile)
                uri = Uri.fromFile(imageFile)
            }

            outputStream?.use {
                bitmap.compress(compressFormat, 100, it)
            }
        } catch (_: Exception) {
            uri = null
        }
        uri
    }

    suspend fun saveBitmapToCache(
        context: Context,
        bitmap: Bitmap,
        format: ExportFormat
    ): Uri? = withContext(Dispatchers.IO) {
        try {
            val compressFormat = if (format == ExportFormat.PNG) Bitmap.CompressFormat.PNG else Bitmap.CompressFormat.JPEG
            val extension = if (format == ExportFormat.PNG) "png" else "jpg"
            val cachePath = File(context.cacheDir, "images")
            cachePath.mkdirs()
            val file = File(cachePath, "share_image.$extension")
            FileOutputStream(file).use { out ->
                bitmap.compress(compressFormat, 100, out)
            }
            FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        } catch (_: Exception) {
            null
        }
    }

    fun shareImage(context: Context, uri: Uri, format: ExportFormat) {
        val mime = if (format == ExportFormat.PNG) "image/png" else "image/jpeg"
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = mime
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "分享作品"))
    }
}
