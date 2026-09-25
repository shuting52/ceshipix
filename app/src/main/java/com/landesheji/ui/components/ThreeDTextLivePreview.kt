package com.landesheji.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.landesheji.data.model.TextProperties
import com.landesheji.ui.theme.KawaiiCardTint
import com.landesheji.ui.theme.KawaiiOutline
import com.landesheji.ui.theme.KawaiiPink
import com.landesheji.ui.theme.KawaiiPinkLight
import com.landesheji.ui.theme.KawaiiTextPrimary
import com.landesheji.ui.theme.KawaiiTextSecondary

/**
 * 3D 文字效果实时预览面板
 *
 * 用户调节任意 3D 属性滑块时，本组件会即时按 0.4x 比例重绘一份简化版 3D 文字，
 * 让用户在底部工具栏里就能直接预览效果，无需依赖主画布。
 */
@Composable
fun ThreeDTextLivePreview(
    props: TextProperties,
    opacity: Float = 1f,
    modifier: Modifier = Modifier,
    height: Int = 140
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().padding(start = 2.dp, bottom = 4.dp)
        ) {
            Text(
                text = "👀 实时预览",
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                color = KawaiiPink
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "调节下方滑块，效果立即变化",
                fontSize = 10.sp,
                color = KawaiiTextSecondary
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(height.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(KawaiiCardTint)
                .border(1.5.dp, KawaiiPinkLight, RoundedCornerShape(14.dp))
        ) {
            Canvas(modifier = Modifier.fillMaxWidth().height(height.dp)) {
                CanvasRenderer.drawTextLayerPreview(
                    drawScope = this,
                    props = props,
                    opacity = opacity,
                    previewScale = 0.4f
                )
            }
        }
    }
}