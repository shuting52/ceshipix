package com.landesheji.data.model

import org.json.JSONArray
import org.json.JSONObject

/**
 * 需求重造3：文字模板系统（模板 = 数据，效果 = 绘制函数，文字 = 可修改参数）
 *
 * 所有文字效果均由本地 Canvas/Paint(Skia) 绘制，不调用任何 AI / 外部 API / 服务器。
 * 模板保存为 JSON 数据（本文件提供 10 组内置模板），套用时深拷贝，绝不修改原始模板。
 *
 * 数据结构与设计师提供的 JS 模板规范一一对应，方便后续扩展第 11、12 组模板。
 */
data class TextTemplateLayer(
    val id: String,
    val label: String,
    val text: String,
    val x: Float,               // 相对画布中心 X（逻辑坐标）
    val y: Float,               // 相对画布中心 Y（逻辑坐标）
    val fontFamily: String = "sans-serif",
    val fontSize: Float,
    val fontWeight: String = "bold",
    val align: String = "center",
    val rotation: Float = 0f,
    val opacity: Float = 1f,
    val colorArgb: Int = 0xFFFFFFFF.toInt(),
    val effect: String,         // crystal / glass / embroidery / threeD / neon / gold / fire / ice / glitch / cyber
    val effectParams: JSONObject
) {
    fun toJson(): JSONObject = JSONObject().apply {
        put("id", id)
        put("label", label)
        put("text", text)
        put("x", x.toDouble())
        put("y", y.toDouble())
        put("fontFamily", fontFamily)
        put("fontSize", fontSize.toDouble())
        put("fontWeight", fontWeight)
        put("align", align)
        put("rotation", rotation.toDouble())
        put("opacity", opacity.toDouble())
        put("colorArgb", colorArgb)
        put("effect", effect)
        put("effectParams", effectParams)
    }

    companion object {
        fun fromJson(o: JSONObject): TextTemplateLayer = TextTemplateLayer(
            id = o.optString("id", "layer"),
            label = o.optString("label", "文字"),
            text = o.optString("text", "请输入文字"),
            x = o.optDouble("x", 0.0).toFloat(),
            y = o.optDouble("y", 0.0).toFloat(),
            fontFamily = o.optString("fontFamily", "sans-serif"),
            fontSize = o.optDouble("fontSize", 60.0).toFloat(),
            fontWeight = o.optString("fontWeight", "bold"),
            align = o.optString("align", "center"),
            rotation = o.optDouble("rotation", 0.0).toFloat(),
            opacity = o.optDouble("opacity", 1.0).toFloat(),
            colorArgb = o.optInt("colorArgb", 0xFFFFFFFF.toInt()),
            effect = o.optString("effect", "threeD"),
            effectParams = o.optJSONObject("effectParams") ?: JSONObject()
        )
    }
}

data class TextTemplate(
    val id: String,
    val name: String,
    val canvasWidth: Int = 1080,
    val canvasHeight: Int = 1080,
    val backgroundColorArgb: Int,
    val editableFields: List<Pair<String, String>>,   // layerId -> label
    val layers: List<TextTemplateLayer>
) {
    /** 深拷贝：套用模板时必须走这里，绝不直接修改原始模板 */
    fun deepCopy(): TextTemplate = TextTemplate.fromJson(toJson())

    fun toJson(): JSONObject = JSONObject().apply {
        put("id", id)
        put("name", name)
        put("canvas", JSONObject().apply {
            put("width", canvasWidth)
            put("height", canvasHeight)
            put("backgroundColor", String.format("#%06X", 0xFFFFFF and backgroundColorArgb))
        })
        put("editableFields", JSONArray().apply {
            editableFields.forEach { (layerId, label) ->
                put(JSONObject().apply {
                    put("layerId", layerId)
                    put("label", label)
                    put("type", "text")
                })
            }
        })
        put("layers", JSONArray().apply {
            layers.forEach { put(it.toJson()) }
        })
    }

    companion object {
        fun fromJson(o: JSONObject): TextTemplate {
            val canvas = o.optJSONObject("canvas")
            val layerArr = o.optJSONArray("layers")
            val layerList = mutableListOf<TextTemplateLayer>()
            if (layerArr != null) {
                for (i in 0 until layerArr.length()) {
                    layerList.add(TextTemplateLayer.fromJson(layerArr.getJSONObject(i)))
                }
            }
            val fields = mutableListOf<Pair<String, String>>()
            val fieldsArr = o.optJSONArray("editableFields")
            if (fieldsArr != null) {
                for (i in 0 until fieldsArr.length()) {
                    val f = fieldsArr.getJSONObject(i)
                    fields.add(f.optString("layerId", "title") to f.optString("label", "文字"))
                }
            }
            return TextTemplate(
                id = o.optString("id", "template"),
                name = o.optString("name", "模板"),
                canvasWidth = canvas?.optInt("width", 1080) ?: 1080,
                canvasHeight = canvas?.optInt("height", 1080) ?: 1080,
                backgroundColorArgb = parseColor(canvas?.optString("backgroundColor", "#101820") ?: "#101820"),
                editableFields = fields,
                layers = layerList
            )
        }

        private fun parseColor(hex: String): Int {
            return try {
                val h = hex.removePrefix("#")
                when (h.length) {
                    3 -> {
                        val r = h[0].toString().repeat(2).toInt(16)
                        val g = h[1].toString().repeat(2).toInt(16)
                        val b = h[2].toString().repeat(2).toInt(16)
                        0xFF000000.toInt() or (r shl 16) or (g shl 8) or b
                    }
                    6 -> 0xFF000000.toInt() or h.toInt(16)
                    8 -> h.toLong(16).toInt()
                    else -> 0xFF101820.toInt()
                }
            } catch (_: Exception) {
                0xFF101820.toInt()
            }
        }
    }
}

