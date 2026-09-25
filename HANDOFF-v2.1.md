# PixelLab v2.1 开发交接文档（Agent 接手指南）

> 项目：`shuting52/ceshipix` ｜ 交接版本：v2.1（versionCode 3）｜ 交接时间：2026-09-25
> 本文件为**后续 Agent / 开发者**提供完整的项目脉络、本次改动清单、架构说明与踩坑记录，请先通读再动手。

---

## 一、项目一句话

**PixelLab 全中文版** —— 仿原版 PixelLab 的 Android 图片文字排版设计工具。
Kotlin + Jetpack Compose（Material3 Kawaii 主题），全部文字特效**本地 Canvas/Skia 绘制，无 AI、无外部 API、无服务器依赖**。

## 二、本次 v2.1 改动清单（8 项需求全落地）

| # | 需求 | 实现方式 | 关键文件 |
|---|---|---|---|
| 1 | OTA 更新 CSS 特效进度条 | 点击「立即更新」→ 霓虹流光进度条跑满 → 自动下载 APK → 系统安装替换老版本 | `util/UpdateManager.kt`、`ui/dialogs/UpdateCheckDialog.kt` |
| 2 | 双指捏合改为**缩放选中文字**（非画布）；画布宽度与手机屏幕左右对齐 | 画布 `pointerInput` 检测选中层则缩放该层；fitted 计算改为宽度铺满（0.985f） | `ui/components/CanvasView.kt` |
| 3 | 删除旧预设，替换为 **10 组可编辑文字模板**（水晶/玻璃/刺绣/3D/霓虹/金属/火焰/冰霜/故障/赛博） | 模板=JSON 数据，效果=本地绘制函数，文字=可改参数；预览图用本地 Canvas 实时渲染 | `data/model/TextTemplate.kt`、`util/TemplateEffectsRenderer.kt`、`ui/components/PresetsToolBar.kt` |
| 4 | 修复**文字放大导出后显示不全** | 根因：导出用硬编码 380px 当预览宽，实际预览宽随屏幕变化 → 字号被错误放大。修复：画布上报真实显示宽度，导出按同一比例映射 | `ui/viewmodel/PixelLabViewModel.kt`（`canvasDisplayWidth`）、`util/BitmapExporter.kt`、`ui/components/CanvasView.kt` |
| 5 | 修复**图片/视频背景未应用到 UI** | 根因：`Scaffold(containerColor = Color(0xF2FFFFFF))` 不透明白底完全遮住背景。修复：半透明（0x66FFFFFF）+ 各面板背景半透明化 | `MainActivity.kt`、`ui/components/UiThemeBackground.kt`、各 ToolBar |
| 6 | PS FX 面板太宽遮挡画布 + 增加特效 | 宽度 292dp→232dp；新增透明度/字间距/行距/曲线弧度/加粗/斜体/下划线设置 | `ui/components/PsFxSidePanel.kt` |
| 7 | 图层面板太宽遮挡画布 | 宽度 310dp→216dp + 背景半透明 | `ui/components/LayersPanel.kt` |
| - | 版本同步 | versionCode 3 / versionName 2.1；update.json 同步；CI 产物名 PixelLab-v2.1.apk | `app/build.gradle.kts`、`backend/update.json`、`.github/workflows/build-release.yml` |

## 三、架构速览（改代码前必读）

