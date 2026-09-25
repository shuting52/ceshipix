package com.landesheji.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.landesheji.data.db.ProjectSerializer
import com.landesheji.data.model.CanvasConfig
import com.landesheji.data.model.LayerItem
import com.landesheji.ui.dialogs.ExportQuality
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

/**
 * 需求4：源码导出/导入
 *
 * - .plp：PixelLab 工程包（ZIP 内嵌 project.json + cover.png）→ 100% 可再次编辑（文字图层全部保留）
 * - .psd：标准 Photoshop 位图（含源数据 8BIM 资源块）→
 *         PixelLab 导入时能还原源数据继续编辑（含文字编辑）；外部 PSD 作为图片图层导入
 */
object SourceCodec {

    private const val PSD_MAGIC = "8BPS"
    private const val PIXELLAB_RESOURCE_ID = 0x0FA0 // 私有 8BIM 资源 ID（0x0FA0-0x0FFF 范围）

    // ======================= PLP =======================
    suspend fun exportPlp(
        context: Context,
        config: CanvasConfig,
        layers: List<LayerItem>,
        fileName: String
    ): File? = withContext(Dispatchers.IO) {
        try {
            val dir = File(context.getExternalFilesDir(null) ?: context.filesDir, "PixelLabSource")
            if (!dir.exists()) dir.mkdirs()
            val file = File(dir, if (fileName.endsWith(".plp")) fileName else "$fileName.plp")

            FileOutputStream(file).use { fos ->
                ZipOutputStream(fos).use { zos ->
                    // project.json
                    val projectJson = JSONObject()
                        .put("app", "PixelLab")
                        .put("version", 1)
                        .put("canvas", JSONObject(ProjectSerializer.serializeCanvas(config)))
                        .put("layers", org.json.JSONArray(ProjectSerializer.serializeLayers(layers)))
                    zos.putNextEntry(ZipEntry("project.json"))
                    zos.write(projectJson.toString().toByteArray(Charsets.UTF_8))
                    zos.closeEntry()

                    // cover.png（封面预览）
                    val cover = BitmapExporter.generateBitmapSync(config, layers, ExportQuality.STANDARD)
                    val coverBytes = ByteArrayOutputStream().also {
                        cover.compress(Bitmap.CompressFormat.PNG, 100, it)
                    }.toByteArray()
                    zos.putNextEntry(ZipEntry("cover.png"))
                    zos.write(coverBytes)
                    zos.closeEntry()
                }
            }
            file
        } catch (_: Exception) {
            null
        }
    }

    data class PlpData(
        val canvas: CanvasConfig,
        val layers: List<LayerItem>
    )

    fun parsePlp(stream: InputStream): PlpData? {
        return try {
            var canvasJson = ""
            var layersJson = ""
            ZipInputStream(stream).use { zis ->
                var entry = zis.nextEntry
                while (entry != null) {
                    if (entry.name == "project.json") {
                        val content = zis.readBytes().toString(Charsets.UTF_8)
                        val obj = JSONObject(content)
                        canvasJson = obj.optJSONObject("canvas")?.toString() ?: ""
                        layersJson = obj.optJSONArray("layers")?.toString() ?: ""
                        break
                    }
                    entry = zis.nextEntry
                }
            }
            if (canvasJson.isBlank() && layersJson.isBlank()) return null
            PlpData(
                canvas = if (canvasJson.isBlank()) CanvasConfig() else ProjectSerializer.deserializeCanvas(canvasJson),
                layers = ProjectSerializer.deserializeLayers(layersJson)
            )
        } catch (_: Exception) {
            null
        }
    }

