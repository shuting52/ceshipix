package com.landesheji.ui.components

import android.net.Uri
import android.widget.VideoView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import coil.compose.AsyncImage

/**
 * 需求1：软件 UI 主题背景
 *
 * 用户上传的图片 / 视频会应用到整个 APP 的界面主题（工具栏、面板、边缘区域），
 * 而非画布内容本身。图片使用 Coil 全屏铺底，视频使用 VideoView 自动静音循环播放。
 */
@Composable
fun UiThemeBackground(
    imageUri: String,
    videoUri: String,
    modifier: Modifier = Modifier,
    dimAlpha: Float = 0.12f
) {
    val context = LocalContext.current

    if (videoUri.isNotEmpty()) {
        val video = remember(videoUri) {
            VideoView(context).apply {
                setVideoURI(Uri.parse(videoUri))
                setZOrderMediaOverlay(true)
            }
        }
        DisposableEffect(videoUri) {
            video.setOnPreparedListener { mp ->
                mp.isLooping = true
                mp.setVolume(0f, 0f)
                video.start()
            }
            video.setOnErrorListener { _, _, _ -> true }
            video.start()
            onDispose {
                video.stopPlayback()
            }
        }
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = dimAlpha))
        ) {
            AndroidView(
                factory = { video },
                modifier = Modifier.fillMaxSize()
            )
        }
    } else if (imageUri.isNotEmpty()) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = dimAlpha))
        ) {
            AsyncImage(
                model = imageUri,
                contentDescription = "App 主题背景",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}