/** 10 组内置文字模板（全部参数与设计师 JSON 规范一致） */
object TextTemplatesData {

    fun getById(id: String): TextTemplate? = templates.firstOrNull { it.id == id }

    // ---------- 1. 蓝色水晶字 ----------
    private val crystalTemplate = TextTemplate(
        id = "crystal",
        name = "蓝色水晶字",
        backgroundColorArgb = 0xFF0F2E55.toInt(),
        editableFields = listOf(
            "title" to "主标题"
        ),
        layers = listOf(
            TextTemplateLayer(
                id = "title",
                label = "主标题",
                text = "水晶",
                x = 0f,
                y = -10f,
                fontSize = 150f,
                fontWeight = "bold",
                effect = "crystal",
                effectParams = JSONObject().apply {
                    put("depth", 20)
                    put("depthColor", "#073b78")
                    put("strokeWidth", 14)
                    put("strokeColor", "#04356f")
                    put("glowColor", "rgba(0,160,255,0.85)")
                    put("glowBlur", 24)
                    put("gradient", JSONArray().apply { put("#ffffff"); put("#d4faff"); put("#4bd7ff"); put("#0877d1"); put("#043b91") })
                    put("highlight", true)
                }
            )
        )
    )

    // ---------- 2. 透明玻璃字 ----------
    private val glassTemplate = TextTemplate(
        id = "glass",
        name = "透明玻璃字",
        backgroundColorArgb = 0xFF1A4D6E.toInt(),
        editableFields = listOf("title" to "主标题"),
        layers = listOf(
            TextTemplateLayer(
                id = "title", label = "主标题", text = "GLASS", x = 0f, y = -10f,
                fontSize = 140f, effect = "glass",
                effectParams = JSONObject().apply {
                    put("fillColor", "rgba(220,245,255,0.3)")
                    put("strokeColor", "rgba(255,255,255,0.9)")
                    put("strokeWidth", 5)
                    put("glowColor", "rgba(255,255,255,0.8)")
                    put("glowBlur", 22)
                    put("opacity", 0.85)
                    put("highlight", true)
                }
            )
        )
    )

    // ---------- 3. 红色刺绣字 ----------
    private val embroideryTemplate = TextTemplate(
        id = "embroidery",
        name = "红色刺绣字",
        backgroundColorArgb = 0xFF241A2E.toInt(),
        editableFields = listOf("title" to "主标题"),
        layers = listOf(
            TextTemplateLayer(
                id = "title", label = "主标题", text = "刺绣", x = 0f, y = -10f,
                fontSize = 150f, effect = "embroidery",
                effectParams = JSONObject().apply {
                    put("fillColor", "#c92135")
                    put("strokeColor", "#721222")
                    put("strokeWidth", 9)
                    put("threadColor", "#ff6870")
                    put("threadWidth", 2)
                    put("threadCount", 12)
                    put("shadowColor", "rgba(50,10,10,0.45)")
                    put("shadowBlur", 8)
                    put("texture", true)
                }
            )
        )
    )

    // ---------- 4. 橙色3D立体字 ----------
    private val threeDTemplate = TextTemplate(
        id = "threeD",
        name = "橙色3D立体字",
        backgroundColorArgb = 0xFF2A1208.toInt(),
        editableFields = listOf("title" to "主标题"),
        layers = listOf(
            TextTemplateLayer(
                id = "title", label = "主标题", text = "3D", x = 0f, y = -10f,
                fontSize = 170f, effect = "threeD",
                effectParams = JSONObject().apply {
                    put("depth", 28)
                    put("depthColor", "#762500")
                    put("strokeColor", "#4b1604")
                    put("strokeWidth", 8)
                    put("shadowColor", "rgba(0,0,0,0.65)")
                    put("shadowBlur", 18)
                    put("shadowOffsetX", 15)
                    put("shadowOffsetY", 20)
                    put("gradient", JSONArray().apply { put("#fff19b"); put("#ffb537"); put("#ef4d12") })
                }
            )
        )
    )

    // ---------- 5. 粉色霓虹字 ----------
    private val neonTemplate = TextTemplate(
        id = "neon",
        name = "粉色霓虹字",
        backgroundColorArgb = 0xFF16022B.toInt(),
        editableFields = listOf("title" to "主标题"),
        layers = listOf(
            TextTemplateLayer(
                id = "title", label = "主标题", text = "NEON", x = 0f, y = -10f,
                fontSize = 150f, effect = "neon",
                effectParams = JSONObject().apply {
                    put("color", "#ff2bbd")
                    put("innerColor", "#ffffff")
                    put("strokeWidth", 5)
                    put("glowColor", "#ff149f")
                    put("glowBlur", 36)
                    put("flicker", false)
                }
            )
        )
    )

