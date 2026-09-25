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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.landesheji.data.model.ShapeType
import com.landesheji.ui.components.jellyClickable
import com.landesheji.ui.components.kawaiiShadow
import com.landesheji.ui.theme.KawaiiCardTint
import com.landesheji.ui.theme.KawaiiOutline
import com.landesheji.ui.theme.KawaiiPink
import com.landesheji.ui.theme.KawaiiSurface
import com.landesheji.ui.theme.KawaiiTextPrimary
import com.landesheji.ui.theme.KawaiiTextSecondary

data class ShapeOption(val type: ShapeType, val name: String, val icon: String)

@Composable
fun ShapesDialog(
    onDismiss: () -> Unit,
    onSelectShape: (ShapeType) -> Unit
) {
    val shapeOptions = listOf(
        ShapeOption(ShapeType.ROUNDED_RECTANGLE, "圆角矩形", "▢"),
        ShapeOption(ShapeType.RECTANGLE, "直角矩形", "■"),
        ShapeOption(ShapeType.CIRCLE, "圆形", "●"),
        ShapeOption(ShapeType.STAR, "五角星", "★"),
        ShapeOption(ShapeType.HEART, "爱心", "♥"),
        ShapeOption(ShapeType.TRIANGLE, "三角形", "▲"),
        ShapeOption(ShapeType.HEXAGON, "六边形", "⬢"),
        ShapeOption(ShapeType.ARROW, "指示箭头", "➔")
    )

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .kawaiiShadow(shadowOffset = 5.dp, cornerRadius = 20.dp)
                .border(2.dp, KawaiiOutline, RoundedCornerShape(20.dp))
                .testTag("shapes_dialog"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = KawaiiSurface)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "🔷 选择几何形状",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        color = KawaiiTextPrimary
                    )
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .jellyClickable { onDismiss() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "关闭",
                            tint = KawaiiTextPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                HorizontalDivider(color = KawaiiOutline.copy(alpha = 0.2f))

                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(shapeOptions) { option ->
                        Column(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(KawaiiCardTint)
                                .border(1.dp, KawaiiOutline.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                                .jellyClickable {
                                    onSelectShape(option.type)
                                    onDismiss()
                                }
                                .padding(10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(text = option.icon, fontSize = 28.sp, color = KawaiiPink)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = option.name,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = KawaiiTextPrimary
                            )
                        }
                    }
                }
            }
        }
    }
}
