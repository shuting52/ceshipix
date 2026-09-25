package com.landesheji.ui.dialogs

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
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
import com.landesheji.ui.theme.KawaiiTextPrimary
import com.landesheji.ui.theme.KawaiiTextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SaveProjectDialog(
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    val defaultName = "可爱设计_" + SimpleDateFormat("MMdd_HHmm", Locale.CHINESE).format(Date())
    var projectName by remember { mutableStateOf(defaultName) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .kawaiiShadow(shadowOffset = 5.dp, cornerRadius = 20.dp)
                .border(2.dp, KawaiiOutline, RoundedCornerShape(20.dp))
                .testTag("save_project_dialog"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = KawaiiSurface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = "💾 保存工程草稿",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Black,
                    color = KawaiiTextPrimary
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "保存后随时可在底部【预设 - 我的工程】载入继续编辑，图层与 3D 效果完好保留 🌸",
                    fontSize = 12.sp,
                    color = KawaiiTextSecondary,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = projectName,
                    onValueChange = { projectName = it },
                    label = { Text("工程名称", fontSize = 11.sp) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.5.dp, KawaiiOutline, RoundedCornerShape(10.dp))
                        .testTag("project_name_input"),
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

                Spacer(modifier = Modifier.height(18.dp))

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
                        text = "确认保存 💖",
                        onClick = {
                            if (projectName.isNotBlank()) {
                                onSave(projectName.trim())
                            }
                            onDismiss()
                        },
                        containerColor = KawaiiPink,
                        modifier = Modifier.testTag("save_project_confirm_button")
                    )
                }
            }
        }
    }
}