    // ======================= PSD =======================
    suspend fun exportPsd(
        context: Context,
        config: CanvasConfig,
        layers: List<LayerItem>,
        fileName: String
    ): File? = withContext(Dispatchers.IO) {
        try {
            val dir = File(context.getExternalFilesDir(null) ?: context.filesDir, "PixelLabSource")
            if (!dir.exists()) dir.mkdirs()
            val file = File(dir, if (fileName.endsWith(".psd")) fileName else "$fileName.psd")

            // 1. 渲染画布位图（绘图区中心内容）
            val bitmap = BitmapExporter.generateBitmapSync(config, layers, ExportQuality.STANDARD)

            // 2. 构建源数据 JSON（用于还原编辑）
            val sourceJson = JSONObject()
                .put("app", "PixelLab")
                .put("version", 1)
                .put("canvas", JSONObject(ProjectSerializer.serializeCanvas(config)))
                .put("layers", org.json.JSONArray(ProjectSerializer.serializeLayers(layers)))

            writePsd(file, bitmap, sourceJson)
            file
        } catch (_: Exception) {
            null
        }
    }

    /**
     * 写入最小标准 PSD：
     * Header(8BPS,RGB 8bit) + ColorModeData(0) + ImageResources(PixelLab 数据 + 缩略图) + 无图层 + 未压缩像素
     */
    private fun writePsd(file: File, bitmap: Bitmap, sourceJson: JSONObject) {
        val w = bitmap.width
        val h = bitmap.height
        val byteOut = ByteArrayOutputStream()

        // ---- Header ----
        byteOut.write(PSD_MAGIC.toByteArray(Charsets.US_ASCII)) // 8BPS
        byteOut.write(shortToBytes(1))            // version 1
        byteOut.write(ByteArray(6))               // reserved
        byteOut.write(shortToBytes(3))            // channels = 3
        byteOut.write(intToBytes(h))              // height
        byteOut.write(intToBytes(w))              // width
        byteOut.write(shortToBytes(8))            // depth
        byteOut.write(shortToBytes(3))            // color mode = RGB

        // ---- Color Mode Data ----
        byteOut.write(intToBytes(0))

        // ---- Image Resources ----
        val resOut = ByteArrayOutputStream()
        // 1. PixelLab 源数据（自定义 8BIM 块）
        val sourceBytes = sourceJson.toString().toByteArray(Charsets.UTF_8)
        resOut.write("8BIM".toByteArray(Charsets.US_ASCII))
        resOut.write(intToBytes(PIXELLAB_RESOURCE_ID))
        resOut.write(pascalStringBytes("PixelLabSource"))
        val dataLen = sourceBytes.size
        resOut.write(intToBytes(dataLen))
        resOut.write(sourceBytes)
        if (dataLen % 2 != 0) resOut.write(0) // pad to even

        val resources = resOut.toByteArray()
        byteOut.write(intToBytes(resources.size))
        byteOut.write(resources)

        // ---- Layer and Mask Info ----
        byteOut.write(intToBytes(0)) // no layer info

        // ---- Image Data (uncompressed RGB) ----
        byteOut.write(shortToBytes(0)) // compression = raw
        // 三通道平面：R、G、B
        val pixels = IntArray(w * h)
        bitmap.getPixels(pixels, 0, w, 0, 0, w, h)
        for (channel in 0 until 3) {
            for (p in pixels) {
                byteOut.write(when (channel) {
                    0 -> (p shr 16) and 0xFF
                    1 -> (p shr 8) and 0xFF
                    else -> p and 0xFF
                })
            }
        }

        FileOutputStream(file).use { it.write(byteOut.toByteArray()) }
    }

    /**
     * 解析 PSD：返回 [PsdParseResult]（源数据优先，其次位图）
     */
    data class PsdParseResult(
        val plp: PlpData? = null,
        val bitmap: Bitmap? = null
    )

