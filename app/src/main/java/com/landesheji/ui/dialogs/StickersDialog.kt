package com.landesheji.ui.dialogs

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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.landesheji.data.model.StickerItem
import com.landesheji.data.model.StickersData
import com.landesheji.ui.components.jellyClickable
import com.landesheji.ui.components.kawaiiShadow
import com.landesheji.ui.theme.KawaiiCardTint
import com.landesheji.ui.theme.KawaiiOutline
import com.landesheji.ui.theme.KawaiiPink
import com.landesheji.ui.theme.KawaiiPinkLight
import com.landesheji.ui.theme.KawaiiSurface
import com.landesheji.ui.theme.KawaiiTextPrimary
import com.landesheji.ui.theme.KawaiiTextSecondary
import com.landesheji.ui.theme.KawaiiTextWhite

@Composable
fun StickersDialog(
    onDismiss: () -> Unit,
    onSelectSticker: (StickerItem) -> Unit,
    customStickers: List<StickerItem> = emptyList(),
    onImportSticker: () -> Unit = {}
) {
    var selectedCategory by remember { mutableStateOf(StickersData.categories.first()) }
    // v2.2：贴纸染色
    var selectedTintArgb by remember { mutableStateOf(0xFFFFFFFF.toInt()) }
    var tintEnabled by remember { mutableStateOf(false) }
    // 需求6："我的贴纸"分类只有用户导入过才加入分类列表
    val allCategories = if (customStickers.isNotEmpty()) {
        StickersData.categories + "我的贴纸"
    } else {
        StickersData.categories
    }
    val filteredStickers = remember(selectedCategory, customStickers) {
        if (selectedCategory == "我的贴纸") {
            customStickers
        } else {
            StickersData.stickers.filter { it.category == selectedCategory }
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(490.dp)
                .kawaiiShadow(shadowOffset = 5.dp, cornerRadius = 20.dp)
                .border(2.dp, KawaiiOutline, RoundedCornerShape(20.dp))
                .testTag("stickers_dialog"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = KawaiiSurface)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "⭐ 可爱贴纸与动漫素材",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        color = KawaiiTextPrimary
                    )
                    Row {
                        // 需求6：导入本地图片贴纸
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .jellyClickable(onClick = onImportSticker)
                                .clip(RoundedCornerShape(8.dp))
                                .background(KawaiiPink)
                                .testTag("import_sticker_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "➕", fontSize = 14.sp, fontWeight = FontWeight.Black, color = KawaiiTextWhite)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
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
                }

                HorizontalDivider(color = KawaiiOutline.copy(alpha = 0.2f))

                // Category Chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    allCategories.forEach { cat ->
                        val isSelected = cat == selectedCategory
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (isSelected) KawaiiPinkLight else KawaiiCardTint)
                                .border(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) KawaiiPink else KawaiiOutline.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(14.dp)
                                )
                                .jellyClickable { selectedCategory = cat }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = cat,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
                                color = if (isSelected) KawaiiPink else KawaiiTextPrimary
                            )
                        }
                    }
                }

                HorizontalDivider(color = KawaiiOutline.copy(alpha = 0.1f))

                // v2.2：贴纸染色（给贴纸上颜色）
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("贴纸颜色：", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = KawaiiTextSecondary)
                    val tintColors = listOf(
                        0xFFFFFFFF.toInt() to "原色",
                        0xFFFF5252.toInt() to "红",
                        0xFFFF9F1C.toInt() to "橙",
                        0xFFFFD166.toInt() to "黄",
                        0xFF06D6A0.toInt() to "绿",
                        0xFF118AB2.toInt() to "蓝",
                        0xFF8338EC.toInt() to "紫",
                        0xFF2A2A38.toInt() to "黑"
                    )
                    tintColors.forEach { (argb, label) ->
                        val isSel = tintEnabled && selectedTintArgb == argb
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 3.dp)
                                .size(26.dp)
                                .clip(CircleShape)
                                .background(Color(argb))
                                .border(
                                    width = if (isSel) 3.dp else 1.dp,
                                    color = if (isSel) KawaiiPink else KawaiiOutline.copy(alpha = 0.3f),
                                    shape = CircleShape
                                )
                                .jellyClickable {
                                    if (argb == 0xFFFFFFFF.toInt()) {
                                        tintEnabled = false
                                    } else {
                                        tintEnabled = true
                                        selectedTintArgb = argb
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            if (argb == 0xFFFFFFFF.toInt()) {
                                Text(text = "✕", fontSize = 10.sp, fontWeight = FontWeight.Black, color = KawaiiTextSecondary)
                            }
                        }
                    }
                }

                HorizontalDivider(color = KawaiiOutline.copy(alpha = 0.1f))

                // Stickers Grid
                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredStickers) { sticker ->
                        Column(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(KawaiiCardTint)
                                .border(1.dp, KawaiiOutline.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                                .jellyClickable {
                                    onSelectSticker(
                                        if (tintEnabled) sticker.copy(hasTint = true, tintArgb = selectedTintArgb)
                                        else sticker.copy(hasTint = false)
                                    )
                                    onDismiss()
                                }
                                .padding(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            if (sticker.imageUri.isNotEmpty()) {
                                // 需求6：自定义导入的本地图片贴纸，用 Coil 渲染
                                AsyncImage(
                                    model = sticker.imageUri,
                                    contentDescription = sticker.name,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.size(44.dp)
                                )
                            } else {
                                Text(text = sticker.emojiOrIcon, fontSize = 32.sp)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = sticker.name,
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
