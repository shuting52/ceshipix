package com.pixellab.fx.demo

import android.graphics.Color

data class TextLayer(
    val id: Int,
    val name: String,
    val type: TextFxType,
    var enabled: Boolean = true,
    var offsetX: Int = 0,
    var offsetY: Int = 0,
    val accentColor: Int = Color.parseColor("#7db4ff"),
    var blurRadius: Float = 12f,
    var layerStrength: Float = 1f,
    var opacity: Float = 1f,
    var blendMode: String = "Normal"
)
