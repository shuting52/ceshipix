package com.landesheji.util

import android.graphics.BlurMaskFilter
import android.graphics.Canvas
import android.graphics.DashPathEffect
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Shader
import android.graphics.Typeface
import org.json.JSONArray
import org.json.JSONObject
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.max

/**
 * 需求重造3：文字模板效果渲染器（统一效果分发器）
 *
 * 纯本地 Canvas/Skia 绘制，不调用 AI、不调用外部 API、不依赖服务器。
 *
 * 设计原则：
 *   - 模板 = 数据（TextTemplatesData / JSON）
 *   - 效果 = 绘制函数（本文件）
 *   - 文字 = 可修改参数（套用模板后文字可编辑，效果由 params 驱动保持不变）
 *
 * 所有效果函数共用统一签名 drawEffect(...)，按 effect 名分发到
 * crystal / glass / embroidery / threeD / neon / gold / fire / ice / glitch / cyber。
 */
object TemplateEffectsRenderer {

    /** 统一入口：按效果名分发的渲染函数（文字中心对齐 x,y） */
    fun drawEffect(
        canvas: android.graphics.Canvas,
        text: String,
        fontSizePx: Float,
        effect: String,
        params: JSONObject,
        x: Float = 0f,
        y: Float = 0f,
        fontFamily: String = "sans-serif",
        fontWeight: String = "bold",
        opacity: Float = 1f,
        maxWidth: Float = Float.MAX_VALUE
    ) {
        val textSafe = text.ifEmpty { " " }
        when (effect) {
            "crystal" -> drawCrystalText(canvas, textSafe, fontSizePx, params, x, y, fontFamily, fontWeight, opacity, maxWidth)
            "glass" -> drawGlassText(canvas, textSafe, fontSizePx, params, x, y, fontFamily, fontWeight, opacity, maxWidth)
            "embroidery" -> drawEmbroideryText(canvas, textSafe, fontSizePx, params, x, y, fontFamily, fontWeight, opacity, maxWidth)
            "threeD" -> draw3DText(canvas, textSafe, fontSizePx, params, x, y, fontFamily, fontWeight, opacity, maxWidth)
            "neon" -> drawNeonText(canvas, textSafe, fontSizePx, params, x, y, fontFamily, fontWeight, opacity, maxWidth)
            "gold" -> drawGoldText(canvas, textSafe, fontSizePx, params, x, y, fontFamily, fontWeight, opacity, maxWidth)
            "fire" -> drawFireText(canvas, textSafe, fontSizePx, params, x, y, fontFamily, fontWeight, opacity, maxWidth)
            "ice" -> drawIceText(canvas, textSafe, fontSizePx, params, x, y, fontFamily, fontWeight, opacity, maxWidth)
            "glitch" -> drawGlitchText(canvas, textSafe, fontSizePx, params, x, y, fontFamily, fontWeight, opacity, maxWidth)
            "cyber" -> drawCyberText(canvas, textSafe, fontSizePx, params, x, y, fontFamily, fontWeight, opacity, maxWidth)
        }
    }

    // ============================ 通用工具 ============================

