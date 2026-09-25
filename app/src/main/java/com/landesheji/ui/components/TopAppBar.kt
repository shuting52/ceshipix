package com.landesheji.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.landesheji.ui.theme.KawaiiBgSoft
import com.landesheji.ui.theme.KawaiiMint
import com.landesheji.ui.theme.KawaiiOutline
import com.landesheji.ui.theme.KawaiiPink
import com.landesheji.ui.theme.KawaiiPinkLight
import com.landesheji.ui.theme.KawaiiSkyBlue
import com.landesheji.ui.theme.KawaiiSurface
import com.landesheji.ui.theme.KawaiiTextMuted
import com.landesheji.ui.theme.KawaiiTextPrimary
import com.landesheji.ui.theme.KawaiiTextSecondary
import com.landesheji.ui.theme.KawaiiTextWhite
import com.landesheji.ui.theme.KawaiiYellow

@Composable
fun PixelLabTopAppBar(
    canUndo: Boolean,
    canRedo: Boolean,
    isZoomMode: Boolean,
    isGridVisible: Boolean,
    isLayersPanelOpen: Boolean,
    layerCount: Int,
    onAddText: () -> Unit,
    onAddDate: () -> Unit,
    onAddSticker: () -> Unit,
    onAddShape: () -> Unit,
    onImportImage: () -> Unit,
    onStartDraw: () -> Unit,
    onUploadBackgroundImage: () -> Unit,
    onUploadBackgroundVideo: () -> Unit,
    onSaveProject: () -> Unit,
    onSaveImage: () -> Unit,
    onShare: () -> Unit,
    onOpenQuotes: () -> Unit,
    onExportSource: () -> Unit,
    onImportSource: () -> Unit,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    onToggleZoom: () -> Unit,
    onToggleGrid: () -> Unit,
    onToggleLayers: () -> Unit,
    onOpenCanvasSize: () -> Unit,
    onClearCanvas: () -> Unit,
    onShowAbout: () -> Unit,
    onShowSettings: () -> Unit,
    onToggleCanvasDraw: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var showAddMenu by remember { mutableStateOf(false) }
    var showSaveMenu by remember { mutableStateOf(false) }
    var showOverflowMenu by remember { mutableStateOf(false) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(KawaiiBgSoft.copy(alpha = 0.86f))
            .border(width = 2.dp, color = KawaiiOutline)
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Left Action Group: Add, Save, Share, Quotes
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // + Add Button & Dropdown (Bubblegum Pink)
            Box {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .kawaiiShadow(shadowOffset = 2.dp, cornerRadius = 10.dp)
                        .jellyClickable { showAddMenu = true }
                        .clip(RoundedCornerShape(10.dp))
                        .background(KawaiiPink)
                        .border(2.dp, KawaiiOutline, RoundedCornerShape(10.dp))
                        .testTag("top_add_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "添加图层",
                        tint = KawaiiTextWhite,
                        modifier = Modifier.size(22.dp)
                    )
                }

                DropdownMenu(
                    expanded = showAddMenu,
                    onDismissRequest = { showAddMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("🅰️ 添加文字", fontWeight = FontWeight.Bold) },
                        onClick = {
                            showAddMenu = false
                            onAddText()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("📅 当前日期", fontWeight = FontWeight.Bold) },
                        onClick = {
                            showAddMenu = false
                            onAddDate()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("⭐ 可爱贴纸", fontWeight = FontWeight.Bold) },
                        onClick = {
                            showAddMenu = false
                            onAddSticker()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("🔷 几何图形", fontWeight = FontWeight.Bold) },
                        onClick = {
                            showAddMenu = false
                            onAddShape()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("🖼️ 导入图片", fontWeight = FontWeight.Bold) },
                        onClick = {
                            showAddMenu = false
                            onImportImage()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("✏️ 自由画笔", fontWeight = FontWeight.Bold) },
                        onClick = {
                            showAddMenu = false
                            onStartDraw()
                        }
                    )
                    // v2.2：画布直接绘制入口
                    DropdownMenuItem(
                        text = { Text("🖌️ 画布直接绘制", color = KawaiiSkyBlue, fontWeight = FontWeight.Bold) },
                        onClick = {
                            showAddMenu = false
                            onToggleCanvasDraw()
                        }
                    )
                    // 需求6：划分分组：全局背景
                    HorizontalDivider(color = KawaiiOutline.copy(alpha = 0.3f), thickness = 0.5.dp)
                    DropdownMenuItem(
                        text = { Text("🖼️ 本地图片→背景", fontWeight = FontWeight.Bold, color = KawaiiMint) },
                        onClick = {
                            showAddMenu = false
                            onUploadBackgroundImage()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("🎬 本地视频→背景", fontWeight = FontWeight.Bold, color = KawaiiMint) },
                        onClick = {
                            showAddMenu = false
                            onUploadBackgroundVideo()
                        }
                    )
                    HorizontalDivider(color = KawaiiOutline.copy(alpha = 0.3f), thickness = 0.5.dp)
                    // 需求4：导入源码工程（.plp / .psd）
                    DropdownMenuItem(
                        text = { Text("📂 导入源码工程", fontWeight = FontWeight.Bold, color = KawaiiSkyBlue) },
                        onClick = {
                            showAddMenu = false
                            onImportSource()
                        }
                    )
                }
            }

            // Save Button & Dropdown (Honey Custard Yellow)
            Box {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .kawaiiShadow(shadowOffset = 2.dp, cornerRadius = 10.dp)
                        .jellyClickable { showSaveMenu = true }
                        .clip(RoundedCornerShape(10.dp))
                        .background(KawaiiYellow)
                        .border(2.dp, KawaiiOutline, RoundedCornerShape(10.dp))
                        .testTag("top_save_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Save,
                        contentDescription = "保存与导出",
                        tint = KawaiiTextPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                DropdownMenu(
                    expanded = showSaveMenu,
                    onDismissRequest = { showSaveMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("💾 保存为工程草稿", fontWeight = FontWeight.Bold) },
                        onClick = {
                            showSaveMenu = false
                            onSaveProject()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("🖼️ 导出高清图片", fontWeight = FontWeight.Bold) },
                        onClick = {
                            showSaveMenu = false
                            onSaveImage()
                        }
                    )
                    // 需求4：导出源码
                    DropdownMenuItem(
                        text = { Text("📦 导出源码 (.plp/.psd)", fontWeight = FontWeight.Bold, color = KawaiiSkyBlue) },
                        onClick = {
                            showSaveMenu = false
                            onExportSource()
                        }
                    )
                }
            }

            // Share Button
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .kawaiiShadow(shadowOffset = 2.dp, cornerRadius = 10.dp)
                    .jellyClickable { onShare() }
                    .clip(RoundedCornerShape(10.dp))
                    .background(KawaiiSurface)
                    .border(2.dp, KawaiiOutline, RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = "分享作品",
                    tint = KawaiiTextPrimary,
                    modifier = Modifier.size(19.dp)
                )
            }

            // Quotes Quick Button (Mint Soda)
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .kawaiiShadow(shadowOffset = 2.dp, cornerRadius = 10.dp)
                    .jellyClickable { onOpenQuotes() }
                    .clip(RoundedCornerShape(10.dp))
                    .background(KawaiiMint)
                    .border(2.dp, KawaiiOutline, RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.FormatQuote,
                    contentDescription = "精选金句",
                    tint = KawaiiTextWhite,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // Right Action Group: Undo, Redo, Zoom, Grid, Layers, Overflow
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            // Undo
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .jellyClickable(enabled = canUndo) { onUndo() }
                    .clip(CircleShape)
                    .background(if (canUndo) KawaiiSurface else Color(0xFFF2E9F2))
                    .border(1.5.dp, if (canUndo) KawaiiOutline else KawaiiOutline.copy(alpha = 0.3f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Undo,
                    contentDescription = "撤销",
                    tint = if (canUndo) KawaiiTextPrimary else KawaiiTextMuted,
                    modifier = Modifier.size(17.dp)
                )
            }

            // Redo
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .jellyClickable(enabled = canRedo) { onRedo() }
                    .clip(CircleShape)
                    .background(if (canRedo) KawaiiSurface else Color(0xFFF2E9F2))
                    .border(1.5.dp, if (canRedo) KawaiiOutline else KawaiiOutline.copy(alpha = 0.3f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Redo,
                    contentDescription = "重做",
                    tint = if (canRedo) KawaiiTextPrimary else KawaiiTextMuted,
                    modifier = Modifier.size(17.dp)
                )
            }

            // Zoom Toggle
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .jellyClickable { onToggleZoom() }
                    .clip(CircleShape)
                    .background(if (isZoomMode) KawaiiSkyBlue else KawaiiSurface)
                    .border(1.5.dp, KawaiiOutline, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ZoomIn,
                    contentDescription = "缩放画布",
                    tint = if (isZoomMode) KawaiiTextWhite else KawaiiTextPrimary,
                    modifier = Modifier.size(18.dp)
                )
            }

            // Grid Toggle
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .jellyClickable { onToggleGrid() }
                    .clip(CircleShape)
                    .background(if (isGridVisible) KawaiiMint else KawaiiSurface)
                    .border(1.5.dp, KawaiiOutline, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.GridView,
                    contentDescription = "对齐网格",
                    tint = if (isGridVisible) KawaiiTextWhite else KawaiiTextPrimary,
                    modifier = Modifier.size(18.dp)
                )
            }

            // Layers Panel Toggle with badge
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .kawaiiShadow(shadowOffset = if (isLayersPanelOpen) 2.dp else 1.dp, cornerRadius = 10.dp)
                    .jellyClickable { onToggleLayers() }
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (isLayersPanelOpen) KawaiiPink else KawaiiSurface)
                    .border(2.dp, KawaiiOutline, RoundedCornerShape(10.dp))
                    .testTag("top_layers_button"),
                contentAlignment = Alignment.Center
            ) {
                BadgedBox(
                    badge = {
                        if (layerCount > 0) {
                            Badge(
                                containerColor = if (isLayersPanelOpen) KawaiiYellow else KawaiiPink,
                                contentColor = KawaiiTextPrimary,
                                modifier = Modifier.border(1.dp, KawaiiOutline, CircleShape)
                            ) {
                                Text(
                                    text = "$layerCount",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Layers,
                        contentDescription = "图层管理",
                        tint = if (isLayersPanelOpen) KawaiiTextWhite else KawaiiTextPrimary,
                        modifier = Modifier.size(19.dp)
                    )
                }
            }

            // Overflow Menu
            Box {
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .jellyClickable { showOverflowMenu = true },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "更多选项",
                        tint = KawaiiTextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                DropdownMenu(
                    expanded = showOverflowMenu,
                    onDismissRequest = { showOverflowMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("📐 画布尺寸与比例", fontWeight = FontWeight.Bold) },
                        onClick = {
                            showOverflowMenu = false
                            onOpenCanvasSize()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("🗑️ 清空所有图层", color = KawaiiPink, fontWeight = FontWeight.Bold) },
                        onClick = {
                            showOverflowMenu = false
                            onClearCanvas()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("ℹ️ 关于 PixelLab 动漫版", fontWeight = FontWeight.Bold) },
                        onClick = {
                            showOverflowMenu = false
                            onShowAbout()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("⚙️ 设置中心", color = KawaiiPink, fontWeight = FontWeight.Bold) },
                        onClick = {
                            showOverflowMenu = false
                            onShowSettings()
                        }
                    )
                }
            }
        }
    }
}
