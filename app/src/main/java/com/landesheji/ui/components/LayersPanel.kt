package com.landesheji.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.landesheji.data.model.LayerItem
import com.landesheji.data.model.LayerType
import com.landesheji.ui.theme.KawaiiBg
import com.landesheji.ui.theme.KawaiiBgSoft
import com.landesheji.ui.theme.KawaiiCardTint
import com.landesheji.ui.theme.KawaiiMint
import com.landesheji.ui.theme.KawaiiOutline
import com.landesheji.ui.theme.KawaiiPink
import com.landesheji.ui.theme.KawaiiPinkLight
import com.landesheji.ui.theme.KawaiiSurface
import com.landesheji.ui.theme.KawaiiTextMuted
import com.landesheji.ui.theme.KawaiiTextPrimary
import com.landesheji.ui.theme.KawaiiTextSecondary
import com.landesheji.ui.theme.KawaiiTextWhite
import com.landesheji.ui.theme.KawaiiYellow

@Composable
fun LayersPanel(
    isOpen: Boolean,
    layers: List<LayerItem>,
    selectedLayerId: String?,
    onClose: () -> Unit,
    onSelectLayer: (String) -> Unit,
    onToggleVisibility: (String) -> Unit,
    onToggleLock: (String) -> Unit,
    onDuplicateLayer: (String) -> Unit,
    onDeleteLayer: (String) -> Unit,
    onMoveUp: (String) -> Unit,
    onMoveDown: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = isOpen,
        enter = slideInHorizontally(
            initialOffsetX = { it },
            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
        ),
        exit = slideOutHorizontally(targetOffsetX = { it }),
        modifier = modifier
    ) {
        Surface(
            modifier = Modifier
                .width(216.dp)
                .fillMaxHeight()
                .border(2.dp, KawaiiOutline)
                .testTag("layers_panel"),
            color = KawaiiBg.copy(alpha = 0.96f)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(KawaiiBgSoft)
                        .border(width = 1.dp, color = KawaiiOutline)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "📑 图层管理",
                            fontWeight = FontWeight.Black,
                            fontSize = 15.sp,
                            color = KawaiiTextPrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(KawaiiYellow)
                                .border(1.5.dp, KawaiiOutline, RoundedCornerShape(8.dp))
                                .padding(horizontal = 7.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "${layers.size}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                color = KawaiiTextPrimary
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .kawaiiShadow(shadowOffset = 2.dp, cornerRadius = 14.dp)
                            .jellyClickable { onClose() }
                            .background(KawaiiPinkLight, CircleShape)
                            .border(1.5.dp, KawaiiOutline, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "关闭图层面板",
                            tint = KawaiiOutline,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                HorizontalDivider(color = KawaiiOutline, thickness = 2.dp)

                if (layers.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "画布暂无图层 🍬\n点击上方 ➕ 添加文字或贴纸",
                            color = KawaiiTextSecondary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Render layers in reverse so top layer on canvas shows at top of list
                        itemsIndexed(layers.reversed()) { index, layer ->
                            val isSelected = layer.id == selectedLayerId
                            val isTopLayer = index == 0
                            val isBottomLayer = index == layers.size - 1

                            val cardBg = if (isSelected) KawaiiPinkLight else KawaiiSurface
                            val borderCol = if (isSelected) KawaiiPink else KawaiiOutline
                            val shadow = if (isSelected) 3.dp else 1.5.dp

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .kawaiiShadow(shadowOffset = shadow, cornerRadius = 12.dp)
                                    .jellyClickable { onSelectLayer(layer.id) }
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(cardBg)
                                    .border(2.dp, borderCol, RoundedCornerShape(12.dp))
                                    .padding(horizontal = 10.dp, vertical = 8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    // Layer Type Icon and Name
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(
                                            text = when (layer.type) {
                                                LayerType.TEXT -> "🅰️"
                                                LayerType.SHAPE -> "🔷"
                                                LayerType.STICKER -> "⭐"
                                                LayerType.IMAGE -> "🖼️"
                                                LayerType.DRAW -> "✏️"
                                            },
                                            fontSize = 16.sp
                                        )

                                        Spacer(modifier = Modifier.width(8.dp))

                                        Column {
                                            Text(
                                                text = if (layer.type == LayerType.TEXT) "\"${layer.textProps.text.take(12)}\"" else layer.name,
                                                fontSize = 13.sp,
                                                fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
                                                color = if (isSelected) KawaiiPink else KawaiiTextPrimary,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                text = if (layer.isLocked) "已锁定" else if (!layer.isVisible) "已隐藏" else "正常",
                                                fontSize = 10.sp,
                                                color = if (layer.isLocked) KawaiiPink else KawaiiTextMuted
                                            )
                                        }
                                    }

                                    // Action buttons for layer
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                                    ) {
                                        // Visibility Toggle
                                        Box(
                                            modifier = Modifier
                                                .size(28.dp)
                                                .jellyClickable { onToggleVisibility(layer.id) },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = if (layer.isVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                                contentDescription = "切换可见性",
                                                tint = if (layer.isVisible) KawaiiMint else KawaiiTextMuted,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }

                                        // Lock Toggle
                                        Box(
                                            modifier = Modifier
                                                .size(28.dp)
                                                .jellyClickable { onToggleLock(layer.id) },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = if (layer.isLocked) Icons.Default.Lock else Icons.Default.LockOpen,
                                                contentDescription = "切换锁定",
                                                tint = if (layer.isLocked) KawaiiPink else KawaiiTextMuted,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }

                                        // Move Up (towards top)
                                        Box(
                                            modifier = Modifier
                                                .size(28.dp)
                                                .jellyClickable(enabled = !isTopLayer) { onMoveUp(layer.id) },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.ArrowUpward,
                                                contentDescription = "上移图层",
                                                tint = if (!isTopLayer) KawaiiTextPrimary else Color(0xFFD4C8D8),
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }

                                        // Move Down (towards bottom)
                                        Box(
                                            modifier = Modifier
                                                .size(28.dp)
                                                .jellyClickable(enabled = !isBottomLayer) { onMoveDown(layer.id) },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.ArrowDownward,
                                                contentDescription = "下移图层",
                                                tint = if (!isBottomLayer) KawaiiTextPrimary else Color(0xFFD4C8D8),
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }

                                        // Duplicate
                                        Box(
                                            modifier = Modifier
                                                .size(28.dp)
                                                .jellyClickable { onDuplicateLayer(layer.id) },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.ContentCopy,
                                                contentDescription = "复制图层",
                                                tint = KawaiiTextPrimary,
                                                modifier = Modifier.size(15.dp)
                                            )
                                        }

                                        // Delete
                                        Box(
                                            modifier = Modifier
                                                .size(28.dp)
                                                .jellyClickable { onDeleteLayer(layer.id) },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "删除图层",
                                                tint = KawaiiPink,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
