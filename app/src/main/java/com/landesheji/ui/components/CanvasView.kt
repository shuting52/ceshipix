package com.landesheji.ui.components

import android.graphics.Paint
import android.graphics.Typeface
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.landesheji.data.model.CanvasConfig
import com.landesheji.data.model.LayerItem
import com.landesheji.data.model.LayerType
import com.landesheji.ui.theme.KawaiiBg
import com.landesheji.ui.theme.KawaiiCardTint
import com.landesheji.ui.theme.KawaiiLavender
import com.landesheji.ui.theme.KawaiiMint
import com.landesheji.ui.theme.KawaiiMintLight
import com.landesheji.ui.theme.KawaiiOutline
import com.landesheji.ui.theme.KawaiiPink
import com.landesheji.ui.theme.KawaiiPinkLight
import com.landesheji.ui.theme.KawaiiShadow
import com.landesheji.ui.theme.KawaiiSkyBlue
import com.landesheji.ui.theme.KawaiiSurface
import com.landesheji.ui.theme.KawaiiTextMuted
import com.landesheji.ui.theme.KawaiiTextPrimary
import com.landesheji.ui.theme.KawaiiTextSecondary
import com.landesheji.ui.theme.KawaiiTextWhite
import com.landesheji.ui.theme.KawaiiYellow
import com.landesheji.ui.theme.KawaiiYellowLight
import kotlin.math.abs
import kotlin.math.roundToInt

