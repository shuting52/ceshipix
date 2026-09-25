package com.landesheji.data.db

import com.landesheji.data.model.BackgroundType
import com.landesheji.data.model.CanvasConfig
import com.landesheji.data.model.DrawPoint
import com.landesheji.data.model.DrawStroke
import com.landesheji.data.model.ImageLayerProperties
import com.landesheji.data.model.LayerItem
import com.landesheji.data.model.LayerType
import com.landesheji.data.model.ShapeProperties
import com.landesheji.data.model.ShapeType
import com.landesheji.data.model.StickerProperties
import com.landesheji.data.model.StrokePosition
import com.landesheji.data.model.TextAlignment
import com.landesheji.data.model.TextProperties
import org.json.JSONArray
import org.json.JSONObject

object ProjectSerializer {

    fun serializeCanvas(config: CanvasConfig): String {
        val json = JSONObject()
        json.put("width", config.width)
        json.put("height", config.height)
        json.put("ratioName", config.ratioName)
        json.put("backgroundType", config.backgroundType.name)
        json.put("backgroundColorArgb", config.backgroundColorArgb)
        json.put("backgroundGradientColor2Argb", config.backgroundGradientColor2Argb)
        json.put("backgroundGradientAngle", config.backgroundGradientAngle.toDouble())
        json.put("backgroundImageUri", config.backgroundImageUri)
        json.put("presetTextureIndex", config.presetTextureIndex)
        json.put("backgroundVideoUri", config.backgroundVideoUri)
        json.put("backgroundVideoMuted", config.backgroundVideoMuted)
        json.put("backgroundVideoLoop", config.backgroundVideoLoop)
        json.put("backgroundBlur", config.backgroundBlur.toDouble())
        json.put("backgroundNeonGlow", config.backgroundNeonGlow.toDouble())
        json.put("backgroundInvert", config.backgroundInvert)
        json.put("vignette", config.vignette.toDouble())
        json.put("noise", config.noise.toDouble())
        json.put("stripes", config.stripes.toDouble())
        json.put("brightness", config.brightness.toDouble())
        json.put("contrast", config.contrast.toDouble())
        return json.toString()
    }

    fun deserializeCanvas(jsonString: String): CanvasConfig {
        if (jsonString.isBlank()) return CanvasConfig()
        return try {
            val json = JSONObject(jsonString)
            CanvasConfig(
                width = json.optInt("width", 1080),
                height = json.optInt("height", 1080),
                ratioName = json.optString("ratioName", "1:1 正方形"),
                backgroundType = runCatching {
                    BackgroundType.valueOf(json.optString("backgroundType", "COLOR"))
                }.getOrDefault(BackgroundType.COLOR),
                backgroundColorArgb = json.optInt("backgroundColorArgb", 0xFF222831.toInt()),
                backgroundGradientColor2Argb = json.optInt("backgroundGradientColor2Argb", 0xFF393E46.toInt()),
                backgroundGradientAngle = json.optDouble("backgroundGradientAngle", 45.0).toFloat(),
                backgroundImageUri = json.optString("backgroundImageUri", ""),
                presetTextureIndex = json.optInt("presetTextureIndex", 0),
                backgroundVideoUri = json.optString("backgroundVideoUri", ""),
                backgroundVideoMuted = json.optBoolean("backgroundVideoMuted", true),
                backgroundVideoLoop = json.optBoolean("backgroundVideoLoop", true),
                backgroundBlur = json.optDouble("backgroundBlur", 0.0).toFloat(),
                backgroundNeonGlow = json.optDouble("backgroundNeonGlow", 0.0).toFloat(),
                backgroundInvert = json.optBoolean("backgroundInvert", false),
                vignette = json.optDouble("vignette", 0.0).toFloat(),
                noise = json.optDouble("noise", 0.0).toFloat(),
                stripes = json.optDouble("stripes", 0.0).toFloat(),
                brightness = json.optDouble("brightness", 1.0).toFloat(),
                contrast = json.optDouble("contrast", 1.0).toFloat()
            )
        } catch (_: Exception) {
            CanvasConfig()
        }
    }

