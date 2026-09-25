package com.landesheji

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.landesheji.data.db.ProjectSerializer
import com.landesheji.data.model.BackgroundType
import com.landesheji.data.model.CanvasConfig
import com.landesheji.data.model.LayerItem
import com.landesheji.data.model.LayerType
import com.landesheji.data.model.PresetsData
import com.landesheji.data.model.QuotesData
import com.landesheji.data.model.TextProperties
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context verifies PixelLab app name`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("PixelLab", appName)
    }

    @Test
    fun `quotes library contains categories and quotes`() {
        assertTrue(QuotesData.categories.isNotEmpty())
        val firstCategory = QuotesData.categories.first()
        assertTrue(firstCategory.quotes.isNotEmpty())
    }

    @Test
    fun `presets library contains templates`() {
        assertTrue(PresetsData.templates.isNotEmpty())
        val defaultPreset = PresetsData.templates.first()
        assertEquals("default", defaultPreset.id)
    }

    @Test
    fun `canvas serialization roundtrip`() {
        val config = CanvasConfig(
            width = 1920,
            height = 1080,
            ratioName = "16:9 横屏",
            backgroundType = BackgroundType.GRADIENT,
            backgroundColorArgb = 0xFF141E30.toInt(),
            backgroundGradientColor2Argb = 0xFF243B55.toInt()
        )
        val json = ProjectSerializer.serializeCanvas(config)
        val deserialized = ProjectSerializer.deserializeCanvas(json)

        assertEquals(1920, deserialized.width)
        assertEquals(1080, deserialized.height)
        assertEquals("16:9 横屏", deserialized.ratioName)
        assertEquals(BackgroundType.GRADIENT, deserialized.backgroundType)
    }

    @Test
    fun `layers serialization roundtrip`() {
        val layers = listOf(
            LayerItem(
                name = "测试文字",
                type = LayerType.TEXT,
                textProps = TextProperties(
                    text = "你好 PixelLab",
                    fontSize = 36f,
                    isBold = true,
                    has3D = true,
                    depth3D = 10f
                )
            )
        )
        val json = ProjectSerializer.serializeLayers(layers)
        val restored = ProjectSerializer.deserializeLayers(json)

        assertEquals(1, restored.size)
        assertEquals("测试文字", restored[0].name)
        assertEquals("你好 PixelLab", restored[0].textProps.text)
        assertTrue(restored[0].textProps.isBold)
        assertTrue(restored[0].textProps.has3D)
    }
}
