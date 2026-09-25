package com.landesheji.util

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.widget.Toast
import com.landesheji.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

/**
 * 需求12：后端更新程序（OTA）
 *
 * 客户端通过检查更新接口获取最新版本信息，若版本号大于当前版本则提示下载并安装。
 *
 * 后端只需提供一个可访问的 JSON 接口（GET），返回格式：
 * {
 *   "versionCode": 2,
 *   "versionName": "1.2",
 *   "apkUrl": "https://example.com/pixellab-1.2.apk",
 *   "changelog": "更新说明：新增 XXX 功能",
 *   "force": false
 * }
 *
 * 后端示例（可直接部署到任意静态托管/GitHub Pages）：
 * https://github.com/你的账号/你的仓库/raw/main/update.json
 */
object UpdateManager {

    /** 更新检查接口地址（部署后替换成自己的后端地址） */
    const val UPDATE_URL = "https://raw.githubusercontent.com/shuting52/ceshipix/main/backend/update.json"

    data class UpdateInfo(
        val versionCode: Int,
        val versionName: String,
        val apkUrl: String,
        val changelog: String,
        val force: Boolean
    )

    /** 检查是否有新版本（返回 null 表示无需更新或检查失败） */
    suspend fun checkUpdate(): UpdateInfo? = withContext(Dispatchers.IO) {
        try {
            val conn = URL(UPDATE_URL).openConnection() as HttpURLConnection
            conn.connectTimeout = 8000
            conn.readTimeout = 8000
            conn.requestMethod = "GET"
            val code = conn.responseCode
            if (code != 200) return@withContext null
            val text = conn.inputStream.bufferedReader().readText()
            val obj = JSONObject(text)

            val serverCode = obj.optInt("versionCode", 0)
            val currentCode = BuildConfig.VERSION_CODE

            if (serverCode <= currentCode) return@withContext null

            UpdateInfo(
                versionCode = serverCode,
                versionName = obj.optString("versionName", "$serverCode"),
                apkUrl = obj.optString("apkUrl", ""),
                changelog = obj.optString("changelog", "发现新版本，快来更新吧！"),
                force = obj.optBoolean("force", false)
            )
        } catch (_: Exception) {
            null
        }
    }

    /** 直接下载 APK 到缓存并通过浏览器/安装器打开 */
    suspend fun downloadAndOpen(context: Context, url: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val fileName = "PixelLab_update_${System.currentTimeMillis()}.apk"
            val dir = File(context.getExternalFilesDir(null) ?: context.filesDir, "updates")
            if (!dir.exists()) dir.mkdirs()
            val file = File(dir, fileName)

            val conn = URL(url).openConnection() as HttpURLConnection
            conn.connectTimeout = 15000
            conn.connect()
            FileOutputStream(file).use { out ->
                conn.inputStream.copyTo(out)
            }

            // 通过 FileProvider 分享安装
            installApk(context, file)
            true
        } catch (_: Exception) {
            false
        }
    }

    /**
     * 需求1：带进度的下载（配合 CSS 特效进度条弹窗）
     * onProgress: 0f..1f 实时回调
     * 下载完成后自动调用系统安装，替换老版本
     */
    suspend fun downloadWithProgress(
        context: Context,
        url: String,
        onProgress: (Float) -> Unit
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val fileName = "PixelLab_update_${System.currentTimeMillis()}.apk"
            val dir = File(context.getExternalFilesDir(null) ?: context.filesDir, "updates")
            if (!dir.exists()) dir.mkdirs()
            val file = File(dir, fileName)

            val conn = URL(url).openConnection() as HttpURLConnection
            conn.connectTimeout = 15000
            conn.connect()
            val total = conn.contentLength.toLong().coerceAtLeast(1L)
            val buffer = ByteArray(64 * 1024)
            var downloaded = 0L
            val input = conn.inputStream
            FileOutputStream(file).use { out ->
                while (true) {
                    val read = input.read(buffer)
                    if (read < 0) break
                    out.write(buffer, 0, read)
                    downloaded += read
                    onProgress((downloaded.toFloat() / total).coerceIn(0f, 1f))
                }
            }
            input.close()

            // 下载完成 → 立即安装（替换老版本）
            installApk(context, file)
            true
        } catch (_: Exception) {
            false
        }
    }

    /** 通过 FileProvider 触发系统安装 */
    private fun installApk(context: Context, file: File) {
        val uri = androidx.core.content.FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/vnd.android.package-archive")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }
}