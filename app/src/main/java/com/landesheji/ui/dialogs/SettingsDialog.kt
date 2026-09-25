package com.landesheji.ui.dialogs

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.landesheji.BuildConfig
import com.landesheji.R
import com.landesheji.ui.components.kawaiiShadow
import com.landesheji.ui.theme.*
import kotlin.math.min

/** v2.2 设置中心：关于软件/联系作者/投喂(支付宝)/加好友(QQ微信)/分享/隐私。作者QQ 307779523 */
@Composable
fun SettingsDialog(onDismiss: () -> Unit, onCheckUpdate: (() -> Unit)? = null) {
    val context = LocalContext.current
    var page by remember { mutableStateOf("main") }
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(14.dp)
                .kawaiiShadow(shadowOffset = 5.dp, cornerRadius = 22.dp)
                .border(2.dp, KawaiiOutline, RoundedCornerShape(22.dp)).testTag("settings_dialog"),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = KawaiiSurface)
        ) {
            when (page) {
                "main" -> SettingsMainPage(context, onDismiss, onCheckUpdate, onOpenPage = { page = it })
                "about" -> SettingsAboutPage(onBack = { page = "main" })
                "contact" -> SettingsContactPage(context, onBack = { page = "main" })
                "feed" -> SettingsFeedPage(context, onBack = { page = "main" })
                "privacy" -> SettingsPrivacyPage(onBack = { page = "main" })
            }
        }
    }
}

@Composable
private fun SettingsMainPage(context: Context, onDismiss: () -> Unit, onCheckUpdate: (() -> Unit)?, onOpenPage: (String) -> Unit) {
    Column(Modifier.fillMaxWidth().padding(18.dp).verticalScroll(rememberScrollState())) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("⚙️ 设置", fontSize = 18.sp, fontWeight = FontWeight.Black, color = KawaiiTextPrimary)
            IconButton(onClick = onDismiss, modifier = Modifier.size(30.dp)) { Text("✕", fontSize = 16.sp, fontWeight = FontWeight.Black, color = KawaiiPink) }
        }
        Spacer(Modifier.height(6.dp))
        HorizontalDivider(color = KawaiiOutline.copy(alpha = 0.2f))
        Spacer(Modifier.height(12.dp))
        SettingsItem("ℹ️", "关于软件", "版本信息、功能亮点", KawaiiSkyBlue.copy(alpha = 0.35f)) { onOpenPage("about") }
        SettingsItem("💬", "联系作者", "QQ 加好友 · 交流反馈", KawaiiPinkLight) { onOpenPage("contact") }
        SettingsItem("💝", "投喂作者", "支付宝赞赏，感谢支持", KawaiiYellowLight) { onOpenPage("feed") }
        SettingsItem("📤", "应用分享", "把 PixelLab 分享给朋友", KawaiiMintLight) { shareApp(context) }
        SettingsItem("🔒", "隐私说明", "数据与隐私政策", KawaiiCardTint) { onOpenPage("privacy") }
        SettingsItem("🔄", "检查更新", "OTA 在线升级", KawaiiCardTint) { if (onCheckUpdate != null) { onDismiss(); onCheckUpdate() } }
        Spacer(Modifier.height(10.dp))
        Text("PixelLab 全中文版 v${BuildConfig.VERSION_NAME}", fontSize = 11.sp, color = KawaiiTextMuted, modifier = Modifier.align(Alignment.CenterHorizontally))
    }
}

