package com.pixellab.fx.demo

import android.graphics.Color

data class TextLayer(
    val id: Int,
    val name: String,
    val type: TextFxType,
    var enabled: Boolean = true,
    var offsetX: Int = 0,
    var offsetY: Int = 0,
    val accentColor: Int = Color.parseColor("#7db4ff")
)
