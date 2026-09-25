package com.landesheji.ui.components

import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.compose.foundation.Image
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.landesheji.data.db.ProjectEntity
import com.landesheji.data.model.TextTemplate
import com.landesheji.data.model.TextTemplatesData
import com.landesheji.ui.theme.KawaiiBg
import com.landesheji.ui.theme.KawaiiOutline
import com.landesheji.ui.theme.KawaiiPink
import com.landesheji.ui.theme.KawaiiPinkLight
import com.landesheji.ui.theme.KawaiiSurface
import com.landesheji.ui.theme.KawaiiTextMuted
import com.landesheji.ui.theme.KawaiiTextPrimary
import com.landesheji.ui.theme.KawaiiTextSecondary
import com.landesheji.ui.theme.KawaiiYellow
import com.landesheji.util.TemplateEffectsRenderer
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 需求重造3：文字模板系统工具栏
 * 展示 10 组文字模板（蓝色水晶/透明玻璃/红色刺绣/橙色3D/粉色霓虹/黄金金属/火焰/冰霜/故障/赛博），
 * 预览图使用本地 Canvas 实时绘制真实特效（非 AI、非外部图片），点击后深拷贝套用并可直接编辑文字。
 */
@Composable
fun PresetsToolBar(
    savedProjects: List<ProjectEntity>,
    onSelectTextTemplate: (TextTemplate) -> Unit,
    onLoadProject: (ProjectEntity) -> Unit,
    onDeleteProject: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedSection by remember { mutableStateOf("TEMPLATES") }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(KawaiiBg.copy(alpha = 0.86f))
            .border(2.dp, KawaiiOutline)
            .testTag("presets_toolbar")
    ) {
        // Toggle tabs: 文字模板 vs 我的工程
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            KawaiiChip(
                text = "🎨 文字模板 (${TextTemplatesData.templates.size})",
                isSelected = selectedSection == "TEMPLATES",
                onClick = { selectedSection = "TEMPLATES" },
                selectedColor = KawaiiYellow
            )

            KawaiiChip(
                text = "📁 我的工程 (${savedProjects.size})",
                isSelected = selectedSection == "MY_PROJECTS",
                onClick = { selectedSection = "MY_PROJECTS" },
                selectedColor = KawaiiPinkLight
            )
        }

        // Horizontal items
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(104.dp)
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 12.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (selectedSection == "TEMPLATES") {
                TextTemplatesData.templates.forEach { template ->
                    Column(
                        modifier = Modifier
                            .width(92.dp)
                            .kawaiiShadow(shadowOffset = 2.dp, cornerRadius = 12.dp)
                            .jellyClickable { onSelectTextTemplate(template) }
                            .clip(RoundedCornerShape(12.dp))
                            .background(KawaiiSurface)
                            .border(1.5.dp, KawaiiOutline, RoundedCornerShape(12.dp))
                            .padding(6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // 用本地 Canvas 渲染真实特效预览（模板=数据，效果=绘制函数）
                        TemplatePreview(
                            template = template,
                            modifier = Modifier.size(52.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = template.name,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = KawaiiTextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "编辑文字",
                            fontSize = 8.sp,
                            color = KawaiiPink,
                            maxLines = 1
                        )
                    }
                }
            } else {
                if (savedProjects.isEmpty()) {
                    Box(
                        modifier = Modifier.padding(start = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "暂无保存的草稿，点击上方 💾 可保存当前可爱设计 🌸",
                            fontSize = 12.sp,
                            color = KawaiiTextSecondary
                        )
                    }
                } else {
                    savedProjects.forEach { project ->
                        val dateFormatted = SimpleDateFormat("MM-dd HH:mm", Locale.CHINESE).format(Date(project.updatedAt))
                        Column(
                            modifier = Modifier
                                .width(96.dp)
                                .kawaiiShadow(shadowOffset = 2.dp, cornerRadius = 12.dp)
                            .jellyClickable { onLoadProject(project) }
                            .clip(RoundedCornerShape(12.dp))
                            .background(KawaiiSurface)
                            .border(1.5.dp, KawaiiOutline, RoundedCornerShape(12.dp))
                            .padding(6.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(project.previewColorArgb)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "📑",
                                    fontSize = 18.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = project.title,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = KawaiiTextPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = dateFormatted,
                                fontSize = 9.sp,
                                color = KawaiiTextMuted,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }
    }
}

/** 模板缩略图：用本地 Canvas 把模板首层文字特效真实渲染出来 */
@Composable
private fun TemplatePreview(
    template: TextTemplate,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val previewSize = remember(template.id) { 208 } // px，2x 密度下约 104dp
    val previewArgb = template.backgroundColorArgb
    val bgColor = Color(previewArgb)

    val layer0 = template.layers.firstOrNull()
    val bitmap = remember(template, layer0?.text) {
        val bmp = Bitmap.createBitmap(previewSize, previewSize, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        canvas.drawColor(previewArgb)
        if (layer0 != null) {
            try {
                val params = layer0.effectParams ?: JSONObject()
                TemplateEffectsRenderer.drawEffect(
                    canvas = canvas,
                    text = layer0.text.take(2),
                    fontSizePx = previewSize * 0.22f,
                    effect = layer0.effect,
                    params = params,
                    x = previewSize / 2f,
                    y = previewSize / 2f,
                    maxWidth = previewSize * 1.8f
                )
            } catch (_: Exception) { }
        }
        bmp
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .border(1.dp, KawaiiOutline.copy(alpha = 0.4f), RoundedCornerShape(8.dp)),
        contentAlignment = Alignment.Center
    ) {
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = template.name,
            modifier = Modifier.size(previewSize.dp)
        )
    }
}