package com.pixellab.fx.demo

import android.app.AlertDialog
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Rect
import android.graphics.Typeface
import android.graphics.Shader
import android.os.Bundle
import android.os.Environment
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.io.File
import java.io.FileOutputStream

class MainActivity : AppCompatActivity() {

    private lateinit var fxView: FxPanelView
    private lateinit var selectedLabel: TextView
    private lateinit var effectList: LinearLayout
    private lateinit var textValueInput: EditText
    private lateinit var fontRow: LinearLayout
    private lateinit var customColorInput: EditText
    private lateinit var presetNameInput: EditText

    private var selectedType: FxType = FxType.DROP_SHADOW
    private var opacityValue = 0.75f
    private var distanceValue = 18f
    private var sizeValue = 20f
    private var selectedTextPreset: TextFxType = TextFxType.GRADIENT_FILL
    private var textFxSizeValue = 60f
    private var textFxShadowBlurValue = 16f
    private var textFxShadowDxValue = 6f
    private var textFxShadowDyValue = 6f
    private var textFxGlowValue = 18f
    private var textFxStrokeValue = 8f
    private var textFxBevelDepthValue = 7f
    private var textInputValue = "PS FX"
    private var currentTextColor = Color.WHITE
    private var currentFontIndex = 0
    private var transparentBgExport = true
    private var layerShadowEnabled = true
    private var layerGlowEnabled = true
    private var layerStrokeEnabled = true
    private var layerGradientEnabled = true
    private var layerBevelEnabled = true
    private var selectedLayerName = "Shadow"
    private val fontNames = listOf("Default", "Bold", "Serif", "Mono")
    private val presetManager by lazy { TextFxPresetManager(this) }
    private val layerStack = mutableListOf(
        TextLayer(1, "Shadow", TextFxType.DROP_SHADOW, true, 0, 0, Color.parseColor("#7db4ff"), 16f, 1f, 1f, "Normal"),
        TextLayer(2, "Glow", TextFxType.OUTER_GLOW, true, 0, 0, Color.parseColor("#7ef0c1"), 18f, 1.2f, 1f, "Screen"),
        TextLayer(3, "Stroke", TextFxType.STROKE, true, 0, 0, Color.parseColor("#ffb86b"), 8f, 1.2f, 1f, "Normal"),
        TextLayer(4, "Gradient", TextFxType.GRADIENT_FILL, true, 0, 0, Color.parseColor("#d0a2ff"), 12f, 1.1f, 1f, "Overlay"),
        TextLayer(5, "Bevel", TextFxType.BEVEL, true, 0, 0, Color.parseColor("#ffd166"), 10f, 1.4f, 1f, "Normal")
    )
    private lateinit var layerStackContainer: LinearLayout
    private lateinit var layerToggleRow: LinearLayout
    private lateinit var layerEditorContainer: LinearLayout

    private fun syncLayerBooleansFromStack() {
        layerShadowEnabled = layerStack.firstOrNull { it.name == "Shadow" }?.enabled == true
        layerGlowEnabled = layerStack.firstOrNull { it.name == "Glow" }?.enabled == true
        layerStrokeEnabled = layerStack.firstOrNull { it.name == "Stroke" }?.enabled == true
        layerGradientEnabled = layerStack.firstOrNull { it.name == "Gradient" }?.enabled == true
        layerBevelEnabled = layerStack.firstOrNull { it.name == "Bevel" }?.enabled == true
    }

    private fun buildTextLayers(): List<TextLayer> = layerStack.toList()

    private fun selectedLayer(): TextLayer? = layerStack.firstOrNull { it.name == selectedLayerName }

    private fun applyColorWithBlend(baseColor: Int, layer: TextLayer): Int {
        val alpha = ((Color.alpha(baseColor) * layer.opacity).toInt()).coerceIn(0, 255)
        val r = Color.red(baseColor)
        val g = Color.green(baseColor)
        val b = Color.blue(baseColor)

        return when (layer.blendMode) {
            "Multiply" -> Color.argb(alpha, (r * 0.7f).toInt().coerceIn(0, 255), (g * 0.7f).toInt().coerceIn(0, 255), (b * 0.7f).toInt().coerceIn(0, 255))
            "Screen" -> Color.argb(alpha, ((255 - (255 - r) * 0.7f)).toInt().coerceIn(0, 255), ((255 - (255 - g) * 0.7f)).toInt().coerceIn(0, 255), ((255 - (255 - b) * 0.7f)).toInt().coerceIn(0, 255))
            "Overlay" -> Color.argb(alpha, ((if (r < 128) r * 2 else 255 - (255 - r) * 2) * 0.75f).toInt().coerceIn(0, 255), ((if (g < 128) g * 2 else 255 - (255 - g) * 2) * 0.75f).toInt().coerceIn(0, 255), ((if (b < 128) b * 2 else 255 - (255 - b) * 2) * 0.75f).toInt().coerceIn(0, 255))
            else -> Color.argb(alpha, r, g, b)
        }
    }

    private fun applySelectedLayerOverrides(config: TextFxConfig): TextFxConfig {
        val layer = selectedLayer() ?: return config
        if (layer.type != config.type) return config

        return when (config.type) {
            TextFxType.DROP_SHADOW -> config.copy(
                shadowRadius = maxOf(config.shadowRadius, layer.blurRadius),
                shadowDx = layer.offsetX.toFloat(),
                shadowDy = layer.offsetY.toFloat(),
                shadowColor = applyColorWithBlend(layer.accentColor, layer)
            )
            TextFxType.OUTER_GLOW -> config.copy(
                glowRadius = maxOf(config.glowRadius, layer.blurRadius),
                glowColor = applyColorWithBlend(layer.accentColor, layer)
            )
            TextFxType.STROKE -> config.copy(
                strokeColor = applyColorWithBlend(layer.accentColor, layer),
                strokeWidth = maxOf(config.strokeWidth, layer.layerStrength * 8f)
            )
            TextFxType.GRADIENT_FILL -> config.copy(
                fillColor = applyColorWithBlend(layer.accentColor, layer),
                gradientColors = intArrayOf(
                    applyColorWithBlend(layer.accentColor, layer),
                    applyColorWithBlend(Color.parseColor("#FD1D1D"), layer),
                    applyColorWithBlend(Color.parseColor("#833AB4"), layer)
                )
            )
            TextFxType.BEVEL -> config.copy(
                bevelDepth = maxOf(config.bevelDepth, layer.layerStrength * 10f),
                bevelHighlight = applyColorWithBlend(layer.accentColor, layer)
            )
            else -> config
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.parseColor("#121922"))
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }

        val topBar = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(24, 18, 24, 18)
            setBackgroundColor(Color.parseColor("#1b2330"))
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }

        val title = TextView(this).apply {
            text = "Photoshop FX"
            textSize = 20f
            setTextColor(Color.WHITE)
        }
        val action = TextView(this).apply {
            text = "Layer Style"
            textSize = 12f
            setTextColor(Color.parseColor("#9ab5d1"))
            gravity = Gravity.END
            layoutParams = LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                1f
            )
        }
        topBar.addView(title)
        topBar.addView(action)
        root.addView(topBar)

        val content = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }

        val leftPanel = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.parseColor("#181f2a"))
            layoutParams = ViewGroup.LayoutParams(
                260,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            setPadding(12, 12, 12, 12)
        }

        val effectTitle = TextView(this).apply {
            text = "Effects"
            textSize = 14f
            setTextColor(Color.parseColor("#b4c4d8"))
            setPadding(12, 8, 12, 12)
        }
        leftPanel.addView(effectTitle)

        effectList = leftPanel
        val effectNames = listOf(
            "Drop Shadow" to FxType.DROP_SHADOW,
            "Inner Shadow" to FxType.INNER_SHADOW,
            "Outer Glow" to FxType.OUTER_GLOW,
            "Inner Glow" to FxType.INNER_GLOW,
            "Bevel & Emboss" to FxType.BEVEL_EMBOSS,
            "Color Overlay" to FxType.COLOR_OVERLAY,
            "Gradient Overlay" to FxType.GRADIENT_OVERLAY,
            "Pattern Overlay" to FxType.PATTERN_OVERLAY,
            "Stroke" to FxType.STROKE
        )

        effectNames.forEach { (label, type) ->
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                setBackgroundColor(if (type == selectedType) Color.parseColor("#24314b") else Color.parseColor("#1d2531"))
                setPadding(8, 6, 8, 6)
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
            }

            val check = CheckBox(this).apply {
                isChecked = type == selectedType
                isEnabled = false
                buttonTintList = android.content.res.ColorStateList.valueOf(
                    if (type == selectedType) Color.parseColor("#7db4ff") else Color.parseColor("#7a8394")
                )
            }

            val button = Button(this, null, android.R.style.Widget_MaterialButton_OutlinedButton).apply {
                text = label
                textSize = 12f
                setTextColor(Color.WHITE)
                setBackgroundColor(Color.TRANSPARENT)
                setPadding(8, 8, 8, 8)
                minHeight = 0
                height = 52
                gravity = Gravity.START or Gravity.CENTER_VERTICAL
                layoutParams = LinearLayout.LayoutParams(
                    0,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    1f
                )
                setOnClickListener {
                    selectedType = type
                    selectedLabel.text = label
                    updateSelectedEffect()
                    refreshEffectRows(effectNames)
                }
            }

            row.addView(check)
            row.addView(button)
            leftPanel.addView(row)
        }

        val rightPanel = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.parseColor("#111820"))
            layoutParams = ViewGroup.LayoutParams(
                0,
                ViewGroup.LayoutParams.MATCH_PARENT,
                1f
            )
            setPadding(20, 20, 20, 20)
        }

        val previewCard = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.parseColor("#1b2330"))
            setPadding(18, 18, 18, 18)
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                420
            )
        }

        val previewHeader = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }

        val previewTitle = TextView(this).apply {
            text = "Preview"
            textSize = 14f
            setTextColor(Color.parseColor("#dfe7f6"))
        }
        previewHeader.addView(previewTitle)

        selectedLabel = TextView(this).apply {
            text = "Drop Shadow"
            textSize = 12f
            setTextColor(Color.parseColor("#7db4ff"))
            gravity = Gravity.END
            layoutParams = LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                1f
            )
        }
        previewHeader.addView(selectedLabel)
        previewCard.addView(previewHeader)

        val previewArea = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }

        fxView = FxPanelView(this).apply {
            layoutParams = ViewGroup.LayoutParams(300, 300)
            setBackgroundColor(Color.parseColor("#0f141d"))
            setSourceBitmap(createDemoBitmap())
        }
        previewArea.addView(fxView)

        val textPreview = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setPadding(0, 20, 0, 0)
        }

        val textPresetNames = listOf(
            "Shadow" to TextFxType.DROP_SHADOW,
            "Glow" to TextFxType.OUTER_GLOW,
            "Stroke" to TextFxType.STROKE,
            "Gradient" to TextFxType.GRADIENT_FILL,
            "Bevel" to TextFxType.BEVEL,
            "Multi" to TextFxType.MULTI_LAYER
        )

        textPresetNames.forEach { (name, type) ->
            val preset = Button(this, null, android.R.style.Widget_MaterialButton_OutlinedButton).apply {
                text = name
                textSize = 11f
                setTextColor(Color.WHITE)
                setOnClickListener {
                    selectedTextPreset = type
                    applyTextFxPreset(type)
                }
            }
            textPreview.addView(preset)
        }

        previewArea.addView(textPreview)
        previewCard.addView(previewArea)
        rightPanel.addView(previewCard)

        val controlsCard = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.parseColor("#171e2a"))
            setPadding(18, 18, 18, 18)
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        }

        val controlsTitle = TextView(this).apply {
            text = "Layer Style"
            textSize = 16f
            setTextColor(Color.WHITE)
            setPadding(0, 0, 0, 12)
        }
        controlsCard.addView(controlsTitle)

        val textFxTitle = TextView(this).apply {
            text = "Text FX"
            textSize = 14f
            setTextColor(Color.parseColor("#b4c4d8"))
            setPadding(0, 12, 0, 8)
        }
        controlsCard.addView(textFxTitle)

        textValueInput = EditText(this).apply {
            setText(textInputValue)
            setTextColor(Color.WHITE)
            setBackgroundColor(Color.parseColor("#1d2531"))
            setPadding(12, 10, 12, 10)
            setSelection(text.length)
            addTextChangedListener(object : android.text.TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    textInputValue = s?.toString() ?: "PS FX"
                    applyTextFxPreset(selectedTextPreset)
                }
                override fun afterTextChanged(s: android.text.Editable?) = Unit
            })
        }
        controlsCard.addView(textValueInput)

        val textFxSliderGroup = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }
        textFxSliderGroup.addView(makeSliderRow("Size", 20f, 150f, textFxSizeValue) { value ->
            textFxSizeValue = value
            applyTextFxPreset(selectedTextPreset)
        })
        textFxSliderGroup.addView(makeSliderRow("Shadow Blur", 0f, 40f, textFxShadowBlurValue) { value ->
            textFxShadowBlurValue = value
            applyTextFxPreset(selectedTextPreset)
        })
        textFxSliderGroup.addView(makeSliderRow("Shadow X", 0f, 30f, textFxShadowDxValue) { value ->
            textFxShadowDxValue = value
            applyTextFxPreset(selectedTextPreset)
        })
        textFxSliderGroup.addView(makeSliderRow("Shadow Y", 0f, 30f, textFxShadowDyValue) { value ->
            textFxShadowDyValue = value
            applyTextFxPreset(selectedTextPreset)
        })
        textFxSliderGroup.addView(makeSliderRow("Glow", 0f, 40f, textFxGlowValue) { value ->
            textFxGlowValue = value
            applyTextFxPreset(selectedTextPreset)
        })
        textFxSliderGroup.addView(makeSliderRow("Stroke", 0f, 30f, textFxStrokeValue) { value ->
            textFxStrokeValue = value
            applyTextFxPreset(selectedTextPreset)
        })
        textFxSliderGroup.addView(makeSliderRow("Bevel", 0f, 20f, textFxBevelDepthValue) { value ->
            textFxBevelDepthValue = value
            applyTextFxPreset(selectedTextPreset)
        })
        controlsCard.addView(textFxSliderGroup)

        fontRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, 8, 0, 8)
        }
        fontNames.forEachIndexed { index, name ->
            val fontButton = Button(this, null, android.R.style.Widget_MaterialButton_OutlinedButton).apply {
                text = name
                textSize = 10f
                setTextColor(if (index == currentFontIndex) Color.parseColor("#7db4ff") else Color.WHITE)
                setBackgroundColor(if (index == currentFontIndex) Color.parseColor("#213149") else Color.parseColor("#1d2531"))
                setOnClickListener {
                    currentFontIndex = index
                    applyTextFxPreset(selectedTextPreset)
                    updateFontButtons(fontRow)
                }
            }
            fontRow.addView(fontButton)
        }
        controlsCard.addView(fontRow)

        customColorInput = EditText(this).apply {
            setText("#FFFFFF")
            setTextColor(Color.WHITE)
            setBackgroundColor(Color.parseColor("#1d2531"))
            setPadding(12, 10, 12, 10)
            setSingleLine()
        }
        val customColorRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, 8, 0, 8)
        }
        val customColorBtn = Button(this).apply {
            text = "Picker"
            setOnClickListener {
                showCustomColorPicker()
            }
        }
        customColorRow.addView(customColorInput)
        customColorRow.addView(customColorBtn)
        controlsCard.addView(customColorRow)

        layerToggleRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, 8, 0, 8)
        }
        val layerNames = listOf(
            "Shadow" to ::layerShadowEnabled,
            "Glow" to ::layerGlowEnabled,
            "Stroke" to ::layerStrokeEnabled,
            "Gradient" to ::layerGradientEnabled,
            "Bevel" to ::layerBevelEnabled
        )
        layerNames.forEach { (label, getter) ->
            val toggle = Button(this, null, android.R.style.Widget_MaterialButton_OutlinedButton).apply {
                text = label
                textSize = 10f
                setOnClickListener {
                    val current = getter.get()
                    val match = layerStack.firstOrNull { it.name == label }
                    if (match != null) {
                        match.enabled = !current
                        syncLayerBooleansFromStack()
                        rebuildLayerStackUI()
                        updateLayerToggleStyles(layerToggleRow)
                        applyTextFxPreset(selectedTextPreset)
                    }
                }
            }
            layerToggleRow.addView(toggle)
        }
        updateLayerToggleStyles(layerToggleRow)
        controlsCard.addView(layerToggleRow)

        val layerPanelTitle = TextView(this).apply {
            text = "Layer Stack"
            textSize = 13f
            setTextColor(Color.parseColor("#dfe7f6"))
            setPadding(0, 10, 0, 6)
        }
        controlsCard.addView(layerPanelTitle)
        layerStackContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }
        controlsCard.addView(layerStackContainer)

        val layerEditorTitle = TextView(this).apply {
            text = "Selected Layer"
            textSize = 13f
            setTextColor(Color.parseColor("#dfe7f6"))
            setPadding(0, 12, 0, 6)
        }
        controlsCard.addView(layerEditorTitle)
        layerEditorContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }
        controlsCard.addView(layerEditorContainer)
        rebuildLayerStackUI()
        refreshSelectedLayerEditor()

        val swatchRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, 8, 0, 8)
        }

        val swatches = listOf(
            Color.parseColor("#7db4ff"),
            Color.parseColor("#ff9d41"),
            Color.parseColor("#ff5ca8"),
            Color.parseColor("#7ef0c1"),
            Color.parseColor("#fce4ec"),
            Color.parseColor("#ffe082")
        )
        swatches.forEach { color ->
            val swatch = View(this).apply {
                setBackgroundColor(color)
                layoutParams = LinearLayout.LayoutParams(36, 36).apply {
                    setMargins(0, 0, 12, 0)
                }
                setOnClickListener {
                    currentTextColor = color
                    customColorInput.setText(String.format("#%06X", (0xFFFFFF and color)))
                    applyTextFxPreset(selectedTextPreset)
                }
            }
            swatchRow.addView(swatch)
        }
        controlsCard.addView(swatchRow)

        presetNameInput = EditText(this).apply {
            setText("MyPreset")
            setTextColor(Color.WHITE)
            setBackgroundColor(Color.parseColor("#1d2531"))
            setPadding(12, 10, 12, 10)
            setSingleLine()
        }
        val presetRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, 8, 0, 8)
        }
        val savePresetBtn = Button(this).apply {
            text = "Save Preset"
            setOnClickListener {
                saveCurrentPreset()
            }
        }
        val loadPresetBtn = Button(this).apply {
            text = "Load Last"
            setOnClickListener {
                loadLatestPreset()
            }
        }
        presetRow.addView(presetNameInput)
        presetRow.addView(savePresetBtn)
        presetRow.addView(loadPresetBtn)
        controlsCard.addView(presetRow)

        val transparentRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, 8, 0, 8)
        }
        val transparentLabel = TextView(this).apply {
            text = "Transparent BG"
            textSize = 12f
            setTextColor(Color.parseColor("#dfe7f6"))
        }
        val transparentToggle = CheckBox(this).apply {
            isChecked = transparentBgExport
            setOnCheckedChangeListener { _, checked ->
                transparentBgExport = checked
            }
        }
        transparentRow.addView(transparentLabel)
        transparentRow.addView(transparentToggle)
        controlsCard.addView(transparentRow)

        val sliderGroup = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        sliderGroup.addView(makeSliderRow("Opacity", 0f, 100f, opacityValue * 100f) { value ->
            opacityValue = value / 100f
            updateSelectedEffect()
        })
        sliderGroup.addView(makeSliderRow("Distance", 0f, 60f, distanceValue) { value ->
            distanceValue = value
            updateSelectedEffect()
        })
        sliderGroup.addView(makeSliderRow("Size", 0f, 80f, sizeValue) { value ->
            sizeValue = value
            updateSelectedEffect()
        })
        controlsCard.addView(sliderGroup)

        val actionBar = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.END or Gravity.CENTER_VERTICAL
            setPadding(0, 18, 0, 0)
        }
        val cancel = Button(this).apply {
            text = "Cancel"
            setBackgroundColor(Color.parseColor("#2a3340"))
            setTextColor(Color.WHITE)
        }
        val apply = Button(this).apply {
            text = "Apply"
            setBackgroundColor(Color.parseColor("#2a74ff"))
            setTextColor(Color.WHITE)
            setOnClickListener {
                applyTextFxPreset(selectedTextPreset)
            }
        }
        val exportBtn = Button(this).apply {
            text = "Export"
            setBackgroundColor(Color.parseColor("#355f8d"))
            setTextColor(Color.WHITE)
            setOnClickListener {
                exportCurrentTextEffect()
            }
        }
        val textFxBtn = Button(this).apply {
            text = "Text FX"
            setBackgroundColor(Color.parseColor("#334a6b"))
            setTextColor(Color.WHITE)
            setOnClickListener {
                applyTextFxPreset(selectedTextPreset)
            }
        }
        actionBar.addView(cancel)
        actionBar.addView(textFxBtn)
        actionBar.addView(exportBtn)
        actionBar.addView(apply)
        controlsCard.addView(actionBar)

        rightPanel.addView(controlsCard)
        content.addView(leftPanel)
        content.addView(rightPanel)
        root.addView(content)
        setContentView(root)

        VersionCheckService.checkForUpdate(this, 1, "1.0")

        refreshEffectRows(effectNames)
        updateSelectedEffect()
    }

    private fun refreshEffectRows(effectNames: List<Pair<String, FxType>>) {
        for (i in 0 until effectList.childCount) {
            val child = effectList.getChildAt(i) as? LinearLayout ?: continue
            val checkbox = child.getChildAt(0) as? CheckBox
            val button = child.getChildAt(1) as? Button
            val label = button?.text?.toString() ?: ""
            val match = effectNames.firstOrNull { it.first == label }?.second ?: selectedType
            child.setBackgroundColor(if (match == selectedType) Color.parseColor("#24314b") else Color.parseColor("#1d2531"))
            checkbox?.isChecked = match == selectedType
        }
    }

    private fun makeSliderRow(
        labelText: String,
        min: Float,
        max: Float,
        current: Float,
        onChange: (Float) -> Unit
    ): LinearLayout {
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, 12, 0, 12)
        }

        val header = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }

        val label = TextView(this).apply {
            text = labelText
            textSize = 12f
            setTextColor(Color.parseColor("#dfe7f6"))
        }
        val value = TextView(this).apply {
            text = current.toInt().toString()
            textSize = 12f
            setTextColor(Color.parseColor("#7db4ff"))
            gravity = Gravity.END
            layoutParams = LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                1f
            )
        }

        header.addView(label)
        header.addView(value)
        row.addView(header)

        val seekBar = SeekBar(this).apply {
            max = 100
            progress = current.toInt().coerceIn(0, 100)
            setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    val v = min + (max - min) * (progress / 100f)
                    value.text = v.toInt().toString()
                    onChange(v)
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) = Unit
                override fun onStopTrackingTouch(seekBar: SeekBar?) = Unit
            })
        }
        row.addView(seekBar)
        return row
    }

    private fun layerStackToJson(layers: List<TextLayer>): String {
        return layers.joinToString(prefix = "[", postfix = "]") { layer ->
            "{\"id\":${layer.id},\"name\":\"${layer.name.replace("\\", "\\\\").replace("\"", "\\\"") }\",\"type\":\"${layer.type.name}\",\"enabled\":${layer.enabled},\"offsetX\":${layer.offsetX},\"offsetY\":${layer.offsetY},\"accentColor\":${layer.accentColor},\"blurRadius\":${layer.blurRadius},\"layerStrength\":${layer.layerStrength},\"opacity\":${layer.opacity},\"blendMode\":\"${layer.blendMode}\"}"
        }
    }

    private fun layerStackFromJson(json: String): List<TextLayer> {
        if (json.isBlank() || json == "[]") return emptyList()
        val layers = mutableListOf<TextLayer>()
        val items = json.removePrefix("[").removeSuffix("]").split("},")
        for (item in items) {
            val clean = item.trim().removePrefix("{").removeSuffix("}")
            if (clean.isBlank()) continue
            val pairs = clean.split(",")
            val map = hashMapOf<String, String>()
            for (pair in pairs) {
                val parts = pair.split(":", limit = 2)
                if (parts.size == 2) {
                    map[parts[0].trim().trim('"')] = parts[1].trim()
                }
            }
            val name = map["name"]?.trim('"') ?: "Layer"
            val typeName = map["type"]?.trim('"') ?: TextFxType.DROP_SHADOW.name
            val type = try { TextFxType.valueOf(typeName) } catch (_: IllegalArgumentException) { TextFxType.DROP_SHADOW }
            val layer = TextLayer(
                id = map["id"]?.toIntOrNull() ?: 1,
                name = name,
                type = type,
                enabled = map["enabled"]?.toBooleanStrictOrNull() ?: true,
                offsetX = map["offsetX"]?.toIntOrNull() ?: 0,
                offsetY = map["offsetY"]?.toIntOrNull() ?: 0,
                accentColor = map["accentColor"]?.toIntOrNull() ?: Color.parseColor("#7db4ff"),
                blurRadius = map["blurRadius"]?.toFloatOrNull() ?: 12f,
                layerStrength = map["layerStrength"]?.toFloatOrNull() ?: 1f,
                opacity = map["opacity"]?.toFloatOrNull() ?: 1f,
                blendMode = map["blendMode"]?.trim('"') ?: "Normal"
            )
            layers.add(layer)
        }
        return layers
    }

    private fun updateSelectedEffect() {
        val fx = FxConfig(
            type = selectedType,
            color = resolveColor(selectedType),
            opacity = opacityValue,
            distance = distanceValue,
            radius = sizeValue,
            angle = 315f,
            strokeWidth = 6f
        )
        selectedLabel.text = when (selectedType) {
            FxType.DROP_SHADOW -> "Drop Shadow"
            FxType.INNER_SHADOW -> "Inner Shadow"
            FxType.OUTER_GLOW -> "Outer Glow"
            FxType.INNER_GLOW -> "Inner Glow"
            FxType.BEVEL_EMBOSS -> "Bevel & Emboss"
            FxType.COLOR_OVERLAY -> "Color Overlay"
            FxType.GRADIENT_OVERLAY -> "Gradient Overlay"
            FxType.PATTERN_OVERLAY -> "Pattern Overlay"
            FxType.STROKE -> "Stroke"
        }
        fxView.setEffects(listOf(fx))
    }

    private fun applyTextFxPreset(type: TextFxType) {
        val config = when (type) {
            TextFxType.DROP_SHADOW -> TextFxConfig(
                text = textInputValue.ifEmpty { "PS FX" },
                type = TextFxType.DROP_SHADOW,
                typeface = resolveCurrentTypeface(),
                textSize = textFxSizeValue,
                fillColor = currentTextColor,
                shadowColor = Color.argb(220, 0, 0, 0),
                shadowRadius = textFxShadowBlurValue,
                shadowDx = textFxShadowDxValue,
                shadowDy = textFxShadowDyValue,
                strokeColor = Color.BLACK,
                strokeWidth = 2f
            )
            TextFxType.OUTER_GLOW -> TextFxConfig(
                text = textInputValue.ifEmpty { "PS FX" },
                type = TextFxType.OUTER_GLOW,
                typeface = resolveCurrentTypeface(),
                textSize = textFxSizeValue,
                fillColor = currentTextColor,
                glowColor = Color.argb(220, 119, 181, 255),
                glowRadius = textFxGlowValue
            )
            TextFxType.STROKE -> TextFxConfig(
                text = textInputValue.ifEmpty { "PS FX" },
                type = TextFxType.STROKE,
                typeface = resolveCurrentTypeface(),
                textSize = textFxSizeValue,
                fillColor = currentTextColor,
                strokeColor = Color.parseColor("#7C4DFF"),
                strokeWidth = textFxStrokeValue
            )
            TextFxType.GRADIENT_FILL -> TextFxConfig(
                text = textInputValue.ifEmpty { "PS FX" },
                type = TextFxType.GRADIENT_FILL,
                typeface = resolveCurrentTypeface(),
                textSize = textFxSizeValue,
                fillColor = currentTextColor,
                gradientColors = intArrayOf(
                    Color.parseColor("#FCB045"),
                    Color.parseColor("#FD1D1D"),
                    Color.parseColor("#833AB4")
                )
            )
            TextFxType.BEVEL -> TextFxConfig(
                text = textInputValue.ifEmpty { "PS FX" },
                type = TextFxType.BEVEL,
                typeface = resolveCurrentTypeface(),
                textSize = textFxSizeValue,
                fillColor = currentTextColor,
                bevelHighlight = Color.argb(180, 255, 255, 255),
                bevelShadow = Color.argb(180, 70, 40, 15),
                bevelDepth = textFxBevelDepthValue
            )
            TextFxType.INNER_GLOW -> TextFxConfig(
                text = textInputValue.ifEmpty { "PS FX" },
                type = TextFxType.INNER_GLOW,
                typeface = resolveCurrentTypeface(),
                textSize = textFxSizeValue,
                fillColor = currentTextColor,
                glowColor = Color.argb(200, 255, 255, 255),
                glowRadius = textFxGlowValue
            )
            TextFxType.MULTI_LAYER -> TextFxConfig(
                text = textInputValue.ifEmpty { "PS FX" },
                type = TextFxType.MULTI_LAYER,
                typeface = resolveCurrentTypeface(),
                textSize = textFxSizeValue,
                fillColor = currentTextColor,
                shadowColor = Color.argb(220, 0, 0, 0),
                shadowRadius = textFxShadowBlurValue,
                shadowDx = textFxShadowDxValue,
                shadowDy = textFxShadowDyValue,
                glowColor = Color.argb(200, 120, 200, 255),
                glowRadius = textFxGlowValue,
                strokeColor = Color.parseColor("#5E35B1"),
                strokeWidth = textFxStrokeValue,
                gradientColors = intArrayOf(
                    Color.parseColor("#FFD54F"),
                    Color.parseColor("#FF7043"),
                    Color.parseColor("#7E57C2")
                ),
                bevelDepth = textFxBevelDepthValue
            )
        }
        fxView.setSourceBitmap(TextFx.renderTextBitmap(applySelectedLayerOverrides(config), buildTextLayers()))
    }

    private fun refreshSelectedLayerEditor() {
        if (!::layerEditorContainer.isInitialized) return
        layerEditorContainer.removeAllViews()

        val layer = selectedLayer() ?: return
        val title = TextView(this).apply {
            text = "${layer.name} • ${layer.type.name.replace('_', ' ')}"
            textSize = 12f
            setTextColor(Color.parseColor("#7db4ff"))
        }
        layerEditorContainer.addView(title)

        val nameInput = EditText(this).apply {
            setText(layer.name)
            setTextColor(Color.WHITE)
            setBackgroundColor(Color.parseColor("#1d2531"))
            setSingleLine()
            setPadding(12, 8, 12, 8)
            setOnEditorActionListener { _, _, _ ->
                val newName = text?.toString()?.trim().orEmpty()
                if (newName.isNotEmpty()) {
                    layer.name = newName
                    selectedLayerName = newName
                    rebuildLayerStackUI()
                    refreshSelectedLayerEditor()
                    applyTextFxPreset(selectedTextPreset)
                }
                false
            }
        }
        layerEditorContainer.addView(nameInput)

        val colorPreview = View(this).apply {
            setBackgroundColor(layer.accentColor)
            layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 32)
        }
        layerEditorContainer.addView(colorPreview)

        val colorBtn = Button(this, null, android.R.style.Widget_MaterialButton_OutlinedButton).apply {
            text = "Color: ${String.format("#%06X", (0xFFFFFF and layer.accentColor))}"
            setOnClickListener {
                val colorDialog = AlertDialog.Builder(this@MainActivity)
                val pickerLayout = LinearLayout(this@MainActivity).apply {
                    orientation = LinearLayout.VERTICAL
                    setPadding(24, 16, 24, 16)
                }
                val preview = View(this@MainActivity).apply {
                    layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 48)
                    setBackgroundColor(layer.accentColor)
                }
                val red = SeekBar(this@MainActivity).apply { max = 255; progress = Color.red(layer.accentColor) }
                val green = SeekBar(this@MainActivity).apply { max = 255; progress = Color.green(layer.accentColor) }
                val blue = SeekBar(this@MainActivity).apply { max = 255; progress = Color.blue(layer.accentColor) }
                fun syncColor() {
                    val c = Color.rgb(red.progress, green.progress, blue.progress)
                    preview.setBackgroundColor(c)
                    layer.accentColor = c
                    colorPreview.setBackgroundColor(c)
                    colorBtn.text = "Color: ${String.format("#%06X", (0xFFFFFF and c))}"
                    applyTextFxPreset(selectedTextPreset)
                }
                red.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) = syncColor()
                    override fun onStartTrackingTouch(seekBar: SeekBar?) = Unit
                    override fun onStopTrackingTouch(seekBar: SeekBar?) = Unit
                })
                green.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) = syncColor()
                    override fun onStartTrackingTouch(seekBar: SeekBar?) = Unit
                    override fun onStopTrackingTouch(seekBar: SeekBar?) = Unit
                })
                blue.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) = syncColor()
                    override fun onStartTrackingTouch(seekBar: SeekBar?) = Unit
                    override fun onStopTrackingTouch(seekBar: SeekBar?) = Unit
                })
                pickerLayout.addView(preview)
                pickerLayout.addView(TextView(this@MainActivity).apply { text = "R"; setTextColor(Color.WHITE) })
                pickerLayout.addView(red)
                pickerLayout.addView(TextView(this@MainActivity).apply { text = "G"; setTextColor(Color.WHITE) })
                pickerLayout.addView(green)
                pickerLayout.addView(TextView(this@MainActivity).apply { text = "B"; setTextColor(Color.WHITE) })
                pickerLayout.addView(blue)
                colorDialog.setTitle("Choose Layer Color").setView(pickerLayout).setPositiveButton("Done", null).show()
            }
        }
        layerEditorContainer.addView(colorBtn)

        val blendBtn = Button(this, null, android.R.style.Widget_MaterialButton_OutlinedButton).apply {
            text = "Blend: ${layer.blendMode}"
            setOnClickListener {
                val modes = listOf("Normal", "Multiply", "Screen", "Overlay")
                val currentIndex = modes.indexOf(layer.blendMode)
                layer.blendMode = modes[(currentIndex + 1) % modes.size]
                refreshSelectedLayerEditor()
                applyTextFxPreset(selectedTextPreset)
            }
        }
        layerEditorContainer.addView(blendBtn)

        layerEditorContainer.addView(makeSliderRow("Opacity", 0.1f, 1.0f, layer.opacity) { value ->
            layer.opacity = value
            applyTextFxPreset(selectedTextPreset)
        })
        layerEditorContainer.addView(makeSliderRow("Offset X", -40f, 40f, layer.offsetX.toFloat()) { value ->
            layer.offsetX = value.toInt()
            applyTextFxPreset(selectedTextPreset)
        })
        layerEditorContainer.addView(makeSliderRow("Offset Y", -40f, 40f, layer.offsetY.toFloat()) { value ->
            layer.offsetY = value.toInt()
            applyTextFxPreset(selectedTextPreset)
        })
        layerEditorContainer.addView(makeSliderRow("Blur", 0f, 40f, layer.blurRadius) { value ->
            layer.blurRadius = value
            applyTextFxPreset(selectedTextPreset)
        })
        layerEditorContainer.addView(makeSliderRow("Strength", 0.2f, 2f, layer.layerStrength) { value ->
            layer.layerStrength = value
            applyTextFxPreset(selectedTextPreset)
        })
    }

    private fun addLayerForCurrentPreset() {
        val layerType = when (selectedTextPreset) {
            TextFxType.DROP_SHADOW -> TextFxType.DROP_SHADOW
            TextFxType.OUTER_GLOW -> TextFxType.OUTER_GLOW
            TextFxType.STROKE -> TextFxType.STROKE
            TextFxType.GRADIENT_FILL -> TextFxType.GRADIENT_FILL
            TextFxType.BEVEL -> TextFxType.BEVEL
            TextFxType.INNER_GLOW -> TextFxType.OUTER_GLOW
            TextFxType.MULTI_LAYER -> TextFxType.DROP_SHADOW
        }

        val baseName = when (layerType) {
            TextFxType.DROP_SHADOW -> "Shadow"
            TextFxType.OUTER_GLOW -> "Glow"
            TextFxType.STROKE -> "Stroke"
            TextFxType.GRADIENT_FILL -> "Gradient"
            TextFxType.BEVEL -> "Bevel"
            TextFxType.INNER_GLOW -> "Glow"
            TextFxType.MULTI_LAYER -> "Shadow"
        }

        val uniqueName = if (layerStack.none { it.name == baseName }) baseName else "${baseName}_${layerStack.size + 1}"
        val newLayer = TextLayer(
            id = (layerStack.maxOfOrNull { it.id } ?: 0) + 1,
            name = uniqueName,
            type = layerType,
            enabled = true,
            offsetX = 0,
            offsetY = 0,
            accentColor = currentTextColor,
            blurRadius = when (layerType) {
                TextFxType.DROP_SHADOW -> textFxShadowBlurValue
                TextFxType.OUTER_GLOW -> textFxGlowValue
                TextFxType.STROKE -> textFxStrokeValue
                TextFxType.GRADIENT_FILL -> textFxSizeValue / 2f
                TextFxType.BEVEL -> textFxBevelDepthValue
                else -> 12f
            },
            layerStrength = 1f,
            opacity = 1f,
            blendMode = "Normal"
        )

        layerStack.add(newLayer)
        selectedLayerName = newLayer.name
        refreshSelectedLayerEditor()
        rebuildLayerStackUI()
        applyTextFxPreset(selectedTextPreset)
    }

    private fun deleteSelectedLayer() {
        val layer = selectedLayer() ?: return
        if (layerStack.size <= 1) return
        layerStack.remove(layer)
        selectedLayerName = layerStack.first().name
        refreshSelectedLayerEditor()
        rebuildLayerStackUI()
        applyTextFxPreset(selectedTextPreset)
    }

    private fun rebuildLayerStackUI() {
        layerStackContainer.removeAllViews()
        layerStack.forEachIndexed { index, layer ->
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                setPadding(0, 6, 0, 6)
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
            }

            val label = TextView(this).apply {
                text = layer.name
                textSize = 12f
                setTextColor(if (layer.name == selectedLayerName) Color.parseColor("#7db4ff") else if (layer.enabled) Color.WHITE else Color.parseColor("#8ea0b7"))
                layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
                setOnClickListener {
                    selectedLayerName = layer.name
                    refreshSelectedLayerEditor()
                    rebuildLayerStackUI()
                }
            }

            val toggle = Button(this, null, android.R.style.Widget_MaterialButton_OutlinedButton).apply {
                text = if (layer.enabled) "On" else "Off"
                textSize = 10f
                setOnClickListener {
                    layer.enabled = !layer.enabled
                    syncLayerBooleansFromStack()
                    refreshSelectedLayerEditor()
                    rebuildLayerStackUI()
                    updateLayerToggleStyles(layerToggleRow)
                    applyTextFxPreset(selectedTextPreset)
                }
            }

            val up = Button(this, null, android.R.style.Widget_MaterialButton_OutlinedButton).apply {
                text = "↑"
                textSize = 10f
                setOnClickListener {
                    if (index > 0) {
                        val previous = layerStack[index - 1]
                        layerStack[index - 1] = layer
                        layerStack[index] = previous
                        refreshSelectedLayerEditor()
                        rebuildLayerStackUI()
                        applyTextFxPreset(selectedTextPreset)
                    }
                }
            }

            val down = Button(this, null, android.R.style.Widget_MaterialButton_OutlinedButton).apply {
                text = "↓"
                textSize = 10f
                setOnClickListener {
                    if (index < layerStack.lastIndex) {
                        val next = layerStack[index + 1]
                        layerStack[index + 1] = layer
                        layerStack[index] = next
                        refreshSelectedLayerEditor()
                        rebuildLayerStackUI()
                        applyTextFxPreset(selectedTextPreset)
                    }
                }
            }

            row.addView(label)
            row.addView(toggle)
            row.addView(up)
            row.addView(down)
            layerStackContainer.addView(row)
        }
    }

    private fun updateFontButtons(fontRow: LinearLayout) {
        for (index in 0 until fontRow.childCount) {
            val button = fontRow.getChildAt(index) as? Button ?: continue
            val selected = index == currentFontIndex
            button.setTextColor(if (selected) Color.parseColor("#7db4ff") else Color.WHITE)
            button.setBackgroundColor(if (selected) Color.parseColor("#213149") else Color.parseColor("#1d2531"))
        }
    }

    private fun resolveCurrentTypeface(): Typeface {
        return when (currentFontIndex) {
            0 -> Typeface.DEFAULT
            1 -> Typeface.DEFAULT_BOLD
            2 -> Typeface.SERIF
            3 -> Typeface.MONOSPACE
            else -> Typeface.DEFAULT
        }
    }

    private fun updateLayerToggleStyles(row: LinearLayout) {
        val states = listOf(
            "Shadow" to layerShadowEnabled,
            "Glow" to layerGlowEnabled,
            "Stroke" to layerStrokeEnabled,
            "Gradient" to layerGradientEnabled,
            "Bevel" to layerBevelEnabled
        )
        for (index in 0 until row.childCount) {
            val button = row.getChildAt(index) as? Button ?: continue
            val label = button.text.toString()
            val active = states.firstOrNull { it.first == label }?.second == true
            button.setTextColor(if (active) Color.parseColor("#7db4ff") else Color.WHITE)
            button.setBackgroundColor(if (active) Color.parseColor("#213149") else Color.parseColor("#1d2531"))
        }
    }

    private fun showCustomColorPicker() {
        val pickerLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24, 16, 24, 16)
        }

        val preview = View(this).apply {
            layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 48)
            setBackgroundColor(currentTextColor)
        }

        val red = SeekBar(this).apply {
            max = 255
            progress = Color.red(currentTextColor)
        }
        val green = SeekBar(this).apply {
            max = 255
            progress = Color.green(currentTextColor)
        }
        val blue = SeekBar(this).apply {
            max = 255
            progress = Color.blue(currentTextColor)
        }

        fun syncPreview() {
            val color = Color.rgb(red.progress, green.progress, blue.progress)
            currentTextColor = color
            preview.setBackgroundColor(color)
            customColorInput.setText(String.format("#%06X", (0xFFFFFF and color)))
            applyTextFxPreset(selectedTextPreset)
        }

        red.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) = syncPreview()
            override fun onStartTrackingTouch(seekBar: SeekBar?) = Unit
            override fun onStopTrackingTouch(seekBar: SeekBar?) = Unit
        })
        green.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) = syncPreview()
            override fun onStartTrackingTouch(seekBar: SeekBar?) = Unit
            override fun onStopTrackingTouch(seekBar: SeekBar?) = Unit
        })
        blue.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) = syncPreview()
            override fun onStartTrackingTouch(seekBar: SeekBar?) = Unit
            override fun onStopTrackingTouch(seekBar: SeekBar?) = Unit
        })

        pickerLayout.addView(preview)
        pickerLayout.addView(TextView(this).apply { text = "R"; setTextColor(Color.WHITE) })
        pickerLayout.addView(red)
        pickerLayout.addView(TextView(this).apply { text = "G"; setTextColor(Color.WHITE) })
        pickerLayout.addView(green)
        pickerLayout.addView(TextView(this).apply { text = "B"; setTextColor(Color.WHITE) })
        pickerLayout.addView(blue)

        AlertDialog.Builder(this)
            .setTitle("Choose Color")
            .setView(pickerLayout)
            .setPositiveButton("Apply") { _, _ ->
                applyCustomColor()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun applyCustomColor() {
        val value = customColorInput.text?.toString()?.trim().orEmpty()
        val normalized = if (value.startsWith("#")) value else "#$value"
        try {
            currentTextColor = Color.parseColor(normalized)
            applyTextFxPreset(selectedTextPreset)
            Toast.makeText(this, "Color updated", Toast.LENGTH_SHORT).show()
        } catch (_: IllegalArgumentException) {
            Toast.makeText(this, "Invalid color format", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveCurrentPreset() {
        val name = presetNameInput.text?.toString()?.trim().takeUnless { it.isNullOrBlank() } ?: "MyPreset"
        val state = TextFxPresetState(
            name = name,
            text = textInputValue,
            textColor = currentTextColor,
            fontIndex = currentFontIndex,
            size = textFxSizeValue,
            shadowBlur = textFxShadowBlurValue,
            shadowX = textFxShadowDxValue,
            shadowY = textFxShadowDyValue,
            glow = textFxGlowValue,
            stroke = textFxStrokeValue,
            bevel = textFxBevelDepthValue,
            layerStackJson = layerStackToJson(layerStack)
        )
        presetManager.save(name, state)
        Toast.makeText(this, "Preset saved: $name", Toast.LENGTH_SHORT).show()
    }

    private fun loadLatestPreset() {
        val loaded = presetManager.loadLatest()
        if (loaded == null) {
            Toast.makeText(this, "No preset saved yet", Toast.LENGTH_SHORT).show()
            return
        }

        textInputValue = loaded.text
        currentTextColor = loaded.textColor
        currentFontIndex = loaded.fontIndex
        textFxSizeValue = loaded.size
        textFxShadowBlurValue = loaded.shadowBlur
        textFxShadowDxValue = loaded.shadowX
        textFxShadowDyValue = loaded.shadowY
        textFxGlowValue = loaded.glow
        textFxStrokeValue = loaded.stroke
        textFxBevelDepthValue = loaded.bevel

        layerStack.clear()
        layerStack.addAll(layerStackFromJson(loaded.layerStackJson))
        if (layerStack.isEmpty()) {
            layerStack.addAll(listOf(
                TextLayer(1, "Shadow", TextFxType.DROP_SHADOW, true, 0, 0, Color.parseColor("#7db4ff"), 16f, 1f, 1f, "Normal"),
                TextLayer(2, "Glow", TextFxType.OUTER_GLOW, true, 0, 0, Color.parseColor("#7ef0c1"), 18f, 1.2f, 1f, "Screen"),
                TextLayer(3, "Stroke", TextFxType.STROKE, true, 0, 0, Color.parseColor("#ffb86b"), 8f, 1.2f, 1f, "Normal"),
                TextLayer(4, "Gradient", TextFxType.GRADIENT_FILL, true, 0, 0, Color.parseColor("#d0a2ff"), 12f, 1.1f, 1f, "Overlay"),
                TextLayer(5, "Bevel", TextFxType.BEVEL, true, 0, 0, Color.parseColor("#ffd166"), 10f, 1.4f, 1f, "Normal")
            ))
        }
        selectedLayerName = layerStack.firstOrNull()?.name ?: "Shadow"

        customColorInput.setText(String.format("#%06X", (0xFFFFFF and currentTextColor)))
        presetNameInput.setText(loaded.name)
        textValueInput.setText(textInputValue)
        updateFontButtons(fontRow)
        syncLayerBooleansFromStack()
        refreshSelectedLayerEditor()
        rebuildLayerStackUI()
        applyTextFxPreset(selectedTextPreset)
        Toast.makeText(this, "Preset loaded: ${loaded.name}", Toast.LENGTH_SHORT).show()
    }

    private fun exportCurrentTextEffect() {
        val config = when (selectedTextPreset) {
            TextFxType.DROP_SHADOW -> TextFxConfig(
                text = textInputValue.ifEmpty { "PS FX" },
                type = TextFxType.DROP_SHADOW,
                typeface = resolveCurrentTypeface(),
                textSize = textFxSizeValue,
                fillColor = currentTextColor,
                shadowColor = Color.argb(220, 0, 0, 0),
                shadowRadius = textFxShadowBlurValue,
                shadowDx = textFxShadowDxValue,
                shadowDy = textFxShadowDyValue
            )
            TextFxType.OUTER_GLOW -> TextFxConfig(
                text = textInputValue.ifEmpty { "PS FX" },
                type = TextFxType.OUTER_GLOW,
                typeface = resolveCurrentTypeface(),
                textSize = textFxSizeValue,
                fillColor = currentTextColor,
                glowColor = Color.argb(220, 119, 181, 255),
                glowRadius = textFxGlowValue
            )
            TextFxType.STROKE -> TextFxConfig(
                text = textInputValue.ifEmpty { "PS FX" },
                type = TextFxType.STROKE,
                typeface = resolveCurrentTypeface(),
                textSize = textFxSizeValue,
                fillColor = currentTextColor,
                strokeColor = Color.parseColor("#7C4DFF"),
                strokeWidth = textFxStrokeValue
            )
            TextFxType.GRADIENT_FILL -> TextFxConfig(
                text = textInputValue.ifEmpty { "PS FX" },
                type = TextFxType.GRADIENT_FILL,
                typeface = resolveCurrentTypeface(),
                textSize = textFxSizeValue,
                fillColor = currentTextColor,
                gradientColors = intArrayOf(
                    Color.parseColor("#FCB045"),
                    Color.parseColor("#FD1D1D"),
                    Color.parseColor("#833AB4")
                )
            )
            TextFxType.BEVEL -> TextFxConfig(
                text = textInputValue.ifEmpty { "PS FX" },
                type = TextFxType.BEVEL,
                typeface = resolveCurrentTypeface(),
                textSize = textFxSizeValue,
                fillColor = currentTextColor,
                bevelHighlight = Color.argb(180, 255, 255, 255),
                bevelShadow = Color.argb(180, 70, 40, 15),
                bevelDepth = textFxBevelDepthValue
            )
            TextFxType.INNER_GLOW -> TextFxConfig(
                text = textInputValue.ifEmpty { "PS FX" },
                type = TextFxType.INNER_GLOW,
                typeface = resolveCurrentTypeface(),
                textSize = textFxSizeValue,
                fillColor = currentTextColor,
                glowColor = Color.argb(200, 255, 255, 255),
                glowRadius = textFxGlowValue
            )
            TextFxType.MULTI_LAYER -> TextFxConfig(
                text = textInputValue.ifEmpty { "PS FX" },
                type = TextFxType.MULTI_LAYER,
                typeface = resolveCurrentTypeface(),
                textSize = textFxSizeValue,
                fillColor = currentTextColor,
                shadowColor = Color.argb(220, 0, 0, 0),
                shadowRadius = textFxShadowBlurValue,
                shadowDx = textFxShadowDxValue,
                shadowDy = textFxShadowDyValue,
                glowColor = Color.argb(200, 120, 200, 255),
                glowRadius = textFxGlowValue,
                strokeColor = Color.parseColor("#5E35B1"),
                strokeWidth = textFxStrokeValue,
                gradientColors = intArrayOf(
                    Color.parseColor("#FFD54F"),
                    Color.parseColor("#FF7043"),
                    Color.parseColor("#7E57C2")
                ),
                bevelDepth = textFxBevelDepthValue
            )
        }

        val baseBitmap = TextFx.renderTextBitmap(config, buildTextLayers())
        val outputBitmap = if (transparentBgExport) {
            baseBitmap
        } else {
            val painted = Bitmap.createBitmap(baseBitmap.width, baseBitmap.height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(painted)
            canvas.drawColor(Color.WHITE)
            canvas.drawBitmap(baseBitmap, 0f, 0f, null)
            painted
        }

        val dir = getExternalFilesDir(Environment.DIRECTORY_PICTURES) ?: filesDir
        val file = File(dir, "psfx_${System.currentTimeMillis()}.png")
        FileOutputStream(file).use { stream ->
            outputBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        }
        Toast.makeText(this, "Saved: ${file.name}", Toast.LENGTH_SHORT).show()
    }

    private fun createDemoBitmap(): Bitmap {
        val bitmap = Bitmap.createBitmap(320, 320, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(
                0f,
                0f,
                320f,
                320f,
                intArrayOf(
                    Color.parseColor("#FF7A18"),
                    Color.parseColor("#FF3D81"),
                    Color.parseColor("#7A5CFF")
                ),
                null,
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRoundRect(
            RectF(28f, 28f, 292f, 292f),
            48f,
            48f,
            paint
        )
        return bitmap
    }

    private fun resolveColor(type: FxType): Int {
        return when (type) {
            FxType.DROP_SHADOW -> Color.argb(180, 20, 20, 30)
            FxType.INNER_SHADOW -> Color.argb(180, 0, 0, 0)
            FxType.OUTER_GLOW -> Color.argb(190, 98, 139, 255)
            FxType.INNER_GLOW -> Color.argb(210, 255, 170, 0)
            FxType.BEVEL_EMBOSS -> Color.argb(180, 220, 220, 220)
            FxType.COLOR_OVERLAY -> Color.argb(170, 255, 127, 80)
            FxType.GRADIENT_OVERLAY -> Color.argb(180, 120, 80, 255)
            FxType.PATTERN_OVERLAY -> Color.argb(180, 39, 196, 198)
            FxType.STROKE -> Color.argb(220, 255, 255, 255)
        }
    }
}