    private fun basePaint(textSize: Float, typeface: Typeface = Typeface.DEFAULT_BOLD): Paint =
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.textSize = textSize
            this.typeface = typeface
            textAlign = Paint.Align.CENTER
        }

    private fun makeGradient(
        colorsHex: List<String>,
        textSize: Float,
        vertical: Boolean = true
    ): LinearGradient {
        val colors = colorsHex.map { parseColor(it) }.toIntArray()
        val x0 = if (vertical) 0f else -textSize * 1.6f
        val y0 = if (vertical) -textSize * 0.9f else 0f
        val x1 = if (vertical) 0f else textSize * 1.6f
        val y1 = if (vertical) textSize * 0.9f else 0f
        return LinearGradient(x0, y0, x1, y1, colors, null, Shader.TileMode.CLAMP)
    }

    private fun parseColor(s: String): Int {
        val t = s.trim()
        if (t.startsWith("rgba")) {
            val inner = t.removePrefix("rgba").removePrefix("(").removeSuffix(")")
            val parts = inner.split(",").map { it.trim() }
            return try {
                val r = parts[0].toFloat().toInt()
                val g = parts[1].toFloat().toInt()
                val b = parts[2].toFloat().toInt()
                val a = (parts[3].toFloat() * 255).toInt().toInt().coerceIn(0, 255)
                (a shl 24) or (r shl 16) or (g shl 8) or b
            } catch (e: Exception) {
                0xFFFFFFFF.toInt()
            }
        }
        val h = t.removePrefix("#")
        return try {
            when (h.length) {
                3 -> {
                    val r = h[0].toString().repeat(2).toInt(16)
                    val g = h[1].toString().repeat(2).toInt(16)
                    val b = h[2].toString().repeat(2).toInt(16)
                    0xFF000000.toInt() or (r shl 16) or (g shl 8) or b
                }
                6 -> 0xFF000000.toInt() or h.toInt(16)
                8 -> h.toLong(16).toInt()
                else -> 0xFFFFFFFF.toInt()
            }
        } catch (e: Exception) {
            0xFFFFFFFF.toInt()
        }
    }

    private fun Int.withAlpha(f: Float): Int =
        ((f.coerceIn(0f, 1f) * 255).toInt() shl 24) or (this and 0x00FFFFFF)

    private fun paramStringArray(p: JSONObject, key: String): List<String> {
        val arr = p.optJSONArray(key) ?: return emptyList()
        return (0 until arr.length()).map { arr.getString(it) }
    }

    private fun makeTypeface(fontFamily: String, fontWeight: String): Typeface = when {
        fontWeight.contains("bold", true) -> Typeface.create(fontFamily, Typeface.BOLD)
        fontWeight.contains("italic", true) -> Typeface.create(fontFamily, Typeface.ITALIC)
        else -> Typeface.create(fontFamily, Typeface.NORMAL)
    }

    /** 自动瘦身字号，使最宽一行不超过 maxWidth；返回 (paint, lineHeight, totalHeight, lines) */
    private fun layoutText(
        text: String,
        fontSizePx: Float,
        typeface: Typeface,
        maxWidth: Float
    ): Triple<Paint, Float, Float> {
        var size = fontSizePx
        var paint = basePaint(fontSizePx, typeface)
        do {
            paint = basePaint(size, typeface)
            val lines = text.split("\n")
            val w = lines.maxOf { paint.measureText(it) }
            if (w <= maxWidth || size <= 12f) break
            size *= 0.92f
        } while (true)
        val fm = paint.fontMetrics
        val lineHeight = fm.descent - fm.ascent
        val totalHeight = lineHeight * text.split("\n").size
        return Triple(paint, lineHeight, totalHeight)
    }

    // ============================ 1. 蓝色水晶 ============================
    private fun drawCrystalText(
        canvas: Canvas, text: String, fsize: Float, p: JSONObject,
        x: Float, y: Float, family: String, weight: String, opacity: Float, maxW: Float
    ) {
        val tf = makeTypeface(family, weight)
        val (paint, lineHeight, totalHeight) = layoutText(text, fsize, tf, maxW)
        val depth = p.optInt("depth", 20)
        val depthColor = parseColor(p.optString("depthColor", "#073b78"))
        val strokeWidth = p.optInt("strokeWidth", 14).toFloat()
        val strokeColor = parseColor(p.optString("strokeColor", "#04356f"))
        val glowColor = parseColor(p.optString("glowColor", "rgba(0,160,255,0.85)"))
        val glowBlur = p.optInt("glowBlur", 24).toFloat()
        val gradient = paramStringArray(p, "gradient")
        val highlight = p.optBoolean("highlight", true)
        val alphaMul = (opacity * 255).toInt()

        val baseY = y - totalHeight / 2f - paint.fontMetrics.ascent
        var curY = baseY

        fun drawAll(pp: Paint) {
            curY = baseY
            for (line in text.split("\n")) {
                // 自动缩放超过 maxW 的行宽
                var pl = pp
                if (paint.measureText(line) > maxW && maxW != Float.MAX_VALUE) {
                    // measureText 在 layoutText 已处理单行，这里多行场景按最长行已缩放
                }
                canvas.drawText(line, x, curY, pl)
                curY += lineHeight
            }
        }

        // 柔和阴影
        val shadowPaint = Paint(paint).apply {
            clearShadowLayer()
            setShadowLayer(glowBlur * 0.5f, 8f, 12f, 0x66000000)
            color = 0xFF00111F.toInt()
            alpha = alphaMul
        }
        drawAll(shadowPaint)

        // 3D 厚度（右下带深度）
        val depthCount = ceil(depth / 2f).toInt()
        for (i in depthCount downTo 1) {
            val dPaint = Paint(paint).apply {
                shader = null
                color = depthColor
                alpha = (alphaMul * (0.45f + 0.55f * (i.toFloat() / depthCount))).toInt()
                clearShadowLayer()
            }
            curY = baseY + i * 1.6f
            var idx = 0
            for (line in text.split("\n")) {
                canvas.drawText(line, x + i * 1.6f, curY, dPaint)
                curY += lineHeight
            }
        }

        // 描边（深蓝）
        val strokeP = Paint(paint).apply {
            style = Paint.Style.STROKE
            this.strokeWidth = strokeWidth
            shader = null
            color = strokeColor
            alpha = alphaMul
            clearShadowLayer()
        }
        drawAll(strokeP)

        // 外发光
        for (scale in intArrayOf(1, 2)) {
            val glowP = Paint(paint).apply {
                style = Paint.Style.FILL
                shader = null
                this.color = glowColor
                alpha = (alphaMul * (if (scale == 1) 150 else 90)).toInt().coerceIn(0, 255)
                maskFilter = BlurMaskFilter(glowBlur * scale, BlurMaskFilter.Blur.NORMAL)
            }
            drawAll(glowP)
        }

        // 渐变填充
        val fillP = Paint(paint).apply {
            style = Paint.Style.FILL
            color = 0xFFFFFFFF.toInt()
            alpha = alphaMul
            if (gradient.isNotEmpty()) shader = makeGradient(gradient, paint.textSize)
            clearShadowLayer()
        }
        drawAll(fillP)

        // 顶部白色高光
        if (highlight) {
            val hiP = Paint(paint).apply {
                style = Paint.Style.STROKE
                this.strokeWidth = max(2f, paint.textSize * 0.045f)
                shader = null
                color = 0xCCFFFFFF.toInt()
                alpha = (alphaMul * 0.9f).toInt()
            }
            curY = baseY - paint.textSize * 0.08f
            for (line in text.split("\n")) {
                canvas.drawText(line, x - paint.textSize * 0.02f, curY, hiP)
                curY += lineHeight
            }
        }
    }

    // ============================ 2. 透明玻璃 ============================
    private fun drawGlassText(
        canvas: Canvas, text: String, fsize: Float, p: JSONObject,
        x: Float, y: Float, family: String, weight: String, opacity: Float, maxW: Float
    ) {
        val tf = makeTypeface(family, weight)
        val (paint, lineHeight, totalHeight) = layoutText(text, fsize, tf, maxW)
        val fillColor = parseColor(p.optString("fillColor", "rgba(220,245,255,0.3)"))
        val strokeColor = parseColor(p.optString("strokeColor", "rgba(255,255,255,0.9)"))
        val strokeW = p.optInt("strokeWidth", 5).toFloat()
        val glowColor = parseColor(p.optString("glowColor", "rgba(255,255,255,0.8)"))
        val glowBlur = p.optInt("glowBlur", 22).toFloat()
        val effOpacity = p.optDouble("opacity", 0.85).toFloat()
        val alphaMul = (opacity * effOpacity * 255).toInt()
        val baseY = y - totalHeight / 2f - paint.fontMetrics.ascent

        fun drawAll(pp: Paint) {
            var curY = baseY
            for (line in text.split("\n")) { canvas.drawText(line, x, curY, pp); curY += lineHeight }
        }

        // 柔和白色光晕
        val glowP = Paint(paint).apply {
            shader = null
            this.color = glowColor
            alpha = (alphaMul * 0.7f).toInt().coerceIn(0, 255)
            maskFilter = BlurMaskFilter(glowBlur, BlurMaskFilter.Blur.NORMAL)
        }
        drawAll(glowP)

        // 半透明玻璃填充
        val fillP = Paint(paint).apply {
            shader = null
            color = fillColor
            alpha = (Color.alpha(fillColor) * opacity * effOpacity * 255f).toInt().coerceIn(0, 255)
        }
        drawAll(fillP)

        // 白色描边（玻璃边缘）
        val strokeP = Paint(paint).apply {
            style = Paint.Style.STROKE
            this.strokeWidth = strokeW
            shader = null
            color = strokeColor
            alpha = (Color.alpha(strokeColor) * opacity * 255).toInt().coerceIn(0, 255)
            clearShadowLayer()
        }
        drawAll(strokeP)

        // 玻璃高光（顶部细白线）
        if (p.optBoolean("highlight", true)) {
            val hiP = Paint(paint).apply {
                style = Paint.Style.STROKE
                strokeWidth = max(1.5f, paint.textSize * 0.03f)
                shader = null
                color = 0xFFFFFFFF.toInt()
                alpha = (alphaMul * 0.85f).toInt().coerceIn(0, 255)
            }
            var curY = baseY - paint.textSize * 0.1f
            for (line in text.split("\n")) {
                canvas.drawText(line, x, curY, hiP)
                curY += lineHeight
            }
        }
    }

    // ============================ 3. 红色刺绣 ============================
    private fun drawEmbroideryText(
        canvas: Canvas, text: String, fsize: Float, p: JSONObject,
        x: Float, y: Float, family: String, weight: String, opacity: Float, maxW: Float
    ) {
        val tf = makeTypeface(family, weight)
        val (paint, lineHeight, totalHeight) = layoutText(text, fsize, tf, maxW)
        val fillColor = parseColor(p.optString("fillColor", "#c92135"))
        val strokeColor = parseColor(p.optString("strokeColor", "#721222"))
        val strokeW = p.optInt("strokeWidth", 9).toFloat()
        val threadColor = parseColor(p.optString("threadColor", "#ff6870"))
        val shadowColor = parseColor(p.optString("shadowColor", "rgba(50,10,10,0.45)"))
        val alphaMul = (opacity * 255).toInt()
        val baseY = y - totalHeight / 2f - paint.fontMetrics.ascent

        fun drawAll(pp: Paint) {
            var curY = baseY
            for (line in text.split("\n")) { canvas.drawText(line, x, curY, pp); curY += lineHeight }
        }

        // 布料阴影
        val shadowP = Paint(paint).apply {
            clearShadowLayer()
            setShadowLayer(p.optInt("shadowBlur", 8).toFloat(), 3f, 5f, shadowColor)
            color = fillColor
            alpha = alphaMul
            shader = null
        }
        drawAll(shadowP)
        // 上面画了 color，需清 shadow 再继续，避免泄漏到主填充
        // 主填充
        val fillP = Paint(paint).apply {
            clearShadowLayer()
            color = fillColor
            alpha = alphaMul
            shader = null
        }
        drawAll(fillP)

        // 多层深红描边
        val strokeP = Paint(paint).apply {
            style = Paint.Style.STROKE
            this.strokeWidth = strokeW
            shader = null
            color = strokeColor
            alpha = alphaMul
        }
        drawAll(strokeP)

        // 内圈描边（更细更亮）
        val innerP = Paint(strokeP).apply {
            this.strokeWidth = strokeW * 0.45f
            color = fillColor
        }
        drawAll(innerP)

        // 针线纹理：细虚线模拟缝线（threadCount 圈）
        val threadCount = p.optInt("threadCount", 12).coerceIn(2, 60)
        val threadW = p.optInt("threadWidth", 2).toFloat().coerceAtLeast(1f)
        for (i in 0 until threadCount) {
            val tP = Paint(paint).apply {
                style = Paint.Style.STROKE
                strokeWidth = threadW
                shader = null
                color = threadColor
                alpha = (alphaMul * (0.5f + 0.5f * (i.toFloat() / threadCount))).toInt()
                pathEffect = DashPathEffect(floatArrayOf(threadW * 3f, threadW * 2.5f), i * 2f)
            }
            drawAll(tP)
        }

        // 纹理：布纹网点（若启用）
        if (p.optBoolean("texture", true)) {
            val dotP = Paint(paint).apply {
                style = Paint.Style.STROKE
                strokeWidth = threadW
                color = threadColor
                alpha = (alphaMul * 0.55f).toInt()
                pathEffect = DashPathEffect(floatArrayOf(0.1f, threadW * 4f), 0f)
            }
            // 在文字内部画细斜纹
            var dy = -paint.textSize * 0.28f
            while (dy <= paint.textSize * 0.28f) {
                val path = Path().apply {
                    moveTo(x - paint.textSize * 0.5f, baseY + dy)
                    lineTo(x + paint.textSize * 0.5f, baseY + dy + paint.textSize * 0.06f)
                }
                canvas.drawPath(path, dotP)
                dy += paint.textSize * 0.07f
            }
        }
    }

    // ============================ 4. 橙色3D立体 ============================
    private fun draw3DText(
        canvas: Canvas, text: String, fsize: Float, p: JSONObject,
        x: Float, y: Float, family: String, weight: String, opacity: Float, maxW: Float
    ) {
        val tf = makeTypeface(family, weight)
        val (paint, lineHeight, totalHeight) = layoutText(text, fsize, tf, maxW)
        val depth = p.optInt("depth", 28)
        val depthColor = parseColor(p.optString("depthColor", "#762500"))
        val strokeColor = parseColor(p.optString("strokeColor", "#4b1604"))
        val strokeW = p.optInt("strokeWidth", 8).toFloat()
        val shadowColor = parseColor(p.optString("shadowColor", "rgba(0,0,0,0.65)"))
        val gradient = paramStringArray(p, "gradient")
        val alphaMul = (opacity * 255).toInt()
        val baseY = y - totalHeight / 2f - paint.fontMetrics.ascent

        // 黑色投影
        val projP = Paint(paint).apply {
            shader = null
            color = 0xFF000000.toInt()
            alpha = (Color.alpha(shadowColor) * opacity * 255).toInt().coerceIn(0, 255)
            clearShadowLayer()
            maskFilter = BlurMaskFilter(p.optInt("shadowBlur", 18).toFloat(), BlurMaskFilter.Blur.NORMAL)
        }
        var curY = baseY + p.optInt("shadowOffsetY", 20)
        for (line in text.split("\n")) {
            canvas.drawText(line, x + p.optInt("shadowOffsetX", 15), curY, projP)
            curY += lineHeight
        }

        // 立体厚度（右下重复绘制）
        for (i in depth downTo 1) {
            val dP = Paint(paint).apply {
                shader = null
                color = depthColor
                alpha = (alphaMul * (0.5f + 0.5f * (depth - i + 0f) / depth)).toInt()
                clearShadowLayer()
            }
            curY = baseY + i * 1.5f
            for (line in text.split("\n")) {
                canvas.drawText(line, x + i * 1.5f - 1, curY, dP)
                curY += lineHeight
            }
        }

        // 描边
        val strokeP = Paint(paint).apply {
            style = Paint.Style.STROKE
            this.strokeWidth = strokeW
            shader = null
            color = strokeColor
            alpha = alphaMul
        }
        curY = baseY
        for (line in text.split("\n")) { canvas.drawText(line, x, curY, strokeP); curY += lineHeight }

        // 正面渐变
        val fillP = Paint(paint).apply {
            style = Paint.Style.FILL
            color = 0xFFFFb537.toInt()
            alpha = alphaMul
            if (gradient.isNotEmpty()) shader = makeGradient(gradient, paint.textSize)
            clearShadowLayer()
        }
        curY = baseY
        for (line in text.split("\n")) { canvas.drawText(line, x, curY, fillP); curY += lineHeight }

        // 顶部高光
        val hiP = Paint(paint).apply {
            style = Paint.Style.STROKE
            strokeWidth = max(2f, paint.textSize * 0.03f)
            shader = null
            color = 0x99FFF7C0.toInt()
            alpha = (alphaMul * 0.8f).toInt()
        }
        curY = baseY - paint.textSize * 0.05f
        for (line in text.split("\n")) { canvas.drawText(line, x, curY, hiP); curY += lineHeight }
    }

    // ============================ 5. 粉色霓虹 ============================
    private fun drawNeonText(
        canvas: Canvas, text: String, fsize: Float, p: JSONObject,
        x: Float, y: Float, family: String, weight: String, opacity: Float, maxW: Float
    ) {
        val tf = makeTypeface(family, weight)
        val (paint, lineHeight, totalHeight) = layoutText(text, fsize, tf, maxW)
        val color = parseColor(p.optString("color", "#ff2bbd"))
        val innerColor = parseColor(p.optString("innerColor", "#ffffff"))
        val strokeW = p.optInt("strokeWidth", 5).toFloat()
        val glowColor = parseColor(p.optString("glowColor", "#ff149f"))
        val glowBlur = p.optInt("glowBlur", 36).toFloat()
        val alphaMul = (opacity * 255).toInt()
        val baseY = y - totalHeight / 2f - paint.fontMetrics.ascent

        fun drawAll(pp: Paint) {
            var curY = baseY
            for (line in text.split("\n")) { canvas.drawText(line, x, curY, pp); curY += lineHeight }
        }

        // 3 层光晕（由外到内）
        listOf(glowBlur * 2.2f, glowBlur * 1.5f, glowBlur).forEachIndexed { idx, blur ->
            val glowP = Paint(paint).apply {
                shader = null
                this.color = glowColor
                alpha = (alphaMul * (if (idx == 0) 90 else if (idx == 1) 130 else 180)).toInt().coerceIn(0, 255)
                maskFilter = BlurMaskFilter(blur, BlurMaskFilter.Blur.NORMAL)
            }
            drawAll(glowP)
        }

        // 彩色填充
        val fillP = Paint(paint).apply {
            shader = null
            this.color = color
            alpha = alphaMul
            clearShadowLayer()
        }
        drawAll(fillP)

        // 白色亮芯
        val coreP = Paint(paint).apply {
            shader = null
            this.color = innerColor
            alpha = (alphaMul * 0.92f).toInt()
        }
        drawAll(coreP)

        // 细描边收边
        val edgeP = Paint(paint).apply {
            style = Paint.Style.STROKE
            this.strokeWidth = strokeW
            shader = null
            this.color = glowColor
            alpha = (alphaMul * 0.8f).toInt()
        }
        drawAll(edgeP)
    }

    // ============================ 6. 黄金金属 ============================
    private fun drawGoldText(
        canvas: Canvas, text: String, fsize: Float, p: JSONObject,
        x: Float, y: Float, family: String, weight: String, opacity: Float, maxW: Float
    ) {
        val tf = makeTypeface(family, weight)
        val (paint, lineHeight, totalHeight) = layoutText(text, fsize, tf, maxW)
        val strokeColor = parseColor(p.optString("strokeColor", "#713f08"))
        val strokeW = p.optInt("strokeWidth", 10).toFloat()
        val shadowColor = parseColor(p.optString("shadowColor", "rgba(0,0,0,0.6)"))
        val gradient = paramStringArray(p, "gradient")
        val reflection = p.optBoolean("reflection", true)
        val alphaMul = (opacity * 255).toInt()
        val baseY = y - totalHeight / 2f - paint.fontMetrics.ascent

        fun drawAll(pp: Paint) {
            var curY = baseY
            for (line in text.split("\n")) { canvas.drawText(line, x, curY, pp); curY += lineHeight }
        }

        // 立体阴影
        val shP = Paint(paint).apply {
            clearShadowLayer()
            setShadowLayer(p.optInt("shadowBlur", 15).toFloat(), 6f, 10f, shadowColor)
            shader = null
            color = 0xFF2A1500.toInt()
            alpha = alphaMul
        }
        drawAll(shP)

        // 深金描边
        val strokeP = Paint(paint).apply {
            style = Paint.Style.STROKE
            this.strokeWidth = strokeW
            shader = null
            color = strokeColor
            alpha = alphaMul
        }
        drawAll(strokeP)

        // 金渐变填充
        val fillP = Paint(paint).apply {
            style = Paint.Style.FILL
            color = 0xFFFFD700.toInt()
            alpha = alphaMul
            if (gradient.isNotEmpty()) shader = makeGradient(gradient, paint.textSize)
            clearShadowLayer()
        }
        drawAll(fillP)

        // 反光高光
        val hiP = Paint(paint).apply {
            style = Paint.Style.STROKE
            strokeWidth = max(2f, paint.textSize * 0.04f)
            shader = null
            color = 0xFFFFFBE8.toInt()
            alpha = (alphaMul * 0.9f).toInt()
        }
        var curY = baseY - paint.textSize * 0.12f
        for (line in text.split("\n")) { canvas.drawText(line, x, curY, hiP); curY += lineHeight }
        // 下缘反射线
        if (reflection) {
            val loP = Paint(paint).apply {
                style = Paint.Style.STROKE
                strokeWidth = max(1.5f, paint.textSize * 0.02f)
                shader = null
                color = 0xA0FFF19B.toInt()
                alpha = (alphaMul * 0.5f).toInt()
            }
            curY = baseY + totalHeight - paint.fontMetrics.descent * 0.7f + paint.textSize * 0.1f
            for (line in text.split("\n")) { canvas.drawText(line, x, curY, loP); curY += lineHeight }
        }
    }

    // ============================ 7. 火焰 ============================
    private fun drawFireText(
        canvas: Canvas, text: String, fsize: Float, p: JSONObject,
        x: Float, y: Float, family: String, weight: String, opacity: Float, maxW: Float
    ) {
        val tf = makeTypeface(family, weight)
        val (paint, lineHeight, totalHeight) = layoutText(text, fsize, tf, maxW)
        val strokeColor = parseColor(p.optString("strokeColor", "#7b1005"))
        val strokeW = p.optInt("strokeWidth", 10).toFloat()
        val glowColor = parseColor(p.optString("glowColor", "#ff3d00"))
        val glowBlur = p.optInt("glowBlur", 30).toFloat()
        val gradient = paramStringArray(p, "gradient")
        val alphaMul = (opacity * 255).toInt()
        val baseY = y - totalHeight / 2f - paint.fontMetrics.ascent

        fun drawAll(pp: Paint) {
            var curY = baseY
            for (line in text.split("\n")) { canvas.drawText(line, x, curY, pp); curY += lineHeight }
        }

        // 外缘红色发光
        listOf(glowBlur * 1.8f, glowBlur).forEachIndexed { i, blur ->
            val glowP = Paint(paint).apply {
                shader = null
                this.color = glowColor
                alpha = (alphaMul * (if (i == 0) 110 else 170)).toInt().coerceIn(0, 255)
                maskFilter = BlurMaskFilter(blur, BlurMaskFilter.Blur.NORMAL)
            }
            drawAll(glowP)
        }

        // 深红描边
        val strokeP = Paint(paint).apply {
            style = Paint.Style.STROKE
            this.strokeWidth = strokeW
            shader = null
            color = strokeColor
            alpha = alphaMul
        }
        drawAll(strokeP)

        // 火焰渐变填充（红→橙→黄）
        val fillP = Paint(paint).apply {
            style = Paint.Style.FILL
            color = 0xFFFF6A00.toInt()
            alpha = alphaMul
            if (gradient.isNotEmpty()) shader = makeGradient(gradient, paint.textSize)
            clearShadowLayer()
        }
        drawAll(fillP)

        // 内部火焰高光（黄色偏差）
        val innerP = Paint(paint).apply {
            style = Paint.Style.FILL
            alpha = (alphaMul * 0.85f).toInt()
            color = 0xFFFFF7A0.toInt()
            shader = null
        }
        var curY = baseY - paint.textSize * 0.1f
        for (line in text.split("\n")) {
            val w = paint.measureText(line)
            // 用横向收缩渐变模拟内部亮芯
            val narrow = innerP.shader // no-op
            canvas.drawText(line, x, curY + paint.textSize * 0.02f, innerP)
            curY += lineHeight
        }

        // 顶部火焰纹理（锯齿小火焰）
        if (p.optBoolean("flameTexture", true)) {
            val flameP = Paint(paint).apply {
                style = Paint.Style.STROKE
                strokeWidth = max(2f, paint.textSize * 0.03f)
                color = 0xFFFFB347.toInt()
                alpha = (alphaMul * 0.9f).toInt()
                pathEffect = DashPathEffect(floatArrayOf(paint.textSize * 0.06f, paint.textSize * 0.05f), 0f)
                shader = null
            }
            curY = baseY - paint.textSize * 0.22f
            for (line in text.split("\n")) {
                canvas.drawText(line, x, curY, flameP)
                curY += lineHeight
            }
        }
    }

    // ============================ 8. 冰霜 ============================
    private fun drawIceText(
        canvas: Canvas, text: String, fsize: Float, p: JSONObject,
        x: Float, y: Float, family: String, weight: String, opacity: Float, maxW: Float
    ) {
        val tf = makeTypeface(family, weight)
        val (paint, lineHeight, totalHeight) = layoutText(text, fsize, tf, maxW)
        val strokeColor = parseColor(p.optString("strokeColor", "#79dfff"))
        val strokeW = p.optInt("strokeWidth", 10).toFloat()
        val glowColor = parseColor(p.optString("glowColor", "rgba(0,190,255,0.8)"))
        val glowBlur = p.optInt("glowBlur", 26).toFloat()
        val gradient = paramStringArray(p, "gradient")
        val highlight = p.optBoolean("highlight", true)
        val alphaMul = (opacity * 255).toInt()
        val baseY = y - totalHeight / 2f - paint.fontMetrics.ascent

        fun drawAll(pp: Paint) {
            var curY = baseY
            for (line in text.split("\n")) { canvas.drawText(line, x, curY, pp); curY += lineHeight }
        }

        // 冷色外发光
        val glowP = Paint(paint).apply {
            shader = null
            this.color = glowColor
            alpha = (alphaMul * 150).toInt().coerceIn(0, 255)
            maskFilter = BlurMaskFilter(glowBlur, BlurMaskFilter.Blur.NORMAL)
        }
        drawAll(glowP)

        // 冰蓝描边
        val strokeP = Paint(paint).apply {
            style = Paint.Style.STROKE
            this.strokeWidth = strokeW
            shader = null
            color = strokeColor
            alpha = alphaMul
        }
        drawAll(strokeP)

        // 白→蓝渐变
        val fillP = Paint(paint).apply {
            style = Paint.Style.FILL
            color = 0xFFBFEFFF.toInt()
            alpha = alphaMul
            if (gradient.isNotEmpty()) shader = makeGradient(gradient, paint.textSize)
            clearShadowLayer()
        }
        drawAll(fillP)

        // 冰晶内部高光
        if (highlight) {
            val hiP = Paint(paint).apply {
                style = Paint.Style.STROKE
                strokeWidth = max(1.5f, paint.textSize * 0.028f)
                shader = null
                color = 0xFFFFFFFF.toInt()
                alpha = (alphaMul * 0.85f).toInt()
            }
            var curY = baseY - paint.textSize * 0.06f
            for (line in text.split("\n")) { canvas.drawText(line, x, curY, hiP); curY += lineHeight }
        }

        // 结霜边缘：短虚线描边
        if (p.optBoolean("crystalTexture", true)) {
            val frostP = Paint(paint).apply {
                style = Paint.Style.STROKE
                strokeWidth = max(1.2f, paint.textSize * 0.022f)
                shader = null
                color = 0xCCFFFFFF.toInt().toInt()
                alpha = (alphaMul * 0.7f).toInt()
                pathEffect = DashPathEffect(floatArrayOf(paint.textSize * 0.045f, paint.textSize * 0.04f), 0f)
            }
            drawAll(frostP)
        }
    }

    // ============================ 9. 复古故障 ============================
    private fun drawGlitchText(
        canvas: Canvas, text: String, fsize: Float, p: JSONObject,
        x: Float, y: Float, family: String, weight: String, opacity: Float, maxW: Float
    ) {
        val tf = makeTypeface(family, weight)
        val (paint, lineHeight, totalHeight) = layoutText(text, fsize, tf, maxW)
        val mainColor = parseColor(p.optString("mainColor", "#ffffff"))
        val redColor = parseColor(p.optString("redColor", "#ff315c"))
        val blueColor = parseColor(p.optString("blueColor", "#00eaff"))
        val strokeW = p.optInt("strokeWidth", 4).toFloat()
        val offX = p.optInt("offsetX", 8).toFloat()
        val offY = p.optInt("offsetY", 3).toFloat()
        val alphaMul = (opacity * 255).toInt()
        val baseY = y - totalHeight / 2f - paint.fontMetrics.ascent
        val R = java.util.Random(42)

        fun drawAll(pp: Paint, dx: Float, dy: Float = 0f) {
            var curY = baseY + dy
            for (line in text.split("\n")) { canvas.drawText(line, x + dx, curY, pp); curY += lineHeight }
        }

        // 蓝色错位层
        val blueP = Paint(paint).apply {
            shader = null
            color = blueColor
            alpha = alphaMul
        }
        drawAll(blueP, -offX, -offY)

        // 红色错位层
        val redP = Paint(paint).apply {
            shader = null
            color = redColor
            alpha = alphaMul
        }
        drawAll(redP, offX, offY)

        // 横向断裂：按行随机偏移抽色块
        var idx = 0
        var curY = baseY
        for (line in text.split("\n")) {
            val shard = Paint(paint).apply {
                shader = null
                color = mainColor
                alpha = alphaMul
            }
            // 模拟断裂：第 N 行加不同偏移 + 部分透明带
            if (idx % 2 == 1) {
                drawAll(shard, R.nextFloat() * offX * 0.6f)
            } else {
                canvas.drawText(line, x, curY, shard)
            }
            // 主色描边
            val mainStroke = Paint(paint).apply {
                style = Paint.Style.STROKE
                this.strokeWidth = strokeW
                color = mainColor
                alpha = alphaMul
                shader = null
            }
            canvas.drawText(line, x, curY, mainStroke)
            curY += lineHeight
            idx++
        }

        // 扫描线
        if (p.optBoolean("scanline", true)) {
            val lineP = Paint().apply {
                color = 0x22000000
                strokeWidth = max(1.5f, paint.textSize * 0.015f)
            }
            curY = baseY
            var li = 0
            while (li < 4) {
                val ly = baseY + totalHeight * R.nextFloat() * 0.8f
                canvas.drawLine(x - paint.textSize * 0.55f, ly, x + paint.textSize * 0.55f, ly, lineP)
                li++
            }
        }

        // 噪点
        if (p.optBoolean("noise", true)) {
            val noiseP = Paint().apply { style = Paint.Style.FILL }
            val boxW = paint.textSize * 0.5f
            val boxH = paint.textSize * 0.5f
            for (i in 0 until 60) {
                val nx = x - paint.textSize * 0.5f + R.nextFloat() * boxW
                val ny = baseY - paint.textSize * 0.3f + R.nextFloat() * boxH
                noiseP.color = if (R.nextBoolean()) 0x66FF315C else 0x6600EAFF
                canvas.drawRect(nx, ny, nx + paint.textSize * 0.018f, ny + paint.textSize * 0.018f, noiseP)
            }
        }
    }

    // ============================ 10. 赛博科技 ============================
    private fun drawCyberText(
        canvas: Canvas, text: String, fsize: Float, p: JSONObject,
        x: Float, y: Float, family: String, weight: String, opacity: Float, maxW: Float
    ) {
        val tf = makeTypeface(family, weight)
        val (paint, lineHeight, totalHeight) = layoutText(text, fsize, tf, maxW)
        val mainColor = parseColor(p.optString("mainColor", "#25f6ff"))
        val secondaryColor = parseColor(p.optString("secondaryColor", "#1768ff"))
        val strokeColor = parseColor(p.optString("strokeColor", "#0affff"))
        val strokeW = p.optInt("strokeWidth", 4).toFloat()
        val glowColor = parseColor(p.optString("glowColor", "#00d9ff"))
        val glowBlur = p.optInt("glowBlur", 24).toFloat()
        val alphaMul = (opacity * 255).toInt()
        val baseY = y - totalHeight / 2f - paint.fontMetrics.ascent

        fun drawAll(pp: Paint) {
            var curY = baseY
            for (line in text.split("\n")) { canvas.drawText(line, x, curY, pp); curY += lineHeight }
        }

        // 蓝色外发光
        listOf(glowBlur * 1.6f, glowBlur).forEachIndexed { i, blur ->
            val glowP = Paint(paint).apply {
                shader = null
                this.color = glowColor
                alpha = (alphaMul * (if (i == 0) 120 else 180)).toInt().coerceIn(0, 255)
                maskFilter = BlurMaskFilter(blur, BlurMaskFilter.Blur.NORMAL)
            }
            drawAll(glowP)
        }

        // 亮青描边
        val strokeP = Paint(paint).apply {
            style = Paint.Style.STROKE
            this.strokeWidth = strokeW
            shader = null
            color = strokeColor
            alpha = alphaMul
        }
        drawAll(strokeP)

        // 青蓝科技渐变
        val fillP = Paint(paint).apply {
            style = Paint.Style.FILL
            color = mainColor
            alpha = alphaMul
            shader = LinearGradient(
                0f, -paint.textSize, 0f, paint.textSize,
                intArrayOf(mainColor, secondaryColor),
                null, Shader.TileMode.CLAMP
            )
            clearShadowLayer()
        }
        drawAll(fillP)

        // 网格纹理（裁剪在文字路径内）
        if (p.optBoolean("grid", true)) {
            val path = Path()
            var curY2 = baseY
            for (line in text.split("\n")) {
                paint.getTextPath(line, 0, line.length, x, curY2, path)
                curY2 += lineHeight
            }
            canvas.save()
            try {
                canvas.clipPath(path)
                val gridP = Paint().apply {
                    color = 0x66FFFFFF and 0x66FFFFFF or 0x3344DDFF
                    strokeWidth = max(1f, paint.textSize * 0.008f)
                    alpha = (alphaMul * 0.35f).toInt()
                }
                val step = paint.textSize * 0.22f
                var gx = x - paint.textSize * 0.6f
                while (gx <= x + paint.textSize * 0.6f) {
                    canvas.drawLine(gx, baseY - paint.textSize * 0.4f, gx, baseY + totalHeight + paint.textSize * 0.2f, gridP)
                    gx += step
                }
                var gy = baseY - paint.textSize * 0.3f
                while (gy <= baseY + totalHeight + paint.textSize * 0.3f) {
                    canvas.drawLine(x - paint.textSize * 0.6f, gy, x + paint.textSize * 0.6f, gy, gridP)
                    gy += step
                }
            } finally {
                canvas.restore()
            }
        }

        // 扫描线
        if (p.optBoolean("scanline", true)) {
            val scanP = Paint().apply {
                color = 0x220AFFFF
                strokeWidth = max(1.5f, paint.textSize * 0.012f)
            }
            var sy = baseY + paint.textSize * 0.12f
            while (sy < baseY + totalHeight) {
                canvas.drawLine(x - paint.textSize * 0.55f, sy, x + paint.textSize * 0.55f, sy, scanP)
                sy += paint.textSize * 0.16f
            }
        }

        // 强调角标线（科技感）
        val cornerP = Paint().apply {
            style = Paint.Style.STROKE
            strokeWidth = max(2f, paint.textSize * 0.02f)
            color = 0xAA25F6FF.toInt()
            alpha = (alphaMul * 0.9f).toInt()
        }
        val cw = paint.textSize * 0.62f
        val ch = paint.textSize * 0.22f
        fun corner(left: Boolean) {
            val sx = if (left) x - cw else x + cw
            val sy = baseY - ch - paint.textSize * 0.12f
            if (left) {
                canvas.drawLine(sx, sy + ch, sx, sy, cornerP)
                canvas.drawLine(sx, sy, sx + ch, sy, cornerP)
            } else {
                canvas.drawLine(sx, sy + ch, sx, sy, cornerP)
                canvas.drawLine(sx, sy, sx - ch, sy, cornerP)
            }
        }
        corner(true)
        corner(false)
    }
}

/** 轻量 ARGB 解析辅助（用于透明度计算） */
private object Color {
    fun alpha(argb: Int): Float = ((argb ushr 24) and 0xFF) / 255f
}