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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.landesheji.data.model.CanvasConfig
import com.landesheji.ui.components.KawaiiButton
import com.landesheji.ui.components.jellyClickable
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
import com.landesheji.ui.theme.KawaiiYellow

enum class ExportFormat(val title: String, val desc: String) {
    PNG("PNG 格式", "支持透明背景，无损超清，做表情包/贴纸首选"),
    JPG("JPEG 格式", "体积小巧，适配朋友圈/小红书/壁纸分享")
}

enum class ExportQuality(val title: String, val multiplier: Float) {
    STANDARD("标准 (1x)", 1f),
    HIGH("高清 (1.5x)", 1.5f),
    ULTRA("超清 (2x)", 2f)
}

/** 需求5：导出画幅比例（适配各平台） */
enum class ExportRatio(val title: String, val w: Float, val h: Float) {
    ORIGINAL("原画布", -1f, -1f),
    SQUARE("1:1 方形", 1f, 1f),
    PORTRAIT_3_4("3:4 竖版", 3f, 4f),
    LANDSCAPE_4_3("4:3 横版", 4f, 3f),
    PORTRAIT_9_16("9:16 竖屏", 9f, 16f),
    LANDSCAPE_16_9("16:9 横屏", 16f, 9f),
    PORTRAIT_2_3("2:3 相册", 2f, 3f),
    LANDSCAPE_3_2("3:2 相册", 3f, 2f)
}

@Composable
fun ExportDialog(
    canvasConfig: CanvasConfig,
    onDismiss: () -> Unit,
    onExport: (format: ExportFormat, quality: ExportQuality, ratio: ExportRatio, shareDirectly: Boolean) -> Unit
) {
    var selectedFormat by remember { mutableStateOf(ExportFormat.PNG) }
    var selectedQuality by remember { mutableStateOf(ExportQuality.HIGH) }
    var selectedRatio by remember { mutableStateOf(ExportRatio.ORIGINAL) }

    val targetW = (canvasConfig.width * selectedQuality.multiplier).toInt()
    val targetH = (canvasConfig.height * selectedQuality.multiplier).toInt()

    // 需求5：按所选比例计算导出尺寸（居中裁剪/留边）
    val outW: Int
    val outH: Int
    if (selectedRatio == ExportRatio.ORIGINAL) {
        outW = targetW
        outH = targetH
    } else {
        val ratio = selectedRatio.w / selectedRatio.h
        val canvasRatio = canvasConfig.width.toFloat() / canvasConfig.height.toFloat()
        val baseW = targetW.toFloat()
        val baseH = targetH.toFloat()
        if (canvasRatio > ratio) {
            // 画布更宽：以高为基准，宽裁至比例
            outH = baseH.toInt()
            outW = (baseH * ratio).toInt()
        } else {
            // 画布更高：以宽为基准，高裁至比例
            outW = baseW.toInt()
            outH = (baseW / ratio).toInt()
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .kawaiiShadow(shadowOffset = 5.dp, cornerRadius = 20.dp)
                .border(2.dp, KawaiiOutline, RoundedCornerShape(20.dp))
                .testTag("export_dialog"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = KawaiiSurface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = "🖼️ 导出与分享作品",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Black,
                    color = KawaiiTextPrimary
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Image Format Selector
                Text(text = "图像格式：", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = KawaiiTextSecondary)
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ExportFormat.values().forEach { fmt ->
                        val isSelected = selectedFormat == fmt
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) KawaiiPinkLight else KawaiiCardTint)
                                .border(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) KawaiiPink else KawaiiOutline.copy(alpha = 0.3f),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .jellyClickable { selectedFormat = fmt }
                                .padding(10.dp)
                        ) {
                            Column {
                                Text(
                                    text = fmt.title,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Black,
                                    color = if (isSelected) KawaiiPink else KawaiiTextPrimary
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = fmt.desc,
                                    fontSize = 10.sp,
                                    color = KawaiiTextSecondary,
                                    lineHeight = 14.sp
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 需求5：平台比例选择
                Text(text = "画幅比例（适配各平台）：", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = KawaiiTextSecondary)
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    ExportRatio.values().take(4).forEach { r ->
                        val isSel = selectedRatio == r
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSel) KawaiiMint.copy(alpha = 0.2f) else KawaiiCardTint)
                                .border(
                                    width = if (isSel) 2.dp else 1.dp,
                                    color = if (isSel) KawaiiMint else KawaiiOutline.copy(alpha = 0.3f),
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .jellyClickable { selectedRatio = r }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = r.title,
                                fontSize = 11.sp,
                                fontWeight = if (isSel) FontWeight.Black else FontWeight.Bold,
                                color = if (isSel) KawaiiMint else KawaiiTextPrimary
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    ExportRatio.values().drop(4).forEach { r ->
                        val isSel = selectedRatio == r
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSel) KawaiiMint.copy(alpha = 0.2f) else KawaiiCardTint)
                                .border(
                                    width = if (isSel) 2.dp else 1.dp,
                                    color = if (isSel) KawaiiMint else KawaiiOutline.copy(alpha = 0.3f),
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .jellyClickable { selectedRatio = r }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = r.title,
                                fontSize = 11.sp,
                                fontWeight = if (isSel) FontWeight.Black else FontWeight.Bold,
                                color = if (isSel) KawaiiMint else KawaiiTextPrimary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Quality Selector
                Text(text = "分辨率清晰度：", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = KawaiiTextSecondary)
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ExportQuality.values().forEach { q ->
                        val isSelected = selectedQuality == q
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) KawaiiPinkLight else KawaiiCardTint)
                                .border(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) KawaiiPink else KawaiiOutline.copy(alpha = 0.3f),
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .jellyClickable { selectedQuality = q }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = q.title,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
                                color = if (isSelected) KawaiiPink else KawaiiTextPrimary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Output size hint
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(KawaiiCardTint)
                        .border(1.dp, KawaiiOutline.copy(alpha = 0.15f), RoundedCornerShape(10.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "导出画幅尺寸:", fontSize = 11.sp, color = KawaiiTextMuted)
                        Text(
                            text = "$outW × $outH 像素",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = KawaiiPink
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(0.7f)
                    ) {
                        Text("取消", color = KawaiiTextSecondary, fontWeight = FontWeight.Bold)
                    }

                    KawaiiButton(
                        text = "分享 🚀",
                        onClick = {
                            onExport(selectedFormat, selectedQuality, selectedRatio, true)
                            onDismiss()
                        },
                        containerColor = KawaiiYellow,
                        modifier = Modifier.weight(1f)
                    )

                    KawaiiButton(
                        text = "保存相册 💾",
                        onClick = {
                            onExport(selectedFormat, selectedQuality, selectedRatio, false)
                            onDismiss()
                        },
                        containerColor = KawaiiPink,
                        modifier = Modifier
                            .weight(1.3f)
                            .testTag("export_save_button")
                    )
                }
            }
        }
    }
}
