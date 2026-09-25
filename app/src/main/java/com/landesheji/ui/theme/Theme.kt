package com.landesheji.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// 🌸 卡通可爱动漫风 (Kawaii Cute Cartoon Theme)
private val KawaiiAnimeColorScheme = lightColorScheme(
    primary = KawaiiPink,
    onPrimary = Color.White,
    primaryContainer = KawaiiPinkLight,
    onPrimaryContainer = KawaiiPink,

    secondary = KawaiiMint,
    onSecondary = Color.White,
    secondaryContainer = KawaiiMintLight,
    onSecondaryContainer = KawaiiMint,

    tertiary = KawaiiLavender,
    onTertiary = Color.White,
    tertiaryContainer = KawaiiLavenderLight,
    onTertiaryContainer = KawaiiLavender,

    background = KawaiiBg,
    onBackground = KawaiiTextPrimary,

    surface = KawaiiSurface,
    onSurface = KawaiiTextPrimary,

    surfaceVariant = KawaiiCardTint,
    onSurfaceVariant = KawaiiTextSecondary,

    outline = KawaiiOutline,
    outlineVariant = KawaiiOutlineLight
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = false,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = KawaiiAnimeColorScheme,
        typography = Typography,
        content = content
    )
}