@Composable
fun CanvasView(
    canvasConfig: CanvasConfig,
    layers: List<LayerItem>,
    selectedLayerId: String?,
    isGridVisible: Boolean,
    isSnapEnabled: Boolean,
    isZoomMode: Boolean,
    zoomScale: Float,
    zoomOffset: Pair<Float, Float>,
    onSelectLayer: (String?) -> Unit,
    onMoveLayer: (id: String, dx: Float, dy: Float) -> Unit,
    onTransformCompleted: () -> Unit,
    onTransformLayer: (id: String, scaleFactor: Float, rotationDelta: Float) -> Unit,
    onDeleteLayer: (id: String) -> Unit,
    onDuplicateLayer: (id: String) -> Unit,
    onEditLayer: (id: String) -> Unit,
    onStepFontSize: (id: String, delta: Float) -> Unit,
    onScaleLayerOnly: (id: String, factor: Float) -> Unit,
    onResetLayerTransform: (id: String) -> Unit,
    onUpdateZoom: (scaleChange: Float, panChangeX: Float, panChangeY: Float) -> Unit,
    onResetZoom: () -> Unit,
    onCanvasDisplayWidth: (Float) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val selectedLayer = layers.find { it.id == selectedLayerId }
    val canvasAspectRatio = (canvasConfig.width.toFloat() / canvasConfig.height.toFloat().coerceAtLeast(1f))
        .coerceIn(0.2f, 5.0f)

    // 需求6：为 IMAGE 图层（含自定义导入贴纸）异步加载位图
    val context = LocalContext.current
    var imageBitmaps by remember { mutableStateOf<Map<String, android.graphics.Bitmap>>(emptyMap()) }
    LaunchedEffect(layers) {
        val uris = layers.filter { it.type == LayerType.IMAGE && it.imageProps.uriString.isNotEmpty() }
            .map { it.imageProps.uriString }.distinct()
        val loaded = mutableMapOf<String, android.graphics.Bitmap>()
        uris.forEach { uri ->
            if (!imageBitmaps.containsKey(uri)) {
                try {
                    val input = context.contentResolver.openInputStream(Uri.parse(uri))
                    if (input != null) {
                        val bmp = android.graphics.BitmapFactory.decodeStream(input)
                        input.close()
                        if (bmp != null) loaded[uri] = bmp
                    }
                } catch (_: Exception) { }
            }
        }
        if (loaded.isNotEmpty()) {
            imageBitmaps = imageBitmaps + loaded
        }
    }

    var snapActiveX by remember { mutableStateOf(false) }
    var snapActiveY by remember { mutableStateOf(false) }

    val density = LocalDensity.current

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Transparent)
            .testTag("canvas_viewport"),
        contentAlignment = Alignment.Center
    ) {
        val viewportWidthPx = constraints.maxWidth.toFloat()
        val viewportHeightPx = constraints.maxHeight.toFloat()

        // 需求2优化：画布与手机左右屏幕对齐（宽度铺满视口），高度按比例适配
        var fittedWPx: Float
        var fittedHPx: Float
        // 宽度优先：让画布宽度对齐手机屏幕宽度（仅保留极小的内边距避免贴边）
        val widthFillFactor = 0.985f
        val heightFillFactor = 0.92f
        val maxAvailableWPx = viewportWidthPx * widthFillFactor
        val maxAvailableHPx = viewportHeightPx * heightFillFactor
        if (maxAvailableWPx / maxAvailableHPx > canvasAspectRatio) {
            // 宽度仍有余地则按宽度铺满
            fittedWPx = maxAvailableWPx
            fittedHPx = fittedWPx / canvasAspectRatio
            if (fittedHPx > maxAvailableHPx) {
                fittedHPx = maxAvailableHPx
                fittedWPx = fittedHPx * canvasAspectRatio
            }
        } else {
            fittedWPx = maxAvailableWPx
            fittedHPx = fittedWPx / canvasAspectRatio
            if (fittedHPx > maxAvailableHPx) {
                fittedHPx = maxAvailableHPx
                fittedWPx = fittedHPx * canvasAspectRatio
            }
        }

        // 需求4修复：把画布实际显示宽度上报（导出时按同一比例映射，保证所见即所得）
        LaunchedEffect(fittedWPx) {
            onCanvasDisplayWidth(fittedWPx)
        }

        val fittedWDp = with(density) { fittedWPx.toDp() }
        val fittedHDp = with(density) { fittedHPx.toDp() }

        // Outer container handles canvas zoom & pan
        // 需求2（新版）：双指手势——若选中了文字/图层，则捏合缩放“选中图层”（放大文字），而非缩放画布；
        // 仅“画布缩放模式”下才缩放画布本身；缩放模式下支持拖动平移
        Box(
            modifier = Modifier
                .size(fittedWDp, fittedHDp)
                .graphicsLayer {
                    scaleX = zoomScale
                    scaleY = zoomScale
                    translationX = zoomOffset.first
                    translationY = zoomOffset.second
                }
                .pointerInput(isZoomMode, selectedLayerId) {
                    if (isZoomMode) {
                        // 缩放模式：支持双指缩放 + 单指/双指拖动平移
                        detectTransformGestures { _, pan, zoom, _ ->
                            onUpdateZoom(zoom, pan.x, pan.y)
                        }
                    } else {
                        val selLayer = layers.find { it.id == selectedLayerId }
                        if (selLayer != null && !selLayer.isLocked) {
                            // 日常模式：双指捏合直接放大/缩小“选中的文字/图层”
                            detectTransformGestures { _, _, zoom, _ ->
                                if (zoom != 1f) {
                                    onTransformLayer(selLayer.id, zoom, 0f)
                                }
                            }
                        } else {
                            // 无选中层时双指缩画布（保持可用性）
                            detectTransformGestures { _, _, zoom, _ ->
                                if (zoom != 1f) {
                                    onUpdateZoom(zoom, 0f, 0f)
                                }
                            }
                        }
                    }
                }
                .pointerInput(isZoomMode) {
                    if (!isZoomMode) {
                        detectTapGestures(
                            onTap = {
                                onSelectLayer(null)
                            }
                        )
                    }
                },
            contentAlignment = Alignment.TopStart
        ) {
            // Main Canvas surface with Neo-Brutalist shadow & border
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .kawaiiShadow(shadowOffset = 4.dp, cornerRadius = 12.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .clipToBounds()
                    .border(2.5.dp, KawaiiOutline, RoundedCornerShape(12.dp))
                    .testTag("inner_canvas_area")
            ) {
                // Background and Layers Drawing Canvas
                Canvas(
                    modifier = Modifier.fillMaxSize()
                ) {
                    val canvasCenter = Offset(size.width / 2f, size.height / 2f)

                    // 1. Draw Background
                    CanvasRenderer.drawCanvasBackground(this, canvasConfig, size)

                    // 2. Draw all layers in order (需求6：传入本地图片位图缓存)
                    layers.forEach { layer ->
                        CanvasRenderer.drawLayer(this, layer, canvasCenter, imageBitmaps)
                    }

                    // 3. Draw Grid if enabled
                    if (isGridVisible) {
                        val gridCols = 8
                        val gridRows = 8
                        val colStep = size.width / gridCols
                        val rowStep = size.height / gridRows
                        val gridPaint = Color.White.copy(alpha = 0.22f)

                        for (i in 1 until gridCols) {
                            drawLine(
                                color = gridPaint,
                                start = Offset(i * colStep, 0f),
                                end = Offset(i * colStep, size.height),
                                strokeWidth = 1.2f,
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f))
                            )
                        }
                        for (j in 1 until gridRows) {
                            drawLine(
                                color = gridPaint,
                                start = Offset(0f, j * rowStep),
                                end = Offset(size.width, j * rowStep),
                                strokeWidth = 1.2f,
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f))
                            )
                        }

                        // Center guide crosshairs in KawaiiPink
                        drawLine(
                            color = KawaiiPink.copy(alpha = 0.6f),
                            start = Offset(size.width / 2f, 0f),
                            end = Offset(size.width / 2f, size.height),
                            strokeWidth = 2f
                        )
                        drawLine(
                            color = KawaiiPink.copy(alpha = 0.6f),
                            start = Offset(0f, size.height / 2f),
                            end = Offset(size.width, size.height / 2f),
                            strokeWidth = 2f
                        )
                    }

                    // 4. Center Snap indicators
                    if (isSnapEnabled && selectedLayer != null) {
                        if (snapActiveX) {
                            drawLine(
                                color = KawaiiMint,
                                start = Offset(size.width / 2f, 0f),
                                end = Offset(size.width / 2f, size.height),
                                strokeWidth = 2.5f
                            )
                        }
                        if (snapActiveY) {
                            drawLine(
                                color = KawaiiMint,
                                start = Offset(0f, size.height / 2f),
                                end = Offset(size.width, size.height / 2f),
                                strokeWidth = 2.5f
                            )
                        }
                    }
                }

                // Interactive touch targets for each layer
                val canvasCenterPx = Offset(fittedWPx / 2f, fittedHPx / 2f)

                layers.forEach { layer ->
                    val isSelected = layer.id == selectedLayerId
                    LayerTouchOverlay(
                        layer = layer,
                        isSelected = isSelected,
                        isZoomMode = isZoomMode,
                        canvasCenterPx = canvasCenterPx,
                        onSelect = { onSelectLayer(layer.id) },
                        onMove = { dx, dy ->
                            var finalDx = dx
                            var finalDy = dy
                            if (isSnapEnabled) {
                                val currentX = layer.x + dx
                                val currentY = layer.y + dy
                                snapActiveX = abs(currentX) < 14f
                                snapActiveY = abs(currentY) < 14f
                                if (snapActiveX) finalDx = -layer.x
                                if (snapActiveY) finalDy = -layer.y
                            } else {
                                snapActiveX = false
                                snapActiveY = false
                            }
                            onMoveLayer(layer.id, finalDx, finalDy)
                        },
                        onMoveEnd = {
                            snapActiveX = false
                            snapActiveY = false
                            onTransformCompleted()
                        },
                        onTransform = { scaleDelta, rotDelta ->
                            onTransformLayer(layer.id, scaleDelta, rotDelta)
                        },
                        onDelete = { onDeleteLayer(layer.id) },
                        onDuplicate = { onDuplicateLayer(layer.id) },
                        onEdit = { onEditLayer(layer.id) },
                        onStepFontSize = { delta -> onStepFontSize(layer.id, delta) },
                        onScaleLayer = { factor -> onScaleLayerOnly(layer.id, factor) },
                        onResetTransform = { onResetLayerTransform(layer.id) }
                    )
                }
            }
        }

        // Floating Zoom Mode badge & reset button (Neo-Brutalist design)
        AnimatedVisibility(
            visible = isZoomMode,
            enter = fadeIn() + scaleIn(spring(dampingRatio = Spring.DampingRatioMediumBouncy)),
            exit = fadeOut() + scaleOut(),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .kawaiiShadow(shadowOffset = 3.dp, cornerRadius = 10.dp)
                    .jellyClickable { onResetZoom() }
                    .clip(RoundedCornerShape(10.dp))
                    .background(KawaiiYellow)
                    .border(2.dp, KawaiiOutline, RoundedCornerShape(10.dp))
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "🔍 画布缩放: ${(zoomScale * 100).toInt()}% (点此复位)",
                    color = KawaiiTextPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black
                )
            }
        }
    }
}