    fun serializeLayers(layers: List<LayerItem>): String {
        val array = JSONArray()
        for (layer in layers) {
            val obj = JSONObject()
            obj.put("id", layer.id)
            obj.put("name", layer.name)
            obj.put("type", layer.type.name)
            obj.put("x", layer.x.toDouble())
            obj.put("y", layer.y.toDouble())
            obj.put("scaleX", layer.scaleX.toDouble())
            obj.put("scaleY", layer.scaleY.toDouble())
            obj.put("rotation", layer.rotation.toDouble())
            obj.put("opacity", layer.opacity.toDouble())
            obj.put("isVisible", layer.isVisible)
            obj.put("isLocked", layer.isLocked)
            obj.put("zIndex", layer.zIndex)

            // Text
            val textObj = JSONObject()
            textObj.put("text", layer.textProps.text)
            textObj.put("fontSize", layer.textProps.fontSize.toDouble())
            textObj.put("colorArgb", layer.textProps.colorArgb)
            textObj.put("isGradient", layer.textProps.isGradient)
            textObj.put("gradientColor2Argb", layer.textProps.gradientColor2Argb)
            textObj.put("gradientAngle", layer.textProps.gradientAngle.toDouble())
            textObj.put("isBold", layer.textProps.isBold)
            textObj.put("isItalic", layer.textProps.isItalic)
            textObj.put("isUnderline", layer.textProps.isUnderline)
            textObj.put("fontStyleName", layer.textProps.fontStyleName)
            textObj.put("alignment", layer.textProps.alignment.name)
            textObj.put("letterSpacing", layer.textProps.letterSpacing.toDouble())
            textObj.put("lineSpacing", layer.textProps.lineSpacing.toDouble())
            textObj.put("curveAngle", layer.textProps.curveAngle.toDouble())
            textObj.put("hasBackgroundBox", layer.textProps.hasBackgroundBox)
            textObj.put("backgroundBoxColorArgb", layer.textProps.backgroundBoxColorArgb)
            textObj.put("backgroundBoxRadius", layer.textProps.backgroundBoxRadius.toDouble())
            textObj.put("backgroundBoxPadding", layer.textProps.backgroundBoxPadding.toDouble())
            textObj.put("hasStroke", layer.textProps.hasStroke)
            textObj.put("strokeColorArgb", layer.textProps.strokeColorArgb)
            textObj.put("strokeWidth", layer.textProps.strokeWidth.toDouble())
            // v2.2：描边位置
            textObj.put("strokePosition", layer.textProps.strokePosition.name)
            textObj.put("hasShadow", layer.textProps.hasShadow)
            textObj.put("shadowColorArgb", layer.textProps.shadowColorArgb)
            textObj.put("shadowRadius", layer.textProps.shadowRadius.toDouble())
            textObj.put("shadowDx", layer.textProps.shadowDx.toDouble())
            textObj.put("shadowDy", layer.textProps.shadowDy.toDouble())
            // v2.2：阴影扩展
            textObj.put("shadowSpread", layer.textProps.shadowSpread.toDouble())
            textObj.put("has3D", layer.textProps.has3D)
            textObj.put("depth3D", layer.textProps.depth3D.toDouble())
            textObj.put("angle3D", layer.textProps.angle3D.toDouble())
            textObj.put("isPerspective3D", layer.textProps.isPerspective3D)
            textObj.put("perspectiveAmount3D", layer.textProps.perspectiveAmount3D.toDouble())
            textObj.put("darken3D", layer.textProps.darken3D.toDouble())
            textObj.put("customExtrusionColor", layer.textProps.customExtrusionColor)
            textObj.put("extrusionColorArgb", layer.textProps.extrusionColorArgb)
            textObj.put("gradientExtrusion", layer.textProps.gradientExtrusion)
            textObj.put("gradientExtrusionColor2Argb", layer.textProps.gradientExtrusionColor2Argb)
            textObj.put("hasBevel", layer.textProps.hasBevel)
            textObj.put("bevelWidth", layer.textProps.bevelWidth.toDouble())
            textObj.put("bevelAngle", layer.textProps.bevelAngle.toDouble())
            textObj.put("bevelHighlightColorArgb", layer.textProps.bevelHighlightColorArgb)
            textObj.put("bevelShadowColorArgb", layer.textProps.bevelShadowColorArgb)
            textObj.put("lightAngle3D", layer.textProps.lightAngle3D.toDouble())
            textObj.put("lightIntensity3D", layer.textProps.lightIntensity3D.toDouble())
            textObj.put("material3D", layer.textProps.material3D)
            textObj.put("rotateX3D", layer.textProps.rotateX3D.toDouble())
            textObj.put("rotateY3D", layer.textProps.rotateY3D.toDouble())
            textObj.put("hasGroundShadow", layer.textProps.hasGroundShadow)
            textObj.put("groundShadowBlur", layer.textProps.groundShadowBlur.toDouble())
            textObj.put("groundShadowDistance", layer.textProps.groundShadowDistance.toDouble())
            textObj.put("groundShadowOpacity", layer.textProps.groundShadowOpacity.toDouble())
            textObj.put("hasReflection", layer.textProps.hasReflection)
            textObj.put("reflectionDistance", layer.textProps.reflectionDistance.toDouble())
            textObj.put("reflectionAlpha", layer.textProps.reflectionAlpha.toDouble())
            textObj.put("hasEmboss", layer.textProps.hasEmboss)
            textObj.put("embossIntensity", layer.textProps.embossIntensity.toDouble())
            // PS FX 新字段（需求3）
            textObj.put("hasGlow", layer.textProps.hasGlow)
            textObj.put("glowColorArgb", layer.textProps.glowColorArgb)
            textObj.put("glowRadius", layer.textProps.glowRadius.toDouble())
            // v2.2：外发光强度
            textObj.put("glowOpacity", layer.textProps.glowOpacity.toDouble())
            textObj.put("hasTextureFill", layer.textProps.hasTextureFill)
            textObj.put("textureIndex", layer.textProps.textureIndex)
            textObj.put("textureColorArgb", layer.textProps.textureColorArgb)
            textObj.put("hasGradientOverlay", layer.textProps.hasGradientOverlay)
            textObj.put("gradientOverlayColor2Argb", layer.textProps.gradientOverlayColor2Argb)
            textObj.put("gradientOverlayAngle", layer.textProps.gradientOverlayAngle.toDouble())
            // 需求重造3：模板特效字段（否则保存工程会丢效果）
            textObj.put("templateEffect", layer.textProps.templateEffect)
            textObj.put("templateEffectParamsJson", layer.textProps.templateEffectParamsJson)
            // v2.2：PS 图层样式增强
            textObj.put("strokePosition", layer.textProps.strokePosition.name)
            textObj.put("shadowSpread", layer.textProps.shadowSpread.toDouble())
            textObj.put("hasInnerShadow", layer.textProps.hasInnerShadow)
            textObj.put("innerShadowColorArgb", layer.textProps.innerShadowColorArgb)
            textObj.put("innerShadowRadius", layer.textProps.innerShadowRadius.toDouble())
            textObj.put("innerShadowDx", layer.textProps.innerShadowDx.toDouble())
            textObj.put("innerShadowDy", layer.textProps.innerShadowDy.toDouble())
            textObj.put("innerShadowOpacity", layer.textProps.innerShadowOpacity.toDouble())
            textObj.put("hasInnerGlow", layer.textProps.hasInnerGlow)
            textObj.put("innerGlowColorArgb", layer.textProps.innerGlowColorArgb)
            textObj.put("innerGlowRadius", layer.textProps.innerGlowRadius.toDouble())
            textObj.put("innerGlowOpacity", layer.textProps.innerGlowOpacity.toDouble())
            textObj.put("hasColorOverlay", layer.textProps.hasColorOverlay)
            textObj.put("colorOverlayColorArgb", layer.textProps.colorOverlayColorArgb)
            textObj.put("colorOverlayOpacity", layer.textProps.colorOverlayOpacity.toDouble())
            obj.put("textProps", textObj)

            // Shape
            val shapeObj = JSONObject()
            shapeObj.put("shapeType", layer.shapeProps.shapeType.name)
            shapeObj.put("fillColorArgb", layer.shapeProps.fillColorArgb)
            shapeObj.put("isGradient", layer.shapeProps.isGradient)
            shapeObj.put("gradientColor2Argb", layer.shapeProps.gradientColor2Argb)
            shapeObj.put("cornerRadius", layer.shapeProps.cornerRadius.toDouble())
            shapeObj.put("hasStroke", layer.shapeProps.hasStroke)
            shapeObj.put("strokeColorArgb", layer.shapeProps.strokeColorArgb)
            shapeObj.put("strokeWidth", layer.shapeProps.strokeWidth.toDouble())
            shapeObj.put("hasShadow", layer.shapeProps.hasShadow)
            shapeObj.put("shadowColorArgb", layer.shapeProps.shadowColorArgb)
            shapeObj.put("shadowRadius", layer.shapeProps.shadowRadius.toDouble())
            obj.put("shapeProps", shapeObj)

            // Sticker
            val stickerObj = JSONObject()
            stickerObj.put("stickerId", layer.stickerProps.stickerId)
            stickerObj.put("stickerName", layer.stickerProps.stickerName)
            stickerObj.put("tintArgb", layer.stickerProps.tintArgb)
            stickerObj.put("hasTint", layer.stickerProps.hasTint)
            obj.put("stickerProps", stickerObj)

            // Image
            val imgObj = JSONObject()
            imgObj.put("uriString", layer.imageProps.uriString)
            imgObj.put("aspectRatio", layer.imageProps.aspectRatio.toDouble())
            imgObj.put("cornerRadius", layer.imageProps.cornerRadius.toDouble())
            imgObj.put("borderStrokeWidth", layer.imageProps.borderStrokeWidth.toDouble())
            imgObj.put("borderColorArgb", layer.imageProps.borderColorArgb)
            obj.put("imageProps", imgObj)

            // Draw
            val strokesArr = JSONArray()
            for (stroke in layer.drawStrokes) {
                val sObj = JSONObject()
                sObj.put("colorArgb", stroke.colorArgb)
                sObj.put("strokeWidth", stroke.strokeWidth.toDouble())
                sObj.put("isEraser", stroke.isEraser)
                val ptsArr = JSONArray()
                for (pt in stroke.points) {
                    val pObj = JSONObject()
                    pObj.put("x", pt.x.toDouble())
                    pObj.put("y", pt.y.toDouble())
                    ptsArr.put(pObj)
                }
                sObj.put("points", ptsArr)
                strokesArr.put(sObj)
            }
            obj.put("drawStrokes", strokesArr)

            array.put(obj)
        }
        return array.toString()
    }