@Composable
private fun SettingsAboutPage(onBack: () -> Unit) {
    Column(Modifier.fillMaxWidth().padding(18.dp).verticalScroll(rememberScrollState()), horizontalAlignment = Alignment.CenterHorizontally) {
        SettingsPageHeader("ℹ️ 关于软件", onBack)
        Box(Modifier.size(84.dp).clip(RoundedCornerShape(22.dp)).background(KawaiiPinkLight).border(2.dp, KawaiiOutline, RoundedCornerShape(22.dp)), contentAlignment = Alignment.Center) {
            Image(painterResource(R.drawable.ic_app_logo), "PixelLab Logo", Modifier.size(78.dp).clip(RoundedCornerShape(20.dp)))
        }
        Spacer(Modifier.height(12.dp))
        Text("🌸 PixelLab 全中文版", fontSize = 18.sp, fontWeight = FontWeight.Black, color = KawaiiTextPrimary)
        Text("v${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})", fontSize = 12.sp, color = KawaiiPink, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text("卡通可爱动画风 · PS级 3D 艺术字工作台\n图片文字排版设计，全部特效本地绘制", fontSize = 12.sp, color = KawaiiTextSecondary, lineHeight = 18.sp, textAlign = TextAlign.Center)
        Spacer(Modifier.height(14.dp))
        HorizontalDivider(color = KawaiiOutline.copy(alpha = 0.2f))
        Spacer(Modifier.height(12.dp))
        FeatureItem("✨ PS 3D 立体字全功能", "三维俯仰旋转、多向透视挤压、斜面浮雕、材质高光与地面阴影")
        FeatureItem("🎀 10 组文字模板", "水晶/玻璃/刺绣/3D/霓虹/金属/火焰/冰霜/故障/赛博，套用后可自由改字")
        FeatureItem("📄 强大图层系统", "多文字、形状、贴纸、画笔涂鸦与图片，自由排序拖拽缩放")
        FeatureItem("💾 高清无损导出", "PNG/JPG 多比例导出、.plp/.psd 源码导入导出、本地工程保存")
        FeatureItem("🔄 OTA 在线更新", "内置更新检查，新版本一键下载安装")
    }
}

@Composable
private fun SettingsContactPage(context: Context, onBack: () -> Unit) {
    val authorQq = "307779523"
    fun copyText(label: String, text: String) {
        val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        cm.setPrimaryClip(ClipData.newPlainText(label, text))
        Toast.makeText(context, "已复制 $label", Toast.LENGTH_SHORT).show()
    }
    Column(Modifier.fillMaxWidth().padding(18.dp).verticalScroll(rememberScrollState()), horizontalAlignment = Alignment.CenterHorizontally) {
        SettingsPageHeader("💬 联系作者", onBack)
        Text("有任何想法、建议或问题\n欢迎联系作者交流反馈~", fontSize = 13.sp, color = KawaiiTextSecondary, lineHeight = 20.sp, textAlign = TextAlign.Center)
        Spacer(Modifier.height(16.dp))
        SettingsContactCard("🐧", "QQ 加好友", "作者 QQ：$authorQq", "点击复制 QQ 号", KawaiiSkyBlue.copy(alpha = 0.35f)) { copyText("QQ号", authorQq) }
        SettingsContactCard("💬", "微信加好友", "先加 QQ 获取微信", "通过 QQ 联系作者", KawaiiMintLight) { copyText("QQ号", authorQq); Toast.makeText(context, "请加QQ后获取微信", Toast.LENGTH_SHORT).show() }
        Spacer(Modifier.height(6.dp))
        Text("开发由：懒得设计 · 原创开发", fontSize = 11.sp, color = KawaiiTextMuted, modifier = Modifier.align(Alignment.CenterHorizontally))
    }
}

@Composable
private fun SettingsFeedPage(context: Context, onBack: () -> Unit) {
    val alipayAccount = "307779523"
    fun copyAlipay() {
        val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        cm.setPrimaryClip(ClipData.newPlainText("支付宝账号", alipayAccount))
        Toast.makeText(context, "已复制支付宝账号，快去投喂吧 💝", Toast.LENGTH_SHORT).show()
    }
    Column(Modifier.fillMaxWidth().padding(18.dp).verticalScroll(rememberScrollState()), horizontalAlignment = Alignment.CenterHorizontally) {
        SettingsPageHeader("💝 投喂作者", onBack)
        Box(Modifier.size(96.dp).clip(CircleShape).background(KawaiiYellowLight).border(2.dp, KawaiiYellow, CircleShape), contentAlignment = Alignment.Center) { Text("🍚", fontSize = 44.sp) }
        Spacer(Modifier.height(12.dp))
        Text("如果 PixelLab 帮到了你", fontSize = 14.sp, fontWeight = FontWeight.Black, color = KawaiiTextPrimary)
        Text("可以请作者喝杯奶茶 / 吃碗饭饭~", fontSize = 12.sp, color = KawaiiTextSecondary)
        Spacer(Modifier.height(16.dp))
        SettingsContactCard("💙", "支付宝投喂", "支付宝账号：$alipayAccount", "点击复制账号", KawaiiSkyBlue.copy(alpha = 0.35f)) { copyAlipay() }
        Spacer(Modifier.height(8.dp))
        Text("每一份投喂都是作者持续更新的动力 🙏", fontSize = 11.sp, color = KawaiiTextMuted, modifier = Modifier.align(Alignment.CenterHorizontally))
    }
}

@Composable
private fun SettingsPrivacyPage(onBack: () -> Unit) {
    Column(Modifier.fillMaxWidth().padding(18.dp).verticalScroll(rememberScrollState())) {
        SettingsPageHeader("🔒 隐私说明", onBack)
        FeatureItem("1. 数据完全本地", "所有文字、图层、模板数据均保存在手机本地（Room 数据库与本地文件），不会上传到任何服务器。")
        FeatureItem("2. 无账号体系", "本应用无需注册登录，不收集任何个人信息。")
        FeatureItem("3. 网络使用", "仅 OTA 更新检查会请求 GitHub 更新接口（获取版本号与下载链接），不涉及隐私数据。")
        FeatureItem("4. 图片权限", "仅在你主动选择图片/视频时访问相册，用于添加图层或背景。")
        FeatureItem("5. 第三方", "本应用不接入广告、不接入统计 SDK、不向第三方共享数据。")
        Spacer(Modifier.height(8.dp))
        Text("更新日期：2026-09-25\n作者：懒得设计（QQ 307779523）", fontSize = 11.sp, color = KawaiiTextMuted, modifier = Modifier.align(Alignment.CenterHorizontally))
    }
}

private fun shareApp(context: Context) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, "PixelLab 全中文版")
        putExtra(Intent.EXTRA_TEXT, "推荐一个超好用的图片文字排版设计 App「PixelLab 全中文版」🌸 PS级3D艺术字/10组文字模板/海量贴纸，全部本地绘制！")
    }
    context.startActivity(Intent.createChooser(intent, "分享 PixelLab"))
}

