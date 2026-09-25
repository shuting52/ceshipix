# PixelLab 全中文版

> 强大的图片与文字排版设计工具 —— 全中文界面，Kawaii 主题。
> 支持 3D 立体文字、多图层管理、海量贴纸与形状、经典中文语录库、丰富滤镜特效、画布尺寸自定义、源码级工程导入导出及高清作品保存分享。

![platform](https://img.shields.io/badge/platform-Android%208.0%2B-brightgreen) ![kotlin](https://img.shields.io/badge/kotlin-2.2.10-purple) ![compose](https://img.shields.io/badge/UI-Jetpack%20Compose-blue) ![license](https://img.shields.io/badge/license-MIT-green)

---

## ✨ 功能亮点

| 模块 | 说明 |
|---|---|
| 📝 **文字排版** | 字体/颜色/渐变/描边/阴影/背景框/3D 立体字/外发光/纹理填充/渐变叠加，PS 图层样式级 FX |
| 🎨 **PS FX 侧边面板** | PS3D 立体字实时预览（挤出厚度、透视、倒角、材质、光照、倾斜、地面投影），5 大类 PS 特效一站式调节 |
| 🖼️ **多图层管理** | 添加/复制/删除/排序/锁定/隐藏/移动/变换，图层侧滑面板随手管理 |
| 🧩 **贴纸与形状** | 矢量图标 / 战队边框 / 动感边框三大类透明贴纸，导入本地图片为自定义贴纸；几何图形圆角与透明度可调，支持本地上传图形 |
| 💬 **中文金句库** | 每类 100 条精选语录，一键插入 |
| ✏️ **手绘涂鸦** | 画笔/橡皮擦/调色，支持撤销与双击重新编辑 |
| 🎞️ **画布与背景** | 1:1 / 3:4 / 9:16 / 16:9 等任意尺寸，纯色/渐变/图片/视频背景，毛玻璃/霓虹光晕/反色一键加持 |
| 📤 **源码级导入导出** | `.plp` 工程包（100% 可再编辑）；`.psd` 位图（内嵌源数据可还原文字编辑） |
| 📐 **多比例导出** | 1:1 / 3:4 / 4:3 / 9:16 / 16:9 / 2:3 / 3:2 高清图片，一键保存/分享 |
| 🔄 **OTA 在线更新** | 内置更新检查，新版本一键下载安装 |

## 🚀 快速开始

### 环境要求
- JDK 17
- Android Studio（Ladybug 或更新版本）
- Android SDK 36（compileSdk 36，targetSdk 36）

### 构建
```bash
# 调试包
gradle :app:assembleDebug

# 发布包（需配置签名，见下文）
gradle :app:assembleRelease
```

调试包签名已内置 `debug.keystore`（项目自动生成），可直接安装测试。

### 安装
将 `app/build/outputs/apk/debug/app-debug.apk` 拷贝到手机后点击安装，或连接电脑后：
```bash
adb install app/build/outputs/apk/debug/app-debug.apk
```

## 🧱 技术栈

- **语言**：Kotlin 2.2.10
- **UI**：Jetpack Compose（Material3 + Kawaii 主题）
- **构建**：Gradle 9.3.1 / AGP 9.1.1 / KSP 2.3.5
- **数据**：Room 2.7（本地工程库）、Moshi（JSON）
- **网络**：OkHttp / Retrofit
- **图像**：Coil、Bitmap Canvas 自绘渲染
- **最低系统**：Android 8.0（minSdk 24）

## 📂 目录结构

```
ceshipix/
├── app/                        # Android 应用主模块
│   └── src/main/java/com/landesheji/
│       ├── data/               # 数据层（Room 数据库、模型、预设/语录/贴纸素材库）
│       ├── ui/                 # UI 层（Canvas 画布、工具栏、对话框、主题、ViewModel）
│       └── util/               # 工具（位图导出、plp/psd 源码编解码、OTA 更新）
├── backend/                    # OTA 更新后端模板（update.json + 部署指南）
├── gradle/                     # Gradle 版本目录与包装器
└── metadata.json               # 应用元数据
```

## 🔄 版本更新（OTA）

1. 将 `backend/update.json` 与新版 APK 发布到静态托管（GitHub Releases 推荐）
2. 修改 `app/src/main/java/com/landesheji/util/UpdateManager.kt` 中的 `UPDATE_URL`
3. 每次发版将 `versionCode` +1

详细部署步骤见 [backend/README.md](backend/README.md)。

## ⚠️ 发布签名（重要）

正式对外分发请使用自己的签名密钥（当前 `build.gradle.kts` 支持通过环境变量注入）：
```bash
export KEYSTORE_PATH=/path/to/your-keystore.jks
export STORE_PASSWORD=xxxx
export KEY_PASSWORD=xxxx
gradle :app:assembleRelease
```

## 📜 License

[MIT](LICENSE)

---

**开发由：懒得设计 · 原创开发** 💜