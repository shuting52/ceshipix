package com.landesheji.ui.dialogs

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.landesheji.ui.components.KawaiiButton
import com.landesheji.ui.components.jellyClickable
import com.landesheji.ui.components.kawaiiShadow
import com.landesheji.ui.theme.KawaiiCardTint
import com.landesheji.ui.theme.KawaiiOutline
import com.landesheji.ui.theme.KawaiiPink
import com.landesheji.ui.theme.KawaiiPinkLight
import com.landesheji.ui.theme.KawaiiSurface
import com.landesheji.ui.theme.KawaiiTextMuted
import com.landesheji.ui.theme.KawaiiTextPrimary
import com.landesheji.ui.theme.KawaiiTextSecondary

data class AspectRatioPreset(val name: String, val w: Int, val h: Int, val desc: String)

@Composable
fun CanvasSizeDialog(
    currentWidth: Int,
    currentHeight: Int,
    onDismiss: () -> Unit,
    onConfirm: (width: Int, height: Int, ratioName: String) -> Unit
) {
    val presets = listOf(
        AspectRatioPreset("1:1 正方形", 1080, 1080, "头像 / 微信朋友圈 / 贴图"),
        AspectRatioPreset("16:9 横屏", 1920, 1080, "电脑壁纸 / 视频封面 / 横幅"),
        AspectRatioPreset("9:16 竖屏", 1080, 1920, "手机壁纸 / 抖音快手 / 故事"),
        AspectRatioPreset("4:3 经典", 1024, 768, "经典画幅 / 平板全屏"),
        AspectRatioPreset("3:4 竖画", 768, 1024, "海报画册 / 小红书图文"),
        AspectRatioPreset("2.35:1 宽银幕", 1920, 817, "电影大片宽画幅")
    )

    var widthStr by remember { mutableStateOf(currentWidth.toString()) }
    var heightStr by remember { mutableStateOf(currentHeight.toString()) }
    var selectedName by remember { mutableStateOf("自定义") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .kawaiiShadow(shadowOffset = 5.dp, cornerRadius = 20.dp)
                .border(2.dp, KawaiiOutline, RoundedCornerShape(20.dp))
                .testTag("canvas_size_dialog"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = KawaiiSurface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = "📐 图像尺寸设置",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Black,
                    color = KawaiiTextPrimary
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Presets Grid
                Text(text = "快捷常用比例：", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = KawaiiTextSecondary)
                Spacer(modifier = Modifier.height(8.dp))

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    presets.chunked(2).forEach { rowPresets ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            rowPresets.forEach { p ->
                                val isSelected = selectedName == p.name
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (isSelected) KawaiiPinkLight else KawaiiCardTint)
                                        .border(
                                            width = if (isSelected) 2.dp else 1.dp,
                                            color = if (isSelected) KawaiiPink else KawaiiOutline.copy(alpha = 0.2f),
                                            shape = RoundedCornerShape(10.dp)
                                        )
                                        .jellyClickable {
                                            selectedName = p.name
                                            widthStr = p.w.toString()
                                            heightStr = p.h.toString()
                                        }
                                        .padding(horizontal = 10.dp, vertical = 8.dp)
                                ) {
                                    Column {
                                        Text(
                                            text = p.name,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Black,
                                            color = if (isSelected) KawaiiPink else KawaiiTextPrimary
                                        )
                                        Text(
                                            text = "${p.w} × ${p.h}",
                                            fontSize = 10.sp,
                                            color = KawaiiTextMuted
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Custom width and height text fields
                Text(text = "自定义像素尺寸：", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = KawaiiTextSecondary)
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = widthStr,
                        onValueChange = {
                            widthStr = it
                            selectedName = "自定义"
                        },
                        label = { Text("宽度 (px)", fontSize = 11.sp) },
                        modifier = Modifier
                            .weight(1f)
                            .border(1.5.dp, KawaiiOutline, RoundedCornerShape(10.dp)),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = KawaiiCardTint,
                            unfocusedContainerColor = KawaiiCardTint,
                            focusedTextColor = KawaiiTextPrimary,
                            unfocusedTextColor = KawaiiTextPrimary,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )

                    OutlinedTextField(
                        value = heightStr,
                        onValueChange = {
                            heightStr = it
                            selectedName = "自定义"
                        },
                        label = { Text("高度 (px)", fontSize = 11.sp) },
                        modifier = Modifier
                            .weight(1f)
                            .border(1.5.dp, KawaiiOutline, RoundedCornerShape(10.dp)),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = KawaiiCardTint,
                            unfocusedContainerColor = KawaiiCardTint,
                            focusedTextColor = KawaiiTextPrimary,
                            unfocusedTextColor = KawaiiTextPrimary,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(text = "取消", color = KawaiiTextSecondary, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    KawaiiButton(
                        text = "应用尺寸 ✨",
                        onClick = {
                            val w = widthStr.toIntOrNull()?.coerceIn(100, 4096) ?: 1080
                            val h = heightStr.toIntOrNull()?.coerceIn(100, 4096) ?: 1080
                            onConfirm(w, h, selectedName)
                            onDismiss()
                        },
                        containerColor = KawaiiPink
                    )
                }
            }
        }
    }
}
