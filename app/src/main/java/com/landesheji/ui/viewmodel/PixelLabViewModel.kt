package com.landesheji.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.landesheji.data.db.AppDatabase
import com.landesheji.data.db.ProjectEntity
import com.landesheji.data.db.ProjectRepository
import com.landesheji.data.db.ProjectSerializer
import com.landesheji.data.model.BackgroundType
import com.landesheji.data.model.CanvasConfig
import com.landesheji.data.model.DrawStroke
import com.landesheji.data.model.ImageLayerProperties
import com.landesheji.data.model.LayerItem
import com.landesheji.data.model.LayerType
import com.landesheji.data.model.PresetTemplate
import com.landesheji.data.model.PresetsData
import com.landesheji.data.model.ShapeProperties
import com.landesheji.data.model.ShapeType
import com.landesheji.data.model.StickerItem
import com.landesheji.data.model.StickerProperties
import com.landesheji.data.model.TextTemplate
import com.landesheji.data.model.TextProperties
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

enum class BottomTab(val title: String, val icon: String) {
    PRESETS("预设", "📁"),
    TEXT("文字", "🅰️"),
    SHAPES("图形", "🔷"),
    BACKGROUND("画布", "🖼️"),
    EFFECTS("特效", "🪄")
}

data class HistorySnapshot(
    val layers: List<LayerItem>,
    val canvasConfig: CanvasConfig
)

class PixelLabViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ProjectRepository

    init {
        val db = AppDatabase.getDatabase(application)
        repository = ProjectRepository(db.projectDao())
    }

    val savedProjects: StateFlow<List<ProjectEntity>> = repository.allProjects.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    // Canvas Configuration
    private val _canvasConfig = MutableStateFlow(
        CanvasConfig(
            width = 1080,
            height = 1080,
            ratioName = "1:1 正方形",
            backgroundColorArgb = 0xFF222831.toInt()
        )
    )
    val canvasConfig: StateFlow<CanvasConfig> = _canvasConfig.asStateFlow()

    // Layers List (ordered from bottom to top)
    private val _layers = MutableStateFlow<List<LayerItem>>(
        listOf(
            LayerItem(
                id = UUID.randomUUID().toString(),
                name = "默认文字",
                type = LayerType.TEXT,
                textProps = TextProperties(
                    text = "新文字",
                    fontSize = 42f,
                    colorArgb = 0xFF00ADB5.toInt(),
                    isBold = true,
                    hasShadow = true,
                    shadowRadius = 8f
                )
            )
        )
    )
    val layers: StateFlow<List<LayerItem>> = _layers.asStateFlow()

    // Selection
    private val _selectedLayerId = MutableStateFlow<String?>(_layers.value.firstOrNull()?.id)
    val selectedLayerId: StateFlow<String?> = _selectedLayerId.asStateFlow()

    val selectedLayer: LayerItem?
        get() = _layers.value.find { it.id == _selectedLayerId.value }

    // Active Bottom Tab
    private val _activeTab = MutableStateFlow(BottomTab.TEXT)
    val activeTab: StateFlow<BottomTab> = _activeTab.asStateFlow()

    // Floating Panels & Dialogs
    private val _isLayersPanelOpen = MutableStateFlow(false)
    val isLayersPanelOpen: StateFlow<Boolean> = _isLayersPanelOpen.asStateFlow()

    private val _isGridVisible = MutableStateFlow(false)
    val isGridVisible: StateFlow<Boolean> = _isGridVisible.asStateFlow()

    private val _isSnapEnabled = MutableStateFlow(true)
    val isSnapEnabled: StateFlow<Boolean> = _isSnapEnabled.asStateFlow()

    private val _isZoomMode = MutableStateFlow(false)
    val isZoomMode: StateFlow<Boolean> = _isZoomMode.asStateFlow()

    private val _zoomScale = MutableStateFlow(1f)
    val zoomScale: StateFlow<Float> = _zoomScale.asStateFlow()

    private val _zoomOffset = MutableStateFlow(Pair(0f, 0f))
    val zoomOffset: StateFlow<Pair<Float, Float>> = _zoomOffset.asStateFlow()

    // 需求1：软件 UI 主题背景（图片/视频应用到整个 App 界面，而非画布）
    private val _uiBackgroundImageUri = MutableStateFlow("")
    val uiBackgroundImageUri: StateFlow<String> = _uiBackgroundImageUri.asStateFlow()

    private val _uiBackgroundVideoUri = MutableStateFlow("")
    val uiBackgroundVideoUri: StateFlow<String> = _uiBackgroundVideoUri.asStateFlow()

    // 需求4修复：画布当前显示宽度（实际拟合后的 px），导出时按同一比例映射，避免文字溢出/显示不全
    private val _canvasDisplayWidth = MutableStateFlow(380f)
    val canvasDisplayWidth: StateFlow<Float> = _canvasDisplayWidth.asStateFlow()

    fun setCanvasDisplayWidth(px: Float) {
        if (px > 0f && (px - _canvasDisplayWidth.value).let { it * it > 1f }) {
            _canvasDisplayWidth.value = px
        }
    }

    // 需求6：用户自定义导入的本地图片贴纸
    private val _customStickers = MutableStateFlow<List<StickerItem>>(emptyList())
    val customStickers: StateFlow<List<StickerItem>> = _customStickers.asStateFlow()

    fun importCustomSticker(uri: String) {
        val count = _customStickers.value.size + 1
        val sticker = StickerItem(
            id = "custom_${count}",
            name = "我的贴纸 $count",
            emojiOrIcon = "🖼️",
            category = "我的贴纸",
            imageUri = uri
        )
        if (!_customStickers.value.any { it.imageUri == uri }) {
            _customStickers.value = _customStickers.value + sticker
        }
    }

    fun setUiBackgroundImage(uri: String) {
        _uiBackgroundImageUri.value = uri
    }

    fun setUiBackgroundVideo(uri: String) {
        _uiBackgroundVideoUri.value = uri
    }

    fun clearUiBackground() {
        _uiBackgroundImageUri.value = ""
        _uiBackgroundVideoUri.value = ""
    }

    // Dialog state controllers
    val showTextEditorDialog = MutableStateFlow(false)
    val showQuotesDialog = MutableStateFlow(false)
    val showCanvasSizeDialog = MutableStateFlow(false)
    val showExportDialog = MutableStateFlow(false)
    val showSaveProjectDialog = MutableStateFlow(false)
    val showStickersDialog = MutableStateFlow(false)
    val showShapesDialog = MutableStateFlow(false)
    val showFontPicker = MutableStateFlow(false)
    val showDrawingMode = MutableStateFlow(false)

    // Current Project ID if editing a loaded project
    private val _currentProjectId = MutableStateFlow<Long?>(null)
    val currentProjectId: StateFlow<Long?> = _currentProjectId.asStateFlow()

    // Undo / Redo Stacks
    private val undoStack = mutableListOf<HistorySnapshot>()
    private val redoStack = mutableListOf<HistorySnapshot>()

    private val _canUndo = MutableStateFlow(false)
    val canUndo: StateFlow<Boolean> = _canUndo.asStateFlow()

    private val _canRedo = MutableStateFlow(false)
    val canRedo: StateFlow<Boolean> = _canRedo.asStateFlow()

    private fun pushHistory() {
        undoStack.add(HistorySnapshot(layers = _layers.value, canvasConfig = _canvasConfig.value))
        if (undoStack.size > 25) {
            undoStack.removeAt(0)
        }
        redoStack.clear()
        updateHistoryFlags()
    }

    private fun updateHistoryFlags() {
        _canUndo.value = undoStack.isNotEmpty()
        _canRedo.value = redoStack.isNotEmpty()
    }

    fun undo() {
        if (undoStack.isEmpty()) return
        val currentSnapshot = HistorySnapshot(layers = _layers.value, canvasConfig = _canvasConfig.value)
        redoStack.add(currentSnapshot)
        val prev = undoStack.removeAt(undoStack.lastIndex)
        _layers.value = prev.layers
        _canvasConfig.value = prev.canvasConfig
        if (_selectedLayerId.value != null && prev.layers.none { it.id == _selectedLayerId.value }) {
            _selectedLayerId.value = prev.layers.lastOrNull()?.id
        }
        updateHistoryFlags()
    }

    fun redo() {
        if (redoStack.isEmpty()) return
        val currentSnapshot = HistorySnapshot(layers = _layers.value, canvasConfig = _canvasConfig.value)
        undoStack.add(currentSnapshot)
        val next = redoStack.removeAt(redoStack.lastIndex)
        _layers.value = next.layers
        _canvasConfig.value = next.canvasConfig
        updateHistoryFlags()
    }

    // UI Tab navigation
    fun setActiveTab(tab: BottomTab) {
        _activeTab.value = tab
    }

    fun toggleLayersPanel() {
        _isLayersPanelOpen.value = !_isLayersPanelOpen.value
    }

    fun setLayersPanelOpen(isOpen: Boolean) {
        _isLayersPanelOpen.value = isOpen
    }

    fun toggleGrid() {
        _isGridVisible.value = !_isGridVisible.value
    }

    fun toggleSnap() {
        _isSnapEnabled.value = !_isSnapEnabled.value
    }

    fun toggleZoomMode() {
        _isZoomMode.value = !_isZoomMode.value
        if (!_isZoomMode.value) {
            _zoomScale.value = 1f
            _zoomOffset.value = Pair(0f, 0f)
        }
    }

    fun updateZoom(scaleChange: Float, panChangeX: Float, panChangeY: Float) {
        val newScale = (_zoomScale.value * scaleChange).coerceIn(0.5f, 5.0f)
        _zoomScale.value = newScale
        _zoomOffset.value = Pair(
            _zoomOffset.value.first + panChangeX,
            _zoomOffset.value.second + panChangeY
        )
    }

    fun resetZoom() {
        _zoomScale.value = 1f
        _zoomOffset.value = Pair(0f, 0f)
    }

    // Layer selection
    fun selectLayer(id: String?) {
        _selectedLayerId.value = id
        if (id != null) {
            val layer = _layers.value.find { it.id == id }
            if (layer != null) {
                if (layer.type == LayerType.TEXT) {
                    _activeTab.value = BottomTab.TEXT
                } else if (layer.type == LayerType.SHAPE || layer.type == LayerType.STICKER || layer.type == LayerType.IMAGE) {
                    _activeTab.value = BottomTab.SHAPES
                }
            }
        }
    }

    // Adding layers
    fun addTextLayer(text: String = "新文字") {
        pushHistory()
        val count = _layers.value.count { it.type == LayerType.TEXT } + 1
        val newLayer = LayerItem(
            name = "文字 $count",
            type = LayerType.TEXT,
            x = 0f,
            y = 0f,
            textProps = TextProperties(
                text = text,
                fontSize = 40f,
                colorArgb = 0xFFFFFFFF.toInt(),
                isBold = true,
                hasShadow = true,
                shadowRadius = 8f
            )
        )
        _layers.value = _layers.value + newLayer
        _selectedLayerId.value = newLayer.id
        _activeTab.value = BottomTab.TEXT
    }

    fun addCurrentDateLayer() {
        val dateStr = SimpleDateFormat("yyyy年MM月dd日", Locale.CHINESE).format(Date())
        addTextLayer(dateStr)
    }

    fun addQuoteLayer(quote: String) {
        pushHistory()
        val newLayer = LayerItem(
            name = "金句语录",
            type = LayerType.TEXT,
            textProps = TextProperties(
                text = quote,
                fontSize = 32f,
                colorArgb = 0xFFFFD166.toInt(),
                isBold = false,
                letterSpacing = 2f,
                hasShadow = true,
                shadowColorArgb = 0x88000000.toInt(),
                shadowRadius = 12f
            )
        )
        _layers.value = _layers.value + newLayer
        _selectedLayerId.value = newLayer.id
        _activeTab.value = BottomTab.TEXT
    }

    fun addStickerLayer(sticker: StickerItem) {
        pushHistory()
        val count = _layers.value.count { it.type == LayerType.STICKER } + 1
        val newLayer = LayerItem(
            name = "${sticker.name} $count",
            type = LayerType.STICKER,
            stickerProps = StickerProperties(
                stickerId = sticker.id,
                stickerName = sticker.emojiOrIcon
            )
        )
        _layers.value = _layers.value + newLayer
        _selectedLayerId.value = newLayer.id
        _activeTab.value = BottomTab.SHAPES
    }

    fun addShapeLayer(shapeType: ShapeType) {
        pushHistory()
        val count = _layers.value.count { it.type == LayerType.SHAPE } + 1
        val shapeName = when (shapeType) {
            ShapeType.RECTANGLE -> "矩形"
            ShapeType.ROUNDED_RECTANGLE -> "圆角矩形"
            ShapeType.CIRCLE -> "圆形"
            ShapeType.STAR -> "星形"
            ShapeType.HEART -> "爱心"
            ShapeType.TRIANGLE -> "三角形"
            ShapeType.HEXAGON -> "六边形"
            ShapeType.ARROW -> "箭头"
        }
        val newLayer = LayerItem(
            name = "$shapeName $count",
            type = LayerType.SHAPE,
            shapeProps = ShapeProperties(
                shapeType = shapeType,
                fillColorArgb = 0xFF00ADB5.toInt()
            )
        )
        _layers.value = _layers.value + newLayer
        _selectedLayerId.value = newLayer.id
        _activeTab.value = BottomTab.SHAPES
    }

    fun addImageLayer(uriString: String) {
        pushHistory()
        val count = _layers.value.count { it.type == LayerType.IMAGE } + 1
        val newLayer = LayerItem(
            name = "图片 $count",
            type = LayerType.IMAGE,
            imageProps = ImageLayerProperties(
                uriString = uriString
            )
        )
        _layers.value = _layers.value + newLayer
        _selectedLayerId.value = newLayer.id
        _activeTab.value = BottomTab.SHAPES
    }

    fun addDrawLayer(strokes: List<DrawStroke>) {
        if (strokes.isEmpty()) return
        pushHistory()
        val count = _layers.value.count { it.type == LayerType.DRAW } + 1
        val newLayer = LayerItem(
            name = "手绘涂鸦 $count",
            type = LayerType.DRAW,
            drawStrokes = strokes
        )
        _layers.value = _layers.value + newLayer
        _selectedLayerId.value = newLayer.id
        _activeTab.value = BottomTab.SHAPES
    }

    // Layer modification
    fun updateSelectedLayer(transform: (LayerItem) -> LayerItem) {
        val currentId = _selectedLayerId.value ?: return
        pushHistory()
        _layers.value = _layers.value.map {
            if (it.id == currentId && !it.isLocked) transform(it) else it
        }
    }

    fun updateLayerPositionSilent(id: String, dx: Float, dy: Float) {
        // Fast move without pushing history on every sub-pixel drag frame
        _layers.value = _layers.value.map {
            if (it.id == id && !it.isLocked) it.copy(x = it.x + dx, y = it.y + dy) else it
        }
    }

    // 需求8：按 ID 更新图层内容（用于重绘/修改已有涂鸦图层）
    fun updateLayerById(id: String, transform: (LayerItem) -> LayerItem) {
        pushHistory()
        _layers.value = _layers.value.map {
            if (it.id == id && !it.isLocked) transform(it) else it
        }
    }

    fun recordTransformCompleted() {
        pushHistory()
    }

    fun updateLayerTransform(id: String, scaleFactor: Float, rotationDelta: Float) {
        _layers.value = _layers.value.map {
            if (it.id == id && !it.isLocked) {
                val newScaleX = (it.scaleX * scaleFactor).coerceIn(0.1f, 10f)
                val newScaleY = (it.scaleY * scaleFactor).coerceIn(0.1f, 10f)
                it.copy(
                    scaleX = newScaleX,
                    scaleY = newScaleY,
                    rotation = (it.rotation + rotationDelta) % 360f
                )
            } else it
        }
    }

    fun updateLayerScaleOnly(id: String, scaleFactorDelta: Float) {
        _layers.value = _layers.value.map {
            if (it.id == id && !it.isLocked) {
                val newScale = (it.scaleX * scaleFactorDelta).coerceIn(0.1f, 10f)
                it.copy(scaleX = newScale, scaleY = newScale)
            } else it
        }
    }

    fun setLayerExactScale(id: String, exactScale: Float) {
        pushHistory()
        val s = exactScale.coerceIn(0.1f, 10f)
        _layers.value = _layers.value.map {
            if (it.id == id && !it.isLocked) it.copy(scaleX = s, scaleY = s) else it
        }
    }

    fun stepTextFontSize(id: String, deltaSp: Float) {
        pushHistory()
        _layers.value = _layers.value.map { layer ->
            if (layer.id == id && layer.type == LayerType.TEXT && !layer.isLocked) {
                val currentSize = layer.textProps.fontSize
                val newSize = (currentSize + deltaSp).coerceIn(12f, 320f)
                layer.copy(textProps = layer.textProps.copy(fontSize = newSize))
            } else layer
        }
    }

    fun setTextFontSize(id: String, newSize: Float, recordHistory: Boolean = false) {
        if (recordHistory) pushHistory()
        _layers.value = _layers.value.map { layer ->
            if (layer.id == id && layer.type == LayerType.TEXT && !layer.isLocked) {
                layer.copy(textProps = layer.textProps.copy(fontSize = newSize.coerceIn(12f, 320f)))
            } else layer
        }
    }

    fun resetLayerTransform(id: String) {
        pushHistory()
        _layers.value = _layers.value.map {
            if (it.id == id && !it.isLocked) it.copy(scaleX = 1f, scaleY = 1f, rotation = 0f) else it
        }
    }

    fun duplicateLayer(id: String) {
        val target = _layers.value.find { it.id == id } ?: return
        pushHistory()
        val copy = target.copy(
            id = UUID.randomUUID().toString(),
            name = "${target.name} (复制)",
            x = target.x + 30f,
            y = target.y + 30f
        )
        _layers.value = _layers.value + copy
        _selectedLayerId.value = copy.id
    }

    fun deleteLayer(id: String) {
        pushHistory()
        _layers.value = _layers.value.filter { it.id != id }
        if (_selectedLayerId.value == id) {
            _selectedLayerId.value = _layers.value.lastOrNull()?.id
        }
    }

    fun toggleLayerVisibility(id: String) {
        pushHistory()
        _layers.value = _layers.value.map {
            if (it.id == id) it.copy(isVisible = !it.isVisible) else it
        }
    }

    fun toggleLayerLock(id: String) {
        pushHistory()
        _layers.value = _layers.value.map {
            if (it.id == id) it.copy(isLocked = !it.isLocked) else it
        }
    }

    fun bringLayerToFront(id: String) {
        val index = _layers.value.indexOfFirst { it.id == id }
        if (index < 0 || index == _layers.value.lastIndex) return
        pushHistory()
        val list = _layers.value.toMutableList()
        val item = list.removeAt(index)
        list.add(item)
        _layers.value = list
    }

    fun sendLayerToBack(id: String) {
        val index = _layers.value.indexOfFirst { it.id == id }
        if (index <= 0) return
        pushHistory()
        val list = _layers.value.toMutableList()
        val item = list.removeAt(index)
        list.add(0, item)
        _layers.value = list
    }

    fun moveLayerUp(id: String) {
        val index = _layers.value.indexOfFirst { it.id == id }
        if (index < 0 || index >= _layers.value.lastIndex) return
        pushHistory()
        val list = _layers.value.toMutableList()
        val item = list.removeAt(index)
        list.add(index + 1, item)
        _layers.value = list
    }

    fun moveLayerDown(id: String) {
        val index = _layers.value.indexOfFirst { it.id == id }
        if (index <= 0) return
        pushHistory()
        val list = _layers.value.toMutableList()
        val item = list.removeAt(index)
        list.add(index - 1, item)
        _layers.value = list
    }

    // Canvas settings
    fun updateCanvasConfig(transform: (CanvasConfig) -> CanvasConfig) {
        pushHistory()
        _canvasConfig.value = transform(_canvasConfig.value)
    }

    fun setCanvasSize(width: Int, height: Int, ratioName: String) {
        pushHistory()
        _canvasConfig.value = _canvasConfig.value.copy(
            width = width,
            height = height,
            ratioName = ratioName
        )
    }

    fun applyPreset(preset: PresetTemplate) {
        pushHistory()
        _canvasConfig.value = preset.canvasConfig
        _layers.value = preset.defaultLayers
        _selectedLayerId.value = _layers.value.firstOrNull()?.id
    }

    // ===== 需求重造3：套用文字模板（深拷贝，绝不修改原始模板） =====
    fun applyTextTemplate(template: TextTemplate) {
        pushHistory()
        // 深拷贝模板数据
        val copy = template.deepCopy()
        // 应用模板画布背景
        _canvasConfig.value = CanvasConfig(
            width = copy.canvasWidth,
            height = copy.canvasHeight,
            ratioName = "${copy.canvasWidth}:${copy.canvasHeight} 模板",
            backgroundType = BackgroundType.COLOR,
            backgroundColorArgb = copy.backgroundColorArgb
        )
        // 模板图层 → 应用图层（文字保留模板特效，修改文字后特效不变）
        val newLayers = copy.layers.mapIndexed { index, tl ->
            LayerItem(
                id = java.util.UUID.randomUUID().toString(),
                name = tl.label,
                type = LayerType.TEXT,
                // 模板坐标以画布中心为原点
                x = tl.x,
                y = tl.y,
                scaleX = 1f,
                scaleY = 1f,
                rotation = tl.rotation,
                opacity = tl.opacity,
                textProps = TextProperties(
                    text = tl.text,
                    fontSize = tl.fontSize,
                    colorArgb = tl.colorArgb,
                    isBold = tl.fontWeight.contains("bold", true),
                    isItalic = tl.fontWeight.contains("italic", true),
                    templateEffect = tl.effect,
                    templateEffectParamsJson = tl.effectParams.toString()
                )
            )
        }
        if (newLayers.isEmpty()) return
        _layers.value = newLayers
        _selectedLayerId.value = newLayers[0].id
        // 切到文字 Tab 方便编辑
        _activeTab.value = BottomTab.TEXT
        // 复位缩放
        _zoomScale.value = 1f
        _zoomOffset.value = Pair(0f, 0f)
    }

    fun clearCanvas() {
        pushHistory()
        _layers.value = emptyList()
        _selectedLayerId.value = null
    }

    // Projects (Room Database)
    fun saveProject(title: String, onFinished: (Long) -> Unit = {}) {
        viewModelScope.launch {
            val id = repository.saveProject(
                title = title,
                canvasConfig = _canvasConfig.value,
                layers = _layers.value,
                existingId = _currentProjectId.value ?: 0
            )
            _currentProjectId.value = id
            onFinished(id)
        }
    }

    fun loadProject(entity: ProjectEntity) {
        pushHistory()
        _currentProjectId.value = entity.id
        _canvasConfig.value = ProjectSerializer.deserializeCanvas(entity.canvasJson)
        val loadedLayers = ProjectSerializer.deserializeLayers(entity.layersJson)
        _layers.value = loadedLayers
        _selectedLayerId.value = loadedLayers.firstOrNull()?.id
    }

    // 需求4：从源码（plp/psd）加载工程数据
    fun loadProjectData(canvas: CanvasConfig, layers: List<LayerItem>) {
        pushHistory()
        _currentProjectId.value = null
        _canvasConfig.value = canvas
        _layers.value = layers
        _selectedLayerId.value = layers.firstOrNull()?.id
        // 切到文字 Tab 方便直接编辑
        _activeTab.value = BottomTab.TEXT
        // 清空缩放
        _zoomScale.value = 1f
        _zoomOffset.value = Pair(0f, 0f)
    }

    fun deleteProject(id: Long) {
        viewModelScope.launch {
            repository.deleteProject(id)
            if (_currentProjectId.value == id) {
                _currentProjectId.value = null
            }
        }
    }
}
