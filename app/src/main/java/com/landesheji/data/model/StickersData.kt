package com.landesheji.data.model

data class StickerItem(
    val id: String,
    val name: String,
    val emojiOrIcon: String,
    val category: String,
    // 需求6：自定义导入的本地贴纸（图片 URI，非空时作为图片贴纸渲染）
    val imageUri: String = ""
)

object StickersData {
    val categories = listOf("表情", "勋章标签", "形状装饰", "潮酷标志", "自然爱心", "矢量图标", "战队边框", "动感边框", "我的贴纸")

    val stickers: List<StickerItem> = listOf(
        // 表情
        StickerItem("emoji_1", "笑脸", "😎", "表情"),
        StickerItem("emoji_2", "星星眼", "🤩", "表情"),
        StickerItem("emoji_3", "调皮", "😜", "表情"),
        StickerItem("emoji_4", "火热", "🔥", "表情"),
        StickerItem("emoji_5", "派对", "🥳", "表情"),
        StickerItem("emoji_6", "庆祝", "🎉", "表情"),
        StickerItem("emoji_7", "点赞", "👍", "表情"),
        StickerItem("emoji_8", "比心", "🫰", "表情"),
        StickerItem("emoji_9", "肌肉", "💪", "表情"),
        StickerItem("emoji_10", "皇冠", "👑", "表情"),
        StickerItem("emoji_11", "火箭", "🚀", "表情"),
        StickerItem("emoji_12", "钻石", "💎", "表情"),

        // 勋章标签
        StickerItem("badge_1", "金牌", "🥇", "勋章标签"),
        StickerItem("badge_2", "勋章", "🎖️", "勋章标签"),
        StickerItem("badge_3", "缎带", "🎗️", "勋章标签"),
        StickerItem("badge_4", "标签", "🏷️", "勋章标签"),
        StickerItem("badge_5", "旗帜", "🚩", "勋章标签"),
        StickerItem("badge_6", "靶心", "🎯", "勋章标签"),

        // 形状装饰
        StickerItem("dec_1", "闪烁", "✨", "形状装饰"),
        StickerItem("dec_2", "闪电", "⚡", "形状装饰"),
        StickerItem("dec_3", "爆炸", "💥", "形状装饰"),
        StickerItem("dec_4", "星星", "⭐", "形状装饰"),
        StickerItem("dec_5", "光晕", "🌟", "形状装饰"),
        StickerItem("dec_6", "彗星", "☄️", "形状装饰"),

        // 潮酷标志
        StickerItem("cool_1", "霓虹", "🔮", "潮酷标志"),
        StickerItem("cool_2", "游戏机", "🎮", "潮酷标志"),
        StickerItem("cool_3", "音乐", "🎵", "潮酷标志"),
        StickerItem("cool_4", "麦克风", "🎙️", "潮酷标志"),
        StickerItem("cool_5", "照相机", "📷", "潮酷标志"),
        StickerItem("cool_6", "电影", "🎬", "潮酷标志"),

        // 自然爱心
        StickerItem("heart_1", "红心", "❤️", "自然爱心"),
        StickerItem("heart_2", "粉心", "💖", "自然爱心"),
        StickerItem("heart_3", "火心", "❤️‍🔥", "自然爱心"),
        StickerItem("heart_4", "花朵", "🌸", "自然爱心"),
        StickerItem("heart_5", "太阳", "☀️", "自然爱心"),
        StickerItem("heart_6", "月亮", "🌙", "自然爱心"),

        // 需求6：矢量透明图标（Unicode 符号 = 矢量可缩放透明贴纸）
        StickerItem("vec_1", "五角星", "⭐", "矢量图标"),
        StickerItem("vec_2", "四角星", "✦", "矢量图标"),
        StickerItem("vec_3", "花形", "❀", "矢量图标"),
        StickerItem("vec_4", "雪花", "❄️", "矢量图标"),
        StickerItem("vec_5", "箭头", "➤", "矢量图标"),
        StickerItem("vec_6", "箭头右", "➜", "矢量图标"),
        StickerItem("vec_7", "三角", "▶", "矢量图标"),
        StickerItem("vec_8", "菱形", "◆", "矢量图标"),
        StickerItem("vec_9", "圆点", "●", "矢量图标"),
        StickerItem("vec_10", "方框", "▣", "矢量图标"),
        StickerItem("vec_11", "音符", "♪", "矢量图标"),
        StickerItem("vec_12", "心标", "♥", "矢量图标"),
        StickerItem("vec_13", "月亮", "☾", "矢量图标"),
        StickerItem("vec_14", "太阳", "☀", "矢量图标"),
        StickerItem("vec_15", "对勾", "✓", "矢量图标"),
        StickerItem("vec_16", "十字", "✚", "矢量图标"),
        StickerItem("vec_17", "准星", "◎", "矢量图标"),
        StickerItem("vec_18", "太极", "☯", "矢量图标"),
        StickerItem("vec_19", "无限", "∞", "矢量图标"),
        StickerItem("vec_20", "五星花", "✺", "矢量图标"),

        // 需求6：战队边框（KR 电竞风）
        StickerItem("team_1", "王者框", "👑", "战队边框"),
        StickerItem("team_2", "荣耀框", "🏆", "战队边框"),
        StickerItem("team_3", "冠军框", "🥇", "战队边框"),
        StickerItem("team_4", "皇冠框", "♛", "战队边框"),
        StickerItem("team_5", "王座框", "♚", "战队边框"),
        StickerItem("team_6", "星芒框", "✶", "战队边框"),
        StickerItem("team_7", "火焰框", "⚜", "战队边框"),
        StickerItem("team_8", "皇冠冠", "♔", "战队边框"),
        StickerItem("team_9", "十字剑", "⚔", "战队边框"),
        StickerItem("team_10", "盾牌", "🛡️", "战队边框"),
        StickerItem("team_11", "骷髅标", "☠", "战队边框"),
        StickerItem("team_12", "目标框", "🎯", "战队边框"),

        // 需求6：动感边框（花式线条装饰）
        StickerItem("fr_1", "齿轮", "⚙", "动感边框"),
        StickerItem("fr_2", "星轨", "✧", "动感边框"),
        StickerItem("fr_3", "螺旋", "➳", "动感边框"),
        StickerItem("fr_4", "爆炸星", "✴", "动感边框"),
        StickerItem("fr_5", "风车", "⌘", "动感边框"),
        StickerItem("fr_6", "尖角", "⧩", "动感边框"),
        StickerItem("fr_7", "双线", "≡", "动感边框"),
        StickerItem("fr_8", "波浪", "∿", "动感边框"),
        StickerItem("fr_9", "大箭头", "⭢", "动感边框"),
        StickerItem("fr_10", "闪烁", "✪", "动感边框"),
        StickerItem("fr_11", "光环", "✯", "动感边框"),
        StickerItem("fr_12", "万花", "❁", "动感边框")
    )
}