    fun deserializeLayers(jsonString: String): List<LayerItem> {
        if (jsonString.isBlank()) return emptyList()
        val list = mutableListOf<LayerItem>()
        try {
            val array = JSONArray(jsonString)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val type = runCatching {
                    LayerType.valueOf(obj.optString("type", "TEXT"))
                }.getOrDefault(LayerType.TEXT)

                val textObj = obj.optJSONObject("textProps") ?: JSONObject()
                val textProps = TextProperties(
                    text = textObj.optString("text", "文字"),
                    fontSize = textObj.optDouble("fontSize", 36.0).toFloat(),
                    colorArgb = textObj.optInt("colorArgb", 0xFFFFFFFF.toInt()),
                    isGradient = textObj.optBoolean("isGradient", false),
                    gradientColor2Argb = textObj.optInt("gradientColor2Argb", 0xFF00ADB5.toInt()),
                    gradientAngle = textObj.optDouble("gradientAngle", 45.0).toFloat(),
                    isBold = textObj.optBoolean("isBold", false),
                    isItalic = textObj.optBoolean("isItalic", false),
                    isUnderline = textObj.optBoolean("isUnderline", false),
                    fontStyleName = textObj.optString("fontStyleName", "默认"),
                    alignment = runCatching {
                        TextAlignment.valueOf(textObj.optString("alignment", "CENTER"))
                    }.getOrDefault(TextAlignment.CENTER),
                    letterSpacing = textObj.optDouble("letterSpacing", 0.0).toFloat(),
                    lineSpacing = textObj.optDouble("lineSpacing", 1.0).toFloat(),
                    curveAngle = textObj.optDouble("curveAngle", 0.0).toFloat(),
                    hasBackgroundBox = textObj.optBoolean("hasBackgroundBox", false),
                    backgroundBoxColorArgb = textObj.optInt("backgroundBoxColorArgb", 0x88000000.toInt()),
                    backgroundBoxRadius = textObj.optDouble("backgroundBoxRadius", 8.0).toFloat(),
                    backgroundBoxPadding = textObj.optDouble("backgroundBoxPadding", 8.0).toFloat(),
                    hasStroke = textObj.optBoolean("hasStroke", false),
                    strokeColorArgb = textObj.optInt("strokeColorArgb", 0xFF000000.toInt()),
                    strokeWidth = textObj.optDouble("strokeWidth", 4.0).toFloat(),
                    strokePosition = runCatching {
                        StrokePosition.valueOf(textObj.optString("strokePosition", "OUTER"))
                    }.getOrDefault(StrokePosition.OUTER),
                    hasShadow = textObj.optBoolean("hasShadow", false),
                    shadowColorArgb = textObj.optInt("shadowColorArgb", 0xAA000000.toInt()),
                    shadowRadius = textObj.optDouble("shadowRadius", 10.0).toFloat(),
                    shadowDx = textObj.optDouble("shadowDx", 4.0).toFloat(),
                    shadowDy = textObj.optDouble("shadowDy", 4.0).toFloat(),
                    shadowSpread = textObj.optDouble("shadowSpread", 0.0).toFloat(),
                    has3D = textObj.optBoolean("has3D", false),
                    depth3D = textObj.optDouble("depth3D", 12.0).toFloat(),
                    angle3D = textObj.optDouble("angle3D", 45.0).toFloat(),
                    isPerspective3D = textObj.optBoolean("isPerspective3D", false),
                    perspectiveAmount3D = textObj.optDouble("perspectiveAmount3D", 0.5).toFloat(),
                    darken3D = textObj.optDouble("darken3D", 0.5).toFloat(),
                    customExtrusionColor = textObj.optBoolean("customExtrusionColor", false),
                    extrusionColorArgb = textObj.optInt("extrusionColorArgb", 0xFF555555.toInt()),
                    gradientExtrusion = textObj.optBoolean("gradientExtrusion", false),
                    gradientExtrusionColor2Argb = textObj.optInt("gradientExtrusionColor2Argb", 0xFF222222.toInt()),
                    hasBevel = textObj.optBoolean("hasBevel", false),
                    bevelWidth = textObj.optDouble("bevelWidth", 4.0).toFloat(),
                    bevelAngle = textObj.optDouble("bevelAngle", 120.0).toFloat(),
                    bevelHighlightColorArgb = textObj.optInt("bevelHighlightColorArgb", 0xFFFFFFFF.toInt()),
                    bevelShadowColorArgb = textObj.optInt("bevelShadowColorArgb", 0xFF000000.toInt()),
                    lightAngle3D = textObj.optDouble("lightAngle3D", 135.0).toFloat(),
                    lightIntensity3D = textObj.optDouble("lightIntensity3D", 1.0).toFloat(),
                    material3D = textObj.optString("material3D", "默认"),
                    rotateX3D = textObj.optDouble("rotateX3D", 0.0).toFloat(),
                    rotateY3D = textObj.optDouble("rotateY3D", 0.0).toFloat(),
                    hasGroundShadow = textObj.optBoolean("hasGroundShadow", false),
                    groundShadowBlur = textObj.optDouble("groundShadowBlur", 14.0).toFloat(),
                    groundShadowDistance = textObj.optDouble("groundShadowDistance", 18.0).toFloat(),
                    groundShadowOpacity = textObj.optDouble("groundShadowOpacity", 0.4).toFloat(),
                    hasReflection = textObj.optBoolean("hasReflection", false),
                    reflectionDistance = textObj.optDouble("reflectionDistance", 4.0).toFloat(),
                    reflectionAlpha = textObj.optDouble("reflectionAlpha", 0.35).toFloat(),
                    hasEmboss = textObj.optBoolean("hasEmboss", false),
                    embossIntensity = textObj.optDouble("embossIntensity", 0.6).toFloat(),
                    // PS FX 新字段（需求3）
                    hasGlow = textObj.optBoolean("hasGlow", false),
                    glowColorArgb = textObj.optInt("glowColorArgb", 0xFF00D4FF.toInt()),
                    glowRadius = textObj.optDouble("glowRadius", 18.0).toFloat(),
                    glowOpacity = textObj.optDouble("glowOpacity", 0.8).toFloat(),
                    hasTextureFill = textObj.optBoolean("hasTextureFill", false),
                    textureIndex = textObj.optInt("textureIndex", 0),
                    textureColorArgb = textObj.optInt("textureColorArgb", 0xFFFFFFFF.toInt()),
                    hasGradientOverlay = textObj.optBoolean("hasGradientOverlay", false),
                    gradientOverlayColor2Argb = textObj.optInt("gradientOverlayColor2Argb", 0xFFFF6B9D.toInt()),
                    gradientOverlayAngle = textObj.optDouble("gradientOverlayAngle", 45.0).toFloat(),
                    // 需求重造3：模板特效字段
                    templateEffect = textObj.optString("templateEffect", ""),
                    templateEffectParamsJson = textObj.optString("templateEffectParamsJson", "{}"),
                    // v2.2：PS 图层样式增强
                    hasInnerShadow = textObj.optBoolean("hasInnerShadow", false),
                    innerShadowColorArgb = textObj.optInt("innerShadowColorArgb", 0xCC000000.toInt()),
                    innerShadowRadius = textObj.optDouble("innerShadowRadius", 8.0).toFloat(),
                    innerShadowDx = textObj.optDouble("innerShadowDx", 3.0).toFloat(),
                    innerShadowDy = textObj.optDouble("innerShadowDy", 3.0).toFloat(),
                    innerShadowOpacity = textObj.optDouble("innerShadowOpacity", 0.7).toFloat(),
                    hasInnerGlow = textObj.optBoolean("hasInnerGlow", false),
                    innerGlowColorArgb = textObj.optInt("innerGlowColorArgb", 0xFFFF8C00.toInt()),
                    innerGlowRadius = textObj.optDouble("innerGlowRadius", 10.0).toFloat(),
                    innerGlowOpacity = textObj.optDouble("innerGlowOpacity", 0.8).toFloat(),
                    hasColorOverlay = textObj.optBoolean("hasColorOverlay", false),
                    colorOverlayColorArgb = textObj.optInt("colorOverlayColorArgb", 0xFF00ADB5.toInt()),
                    colorOverlayOpacity = textObj.optDouble("colorOverlayOpacity", 1.0).toFloat()
                )

                val shapeObj = obj.optJSONObject("shapeProps") ?: JSONObject()
                val shapeProps = ShapeProperties(
                    shapeType = runCatching {
                        ShapeType.valueOf(shapeObj.optString("shapeType", "ROUNDED_RECTANGLE"))
                    }.getOrDefault(ShapeType.ROUNDED_RECTANGLE),
                    fillColorArgb = shapeObj.optInt("fillColorArgb", 0xFF00ADB5.toInt()),
                    isGradient = shapeObj.optBoolean("isGradient", false),
                    gradientColor2Argb = shapeObj.optInt("gradientColor2Argb", 0xFF393E46.toInt()),
                    cornerRadius = shapeObj.optDouble("cornerRadius", 16.0).toFloat(),
                    hasStroke = shapeObj.optBoolean("hasStroke", false),
                    strokeColorArgb = shapeObj.optInt("strokeColorArgb", 0xFFFFFFFF.toInt()),
                    strokeWidth = shapeObj.optDouble("strokeWidth", 3.0).toFloat(),
                    hasShadow = shapeObj.optBoolean("hasShadow", true),
                    shadowColorArgb = shapeObj.optInt("shadowColorArgb", 0x88000000.toInt()),
                    shadowRadius = shapeObj.optDouble("shadowRadius", 8.0).toFloat()
                )

                val stickerObj = obj.optJSONObject("stickerProps") ?: JSONObject()
                val stickerProps = StickerProperties(
                    stickerId = stickerObj.optString("stickerId", "star"),
                    stickerName = stickerObj.optString("stickerName", "星星"),
                    tintArgb = stickerObj.optInt("tintArgb", 0xFFFFFFFF.toInt()),
                    hasTint = stickerObj.optBoolean("hasTint", false)
                )

                val imgObj = obj.optJSONObject("imageProps") ?: JSONObject()
                val imageProps = ImageLayerProperties(
                    uriString = imgObj.optString("uriString", ""),
                    aspectRatio = imgObj.optDouble("aspectRatio", 1.0).toFloat(),
                    cornerRadius = imgObj.optDouble("cornerRadius", 0.0).toFloat(),
                    borderStrokeWidth = imgObj.optDouble("borderStrokeWidth", 0.0).toFloat(),
                    borderColorArgb = imgObj.optInt("borderColorArgb", 0xFFFFFFFF.toInt())
                )

                val strokesList = mutableListOf<DrawStroke>()
                val strokesArr = obj.optJSONArray("drawStrokes")
                if (strokesArr != null) {
                    for (s in 0 until strokesArr.length()) {
                        val sObj = strokesArr.getJSONObject(s)
                        val ptsList = mutableListOf<DrawPoint>()
                        val ptsArr = sObj.optJSONArray("points")
                        if (ptsArr != null) {
                            for (p in 0 until ptsArr.length()) {
                                val pObj = ptsArr.getJSONObject(p)
                                ptsList.add(
                                    DrawPoint(
                                        x = pObj.optDouble("x", 0.0).toFloat(),
                                        y = pObj.optDouble("y", 0.0).toFloat()
                                    )
                                )
                            }
                        }
                        strokesList.add(
                            DrawStroke(
                                points = ptsList,
                                colorArgb = sObj.optInt("colorArgb", 0xFFFFFFFF.toInt()),
                                strokeWidth = sObj.optDouble("strokeWidth", 6.0).toFloat(),
                                isEraser = sObj.optBoolean("isEraser", false)
                            )
                        )
                    }
                }

                list.add(
                    LayerItem(
                        id = obj.optString("id", java.util.UUID.randomUUID().toString()),
                        name = obj.optString("name", "图层"),
                        type = type,
                        x = obj.optDouble("x", 0.0).toFloat(),
                        y = obj.optDouble("y", 0.0).toFloat(),
                        scaleX = obj.optDouble("scaleX", 1.0).toFloat(),
                        scaleY = obj.optDouble("scaleY", 1.0).toFloat(),
                        rotation = obj.optDouble("rotation", 0.0).toFloat(),
                        opacity = obj.optDouble("opacity", 1.0).toFloat(),
                        isVisible = obj.optBoolean("isVisible", true),
                        isLocked = obj.optBoolean("isLocked", false),
                        zIndex = obj.optInt("zIndex", 0),
                        textProps = textProps,
                        shapeProps = shapeProps,
                        stickerProps = stickerProps,
                        imageProps = imageProps,
                        drawStrokes = strokesList
                    )
                )
            }
        } catch (_: Exception) {}
        return list
    }
}
