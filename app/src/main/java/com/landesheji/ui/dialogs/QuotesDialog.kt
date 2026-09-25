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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.landesheji.data.model.QuotesData
import com.landesheji.ui.components.jellyClickable
import com.landesheji.ui.components.kawaiiShadow
import com.landesheji.ui.theme.KawaiiCardTint
import com.landesheji.ui.theme.KawaiiOutline
import com.landesheji.ui.theme.KawaiiPink
import com.landesheji.ui.theme.KawaiiPinkLight
import com.landesheji.ui.theme.KawaiiSurface
import com.landesheji.ui.theme.KawaiiTextMuted
import com.landesheji.ui.theme.KawaiiTextPrimary
import com.landesheji.ui.theme.KawaiiTextSecondary
import com.landesheji.ui.theme.KawaiiYellow

@Composable
fun QuotesDialog(
    onDismiss: () -> Unit,
    onSelectQuote: (String) -> Unit
) {
    var selectedCategoryIndex by remember { mutableStateOf(0) }
    val currentCategory = QuotesData.categories.getOrElse(selectedCategoryIndex) { QuotesData.categories.first() }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(530.dp)
                .kawaiiShadow(shadowOffset = 5.dp, cornerRadius = 20.dp)
                .border(2.dp, KawaiiOutline, RoundedCornerShape(20.dp))
                .testTag("quotes_dialog"),
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
                        text = "💬 中文精选文案库",
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

                // Category Chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    QuotesData.categories.forEachIndexed { index, cat ->
                        val isSelected = index == selectedCategoryIndex
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (isSelected) KawaiiPinkLight else KawaiiCardTint)
                                .border(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) KawaiiPink else KawaiiOutline.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(14.dp)
                                )
                                .jellyClickable { selectedCategoryIndex = index }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "${cat.icon} ${cat.categoryName}",
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
                                color = if (isSelected) KawaiiPink else KawaiiTextPrimary
                            )
                        }
                    }
                }

                HorizontalDivider(color = KawaiiOutline.copy(alpha = 0.1f))

                // Quotes List
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(currentCategory.quotes) { quoteText ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(KawaiiCardTint)
                                .border(1.dp, KawaiiOutline.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                                .jellyClickable {
                                    onSelectQuote(quoteText)
                                    onDismiss()
                                }
                                .padding(14.dp)
                        ) {
                            Column {
                                Text(
                                    text = "“$quoteText”",
                                    fontSize = 14.sp,
                                    color = KawaiiTextPrimary,
                                    fontWeight = FontWeight.Bold,
                                    lineHeight = 20.sp
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = currentCategory.categoryName,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = KawaiiPink
                                    )
                                    Text(
                                        text = "点击使用 🌸",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = KawaiiTextSecondary
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