@Composable
private fun LayerTouchOverlay(
    layer: LayerItem,
    isSelected: Boolean,
    isZoomMode: Boolean,
    canvasCenterPx: Offset,
    onSelect: () -> Unit,
    onMove: (dx: Float, dy: Float) -> Unit,
    onMoveEnd: () -> Unit,
    onTransform: (scaleDelta: Float, rotDelta: Float) -> Unit,
    onDelete: () -> Unit,
    onDuplicate: () -> Unit,
    onEdit: () -> Unit,
    onStepFontSize: (delta: Float) -> Unit,
    onScaleLayer: (factor: Float) -> Unit,
    onResetTransform: () -> Unit
) {
    if (!layer.isVisible || isZoomMode) return

    val density = LocalDensity.current

    // Accurately compute layer bounding box in pixels based on its content
    val (unscaledWidthPx, unscaledHeightPx) = remember(
        layer.type,
        layer.textProps.fontSize,
        layer.textProps.text,
        layer.textProps.lineSpacing,
        layer.textProps.letterSpacing,
        layer.textProps.isBold,
        layer.textProps.isItalic,
        layer.textProps.hasBackgroundBox,
        layer.textProps.backgroundBoxPadding
    ) {
        when (layer.type) {
            LayerType.TEXT -> {
                val lines = layer.textProps.text.split("\n")
                val baseFontSize = layer.textProps.fontSize * 1.5f
                val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    textSize = baseFontSize
                    typeface = when {
                        layer.textProps.isBold && layer.textProps.isItalic -> Typeface.create(Typeface.DEFAULT, Typeface.BOLD_ITALIC)
                        layer.textProps.isBold -> Typeface.DEFAULT_BOLD
                        layer.textProps.isItalic -> Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
                        else -> Typeface.DEFAULT
                    }
                    letterSpacing = layer.textProps.letterSpacing / 10f
                }
                val fm = paint.fontMetrics
                val lineHeight = (fm.descent - fm.ascent) * layer.textProps.lineSpacing
                val rawTextWidth = lines.maxOfOrNull { paint.measureText(it) } ?: 100f
                val rawTextHeight = (lines.size * lineHeight).coerceAtLeast(40f)
                val extraPad = if (layer.textProps.hasBackgroundBox) layer.textProps.backgroundBoxPadding * 4f else 32f

                Pair(
                    (rawTextWidth + extraPad).coerceAtLeast(90f),
                    (rawTextHeight + extraPad).coerceAtLeast(50f)
                )
            }
            LayerType.SHAPE -> Pair(160f, 160f)
            LayerType.STICKER -> Pair(130f, 130f)
            LayerType.IMAGE -> Pair(180f, 180f)
            LayerType.DRAW -> Pair(180f, 180f)
        }
    }

    val boxWidthPx = (unscaledWidthPx * layer.scaleX).coerceAtLeast(60f)
    val boxHeightPx = (unscaledHeightPx * layer.scaleY).coerceAtLeast(40f)

    // Center of this layer in canvas pixels
    val layerCenterX = canvasCenterPx.x + layer.x
    val layerCenterY = canvasCenterPx.y + layer.y

    // Top-left of overlay in canvas pixels
    val leftPx = layerCenterX - (boxWidthPx / 2f)
    val topPx = layerCenterY - (boxHeightPx / 2f)

    val boxWidthDp = with(density) { boxWidthPx.toDp() }
    val boxHeightDp = with(density) { boxHeightPx.toDp() }

    Box(
        modifier = Modifier
            .offset {
                IntOffset(
                    x = leftPx.roundToInt(),
                    y = topPx.roundToInt()
                )
            }
            .size(boxWidthDp, boxHeightDp)
            .graphicsLayer {
                rotationZ = layer.rotation
            }
            // Multi-touch gestures: support pinch-to-scale and rotate directly on the layer
            .pointerInput(layer.id, layer.isLocked) {
                if (!layer.isLocked) {
                    detectTransformGestures(
                        onGesture = { _, pan, zoom, rotation ->
                            if (zoom != 1f || rotation != 0f) {
                                onTransform(zoom, rotation)
                            }
                            if (pan != Offset.Zero) {
                                onMove(pan.x, pan.y)
                            }
                        }
                    )
                }
            }
            .pointerInput(layer.id) {
                detectTapGestures(
                    onTap = { onSelect() },
                    onDoubleTap = {
                        onSelect()
                        if (layer.type == LayerType.TEXT) {
                            onEdit()
                        }
                    }
                )
            }
    ) {
        if (isSelected && !layer.isLocked) {
            // Kawaii Cute Cartoon Anime Selection Outline
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .border(
                        width = 2.5.dp,
                        color = KawaiiPink,
                        shape = RoundedCornerShape(10.dp)
                    )
            )

            // Top-Left: Delete Handle (Bubblegum Pink Mochi)
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset((-12).dp, (-12).dp)
                    .size(28.dp)
                    .kawaiiShadow(shadowOffset = 2.dp, cornerRadius = 14.dp)
                    .jellyClickable { onDelete() }
                    .background(KawaiiPink, CircleShape)
                    .border(2.dp, KawaiiOutline, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "✕", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Black)
            }

            // Top-Right: Rotate Handle (Lavender Mochi)
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(12.dp, (-12).dp)
                    .size(28.dp)
                    .kawaiiShadow(shadowOffset = 2.dp, cornerRadius = 14.dp)
                    .background(KawaiiLavender, CircleShape)
                    .border(2.dp, KawaiiOutline, CircleShape)
                    .pointerInput(layer.id) {
                        detectDragGestures(
                            onDrag = { change, dragAmount ->
                                change.consume()
                                val rotDelta = (dragAmount.x - dragAmount.y) * 0.5f
                                onTransform(1f, rotDelta)
                            },
                            onDragEnd = { onMoveEnd() }
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(text = "↻", color = KawaiiTextWhite, fontSize = 14.sp, fontWeight = FontWeight.Black)
            }

            // Bottom-Left: Edit Handle (Mint Soda Mochi)
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .offset((-12).dp, 12.dp)
                    .size(28.dp)
                    .kawaiiShadow(shadowOffset = 2.dp, cornerRadius = 14.dp)
                    .jellyClickable { onEdit() }
                    .background(KawaiiMint, CircleShape)
                    .border(2.dp, KawaiiOutline, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "✎", color = KawaiiTextWhite, fontSize = 12.sp, fontWeight = FontWeight.Black)
            }

            // Bottom-Right: Dedicated Scale / Enlarge Handle (Custard Honey Mochi)
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(12.dp, 12.dp)
                    .size(32.dp)
                    .kawaiiShadow(shadowOffset = 3.dp, cornerRadius = 16.dp)
                    .background(KawaiiYellow, CircleShape)
                    .border(2.dp, KawaiiOutline, CircleShape)
                    .pointerInput(layer.id) {
                        detectDragGestures(
                            onDrag = { change, dragAmount ->
                                change.consume()
                                // Dragging down-right enlarges; dragging up-left shrinks
                                val dragDistance = dragAmount.x + dragAmount.y
                                val scaleFactor = 1f + (dragDistance / 100f)
                                onTransform(scaleFactor, 0f)
                            },
                            onDragEnd = { onMoveEnd() }
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(text = "⤢", color = KawaiiTextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Black)
            }

            // Quick Floating Zoom & Action Pill directly above the selected layer
            // 需求1：选中文字层时不再显示该浮层，避免干扰排版（用户可在底部工具栏精确调整字号/缩放）
            if (layer.type != LayerType.TEXT) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = (-44).dp)
                    .kawaiiShadow(shadowOffset = 3.dp, cornerRadius = 20.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(KawaiiSurface)
                    .border(2.dp, KawaiiOutline, RoundedCornerShape(20.dp))
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                        // Scale down
                        Box(
                            modifier = Modifier
                                .jellyClickable { onScaleLayer(0.85f) }
                                .clip(RoundedCornerShape(8.dp))
                                .background(KawaiiCardTint)
                                .border(1.dp, KawaiiOutline, RoundedCornerShape(8.dp))
                                .padding(horizontal = 7.dp, vertical = 2.dp)
                        ) {
                            Text(text = "➖ 缩小", color = KawaiiTextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        // Scale up
                        Box(
                            modifier = Modifier
                                .jellyClickable { onScaleLayer(1.15f) }
                                .clip(RoundedCornerShape(8.dp))
                                .background(KawaiiYellow)
                                .border(1.dp, KawaiiOutline, RoundedCornerShape(8.dp))
                                .padding(horizontal = 7.dp, vertical = 2.dp)
                        ) {
                            Text(text = "➕ 放大", color = KawaiiTextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Black)
                        }
                    }

                    // Reset 100%
                    Box(
                        modifier = Modifier
                            .jellyClickable { onResetTransform() }
                            .clip(RoundedCornerShape(8.dp))
                            .background(KawaiiMintLight)
                            .border(1.dp, KawaiiOutline, RoundedCornerShape(8.dp))
                            .padding(horizontal = 7.dp, vertical = 2.dp)
                    ) {
                        Text(text = "↺ 100%", color = KawaiiMint, fontSize = 10.sp, fontWeight = FontWeight.Black)
                    }
                }
            }
        }
    }
}
