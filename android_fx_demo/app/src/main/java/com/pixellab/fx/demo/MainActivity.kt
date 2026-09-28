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
    private val fontNames = listOf("Default", "Bold", "Serif", "Mono")
    private val presetManager by lazy { TextFxPresetManager(this) }

    private fun buildTextLayers(): List<TextLayer> = listOf(
        TextLayer(1, "Shadow", TextFxType.DROP_SHADOW, layerShadowEnabled),
        TextLayer(2, "Glow", TextFxType.OUTER_GLOW, layerGlowEnabled),
        TextLayer(3, "Stroke", TextFxType.STROKE, layerStrokeEnabled),
        TextLayer(4, "Gradient", TextFxType.GRADIENT_FILL, layerGradientEnabled),
        TextLayer(5, "Bevel", TextFxType.BEVEL, layerBevelEnabled)
    )

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

        val layerToggleRow = LinearLayout(this).apply {
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
                    if (label == "Shadow") layerShadowEnabled = !current
                    if (label == "Glow") layerGlowEnabled = !current
                    if (label == "Stroke") layerStrokeEnabled = !current
                    if (label == "Gradient") layerGradientEnabled = !current
                    if (label == "Bevel") layerBevelEnabled = !current
                    updateLayerToggleStyles(layerToggleRow)
                    applyTextFxPreset(selectedTextPreset)
                }
            }
            layerToggleRow.addView(toggle)
        }
        updateLayerToggleStyles(layerToggleRow)
        controlsCard.addView(layerToggleRow)

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
        fxView.setSourceBitmap(TextFx.renderTextBitmap(config, buildTextLayers()))
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
            bevel = textFxBevelDepthValue
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

        customColorInput.setText(String.format("#%06X", (0xFFFFFF and currentTextColor)))
        presetNameInput.setText(loaded.name)
        textValueInput.setText(textInputValue)
        updateFontButtons(fontRow)
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
