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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.landesheji.data.model.CanvasConfig
import com.landesheji.ui.theme.KawaiiBg
import com.landesheji.ui.theme.KawaiiOutline
import com.landesheji.ui.theme.KawaiiPink
import com.landesheji.ui.theme.KawaiiPinkLight
import com.landesheji.ui.theme.KawaiiSurface
import com.landesheji.ui.theme.KawaiiTextPrimary
import com.landesheji.ui.theme.KawaiiTextSecondary
import com.landesheji.ui.theme.KawaiiYellow

private enum class EffectType(val label: String, val icon: String) {
    VIGNETTE("暗角", "🔘"),
    NOISE("噪点", "✨"),
    STRIPES("百叶条纹", "▤"),
    BRIGHTNESS("亮度", "☀️"),
    CONTRAST("对比度", "◐")
}

@Composable
fun EffectsToolBar(
    canvasConfig: CanvasConfig,
    onUpdateConfig: ((CanvasConfig) -> CanvasConfig) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedEffect by remember { mutableStateOf<EffectType?>(null) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(KawaiiBg.copy(alpha = 0.86f))
            .border(2.dp, KawaiiOutline)
            .testTag("effects_toolbar")
    ) {
        // Expandable control panel
        if (selectedEffect != null) {
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
                        text = "${selectedEffect!!.icon} ${selectedEffect!!.label}设置",
                        fontWeight = FontWeight.Black,
                        fontSize = 13.sp,
                        color = KawaiiPink
                    )
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .jellyClickable { selectedEffect = null },
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

                when (selectedEffect!!) {
                    EffectType.VIGNETTE -> {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "启用画面暗角", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = KawaiiTextPrimary)
                            Switch(
                                checked = canvasConfig.vignette > 0f,
                                onCheckedChange = { isEnabled ->
                                    onUpdateConfig { it.copy(vignette = if (isEnabled) 0.5f else 0f) }
                                },
                                colors = SwitchDefaults.colors(checkedThumbColor = KawaiiPink, checkedTrackColor = KawaiiPinkLight)
                            )
                        }
                        if (canvasConfig.vignette > 0f) {
                            Text(
                                text = "暗角强度: ${(canvasConfig.vignette * 100).toInt()}%",
                                fontSize = 12.sp,
                                color = KawaiiTextSecondary
                            )
                            Slider(
                                value = canvasConfig.vignette,
                                onValueChange = { v -> onUpdateConfig { it.copy(vignette = v) } },
                                valueRange = 0.1f..1f,
                                colors = SliderDefaults.colors(thumbColor = KawaiiPink, activeTrackColor = KawaiiPink)
                            )
                        }
                    }

                    EffectType.STRIPES -> {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "启用百叶条纹", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = KawaiiTextPrimary)
                            Switch(
                                checked = canvasConfig.stripes > 0f,
                                onCheckedChange = { isEnabled ->
                                    onUpdateConfig { it.copy(stripes = if (isEnabled) 0.4f else 0f) }
                                },
                                colors = SwitchDefaults.colors(checkedThumbColor = KawaiiPink, checkedTrackColor = KawaiiPinkLight)
                            )
                        }
                        if (canvasConfig.stripes > 0f) {
                            Text(
                                text = "条纹深浅: ${(canvasConfig.stripes * 100).toInt()}%",
                                fontSize = 12.sp,
                                color = KawaiiTextSecondary
                            )
                            Slider(
                                value = canvasConfig.stripes,
                                onValueChange = { s -> onUpdateConfig { it.copy(stripes = s) } },
                                valueRange = 0.1f..1f,
                                colors = SliderDefaults.colors(thumbColor = KawaiiPink, activeTrackColor = KawaiiPink)
                            )
                        }
                    }

                    EffectType.NOISE -> {
                        Text(text = "胶片颗粒质感: ${(canvasConfig.noise * 100).toInt()}%", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = KawaiiTextPrimary)
                        Slider(
                            value = canvasConfig.noise,
                            onValueChange = { n -> onUpdateConfig { it.copy(noise = n) } },
                            valueRange = 0f..1f,
                            colors = SliderDefaults.colors(thumbColor = KawaiiPink, activeTrackColor = KawaiiPink)
                        )
                    }

                    EffectType.BRIGHTNESS -> {
                        Text(
                            text = "画面亮度: ${(canvasConfig.brightness * 100).toInt()}%",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = KawaiiTextPrimary
                        )
                        Slider(
                            value = canvasConfig.brightness,
                            onValueChange = { b -> onUpdateConfig { it.copy(brightness = b) } },
                            valueRange = 0.5f..1.5f,
                            colors = SliderDefaults.colors(thumbColor = KawaiiYellow, activeTrackColor = KawaiiYellow)
                        )
                    }

                    EffectType.CONTRAST -> {
                        Text(
                            text = "画面对比度: ${(canvasConfig.contrast * 100).toInt()}%",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = KawaiiTextPrimary
                        )
                        Slider(
                            value = canvasConfig.contrast,
                            onValueChange = { c -> onUpdateConfig { it.copy(contrast = c) } },
                            valueRange = 0.5f..1.5f,
                            colors = SliderDefaults.colors(thumbColor = KawaiiPink, activeTrackColor = KawaiiPink)
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
            EffectType.values().forEach { effect ->
                val isSelected = selectedEffect == effect
                Column(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .jellyClickable { selectedEffect = if (isSelected) null else effect }
                        .background(if (isSelected) KawaiiPinkLight else Color.Transparent)
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = effect.icon, fontSize = 18.sp)
                    Text(
                        text = effect.label,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) KawaiiPink else KawaiiTextPrimary
                    )
                }
            }
        }
    }
}
