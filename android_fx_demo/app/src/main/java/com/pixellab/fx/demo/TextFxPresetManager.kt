package com.pixellab.fx.demo

import android.content.Context

data class TextFxPresetState(
    val name: String = "MyPreset",
    val text: String = "PS FX",
    val textColor: Int = android.graphics.Color.WHITE,
    val fontIndex: Int = 0,
    val size: Float = 60f,
    val shadowBlur: Float = 16f,
    val shadowX: Float = 6f,
    val shadowY: Float = 6f,
    val glow: Float = 18f,
    val stroke: Float = 8f,
    val bevel: Float = 7f,
    val layerStackJson: String = "[]"
)

class TextFxPresetManager(context: Context) {
    private val prefs = context.getSharedPreferences("ps_text_fx_presets", Context.MODE_PRIVATE)

    fun save(name: String, state: TextFxPresetState) {
        val safeName = name.trim().ifBlank { "MyPreset" }
        val editor = prefs.edit()
        editor.putString("preset_${safeName}_text", state.text)
        editor.putInt("preset_${safeName}_color", state.textColor)
        editor.putInt("preset_${safeName}_font", state.fontIndex)
        editor.putFloat("preset_${safeName}_size", state.size)
        editor.putFloat("preset_${safeName}_shadow_blur", state.shadowBlur)
        editor.putFloat("preset_${safeName}_shadow_x", state.shadowX)
        editor.putFloat("preset_${safeName}_shadow_y", state.shadowY)
        editor.putFloat("preset_${safeName}_glow", state.glow)
        editor.putFloat("preset_${safeName}_stroke", state.stroke)
        editor.putFloat("preset_${safeName}_bevel", state.bevel)
        editor.putString("preset_${safeName}_layers", state.layerStackJson)
        editor.putString("preset_last_name", safeName)
        editor.apply()
    }

    fun listSavedPresets(): List<String> {
        return prefs.all.keys
            .filter { it.startsWith("preset_") && it.endsWith("_text") }
            .map { it.removePrefix("preset_").removeSuffix("_text") }
            .distinct()
            .sorted()
    }

    fun deletePreset(name: String): Boolean {
        val safeName = name.trim().ifBlank { return false }
        val keysToDelete = prefs.all.keys.filter { it.startsWith("preset_${safeName}_") || it == "preset_last_name" }
        if (keysToDelete.isEmpty()) return false
        val editor = prefs.edit()
        for (key in keysToDelete) {
            editor.remove(key)
        }
        editor.apply()
        return true
    }

    fun loadLatest(): TextFxPresetState? {
        val lastName = prefs.getString("preset_last_name", "") ?: return null
        return loadByName(lastName)
    }

    fun loadByName(name: String): TextFxPresetState? {
        val safeName = name.trim().ifBlank { return null }
        val text = prefs.getString("preset_${safeName}_text", "PS FX") ?: "PS FX"
        return TextFxPresetState(
            name = safeName,
            text = text,
            textColor = prefs.getInt("preset_${safeName}_color", android.graphics.Color.WHITE),
            fontIndex = prefs.getInt("preset_${safeName}_font", 0),
            size = prefs.getFloat("preset_${safeName}_size", 60f),
            shadowBlur = prefs.getFloat("preset_${safeName}_shadow_blur", 16f),
            shadowX = prefs.getFloat("preset_${safeName}_shadow_x", 6f),
            shadowY = prefs.getFloat("preset_${safeName}_shadow_y", 6f),
            glow = prefs.getFloat("preset_${safeName}_glow", 18f),
            stroke = prefs.getFloat("preset_${safeName}_stroke", 8f),
            bevel = prefs.getFloat("preset_${safeName}_bevel", 7f),
            layerStackJson = prefs.getString("preset_${safeName}_layers", "[]") ?: "[]"
        )
    }
}