    fun parsePsd(stream: InputStream): PsdParseResult {
        return try {
            val bytes = stream.readBytes()
            if (bytes.size < 26 || !bytes.copyOfRange(0, 4).contentEquals(PSD_MAGIC.toByteArray(Charsets.US_ASCII))) return PsdParseResult()

            var pos = 26 // after header

            // Color Mode Data
            val cmLen = readInt(bytes, pos); pos += 4 + cmLen

            // Image Resources
            val resLen = readInt(bytes, pos); pos += 4
            val resEnd = pos + resLen
            var sourceJson: JSONObject? = null
            while (pos + 12 <= resEnd) {
                val sig = String(bytes, pos, 4, Charsets.US_ASCII)
                pos += 4
                if (sig != "8BIM") break
                val resId = readInt(bytes, pos); pos += 4
                // Pascal string (named, padded to even)
                val nameLen = bytes[pos].toInt() and 0xFF
                pos += 1 + nameLen
                if ((1 + nameLen) % 2 != 0) pos += 1
                val size = readInt(bytes, pos); pos += 4
                if (resId == PIXELLAB_RESOURCE_ID) {
                    val data = bytes.copyOfRange(pos, pos + size)
                    sourceJson = try { JSONObject(String(data, Charsets.UTF_8)) } catch (_: Exception) { null }
                }
                pos += size
                if (size % 2 != 0) pos += 1
            }

            if (sourceJson != null) {
                val canvas = ProjectSerializer.deserializeCanvas(
                    sourceJson.optJSONObject("canvas")?.toString()
                        ?: return PsdParseResult()
                )
                val layers = ProjectSerializer.deserializeLayers(
                    sourceJson.optJSONArray("layers")?.toString() ?: ""
                )
                return PsdParseResult(plp = PlpData(canvas, layers))
            }

            // 无源数据 → 解析位图（RGB raw）
            pos = resEnd
            val layerMaskLen = readInt(bytes, pos); pos += 4
            pos += layerMaskLen
            val compression = readShort(bytes, pos); pos += 2
            if (compression != 0) return PsdParseResult()
            val height = readInt(bytes, 14)
            val width = readInt(bytes, 18)
            val pixels = IntArray(width * height)
            for (channel in 0 until 3) {
                for (i in pixels.indices) {
                    val v = bytes[pos].toInt() and 0xFF
                    pos++
                    val shift = when (channel) { 0 -> 16; 1 -> 8; else -> 0 }
                    pixels[i] = pixels[i] or (v shl shift) or 0xFF000000.toInt()
                }
            }
            val bmp = Bitmap.createBitmap(pixels, width, height, Bitmap.Config.ARGB_8888)
            PsdParseResult(bitmap = bmp)
        } catch (_: Exception) {
            PsdParseResult()
        }
    }

    private fun shortToBytes(v: Int): ByteArray {
        return byteArrayOf(((v shr 8) and 0xFF).toByte(), (v and 0xFF).toByte())
    }

    private fun intToBytes(v: Int): ByteArray {
        return byteArrayOf(
            ((v shr 24) and 0xFF).toByte(),
            ((v shr 16) and 0xFF).toByte(),
            ((v shr 8) and 0xFF).toByte(),
            (v and 0xFF).toByte()
        )
    }

    private fun readShort(b: ByteArray, offset: Int): Int {
        return ((b[offset].toInt() and 0xFF) shl 8) or (b[offset + 1].toInt() and 0xFF)
    }

    private fun readInt(b: ByteArray, offset: Int): Int {
        return ((b[offset].toInt() and 0xFF) shl 24) or
            ((b[offset + 1].toInt() and 0xFF) shl 16) or
            ((b[offset + 2].toInt() and 0xFF) shl 8) or
            (b[offset + 3].toInt() and 0xFF)
    }

    private fun pascalStringBytes(s: String): ByteArray {
        val b = s.toByteArray(Charsets.US_ASCII)
        val maxLen = minOf(b.size, 254)
        val out = ByteArrayOutputStream()
        out.write(maxLen)
        out.write(b, 0, maxLen)
        if (b.size % 2 != 0) out.write(0)
        return out.toByteArray()
    }

    /** 便捷：从 Uri 读取字节 */
    fun readUriBytes(context: Context, uri: Uri): ByteArray {
        return context.contentResolver.openInputStream(uri)?.use { it.readBytes() } ?: ByteArray(0)
    }
}