```
app/src/main/java/com/landesheji/
├── MainActivity.kt              # 主界面组装（顶栏/底栏/画布/对话框全部在此接线）
├── SplashActivity.kt            # 开屏动画
├── data/
│   ├── model/
│   │   ├── LayerModel.kt        # 核心数据模型（LayerItem/TextProperties/CanvasConfig…）
│   │   ├── TextTemplate.kt      # ★ 文字模板数据（10 组模板 JSON 结构 + 深拷贝）
│   │   ├── PresetsData.kt       # 旧预设（已被模板系统取代，可留作参考或删除）
│   │   └── QuotesData.kt / StickersData.kt
│   └── db/                      # Room 工程库 + ProjectSerializer（JSON 序列化）
├── ui/
│   ├── components/
│   │   ├── CanvasView.kt        # ★ 画布视图 + 手势（拖拽/双指/手柄）
│   │   ├── CanvasRenderer.kt    # ★ 画布渲染（背景/图层/文字特效全在此）
│   │   ├── PresetsToolBar.kt    # ★ 文字模板工具栏（底部 Tab 1）
│   │   ├── PsFxSidePanel.kt     # PS FX 侧边面板
│   │   ├── LayersPanel.kt       # 图层面板
│   │   └── TextToolBar / ShapeToolBar / BackgroundToolBar / EffectsToolBar
│   ├── dialogs/                 # 全部弹窗（文字编辑/导出/更新检查…）
│   └── viewmodel/PixelLabViewModel.kt   # ★ 状态中枢（StateFlow）
└── util/
    ├── TemplateEffectsRenderer.kt  # ★★ 10 种文字特效渲染器（纯本地 Skia）
    ├── BitmapExporter.kt           # 位图导出（保存/分享）
    ├── SourceCodec.kt              # .plp/.psd 源码编解码
    └── UpdateManager.kt            # OTA 更新
```

## 四、★ 模板系统设计（需求3核心，务必理解）

```
模板 = 数据（TextTemplatesData，JSON 可序列化）
效果 = 绘制函数（TemplateEffectsRenderer 内 10 个 drawXxxText）
文字 = 可修改参数（套用后 TextProperties.templateEffect 驱动渲染，改字特效不变）
```

- **数据流**：用户点模板 → `ViewModel.applyTextTemplate()` **深拷贝**（`TextTemplate.deepCopy()`）→ 转成 `LayerItem`（`textProps.templateEffect = "crystal"` 等）→ `CanvasRenderer.drawTextLayer()` 检测到 `templateEffect` 非空即走特效渲染器 → 用户改文字只改 `textProps.text`，特效由 `templateEffectParamsJson` 驱动**保持不变**。
- **新增模板**（第 11、12 组）：只需在 `TextTemplatesData` 加一个 `TextTemplate` 对象并加入 `templates` 列表（**注意：templates 列表在文件末尾**，因 Kotlin 对象初始化顺序）；再在 `TemplateEffectsRenderer.drawEffect()` 加一个分支即可。
- **效果参数**：每个效果函数从 JSONObject 读参数（`depth/strokeColor/gradient/glowBlur`…），与 JS 设计师稿逐字段对应。
- **导出同步**：`BitmapExporter.generateBitmap()` 同样走 `TemplateEffectsRenderer.drawEffect()`，保证画布预览与导出图**完全一致**（否则会二次开发出"预览和导出不一样"的 bug）。
- **序列化**：`ProjectSerializer` 已补齐 `templateEffect`/`templateEffectParamsJson` 两字段，保存工程/导出 plp/psd 不会丢特效。**新增 TextProperties 字段时必须同步更新 ProjectSerializer 两端**。

## 五、坐标系统一（需求4，非常重要）

| 场景 | 坐标基准 |
|---|---|
| 画布预览 | 画布 fitted 尺寸（宽度铺满屏幕，~0.985×屏宽），图层 x/y 相对画布中心 |
| 导出 | `previewScale = 目标宽 / 预览画布真实宽度`（由 ViewModel.canvasDisplayWidth 提供） |

**教训**：BitmapExporter 曾硬编码 `previewScale = width/380`，而实际预览宽不是 380 → 文字被错误放大溢出。**任何新特效/新图层类型加入时，预览与导出必须共用同一套缩放参数。**

## 六、OTA 更新链路（已全链路验证）