@Composable
private fun SettingsPageHeader(title: String, onBack: () -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = onBack, modifier = Modifier.size(32.dp)) {
            Icon(Icons.Default.ArrowBack, contentDescription = "返回", tint = KawaiiPink, modifier = Modifier.size(18.dp))
        }
        Text(title, fontSize = 17.sp, fontWeight = FontWeight.Black, color = KawaiiTextPrimary)
    }
    Spacer(Modifier.height(10.dp))
    HorizontalDivider(color = KawaiiOutline.copy(alpha = 0.2f))
    Spacer(Modifier.height(12.dp))
}

@Composable
private fun SettingsItem(icon: String, title: String, desc: String, bg: Color, onClick: () -> Unit) {
    Row(Modifier.fillMaxWidth().padding(vertical = 5.dp).clip(RoundedCornerShape(14.dp)).background(bg)
        .border(1.5.dp, KawaiiOutline.copy(alpha = 0.25f), RoundedCornerShape(14.dp))
        .clickable { onClick() }.padding(horizontal = 14.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(38.dp).clip(RoundedCornerShape(10.dp)).background(Color.White.copy(alpha = 0.6f)), contentAlignment = Alignment.Center) { Text(icon, fontSize = 20.sp) }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(title, fontSize = 14.sp, fontWeight = FontWeight.Black, color = KawaiiTextPrimary)
            Text(desc, fontSize = 11.sp, color = KawaiiTextSecondary)
        }
        Text("›", fontSize = 18.sp, color = KawaiiTextMuted)
    }
}

@Composable
private fun SettingsContactCard(icon: String, title: String, desc: String, sub: String, bg: Color, onClick: () -> Unit) {
    Column(Modifier.fillMaxWidth().padding(vertical = 6.dp).clip(RoundedCornerShape(16.dp)).background(bg)
        .border(1.5.dp, KawaiiOutline.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
        .clickable { onClick() }.padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(44.dp).clip(RoundedCornerShape(12.dp)).background(Color.White.copy(alpha = 0.7f)), contentAlignment = Alignment.Center) { Text(icon, fontSize = 22.sp) }
            Spacer(Modifier.width(12.dp))
            Column {
                Text(title, fontSize = 15.sp, fontWeight = FontWeight.Black, color = KawaiiTextPrimary)
                Text(desc, fontSize = 12.sp, color = KawaiiTextSecondary)
                Text(sub, fontSize = 10.sp, color = KawaiiPink, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun FeatureItem(title: String, desc: String) {
    Column(Modifier.fillMaxWidth().padding(vertical = 4.dp).clip(RoundedCornerShape(10.dp)).background(KawaiiCardTint)
        .border(1.dp, KawaiiOutline.copy(alpha = 0.15f), RoundedCornerShape(10.dp)).padding(10.dp)) {
        Text(title, fontSize = 12.sp, fontWeight = FontWeight.Black, color = KawaiiTextPrimary)
        Spacer(Modifier.height(2.dp))
        Text(desc, fontSize = 11.sp, color = KawaiiTextSecondary, lineHeight = 15.sp)
    }
}