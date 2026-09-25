package com.landesheji.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.landesheji.data.model.BackgroundType
import com.landesheji.data.model.CanvasConfig
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import com.landesheji.ui.theme.KawaiiBg
import com.landesheji.ui.theme.KawaiiCardTint
import com.landesheji.ui.theme.KawaiiLavender
import com.landesheji.ui.theme.KawaiiLavenderLight
import com.landesheji.ui.theme.KawaiiMint
import com.landesheji.ui.theme.KawaiiMintLight
import com.landesheji.ui.theme.KawaiiOutline
import com.landesheji.ui.theme.KawaiiPink
import com.landesheji.ui.theme.KawaiiPinkLight
import com.landesheji.ui.theme.KawaiiSurface
import com.landesheji.ui.theme.KawaiiTextPrimary
import com.landesheji.ui.theme.KawaiiTextSecondary
import com.landesheji.ui.theme.KawaiiYellow

@Composable
fun BackgroundToolBar(
    canvasConfig: CanvasConfig,
    onUpdateConfig: ((CanvasConfig) -> CanvasConfig) -> Unit,
    onOpenSizeDialog: () -> Unit,
    onPickImageFromGallery: () -> Unit,
    onPickVideoFromGallery: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showColorPalette by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(KawaiiBg.copy(alpha = 0.86f))
            .border(2.dp, KawaiiOutline)
            .testTag("background_toolbar")
    ) {
        // Expandable Color Palette
        if (showColorPalette) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(KawaiiSurface)
                    .padding(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "🎨 画布底色选择",
                        fontWeight = FontWeight.Black,
                        fontSize = 13.sp,
                        color = KawaiiLavender
                    )
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .jellyClickable { showColorPalette = false },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "关闭",
                            tint = KawaiiTextPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                val bgPalette = listOf(
                    Color(0xFFFFF0F5), // Lavender blush
                    Color(0xFFFFEEF0), // Soft pink
                    Color(0xFFF0F8FF), // Alice blue
                    Color(0xFFF4F9F4), // Mint cream
                    Color(0xFFFFF9E6), // Custard cream
                    Color.White,
                    Color(0xFF222831), // Dark
                    Color(0xFF1E1B2E), // Anime midnight
                    Color(0xFFFF6B9D), // Bubblegum
                    Color(0xFF4EA8DE), // Sky blue
                    Color(0xFFB57EDC)  // Lavender
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    bgPalette.forEach { c ->
                        val isSel = canvasConfig.backgroundColorArgb == c.toArgb() &&
                                canvasConfig.backgroundType == BackgroundType.COLOR
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(c)
                                .border(
                                    width = if (isSel) 3.dp else 1.5.dp,
                                    color = if (isSel) KawaiiPink else KawaiiOutline,
                                    shape = CircleShape
                                )
                                .jellyClickable {
                                    onUpdateConfig {
                                        it.copy(
                                            backgroundType = BackgroundType.COLOR,
                                            backgroundColorArgb = c.toArgb()
                                        )
                                    }
                                }
                        )
                    }
                }
            }
            HorizontalDivider(color = KawaiiOutline, thickness = 1.dp)
        }

        // Horizontal tools list
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp)
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Color Background
            Column(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .jellyClickable { showColorPalette = !showColorPalette }
                    .background(if (showColorPalette) KawaiiPinkLight else Color.Transparent)
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = "🎨", fontSize = 18.sp)
                Text(text = "纯色底色", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = KawaiiTextPrimary)
            }

            // Transparent
            Column(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .jellyClickable {
                        onUpdateConfig {
                            it.copy(backgroundType = BackgroundType.TRANSPARENT)
                        }
                    }
                    .background(
                        if (canvasConfig.backgroundType == BackgroundType.TRANSPARENT)
                            KawaiiPinkLight
                        else Color.Transparent
                    )
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = "🏁", fontSize = 18.sp)
                Text(
                    text = "透明背景",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (canvasConfig.backgroundType == BackgroundType.TRANSPARENT) KawaiiPink else KawaiiTextPrimary
                )
            }

            // Gradient
            Column(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .jellyClickable {
                        onUpdateConfig {
                            it.copy(
                                backgroundType = BackgroundType.GRADIENT,
                                backgroundColorArgb = 0xFFFFD6E8.toInt(),
                                backgroundGradientColor2Argb = 0xFFC9F0FF.toInt()
                            )
                        }
                    }
                    .background(
                        if (canvasConfig.backgroundType == BackgroundType.GRADIENT)
                            KawaiiPinkLight
                        else Color.Transparent
                    )
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = "🌈", fontSize = 18.sp)
                Text(
                    text = "甜美渐变",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (canvasConfig.backgroundType == BackgroundType.GRADIENT) KawaiiPink else KawaiiTextPrimary
                )
            }

            // Texture
            Column(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .jellyClickable {
                        onUpdateConfig {
                            it.copy(
                                backgroundType = BackgroundType.PRESET_TEXTURE,
                                backgroundColorArgb = 0xFFFFF5F7.toInt()
                            )
                        }
                    }
                    .background(
                        if (canvasConfig.backgroundType == BackgroundType.PRESET_TEXTURE)
                            KawaiiPinkLight
                        else Color.Transparent
                    )
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = "▦", fontSize = 18.sp)
                Text(
                    text = "可爱网格",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (canvasConfig.backgroundType == BackgroundType.PRESET_TEXTURE) KawaiiPink else KawaiiTextPrimary
                )
            }

            Spacer(modifier = Modifier.width(6.dp))
            Box(
                modifier = Modifier
                    .width(1.5.dp)
                    .height(36.dp)
                    .background(KawaiiOutline)
            )
            Spacer(modifier = Modifier.width(6.dp))

            // Canvas Size
            Column(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .jellyClickable { onOpenSizeDialog() }
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = "📐", fontSize = 18.sp)
                Text(text = "图像大小", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = KawaiiTextPrimary)
            }

            // From Gallery
            Column(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .jellyClickable { onPickImageFromGallery() }
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = "🖼️", fontSize = 18.sp)
                Text(text = "相册选图", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = KawaiiTextPrimary)
            }

            // 需求6：本地视频背景
            Column(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .jellyClickable { onPickVideoFromGallery() }
                    .background(
                        if (canvasConfig.backgroundType == BackgroundType.VIDEO_URI)
                            KawaiiPinkLight
                        else Color.Transparent
                    )
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = "🎬", fontSize = 18.sp)
                Text(
                    text = "本地视频背景",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (canvasConfig.backgroundType == BackgroundType.VIDEO_URI) KawaiiPink else KawaiiTextPrimary
                )
            }

            Spacer(modifier = Modifier.width(6.dp))
            Box(
                modifier = Modifier
                    .width(1.5.dp)
                    .height(36.dp)
                    .background(KawaiiOutline)
            )
            Spacer(modifier = Modifier.width(6.dp))

            // 需求6：毛玻璃背景
            Column(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .jellyClickable {
                        onUpdateConfig {
                            it.copy(
                                backgroundBlur = if (it.backgroundBlur > 0f) 0f else 0.6f
                            )
                        }
                    }
                    .background(
                        if (canvasConfig.backgroundBlur > 0f) KawaiiMintLight else Color.Transparent
                    )
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = "🪟", fontSize = 18.sp)
                Text(
                    text = "毛玻璃",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (canvasConfig.backgroundBlur > 0f) KawaiiMint else KawaiiTextPrimary
                )
            }

            // 需求6：霓虹光晕背景
            Column(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .jellyClickable {
                        onUpdateConfig {
                            it.copy(
                                backgroundNeonGlow = if (it.backgroundNeonGlow > 0f) 0f else 0.8f
                            )
                        }
                    }
                    .background(
                        if (canvasConfig.backgroundNeonGlow > 0f) KawaiiLavenderLight else Color.Transparent
                    )
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = "✨", fontSize = 18.sp)
                Text(
                    text = "霓虹光晕",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (canvasConfig.backgroundNeonGlow > 0f) KawaiiLavender else KawaiiTextPrimary
                )
            }

            // 需求6：反色背景
            Column(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .jellyClickable {
                        onUpdateConfig { it.copy(backgroundInvert = !it.backgroundInvert) }
                    }
                    .background(
                        if (canvasConfig.backgroundInvert) KawaiiPinkLight else Color.Transparent
                    )
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = "🔄", fontSize = 18.sp)
                Text(
                    text = "反色背景",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (canvasConfig.backgroundInvert) KawaiiPink else KawaiiTextPrimary
                )
            }
        }
    }
}
