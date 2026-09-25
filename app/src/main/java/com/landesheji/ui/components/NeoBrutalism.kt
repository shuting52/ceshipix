package com.landesheji.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.landesheji.ui.theme.KawaiiOutline
import com.landesheji.ui.theme.KawaiiPink
import com.landesheji.ui.theme.KawaiiShadow
import com.landesheji.ui.theme.KawaiiSurface
import com.landesheji.ui.theme.KawaiiTextPrimary
import com.landesheji.ui.theme.KawaiiTextWhite
import com.landesheji.ui.theme.KawaiiYellow

// Forwarding all legacy Neo methods to Kawaii Cartoon components
fun Modifier.bouncyClickable(
    enabled: Boolean = true,
    onClick: () -> Unit
): Modifier = jellyClickable(enabled = enabled, onClick = onClick)

fun Modifier.neoHardShadow(
    shadowOffset: Dp = 3.5.dp,
    shadowColor: Color = KawaiiShadow,
    cornerRadius: Dp = 14.dp
): Modifier = kawaiiShadow(shadowOffset = shadowOffset, shadowColor = shadowColor, cornerRadius = cornerRadius)

@Composable
fun NeoButton(
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
    KawaiiButton(
        text = text,
        onClick = onClick,
        modifier = modifier,
        containerColor = containerColor,
        contentColor = contentColor,
        icon = icon,
        enabled = enabled,
        cornerRadius = cornerRadius,
        borderWidth = borderWidth,
        shadowOffset = shadowOffset
    )
}

@Composable
fun NeoIconButton(
    icon: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = KawaiiSurface,
    borderColor: Color = KawaiiOutline,
    borderWidth: Dp = 2.dp,
    shadowOffset: Dp = 3.dp,
    cornerRadius: Dp = 14.dp,
    size: Dp = 40.dp
) {
    KawaiiIconButton(
        icon = icon,
        onClick = onClick,
        modifier = modifier,
        containerColor = containerColor,
        borderColor = borderColor,
        borderWidth = borderWidth,
        shadowOffset = shadowOffset,
        size = size
    )
}

@Composable
fun NeoChip(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    selectedColor: Color = KawaiiYellow,
    unselectedColor: Color = KawaiiSurface,
    icon: String? = null
) {
    KawaiiChip(
        text = text,
        isSelected = isSelected,
        onClick = onClick,
        modifier = modifier,
        selectedColor = selectedColor,
        unselectedColor = unselectedColor,
        icon = icon
    )
}

@Composable
fun Modifier.cssPulse(minScale: Float = 0.95f, maxScale: Float = 1.05f): Modifier = kawaiiWobble()
