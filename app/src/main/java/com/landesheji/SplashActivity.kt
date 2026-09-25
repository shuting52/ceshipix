package com.landesheji

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.landesheji.ui.theme.KawaiiPink
import com.landesheji.ui.theme.KawaiiSkyBlue
import kotlinx.coroutines.delay

class SplashActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SplashScreen(onFinish = {
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            })
        }
    }
}

@Composable
fun SplashScreen(onFinish: () -> Unit) {
    // 渐变背景
    val bgBrush = Brush.linearGradient(
        colors = listOf(Color(0xFF191326), Color(0xFF2D1B4E), Color(0xFF4A1E5F))
    )

    // logo 浮动动画
    val infinite = rememberInfiniteTransition(label = "splash")
    val floatY by infinite.animateFloat(
        initialValue = -8f,
        targetValue = 8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200),
            repeatMode = RepeatMode.Reverse
        ),
        label = "float"
    )
    val glow by infinite.animateFloat(
        initialValue = 0.7f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(700),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    // 出场 scale 动画
    val scale = remember { Animatable(0.4f) }
    val fadeIn = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        fadeIn.animateTo(1f, animationSpec = tween(600))
        scale.animateTo(1f, animationSpec = tween(900, easing = FastOutSlowInEasing))
        delay(1800)
        onFinish()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgBrush),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Logo 圆形发光容器
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .scale(scale.value)
                    .padding(0.dp),
                contentAlignment = Alignment.Center
            ) {
                // 外层霓虹光环
                Box(
                    modifier = Modifier
                        .size(140.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    KawaiiPink.copy(alpha = 0.5f * glow),
                                    Color.Transparent
                                )
                            )
                        )
                )
                Box(
                    modifier = Modifier
                        .size(118.dp)
                        .clip(RoundedCornerShape(26.dp))
                        .background(Color(0xFF241A3A))
                        .align(Alignment.Center)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_app_logo),
                        contentDescription = "PixelLab Logo",
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(0.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // App 名称
            Text(
                text = "PixelLab",
                fontSize = 40.sp,
                fontWeight = FontWeight.Black,
                color = Color.White,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Text(
                text = "全中文版 · 图片文字排版设计",
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.75f),
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(40.dp))

            // 动态标签
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(50.dp))
                    .background(Color.Black.copy(alpha = 0.3f))
                    .padding(horizontal = 18.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // 跳动的小圆点
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(KawaiiPink.copy(alpha = glow))
                ) { }
                val tagScale = remember { Animatable(1f) }
                LaunchedEffect(Unit) {
                    tagScale.snapTo(0.92f)
                    tagScale.animateTo(1f, animationSpec = tween(400))
                }
                Text(
                    text = "2026 最新改版 PixelLab",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    modifier = Modifier.scale(tagScale.value)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 开发署名
            Text(
                text = "开发由：懒得设计 · 原创开发",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = KawaiiSkyBlue.copy(alpha = 0.95f)
            )
        }
    }
}