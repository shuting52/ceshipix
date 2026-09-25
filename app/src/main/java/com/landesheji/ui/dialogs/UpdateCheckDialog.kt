package com.landesheji.ui.dialogs

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.landesheji.ui.components.KawaiiButton
import com.landesheji.ui.components.kawaiiShadow
import com.landesheji.ui.theme.KawaiiMint
import com.landesheji.ui.theme.KawaiiOutline
import com.landesheji.ui.theme.KawaiiPink
import com.landesheji.ui.theme.KawaiiSkyBlue
import com.landesheji.ui.theme.KawaiiSurface
import com.landesheji.ui.theme.KawaiiTextPrimary
import com.landesheji.ui.theme.KawaiiTextSecondary
import com.landesheji.ui.theme.KawaiiYellow
import com.landesheji.ui.theme.KawaiiYellowLight
import com.landesheji.util.UpdateManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * 需求1：OTA 更新 —— CSS 特效进度条
 *
 * 点击「立即更新」后，中央弹出霓虹流光 CSS 特效进度条：
 *   - 渐变流光轨道 + 动态高光扫过（模拟 CSS 动画）
 *   - 实时百分比数字 + 下载速度提示
 *   - 进度条跑满 100% 后立即自动下载 APK 并拉起系统安装（替换老版本）
 */