```
App「关于→检查更新」→ GET raw.githubusercontent.com/shuting52/ceshipix/main/backend/update.json
→ versionCode(3) > 本地(3 之前是 2) → 弹窗 → 立即更新 → CSS 进度条 → 下载 Release APK → FileProvider 安装
```

- **发布新版流程**：改 `versionCode`+1 → 改 `backend/update.json` → 构建 APK → 传 Release（**资产名必须与 update.json 的 apkUrl 文件名一致**，当前为 `PixelLab-v2.1.apk`）→ 用户端自动收到更新。
- `UpdateManager.UPDATE_URL` 指向 `main` 分支 raw 地址，有 ~5 分钟 CDN 缓存。
- 注意：`releases/latest/download/` 只认**最新非预发布** Release 的第一个同名资产。

## 七、构建环境（本地已验证）

```bash
# 依赖：JDK 17（便携版在 ~/tools/jdk17）+ Android SDK 36（~/tools/android-sdk）+ Gradle 9.3.1（~/tools/gradle-9.3.1）
export JAVA_HOME=~/tools/jdk17 && export PATH=$JAVA_HOME/bin:$PATH
export ANDROID_HOME=~/tools/android-sdk && export ANDROID_SDK_ROOT=~/tools/android-sdk
./gradlew :app:assembleDebug          # debug 包（内置 debug.keystore 自动签名）
./gradlew :app:testDebugUnitTest      # 14 个单元测试（模板/序列化/容错）
# release 包需签名：KEYSTORE_PATH/STORE_PASSWORD/KEY_PASSWORD 环境变量
```

- **单元测试**：`TextTemplateTest`（10 组模板完整性/深拷贝/JSON 往返）、`ProjectSerializerFullTest`（全类型图层往返+容错）、`ExampleRobolectricTest`、`ExampleUnitTest`。
- **CI**：`.github/workflows/build-release.yml` —— push 构建+测试，打 `v*` 标签自动建 Release 传 APK。

## 八、已知限制 & 后续可做

1. **字体库单薄**：`TextProperties.fontStyleName` 目前映射到系统默认字体，接字体文件可做（如 Google Fonts 打包 assets）。
2. **模板只支持单行文字层**：多文字图层模板（主副标题）数据结构已支持，效果渲染器按行渲染已就绪，可直接加第 11、12 组多图层模板。
3. **纹理填充/曲线弧度**：`curveAngle`（曲线弧度）在渲染器中尚未实现绘制（FX 面板可调但无效果），是已知 TODO。
4. **Firebase/Gemini 未接入**：依赖与 metadata 声明了，但源码未调用。
5. **故障/赛博的动画开关**：`animation: false` 预留，可后续加 Shader 动画。
6. **撤销重做**：已支持（HistorySnapshot）。
7. **ProGuard 未开启**：release minify=false。

## 九、安全与规范提醒

- 仓库 **没有 .gitignore 中的 debug.keystore / my-upload-key.jks 不应提交**（已在 .gitignore）。
- 本机 `my-upload-key.jks` 是临时签名（密码 android），**正式上架必须换自有 keystore**，并在 GitHub Secrets 配 `STORE_PASSWORD`/`KEY_PASSWORD`。
- 构建产物、临时 keystore、JDK/SDK 均不属于仓库交付物。

## 十、快速自测清单（改完跑一遍）

```
1. 打开 App → 预设 Tab → 应显示 10 个模板（预览图带真实特效）
2. 点「蓝色水晶字」→ 画布出现水晶字 → 点文字编辑改字 → 特效不变
3. 双指外扩 → 文字放大（不是画布）；画布宽度与屏幕左右对齐
4. 设图片背景（+菜单→本地上传背景）→ 整个 UI 透出图片背景
5. 导出图片 → 与画布预览一致，大字号文字完整显示
6. 图层面板/FX 面板 → 更窄，不遮画布
7. 关于→检查更新 → 看到 v2.1 进度条 → 跑完自动安装
```
