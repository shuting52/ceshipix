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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.landesheji.ui.components.KawaiiButton
import com.landesheji.ui.components.kawaiiShadow
import com.landesheji.ui.theme.KawaiiCardTint
import com.landesheji.ui.theme.KawaiiOutline
import com.landesheji.ui.theme.KawaiiPink
import com.landesheji.ui.theme.KawaiiSurface
import com.landesheji.ui.theme.KawaiiTextMuted
import com.landesheji.ui.theme.KawaiiTextPrimary
import com.landesheji.ui.theme.KawaiiTextSecondary

@Composable
fun TextEditorDialog(
    initialText: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
    onDelete: (() -> Unit)? = null
) {
    var text by remember { mutableStateOf(initialText) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .kawaiiShadow(shadowOffset = 5.dp, cornerRadius = 20.dp)
                .border(2.dp, KawaiiOutline, RoundedCornerShape(20.dp))
                .testTag("text_editor_dialog"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = KawaiiSurface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "✏️ 编辑文字内容",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Black,
                        color = KawaiiTextPrimary
                    )

                    Row {
                        if (onDelete != null) {
                            IconButton(
                                onClick = {
                                    onDelete()
                                    onDismiss()
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "删除该图层",
                                    tint = KawaiiPink
                                )
                            }
                        }

                        IconButton(
                            onClick = { text = "" },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "清空文字",
                                tint = KawaiiTextSecondary
                            )
                        }

                        // 需求3：右上角 X 按钮，一键关闭弹窗（不做任何修改）
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .size(32.dp)
                                .testTag("text_editor_close_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "关闭",
                                tint = KawaiiTextPrimary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Input Box
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                        .border(1.5.dp, KawaiiOutline, RoundedCornerShape(12.dp))
                        .testTag("text_editor_input"),
                    placeholder = { Text("在此输入您的文字内容...", color = KawaiiTextMuted) },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = KawaiiCardTint,
                        unfocusedContainerColor = KawaiiCardTint,
                        focusedTextColor = KawaiiTextPrimary,
                        unfocusedTextColor = KawaiiTextPrimary,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Info row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "字数: ${text.length}",
                        fontSize = 11.sp,
                        color = KawaiiTextMuted
                    )
                    Text(
                        text = "行数: ${text.split("\n").size}",
                        fontSize = 11.sp,
                        color = KawaiiTextMuted
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
                        text = "完成 ✓",
                        onClick = {
                            if (text.isNotBlank()) {
                                onConfirm(text)
                            }
                            onDismiss()
                        },
                        containerColor = KawaiiPink,
                        modifier = Modifier.testTag("text_editor_confirm_button")
                    )
                }
            }
        }
    }
}