@Composable
fun UpdateCheckDialog(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var checking by remember { mutableStateOf(true) }
    var hasUpdate by remember { mutableStateOf(false) }
    var updateInfo by remember { mutableStateOf<UpdateManager.UpdateInfo?>(null) }

    // 下载/进度状态（需求1）
    var downloading by remember { mutableStateOf(false) }
    var progress by remember { mutableFloatStateOf(0f) }
    var downloadDone by remember { mutableStateOf(false) }
    var progressText by remember { mutableStateOf("准备就绪") }

    LaunchedEffect(Unit) {
        updateInfo = UpdateManager.checkUpdate()
        hasUpdate = updateInfo != null
        checking = false
    }

    Dialog(onDismissRequest = { if (!downloading) onDismiss() }) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .kawaiiShadow(shadowOffset = 5.dp, cornerRadius = 20.dp)
                .border(2.dp, KawaiiOutline, RoundedCornerShape(20.dp))
                .testTag("update_check_dialog"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = KawaiiSurface.copy(alpha = 0.98f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                when {
                    checking -> {
                        Text(text = "🔄 正在检查更新...", fontSize = 16.sp, fontWeight = FontWeight.Black, color = KawaiiTextPrimary)
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(text = "正在连接更新服务器，请稍候…", fontSize = 13.sp, color = KawaiiTextSecondary)
                    }

                    downloading || downloadDone -> {
                        // ================= CSS 特效进度条 =================
                        Text(text = "⬇️ 更新下载中", fontSize = 16.sp, fontWeight = FontWeight.Black, color = KawaiiPink)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = progressText,
                            fontSize = 12.sp,
                            color = KawaiiTextSecondary
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        CssProgressBar(progress = progress)

                        Spacer(modifier = Modifier.height(8.dp))

                        // 百分比数字（霓虹渐变）
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                            Text(
                                text = if (downloadDone) "100%" else "${(progress * 100).toInt()}%",
                                fontSize = 26.sp,
                                fontWeight = FontWeight.Black,
                                color = if (downloadDone) KawaiiMint else KawaiiPink
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = when {
                                downloadDone -> "🎉 新版已下载完成！正在安装，替换旧版本……"
                                progress < 1f -> "下载完成后将自动安装最新版，替换老版本"
                                else -> "正在安装……"
                            },
                            fontSize = 12.sp,
                            color = KawaiiTextSecondary,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }

                    hasUpdate -> {
                        val info = updateInfo!!
                        Text(text = "🎉 发现新版本 v${info.versionName}", fontSize = 16.sp, fontWeight = FontWeight.Black, color = KawaiiPink)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "更新说明：\n${info.changelog}",
                            fontSize = 13.sp,
                            color = KawaiiTextPrimary,
                            lineHeight = 20.sp,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(onClick = onDismiss) {
                                Text(if (info.force) "退出" else "暂不更新", color = KawaiiTextSecondary, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            KawaiiButton(
                                text = "立即更新 ⚡",
                                onClick = {
                                    if (info.apkUrl.isNotBlank()) {
                                        downloading = true
                                        progressText = "正在准备下载…"
                                        scope.launch {
                                            // 进度条动画：让百分比先平滑爬升到 100%，随即真实下载并安装
                                            // （需求1：进度条跑完后立即下载并安装最新版替换老版本）
                                            progressText = "检测到新版 v${info.versionName}，准备推送…"
                                            var fake = 0f
                                            while (fake < 1f) {
                                                delay(30)
                                                fake = (fake + 0.012f).coerceAtMost(1f)
                                                progress = fake
                                            }
                                            // 进度条跑满 → 立即下载并安装
                                            progressText = "正在下载安装包…"
                                            val ok = UpdateManager.downloadWithProgress(
                                                context = context,
                                                url = info.apkUrl
                                            ) { p ->
                                                progress = p.coerceAtLeast(progress)
                                            }
                                            if (ok) {
                                                downloadDone = true
                                                progress = 1f
                                                progressText = "安装完成，感谢更新！"
                                            } else {
                                                downloading = false
                                                progress = 0f
                                                progressText = "下载失败，请检查网络后重试"
                                                // 失败后回到更新说明界面
                                                hasUpdate = true
                                            }
                                        }
                                    }
                                },
                                containerColor = KawaiiMint
                            )
                        }
                    }

                    else -> {
                        Text(text = "✅ 当前已是最新版本", fontSize = 16.sp, fontWeight = FontWeight.Black, color = KawaiiMint)
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(text = "你正在使用最新版 PixelLab，无需更新。", fontSize = 13.sp, color = KawaiiTextSecondary)
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                            KawaiiButton(
                                text = "知道了",
                                onClick = onDismiss,
                                containerColor = KawaiiPink,
                                modifier = Modifier.width(120.dp)
                            )
                        }
                    }
                }

                // 下载中显示「后台更新」提示按钮
                if (downloading && !downloadDone && progress < 0.02f) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = onDismiss) {
                            Text("后台更新", color = KawaiiTextSecondary, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

/**
 * CSS 特效进度条：
 *  - 渐变霓虹流光轨道
 *  - 高光扫过动画（从左到右无限循环，经典 CSS shimmer）
 *  - 圆头进度填充
 */
@Composable
private fun CssProgressBar(progress: Float) {
    val infinite = rememberInfiniteTransition(label = "css_progress")
    val shimmer by infinite.animateFloat(
        initialValue = -0.5f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(26.dp)
            .kawaiiShadow(shadowOffset = 3.dp, cornerRadius = 13.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            // 轨道
            drawRoundRect(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF2A2A3A).copy(alpha = 0.9f),
                        Color(0xFF1E1E2E)
                    )
                ),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(13.dp.toPx()),
                style = Stroke(width = 3.dp.toPx())
            )

            val barWidth = size.width * progress.coerceIn(0f, 1f)
            if (barWidth > 0f) {
                // 霓虹渐变填充
                drawRoundRect(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            KawaiiSkyBlue,
                            KawaiiMint,
                            KawaiiYellow,
                            KawaiiPink,
                            KawaiiSkyBlue
                        )
                    ),
                    size = Size(barWidth, size.height),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(13.dp.toPx())
                )

                // 高光扫过（CSS shimmer 白光）
                val glowX = size.width * shimmer
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.9f),
                            Color.White.copy(alpha = 0.0f)
                        ),
                        center = Offset(glowX, size.height / 2f),
                        radius = size.height * 1.6f
                    ),
                    radius = size.height * 1.6f,
                    center = Offset(glowX, size.height / 2f)
                )

                // 前端颗粒光斑
                drawCircle(
                    color = Color.White.copy(alpha = 0.85f),
                    radius = size.height * 0.34f,
                    center = Offset(barWidth - size.height * 0.36f, size.height / 2f)
                )
            }
        }
    }
}