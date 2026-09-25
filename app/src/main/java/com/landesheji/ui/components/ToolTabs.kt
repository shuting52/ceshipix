package com.landesheji.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.landesheji.ui.theme.KawaiiBgSoft
import com.landesheji.ui.theme.KawaiiLavender
import com.landesheji.ui.theme.KawaiiMint
import com.landesheji.ui.theme.KawaiiOutline
import com.landesheji.ui.theme.KawaiiPeach
import com.landesheji.ui.theme.KawaiiPink
import com.landesheji.ui.theme.KawaiiShadow
import com.landesheji.ui.theme.KawaiiTextMuted
import com.landesheji.ui.theme.KawaiiTextPrimary
import com.landesheji.ui.theme.KawaiiTextWhite
import com.landesheji.ui.theme.KawaiiYellow
import com.landesheji.ui.viewmodel.BottomTab

@Composable
fun PixelLabBottomNav(
    activeTab: BottomTab,
    onTabSelected: (BottomTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(66.dp)
            .background(KawaiiBgSoft)
            .border(width = 2.dp, color = KawaiiOutline)
            .navigationBarsPadding()
            .padding(horizontal = 8.dp, vertical = 6.dp)
            .testTag("bottom_nav_bar"),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        BottomTab.values().forEach { tab ->
            val isSelected = activeTab == tab

            val targetColor = when (tab) {
                BottomTab.PRESETS -> KawaiiYellow
                BottomTab.TEXT -> KawaiiPink
                BottomTab.SHAPES -> KawaiiMint
                BottomTab.BACKGROUND -> KawaiiLavender
                BottomTab.EFFECTS -> KawaiiPeach
            }

            val animShadow by animateDpAsState(
                targetValue = if (isSelected) 3.5.dp else 0.dp,
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                label = "tab_shadow"
            )

            val animBg by animateColorAsState(
                targetValue = if (isSelected) targetColor else Color.Transparent,
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                label = "tab_bg"
            )

            val animTextCol by animateColorAsState(
                targetValue = if (isSelected) {
                    if (tab == BottomTab.PRESETS) KawaiiTextPrimary else KawaiiTextWhite
                } else KawaiiTextMuted,
                label = "tab_text_color"
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .kawaiiShadow(shadowOffset = animShadow, cornerRadius = 14.dp)
                    .jellyClickable { onTabSelected(tab) }
                    .clip(RoundedCornerShape(14.dp))
                    .background(animBg)
                    .border(
                        width = if (isSelected) 2.dp else 0.dp,
                        color = if (isSelected) KawaiiOutline else Color.Transparent,
                        shape = RoundedCornerShape(14.dp)
                    )
                    .padding(vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = tab.icon,
                        fontSize = if (isSelected) 20.sp else 18.sp
                    )
                    Text(
                        text = tab.title,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
                        color = animTextCol
                    )
                }
            }
        }
    }
}