    // ---------- 6. 黄金金属字 ----------
    private val goldTemplate = TextTemplate(
        id = "gold",
        name = "黄金金属字",
        backgroundColorArgb = 0xFF14100A.toInt(),
        editableFields = listOf("title" to "主标题"),
        layers = listOf(
            TextTemplateLayer(
                id = "title", label = "主标题", text = "GOLD", x = 0f, y = -10f,
                fontSize = 145f, effect = "gold",
                effectParams = JSONObject().apply {
                    put("strokeWidth", 10)
                    put("strokeColor", "#713f08")
                    put("shadowColor", "rgba(0,0,0,0.6)")
                    put("shadowBlur", 15)
                    put("gradient", JSONArray().apply { put("#fff5a6"); put("#dfa51c"); put("#fff4a2"); put("#a96408"); put("#fff09a") })
                    put("reflection", true)
                }
            )
        )
    )

    // ---------- 7. 火焰字 ----------
    private val fireTemplate = TextTemplate(
        id = "fire",
        name = "火焰字",
        backgroundColorArgb = 0xFF1A0A0A.toInt(),
        editableFields = listOf("title" to "主标题"),
        layers = listOf(
            TextTemplateLayer(
                id = "title", label = "主标题", text = "火焰", x = 0f, y = -10f,
                fontSize = 160f, effect = "fire",
                effectParams = JSONObject().apply {
                    put("strokeWidth", 10)
                    put("strokeColor", "#7b1005")
                    put("glowColor", "#ff3d00")
                    put("glowBlur", 30)
                    put("gradient", JSONArray().apply { put("#fff7a0"); put("#ffd000"); put("#ff6a00"); put("#d71900") })
                    put("flameTexture", true)
                    put("animation", false)
                }
            )
        )
    )

    // ---------- 8. 冰霜字 ----------
    private val iceTemplate = TextTemplate(
        id = "ice",
        name = "冰霜字",
        backgroundColorArgb = 0xFF0B2233.toInt(),
        editableFields = listOf("title" to "主标题"),
        layers = listOf(
            TextTemplateLayer(
                id = "title", label = "主标题", text = "ICE", x = 0f, y = -10f,
                fontSize = 150f, effect = "ice",
                effectParams = JSONObject().apply {
                    put("strokeWidth", 10)
                    put("strokeColor", "#79dfff")
                    put("glowColor", "rgba(0,190,255,0.8)")
                    put("glowBlur", 26)
                    put("gradient", JSONArray().apply { put("#ffffff"); put("#d9fbff"); put("#85e6ff"); put("#2aa9e8"); put("#0b559d") })
                    put("crystalTexture", true)
                    put("highlight", true)
                }
            )
        )
    )

    // ---------- 9. 复古故障字 ----------
    private val glitchTemplate = TextTemplate(
        id = "glitch",
        name = "复古故障字",
        backgroundColorArgb = 0xFF101015.toInt(),
        editableFields = listOf("title" to "主标题"),
        layers = listOf(
            TextTemplateLayer(
                id = "title", label = "主标题", text = "GLITCH", x = 0f, y = -10f,
                fontSize = 150f, effect = "glitch",
                effectParams = JSONObject().apply {
                    put("mainColor", "#ffffff")
                    put("redColor", "#ff315c")
                    put("blueColor", "#00eaff")
                    put("strokeWidth", 4)
                    put("offsetX", 8)
                    put("offsetY", 3)
                    put("scanline", true)
                    put("noise", true)
                    put("animation", false)
                }
            )
        )
    )

    // ---------- 10. 赛博科技字 ----------
    private val cyberTemplate = TextTemplate(
        id = "cyber",
        name = "赛博科技字",
        backgroundColorArgb = 0xFF041022.toInt(),
        editableFields = listOf("title" to "主标题"),
        layers = listOf(
            TextTemplateLayer(
                id = "title", label = "主标题", text = "CYBER", x = 0f, y = -10f,
                fontSize = 140f, effect = "cyber",
                effectParams = JSONObject().apply {
                    put("mainColor", "#25f6ff")
                    put("secondaryColor", "#1768ff")
                    put("strokeColor", "#0affff")
                    put("strokeWidth", 4)
                    put("glowColor", "#00d9ff")
                    put("glowBlur", 24)
                    put("grid", true)
                    put("scanline", true)
                    put("animation", false)
                }
            )
        )
    )

    /** 全部模板列表（可继续在末尾追加第 11、12 组） */
    val templates: List<TextTemplate> = listOf(
        crystalTemplate,
        glassTemplate,
        embroideryTemplate,
        threeDTemplate,
        neonTemplate,
        goldTemplate,
        fireTemplate,
        iceTemplate,
        glitchTemplate,
        cyberTemplate
    )
}