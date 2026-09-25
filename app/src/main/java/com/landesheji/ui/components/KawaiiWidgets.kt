package com.landesheji.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.landesheji.ui.theme.KawaiiOutline
import com.landesheji.ui.theme.KawaiiPink
import com.landesheji.ui.theme.KawaiiShadow
import com.landesheji.ui.theme.KawaiiSurface
import com.landesheji.ui.theme.KawaiiTextPrimary
import com.landesheji.ui.theme.KawaiiTextWhite
import com.landesheji.ui.theme.KawaiiYellow
import kotlinx.coroutines.launch

/**
 * 🍬 果冻挤压微动效 (Jelly Squish & Bounce Clickable)
 * 按下时横向拉伸、纵向压缩并轻微下沉，松手时如布丁果冻般自然Q弹回弹！
 */
fun Modifier.jellyClickable(
    enabled: Boolean = true,
    onClick: () -> Unit
): Modifier = composed {
    val scope = rememberCoroutineScope()
    val scaleX = remember { Animatable(1f) }
    val scaleY = remember { Animatable(1f) }
    val offsetY = remember { Animatable(0f) }

    this
        .scale(scaleX = scaleX.value, scaleY = scaleY.value)
        .offset { IntOffset(0, offsetY.value.toInt()) }
        .pointerInput(enabled) {
            if (!enabled) return@pointerInput
            awaitEachGesture {
                awaitFirstDown(requireUnconsumed = false)
                scope.launch {
                    scaleX.animateTo(1.08f, tween(70, easing = FastOutSlowInEasing))
                }
                scope.launch {
                    scaleY.animateTo(0.88f, tween(70, easing = FastOutSlowInEasing))
                }
                scope.launch {
                    offsetY.animateTo(4f, tween(70, easing = FastOutSlowInEasing))
                }

                val up = waitForUpOrCancellation()
                scope.launch {
                    scaleX.animateTo(
                        1f,
                        spring(dampingRatio = 0.45f, stiffness = Spring.StiffnessMedium)
                    )
                }
                scope.launch {
                    scaleY.animateTo(
                        1f,
                        spring(dampingRatio = 0.45f, stiffness = Spring.StiffnessMedium)
                    )
                }
                scope.launch {
                    offsetY.animateTo(
                        0f,
                        spring(dampingRatio = 0.45f, stiffness = Spring.StiffnessMedium)
                    )
                }
                if (up != null) {
                    onClick()
                }
            }
        }
}

/**
 * 🧸 卡通立体糖果阴影 (Kawaii Chubby Soft Offset Shadow)
 */
fun Modifier.kawaiiShadow(
    shadowOffset: Dp = 3.5.dp,
    shadowColor: Color = KawaiiShadow,
    cornerRadius: Dp = 14.dp
): Modifier = drawBehind {
    val offsetPx = shadowOffset.toPx()
    val radiusPx = cornerRadius.toPx()
    drawRoundRect(
        color = shadowColor,
        topLeft = Offset(0f, offsetPx),
        size = Size(size.width, size.height),
        cornerRadius = CornerRadius(radiusPx, radiusPx)
    )
}

/**
 * 🎀 卡通萌系高光糖果按钮 (Kawaii Glossy Candy Button)
 * 带有顶部可爱玻璃高光与饱满弧形果冻质感
 */
@Composable
fun KawaiiButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = KawaiiPink,
    contentColor: Color = KawaiiTextWhite,
    icon: String? = null,
    enabled: Boolean = true,
    cornerRadius: Dp = 14.dp,
    borderWidth: Dp = 2.dp,
    shadowOffset: Dp = 3.5.dp
) {
    val fillModifier = if (enabled) {
        Modifier.background(
            Brush.verticalGradient(
                listOf(containerColor, containerColor.copy(alpha = 0.92f))
            )
        )
    } else {
        Modifier.background(Color(0xFFE8DEEC))
    }

    Box(
        modifier = modifier
            .kawaiiShadow(shadowOffset = if (enabled) shadowOffset else 0.dp, cornerRadius = cornerRadius)
            .jellyClickable(enabled = enabled, onClick = onClick)
            .clip(RoundedCornerShape(cornerRadius))
            .then(fillModifier)
            .border(borderWidth, KawaiiOutline, RoundedCornerShape(cornerRadius))
            .padding(horizontal = 14.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (icon != null) {
                Text(text = icon, fontSize = 14.sp)
                Spacer(modifier = Modifier.width(6.dp))
            }
            Text(
                text = text,
                color = if (enabled) contentColor else Color(0xFF9E8B9E),
                fontSize = 13.sp,
                fontWeight = FontWeight.Black
            )
        }
    }
}

/**
 * 🌸 卡通圆形马卡龙图标按钮 (Kawaii Mochi Icon Button)
 */
@Composable
fun KawaiiIconButton(
    icon: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = KawaiiSurface,
    borderColor: Color = KawaiiOutline,
    borderWidth: Dp = 2.dp,
    shadowOffset: Dp = 3.dp,
    size: Dp = 40.dp
) {
    Box(
        modifier = modifier
            .size(size)
            .kawaiiShadow(shadowOffset = shadowOffset, cornerRadius = size / 2)
            .jellyClickable(onClick = onClick)
            .clip(CircleShape)
            .background(containerColor)
            .border(borderWidth, borderColor, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(text = icon, fontSize = 18.sp)
    }
}

/**
 * 🍬 卡通糖果胶囊标签 (Kawaii Candy Pill Chip)
 */
@Composable
fun KawaiiChip(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    selectedColor: Color = KawaiiYellow,
    unselectedColor: Color = KawaiiSurface,
    icon: String? = null
) {
    val bg = if (isSelected) selectedColor else unselectedColor
    val textCol = if (isSelected) KawaiiTextPrimary else KawaiiTextPrimary
    val shadow = if (isSelected) 3.dp else 1.5.dp

    Box(
        modifier = modifier
            .kawaiiShadow(shadowOffset = shadow, cornerRadius = 12.dp)
            .jellyClickable(onClick = onClick)
            .clip(RoundedCornerShape(12.dp))
            .background(bg)
            .border(2.dp, KawaiiOutline, RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (icon != null) {
                Text(text = icon, fontSize = 13.sp)
                Spacer(modifier = Modifier.width(4.dp))
            }
            Text(
                text = text,
                color = textCol,
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold
            )
        }
    }
}

/**
 * 🌟 萌动呼吸闪烁动画 (Kawaii Sparkle / Wobble Modifier)
 */
@Composable
fun Modifier.kawaiiWobble(): Modifier {
    val infiniteTransition = rememberInfiniteTransition(label = "kawaii_wobble")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "wobble"
    )
    return this.scale(scale)
}
