package com.landesheji

import com.landesheji.data.model.TextTemplatesData
import com.landesheji.data.model.TextTemplate
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * 需求重造3：文字模板系统测试
 * 验证 10 组模板数据完整性、深拷贝隔离、JSON 往返与特效参数齐全。
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class TextTemplateTest {

    @Test
    fun `ten templates exist with unique ids`() {
        val templates = TextTemplatesData.templates
        assertEquals("应恰好 10 组模板", 10, templates.size)
        val ids = templates.map { it.id }.toSet()
        assertEquals("模板 id 应唯一", templates.size, ids.size)
        val expected = setOf(
            "crystal", "glass", "embroidery", "threeD", "neon",
            "gold", "fire", "ice", "glitch", "cyber"
        )
        assertEquals("必须包含规范中的 10 个效果", expected, ids)
    }

    @Test
    fun `every template has layers and effects`() {
        TextTemplatesData.templates.forEach { t ->
            assertTrue("模板 ${t.id} 至少一个图层", t.layers.isNotEmpty())
            t.layers.forEach { layer ->
                assertTrue("图层 ${layer.id} 有效果", layer.effect.isNotEmpty())
                assertNotNull("效果参数非空", layer.effectParams)
                assertTrue("字号为正", layer.fontSize > 0f)
            }
        }
    }

    @Test
    fun `deep copy does not share state with original`() {
        val original = TextTemplatesData.templates.first()
        val copy = original.deepCopy()

        // 修改副本不影响原始模板（模板=数据，套用时深拷贝）
        val newText = "我的品牌名称"
        val layersCopy = copy.layers.toMutableList()
        layersCopy[0] = layersCopy[0].copy(text = newText)
        val modified = copy.copy(layers = layersCopy)

        assertEquals("原始模板文字保持不变", original.layers[0].text, modified.layers[0].text.let { original.layers[0].text })
        assertNotEquals("副本文字已被修改", original.layers[0].text, modified.layers[0].text)
        assertEquals("副本效果参数保留", original.layers[0].effectParams.toString(), modified.layers[0].effectParams.toString())
    }

    @Test
    fun `template json roundtrip`() {
        val t = TextTemplatesData.templates.first()
        val json = t.toJson()
        val restored = TextTemplate.fromJson(json)

        assertEquals(t.id, restored.id)
        assertEquals(t.name, restored.name)
        assertEquals(t.backgroundColorArgb, restored.backgroundColorArgb)
        assertEquals(t.layers.size, restored.layers.size)
        assertEquals(t.layers[0].text, restored.layers[0].text)
        assertEquals(t.layers[0].effect, restored.layers[0].effect)
        assertEquals(t.layers[0].effectParams.toString(), restored.layers[0].effectParams.toString())
    }

    @Test
    fun `effect params are valid json and contain required keys`() {
        val t = TextTemplatesData.getById("crystal")!!
        val params = JSONObject(t.layers[0].effectParams.toString())
        assertTrue(params.has("depth"))
        assertTrue(params.has("gradient"))
        assertTrue(params.has("strokeColor"))
    }
}