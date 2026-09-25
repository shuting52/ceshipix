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
import com.landesheji.ui.components.KawaiiButton
import com.landesheji.ui.components.jellyClickable
import com.landesheji.ui.components.kawaiiShadow
import com.landesheji.ui.theme.KawaiiCardTint
import com.landesheji.ui.theme.KawaiiMint
import com.landesheji.ui.theme.KawaiiOutline
import com.landesheji.ui.theme.KawaiiPink
import com.landesheji.ui.theme.KawaiiPinkLight
import com.landesheji.ui.theme.KawaiiSkyBlue
import com.landesheji.ui.theme.KawaiiSurface
import com.landesheji.ui.theme.KawaiiTextMuted
import com.landesheji.ui.theme.KawaiiTextPrimary
import com.landesheji.ui.theme.KawaiiTextSecondary
import com.landesheji.ui.theme.KawaiiYellow

/** 需求4：导出源码格式选择对话框（plp 工程包 / psd 位图源码） */
@Composable
fun ExportSourceDialog(
    onDismiss: () -> Unit,
    onExport: (format: String, fileName: String) -> Unit
) {
    var selectedFormat by remember { mutableStateOf("plp") }
    var fileName by remember { mutableStateOf("PixelLab_${System.currentTimeMillis() / 1000}") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .kawaiiShadow(shadowOffset = 5.dp, cornerRadius = 20.dp)
                .border(2.dp, KawaiiOutline, RoundedCornerShape(20.dp))
                .testTag("export_source_dialog"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = KawaiiSurface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = "📦 导出绘图源码",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Black,
                    color = KawaiiTextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "可将当前工程导出为可继续编辑的源码文件，导入回来即可还原全部图层。",
                    fontSize = 11.sp,
                    color = KawaiiTextMuted
                )

                Spacer(modifier = Modifier.height(14.dp))

                // 格式选择
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        "plp" to "PixelLab 工程包\n100% 可编辑（含文字图层）",
                        "psd" to "Photoshop 位图\n含源数据可还原编辑"
                    ).forEach { (fmt, desc) ->
                        val isSel = selectedFormat == fmt
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSel) KawaiiPinkLight else KawaiiCardTint)
                                .border(
                                    width = if (isSel) 2.dp else 1.dp,
                                    color = if (isSel) KawaiiPink else KawaiiOutline.copy(alpha = 0.3f),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .jellyClickable { selectedFormat = fmt }
                                .padding(12.dp)
                        ) {
                            Column {
                                Text(
                                    text = ".$fmt",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Black,
                                    color = if (isSel) KawaiiPink else KawaiiTextPrimary
                                )
                                Spacer(modifier = Modifier.height(3.dp))
                                Text(
                                    text = desc,
                                    fontSize = 10.sp,
                                    color = KawaiiTextSecondary,
                                    lineHeight = 14.sp
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 文件名
                Text(text = "文件名：", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = KawaiiTextSecondary)
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = fileName,
                    onValueChange = { fileName = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.5.dp, KawaiiOutline, RoundedCornerShape(12.dp)),
                    placeholder = { Text("工程名称...", color = KawaiiTextMuted) },
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold)
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("取消", color = KawaiiTextSecondary, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    KawaiiButton(
                        text = "导出 📦",
                        onClick = {
                            val clean = fileName.trim().ifEmpty { "PixelLab_${System.currentTimeMillis() / 1000}" }
                            onExport(selectedFormat, clean)
                            onDismiss()
                        },
                        containerColor = KawaiiSkyBlue
                    )
                }
            }
        }
    }
}