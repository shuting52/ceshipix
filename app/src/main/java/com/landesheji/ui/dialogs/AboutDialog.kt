package com.landesheji.ui.dialogs

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.landesheji.R
import com.landesheji.ui.components.KawaiiButton
import com.landesheji.ui.components.kawaiiShadow
import com.landesheji.ui.theme.KawaiiCardTint
import com.landesheji.ui.theme.KawaiiMint
import com.landesheji.ui.theme.KawaiiOutline
import com.landesheji.ui.theme.KawaiiPink
import com.landesheji.ui.theme.KawaiiPinkLight
import com.landesheji.ui.theme.KawaiiSurface
import com.landesheji.ui.theme.KawaiiTextMuted
import com.landesheji.ui.theme.KawaiiTextPrimary
import com.landesheji.ui.theme.KawaiiTextSecondary

@Composable
fun AboutDialog(onDismiss: () -> Unit, onCheckUpdate: (() -> Unit)? = null) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .kawaiiShadow(shadowOffset = 5.dp, cornerRadius = 20.dp)
                .border(2.dp, KawaiiOutline, RoundedCornerShape(20.dp))
                .testTag("about_dialog"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = KawaiiSurface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // App Icon
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(KawaiiPinkLight)
                        .border(2.dp, KawaiiOutline, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_app_logo),
                        contentDescription = "PixelLab Logo",
                        modifier = Modifier.size(58.dp).clip(CircleShape)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "🌸 PixelLab 全中文版",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    color = KawaiiTextPrimary
                )

                Text(
                    text = "卡通可爱动画风 · PS级 3D 艺术字工作台",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = KawaiiPink
                )

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = KawaiiOutline.copy(alpha = 0.2f))
                Spacer(modifier = Modifier.height(12.dp))

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    FeatureItem("✨ PS 3D 立体字全功能", "支持三维俯仰旋转、多向透视挤压、斜面浮雕、材质高光反射与真实地面阴影")
                    FeatureItem("🎀 可爱卡通动画体验", "全新果冻 Q 弹交互动画，糖果色视觉设计与舒适触控反馈")
                    FeatureItem("📄 强大图层系统", "支持多文字、多形状、贴纸、画笔涂鸦与多图片，自由上下层排序与拖拽缩放")
                    FeatureItem("💬 精选中文字体与名句库", "预置可爱手写、黑体、书法风格与海量励志、爱情、诗意中文经典语句")
                    FeatureItem("💾 高清无损导出", "支持保存本地草稿工程随时二次编辑，一键高清 PNG / JPG 导出与系统分享")
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "版本 v2.0.0 (Cute Cartoon & PS 3D Edition)",
                    fontSize = 11.sp,
                    color = KawaiiTextMuted
                )

                Spacer(modifier = Modifier.height(14.dp))

                if (onCheckUpdate != null) {
                    KawaiiButton(
                        text = "🔄 检查更新",
                        onClick = {
                            onDismiss()
                            onCheckUpdate()
                        },
                        containerColor = KawaiiMint,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                KawaiiButton(
                    text = "我知道啦 💖",
                    onClick = onDismiss,
                    containerColor = KawaiiPink,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun FeatureItem(title: String, desc: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(KawaiiCardTint)
            .border(1.dp, KawaiiOutline.copy(alpha = 0.15f), RoundedCornerShape(10.dp))
            .padding(10.dp)
    ) {
        Text(
            text = title,
            fontSize = 12.sp,
            fontWeight = FontWeight.Black,
            color = KawaiiTextPrimary
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = desc,
            fontSize = 11.sp,
            color = KawaiiTextSecondary,
            lineHeight = 15.sp
        )
    }
}
