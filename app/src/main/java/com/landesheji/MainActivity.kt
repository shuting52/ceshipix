package com.landesheji

import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import com.landesheji.data.model.BackgroundType
import com.landesheji.data.model.LayerType
import com.landesheji.ui.components.UiThemeBackground
import com.landesheji.ui.components.BackgroundToolBar
import com.landesheji.ui.components.CanvasView
import com.landesheji.ui.components.EffectsToolBar
import com.landesheji.ui.components.LayersPanel
import com.landesheji.ui.components.PixelLabBottomNav
import com.landesheji.ui.components.PixelLabTopAppBar
import com.landesheji.ui.components.PresetsToolBar
import com.landesheji.ui.components.PsFxSidePanel
import com.landesheji.ui.components.ShapeToolBar
import com.landesheji.ui.components.TextToolBar
import com.landesheji.ui.dialogs.AboutDialog
import com.landesheji.ui.dialogs.CanvasSizeDialog
import com.landesheji.ui.dialogs.DrawingCanvasDialog
import com.landesheji.ui.dialogs.ExportDialog
import com.landesheji.ui.dialogs.ExportFormat
import com.landesheji.ui.dialogs.ExportSourceDialog
import com.landesheji.ui.dialogs.QuotesDialog
import com.landesheji.ui.dialogs.SaveProjectDialog
import com.landesheji.ui.dialogs.ShapesDialog
import com.landesheji.ui.dialogs.StickersDialog
import com.landesheji.ui.dialogs.TextEditorDialog
import com.landesheji.ui.dialogs.UpdateCheckDialog
import com.landesheji.ui.theme.KawaiiBg
import com.landesheji.ui.theme.MyApplicationTheme
import com.landesheji.ui.viewmodel.BottomTab
import com.landesheji.ui.viewmodel.PixelLabViewModel
import com.landesheji.util.BitmapExporter
import com.landesheji.util.SourceCodec
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val viewModel: PixelLabViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                PixelLabMainScreen(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun PixelLabMainScreen(viewModel: PixelLabViewModel) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Observable states
    val canvasConfig by viewModel.canvasConfig.collectAsState()
    val layers by viewModel.layers.collectAsState()
    val selectedLayerId by viewModel.selectedLayerId.collectAsState()
    val activeTab by viewModel.activeTab.collectAsState()
    val isLayersPanelOpen by viewModel.isLayersPanelOpen.collectAsState()
    val isGridVisible by viewModel.isGridVisible.collectAsState()
    val isSnapEnabled by viewModel.isSnapEnabled.collectAsState()
    val isZoomMode by viewModel.isZoomMode.collectAsState()
    val zoomScale by viewModel.zoomScale.collectAsState()
    val zoomOffset by viewModel.zoomOffset.collectAsState()
    val canUndo by viewModel.canUndo.collectAsState()
    val canRedo by viewModel.canRedo.collectAsState()
    val savedProjects by viewModel.savedProjects.collectAsState()
    // 需求1：UI 主题背景（图片/视频应用整个 App 界面）
    val uiBackgroundImageUri by viewModel.uiBackgroundImageUri.collectAsState()
    val uiBackgroundVideoUri by viewModel.uiBackgroundVideoUri.collectAsState()
    // 需求6：自定义贴纸
    val customStickers by viewModel.customStickers.collectAsState()

    // Dialog flags
    var showTextEditor by remember { mutableStateOf(false) }
    var showQuotesDialog by remember { mutableStateOf(false) }
    var showCanvasSizeDialog by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }
    var showSaveProjectDialog by remember { mutableStateOf(false) }
    // 需求4：源码导出/导入
    var showExportSourceDialog by remember { mutableStateOf(false) }
    var showStickersDialog by remember { mutableStateOf(false) }
    var showShapesDialog by remember { mutableStateOf(false) }
    var showDrawingDialog by remember { mutableStateOf(false) }
    // 需求8：正在编辑的涂鸦图层 ID（双击/编辑涂鸦图层时记录）
    var editingDrawLayerId by remember { mutableStateOf<String?>(null) }
    // 需求3：PS FX 侧边面板开关
    var showFxPanel by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    // 需求12：更新检查
    var showUpdateCheckDialog by remember { mutableStateOf(false) }

    // Photo Picker
    val imageLayerPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.addImageLayer(uri.toString())
            Toast.makeText(context, "已添加图片图层", Toast.LENGTH_SHORT).show()
        }
    }

    val backgroundPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.updateCanvasConfig {
                it.copy(
                    backgroundType = BackgroundType.IMAGE_URI,
                    backgroundImageUri = uri.toString()
                )
            }
            Toast.makeText(context, "已更新画布背景图", Toast.LENGTH_SHORT).show()
        }
    }

    // 需求6：本地视频背景选择器（使用 PickVisualMedia，视频/图片都能选）
    val backgroundVideoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            // 需求1：视频应用到软件 UI 主题背景，而非画布
            viewModel.setUiBackgroundVideo(uri.toString())
            Toast.makeText(context, "已设置视频主题背景 🎬", Toast.LENGTH_SHORT).show()
        }
    }

    val backgroundImagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            // 需求1：图片应用到软件 UI 主题背景，而非画布
            viewModel.setUiBackgroundImage(uri.toString())
            Toast.makeText(context, "已设置图片主题背景 🖼️", Toast.LENGTH_SHORT).show()
        }
    }

    // 需求6：导入本地图片作为自定义贴纸
    val stickerImportPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.importCustomSticker(uri.toString())
            Toast.makeText(context, "已导入自定义贴纸，可在「我的贴纸」分类使用", Toast.LENGTH_SHORT).show()
        }
    }

    // 需求4：导入源码（.plp / .psd）
    val sourceImportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            coroutineScope.launch {
                val ext = uri.lastPathSegment?.substringAfterLast('.', "")?.lowercase() ?: ""
                val bytes = SourceCodec.readUriBytes(context, uri)
                when (ext) {
                    "plp" -> {
                        val data = SourceCodec.parsePlp(bytes.inputStream())
                        if (data != null) {
                            viewModel.loadProjectData(data.canvas, data.layers)
                            Toast.makeText(context, "已导入 PixelLab 源码工程 📦", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "无法解析此 .plp 文件", Toast.LENGTH_SHORT).show()
                        }
                    }
                    "psd" -> {
                        val result = SourceCodec.parsePsd(bytes.inputStream())
                        if (result.plp != null) {
                            viewModel.loadProjectData(result.plp.canvas, result.plp.layers)
                            Toast.makeText(context, "已导入 PSD 源数据，可继续编辑 ✨", Toast.LENGTH_SHORT).show()
                        } else if (result.bitmap != null) {
                            // 外部 PSD → 作为位图图层导入
                            val saved = BitmapExporter.saveBitmapToCache(context, result.bitmap, ExportFormat.PNG)
                            if (saved != null) {
                                viewModel.addImageLayer(saved.toString())
                                Toast.makeText(context, "已导入 PSD 为图片图层 🖼️", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "PSD 导入失败", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            Toast.makeText(context, "无法解析此 .psd 文件", Toast.LENGTH_SHORT).show()
                        }
                    }
                    else -> Toast.makeText(context, "请选择 .plp 或 .psd 源码文件", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    val selectedLayer = layers.find { it.id == selectedLayerId }

    // 需求1：整个 App 界面以用户上传的图片/视频为主题背景
    Box(modifier = Modifier.fillMaxSize()) {
        UiThemeBackground(
            imageUri = uiBackgroundImageUri,
            videoUri = uiBackgroundVideoUri
        )

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .testTag("pixellab_main_scaffold"),
        containerColor = Color(0x66FFFFFF),
        topBar = {
            PixelLabTopAppBar(
                canUndo = canUndo,
                canRedo = canRedo,
                isZoomMode = isZoomMode,
                isGridVisible = isGridVisible,
                isLayersPanelOpen = isLayersPanelOpen,
                layerCount = layers.size,
                onAddText = { viewModel.addTextLayer() },
                onAddDate = { viewModel.addCurrentDateLayer() },
                onAddSticker = { showStickersDialog = true },
                onAddShape = { showShapesDialog = true },
                onImportImage = {
                    imageLayerPickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
                onStartDraw = { showDrawingDialog = true },
                onUploadBackgroundImage = {
                    backgroundImagePickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
                onUploadBackgroundVideo = {
                    backgroundVideoPickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)
                    )
                },
                onToggleBlurBackground = {
                    viewModel.updateCanvasConfig {
                        it.copy(backgroundBlur = if (it.backgroundBlur > 0f) 0f else 0.6f)
                    }
                    Toast.makeText(context, "已切换毛玻璃背景效果", Toast.LENGTH_SHORT).show()
                },
                onToggleNeonBackground = {
                    viewModel.updateCanvasConfig {
                        it.copy(backgroundNeonGlow = if (it.backgroundNeonGlow > 0f) 0f else 0.8f)
                    }
                    Toast.makeText(context, "已切换霓虹光晕背景效果", Toast.LENGTH_SHORT).show()
                },
                onToggleInvertBackground = {
                    viewModel.updateCanvasConfig {
                        it.copy(backgroundInvert = !it.backgroundInvert)
                    }
                    Toast.makeText(context, "已切换反色背景效果", Toast.LENGTH_SHORT).show()
                },
                onSaveProject = { showSaveProjectDialog = true },
                onSaveImage = { showExportDialog = true },
                onShare = { showExportDialog = true },
                onOpenQuotes = { showQuotesDialog = true },
                onExportSource = { showExportSourceDialog = true },
                onImportSource = {
                    sourceImportLauncher.launch(arrayOf("application/octet-stream", "*/*"))
                },
                onUndo = { viewModel.undo() },
                onRedo = { viewModel.redo() },
                onToggleZoom = { viewModel.toggleZoomMode() },
                onToggleGrid = { viewModel.toggleGrid() },
                onToggleLayers = { viewModel.toggleLayersPanel() },
                onOpenCanvasSize = { showCanvasSizeDialog = true },
                onClearCanvas = {
                    viewModel.clearCanvas()
                    Toast.makeText(context, "画布已清空", Toast.LENGTH_SHORT).show()
                },
                onShowAbout = { showAboutDialog = true }
            )
        },
        bottomBar = {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Secondary editing tool shelf depending on selected bottom tab
                when (activeTab) {
                    BottomTab.PRESETS -> {
                        PresetsToolBar(
                            savedProjects = savedProjects,
                            onSelectTextTemplate = { template ->
                                viewModel.applyTextTemplate(template)
                                Toast.makeText(context, "已套用模板: ${template.name}，可点文字直接编辑", Toast.LENGTH_SHORT).show()
                            },
                            onLoadProject = { proj ->
                                viewModel.loadProject(proj)
                                Toast.makeText(context, "已加载工程: ${proj.title}", Toast.LENGTH_SHORT).show()
                            },
                            onDeleteProject = { id ->
                                viewModel.deleteProject(id)
                                Toast.makeText(context, "已删除工程", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }

                    BottomTab.TEXT -> {
                        TextToolBar(
                            selectedLayer = if (selectedLayer?.type == LayerType.TEXT) selectedLayer else null,
                            onAddText = { viewModel.addTextLayer() },
                            onOpenQuotes = { showQuotesDialog = true },
                            onOpenEditor = { showTextEditor = true },
                            onDelete = {
                                selectedLayerId?.let { viewModel.deleteLayer(it) }
                            },
                            onDuplicate = {
                                selectedLayerId?.let { viewModel.duplicateLayer(it) }
                            },
                            onToFront = {
                                selectedLayerId?.let { viewModel.bringLayerToFront(it) }
                            },
                            onToBack = {
                                selectedLayerId?.let { viewModel.sendLayerToBack(it) }
                            },
                            onMoveDelta = { dx, dy ->
                                selectedLayerId?.let {
                                    viewModel.updateLayerPositionSilent(it, dx, dy)
                                    viewModel.recordTransformCompleted()
                                }
                            },
                            onUpdateTextProps = { transform ->
                                viewModel.updateSelectedLayer { layer ->
                                    layer.copy(textProps = transform(layer.textProps))
                                }
                            },
                            onUpdateLayerOpacity = { opacity ->
                                viewModel.updateSelectedLayer { it.copy(opacity = opacity) }
                            },
                            onStepFontSize = { delta ->
                                selectedLayerId?.let { viewModel.stepTextFontSize(it, delta) }
                            },
                            onSetFontSize = { size ->
                                selectedLayerId?.let { viewModel.setTextFontSize(it, size, false) }
                            },
                            onScaleLayer = { factor ->
                                selectedLayerId?.let { viewModel.updateLayerScaleOnly(it, factor) }
                            },
                            onSetExactScale = { exact ->
                                selectedLayerId?.let { viewModel.setLayerExactScale(it, exact) }
                            },
                            onResetTransform = {
                                selectedLayerId?.let { viewModel.resetLayerTransform(it) }
                            },
                            // 需求3：打开右侧 PS FX 侧边面板
                            onOpenFxPanel = { showFxPanel = true }
                        )
                    }

                    BottomTab.SHAPES -> {
                        ShapeToolBar(
                            selectedLayer = selectedLayer,
                            onOpenStickers = { showStickersDialog = true },
                            onOpenShapes = { showShapesDialog = true },
                            onImportImage = {
                                imageLayerPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            onStartDraw = { showDrawingDialog = true },
                            onDelete = {
                                selectedLayerId?.let { viewModel.deleteLayer(it) }
                            },
                            onDuplicate = {
                                selectedLayerId?.let { viewModel.duplicateLayer(it) }
                            },
                            onToFront = {
                                selectedLayerId?.let { viewModel.bringLayerToFront(it) }
                            },
                            onToBack = {
                                selectedLayerId?.let { viewModel.sendLayerToBack(it) }
                            },
                            onUpdateShapeProps = { transform ->
                                viewModel.updateSelectedLayer { layer ->
                                    layer.copy(shapeProps = transform(layer.shapeProps))
                                }
                            },
                            onUpdateOpacity = { opacity ->
                                viewModel.updateSelectedLayer { it.copy(opacity = opacity) }
                            }
                        )
                    }

                    BottomTab.BACKGROUND -> {
                        BackgroundToolBar(
                            canvasConfig = canvasConfig,
                            onUpdateConfig = { transform ->
                                viewModel.updateCanvasConfig(transform)
                            },
                            onOpenSizeDialog = { showCanvasSizeDialog = true },
                            onPickImageFromGallery = {
                                backgroundImagePickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            onPickVideoFromGallery = {
                                backgroundVideoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)
                                )
                            }
                        )
                    }

                    BottomTab.EFFECTS -> {
                        EffectsToolBar(
                            canvasConfig = canvasConfig,
                            onUpdateConfig = { transform ->
                                viewModel.updateCanvasConfig(transform)
                            }
                        )
                    }
                }

                // 5 Bottom Nav Tabs
                PixelLabBottomNav(
                    activeTab = activeTab,
                    onTabSelected = { tab ->
                        viewModel.setActiveTab(tab)
                    }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Main Central Interactive Canvas
            CanvasView(
                canvasConfig = canvasConfig,
                layers = layers,
                selectedLayerId = selectedLayerId,
                isGridVisible = isGridVisible,
                isSnapEnabled = isSnapEnabled,
                isZoomMode = isZoomMode,
                zoomScale = zoomScale,
                zoomOffset = zoomOffset,
                onSelectLayer = { viewModel.selectLayer(it) },
                onMoveLayer = { id, dx, dy -> viewModel.updateLayerPositionSilent(id, dx, dy) },
                onTransformCompleted = { viewModel.recordTransformCompleted() },
                onTransformLayer = { id, scale, rot -> viewModel.updateLayerTransform(id, scale, rot) },
                onDeleteLayer = { viewModel.deleteLayer(it) },
                onDuplicateLayer = { viewModel.duplicateLayer(it) },
                onEditLayer = { id ->
                    // 需求8：双击编辑 —— 文字图层编辑文字，涂鸦图层重新编辑画笔
                    val target = layers.find { it.id == id }
                    if (target?.type == LayerType.DRAW) {
                        editingDrawLayerId = id
                        showDrawingDialog = true
                    } else if (target?.type == LayerType.TEXT) {
                        viewModel.selectLayer(id)
                        showTextEditor = true
                    } else {
                        viewModel.selectLayer(id)
                    }
                },
                onStepFontSize = { id, delta -> viewModel.stepTextFontSize(id, delta) },
                onScaleLayerOnly = { id, factor -> viewModel.updateLayerScaleOnly(id, factor) },
                onResetLayerTransform = { id -> viewModel.resetLayerTransform(id) },
                onUpdateZoom = { scale, px, py -> viewModel.updateZoom(scale, px, py) },
                onResetZoom = { viewModel.resetZoom() },
                onCanvasDisplayWidth = { viewModel.setCanvasDisplayWidth(it) }
            )

            // Dimmed overlay background behind Layers Panel if open
            AnimatedVisibility(
                visible = isLayersPanelOpen,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.45f))
                        .clickable { viewModel.setLayersPanelOpen(false) }
                )
            }

            // Floating Layers Management Panel (Slide from Right)
            LayersPanel(
                isOpen = isLayersPanelOpen,
                layers = layers,
                selectedLayerId = selectedLayerId,
                onClose = { viewModel.setLayersPanelOpen(false) },
                onSelectLayer = { id -> viewModel.selectLayer(id) },
                onToggleVisibility = { id -> viewModel.toggleLayerVisibility(id) },
                onToggleLock = { id -> viewModel.toggleLayerLock(id) },
                onDuplicateLayer = { id -> viewModel.duplicateLayer(id) },
                onDeleteLayer = { id -> viewModel.deleteLayer(id) },
                onMoveUp = { id -> viewModel.moveLayerUp(id) },
                onMoveDown = { id -> viewModel.moveLayerDown(id) },
                modifier = Modifier.align(Alignment.CenterEnd)
            )

            // 需求3：PS FX 全面化侧边面板（右侧悬浮，边调边看实时预览）
            if (showFxPanel && selectedLayer?.type == LayerType.TEXT) {
                // 半透明遮罩
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.2f))
                        .clickable { showFxPanel = false }
                )
                PsFxSidePanel(
                    props = selectedLayer.textProps,
                    opacity = selectedLayer.opacity,
                    onUpdateProps = { transform ->
                        viewModel.updateSelectedLayer { layer ->
                            layer.copy(textProps = transform(layer.textProps))
                        }
                    },
                    onUpdateOpacity = { newOpacity ->
                        viewModel.updateSelectedLayer { layer ->
                            layer.copy(opacity = newOpacity)
                        }
                    },
                    onClose = { showFxPanel = false },
                    modifier = Modifier.align(Alignment.CenterEnd)
                )
            }
        }
    }

    // Dialogs
    if (showTextEditor && selectedLayer?.type == LayerType.TEXT) {
        TextEditorDialog(
            initialText = selectedLayer.textProps.text,
            onDismiss = { showTextEditor = false },
            onConfirm = { newText ->
                viewModel.updateSelectedLayer { layer ->
                    layer.copy(textProps = layer.textProps.copy(text = newText))
                }
            },
            onDelete = {
                selectedLayerId?.let { viewModel.deleteLayer(it) }
            }
        )
    }

    if (showQuotesDialog) {
        QuotesDialog(
            onDismiss = { showQuotesDialog = false },
            onSelectQuote = { quote ->
                viewModel.addQuoteLayer(quote)
                Toast.makeText(context, "已插入金句", Toast.LENGTH_SHORT).show()
            }
        )
    }

    if (showCanvasSizeDialog) {
        CanvasSizeDialog(
            currentWidth = canvasConfig.width,
            currentHeight = canvasConfig.height,
            onDismiss = { showCanvasSizeDialog = false },
            onConfirm = { w, h, ratioName ->
                viewModel.setCanvasSize(w, h, ratioName)
                Toast.makeText(context, "已调整画布: $w × $h", Toast.LENGTH_SHORT).show()
            }
        )
    }

    if (showStickersDialog) {
        StickersDialog(
            onDismiss = { showStickersDialog = false },
            customStickers = customStickers,
            onImportSticker = {
                stickerImportPickerLauncher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
                showStickersDialog = false
            },
            onSelectSticker = { sticker ->
                if (sticker.imageUri.isNotEmpty()) {
                    // 自定义贴纸 → 作为图片图层添加
                    viewModel.addImageLayer(sticker.imageUri)
                    Toast.makeText(context, "已添加自定义贴纸: ${sticker.name}", Toast.LENGTH_SHORT).show()
                } else {
                    viewModel.addStickerLayer(sticker)
                    Toast.makeText(context, "已添加贴纸: ${sticker.name}", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    if (showShapesDialog) {
        ShapesDialog(
            onDismiss = { showShapesDialog = false },
            onSelectShape = { shapeType ->
                viewModel.addShapeLayer(shapeType)
                Toast.makeText(context, "已添加形状", Toast.LENGTH_SHORT).show()
            }
        )
    }

    if (showDrawingDialog) {
        // 需求8：若正在编辑已有涂鸦图层（可以移动修改），否则新建涂鸦
        val editingLayer = layers.find { it.id == editingDrawLayerId }
        DrawingCanvasDialog(
            initialStrokes = editingLayer?.drawStrokes ?: emptyList(),
            onDismiss = {
                showDrawingDialog = false
                editingDrawLayerId = null
            },
            onConfirm = { strokes ->
                if (editingLayer != null) {
                    // 更新已有涂鸦图层内容
                    viewModel.updateLayerById(editingLayer.id) { layer ->
                        layer.copy(drawStrokes = strokes)
                    }
                    Toast.makeText(context, "已更新涂鸦图层 ✏️", Toast.LENGTH_SHORT).show()
                } else {
                    viewModel.addDrawLayer(strokes)
                    Toast.makeText(context, "已加入手绘涂鸦图层", Toast.LENGTH_SHORT).show()
                }
                editingDrawLayerId = null
            }
        )
    }

    if (showSaveProjectDialog) {
        SaveProjectDialog(
            onDismiss = { showSaveProjectDialog = false },
            onSave = { projectName ->
                viewModel.saveProject(projectName) {
                    Toast.makeText(context, "工程【$projectName】已保存成功！", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    if (showExportDialog) {
        ExportDialog(
            canvasConfig = canvasConfig,
            onDismiss = { showExportDialog = false },
            onExport = { format, quality, ratio, shareDirectly ->
                coroutineScope.launch {
                    Toast.makeText(context, "正在渲染高清图像...", Toast.LENGTH_SHORT).show()
                    // 需求5：按所选比例计算目标尺寸（以中心裁剪模式）
                    var tw = (canvasConfig.width * quality.multiplier).toInt().coerceIn(200, 4096)
                    var th = (canvasConfig.height * quality.multiplier).toInt().coerceIn(200, 4096)
                    if (ratio != com.landesheji.ui.dialogs.ExportRatio.ORIGINAL) {
                        val r = ratio.w / ratio.h
                        val cr = canvasConfig.width.toFloat() / canvasConfig.height.toFloat()
                        if (cr > r) {
                            // 画布更宽：保持高度不变，宽度按比例收缩（左右裁掉）
                            th = th
                            tw = (th * r).toInt()
                        } else {
                            // 画布更高：保持宽度不变，高度按比例收缩（上下裁掉）
                            tw = tw
                            th = (tw / r).toInt()
                        }
                    }
                    val bitmap = BitmapExporter.generateBitmap(
                        canvasConfig, layers, quality, tw, th,
                        previewCanvasWidthPx = viewModel.canvasDisplayWidth.value
                    )
                    if (shareDirectly) {
                        val uri = BitmapExporter.saveBitmapToCache(context, bitmap, format)
                        if (uri != null) {
                            BitmapExporter.shareImage(context, uri, format)
                        } else {
                            Toast.makeText(context, "分享准备失败", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        val uri = BitmapExporter.saveBitmapToGallery(context, bitmap, format)
                        if (uri != null) {
                            Toast.makeText(context, "🎉 图片已保存至相册 Picture/PixelLab", Toast.LENGTH_LONG).show()
                        } else {
                            Toast.makeText(context, "保存失败，请检查存储权限", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        )
    }

    if (showAboutDialog) {
        AboutDialog(
            onDismiss = { showAboutDialog = false },
            onCheckUpdate = { showUpdateCheckDialog = true }
        )
    }

    // 需求12：检查更新
    if (showUpdateCheckDialog) {
        UpdateCheckDialog(onDismiss = { showUpdateCheckDialog = false })
    }

    // 需求4：导出源码对话框
    if (showExportSourceDialog) {
        ExportSourceDialog(
            onDismiss = { showExportSourceDialog = false },
            onExport = { format, fileName ->
                coroutineScope.launch {
                    val file = if (format == "plp") {
                        SourceCodec.exportPlp(context, canvasConfig, layers, fileName)
                    } else {
                        SourceCodec.exportPsd(context, canvasConfig, layers, fileName)
                    }
                    if (file != null) {
                        Toast.makeText(context, "✅ 已导出源码: ${file.name}（路径 ${file.absolutePath}）", Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(context, "导出失败，请重试", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        )
    }
    }
}
