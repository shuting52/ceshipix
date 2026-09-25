# PixelLab v2.2 开发交接文档（Agent 接手指南 · 续）

> 项目：`shuting52/ceshipix` ｜ 版本：v2.2（versionCode 4）｜ 日期：2026-09-25
> 前置阅读：`HANDOFF-v2.1.md`（架构/模板系统/坐标系统/踩坑记录），本文件只补充 v2.2 增量。

## 一、v2.2 改动清单

| 需求 | 实现 | 关键文件 |
|---|---|---|
| 设置中心 | 关于软件(含 logo)/联系作者(QQ 307779523)/投喂(支付宝)/QQ·微信加好友(复制账号)/应用分享(系统分享)/隐私说明/检查更新入口；顶栏 ⋮ 菜单加入口 | `ui/dialogs/SettingsDialog.kt`（新）、`ui/components/TopAppBar.kt`、`MainActivity.kt` |
| PS 图层样式 FX | 描边位置(内/中/外)、内阴影、内发光、颜色叠加、阴影扩展、外发光强度、6 组 FX 预设、一键清除全部效果 | `data/model/LayerModel.kt`（新字段+StrokePosition）、`ui/components/CanvasRenderer.kt`、`ui/components/PsFxSidePanel.kt`、`util/BitmapExporter.kt`、`data/db/ProjectSerializer.kt` |
| 贴纸染色 | 弹窗颜色行选择，选中后整体染色（PorterDuffColorFilter SRC_IN） | `ui/dialogs/StickersDialog.kt`、`data/model/StickersData.kt`（StickerItem 加字段）、`CanvasRenderer.drawStickerLayer` |
| 文字四边手柄删除 | 文字图层不再显示 4 角按钮，双击直接编辑文字；其他图层保留手柄 | `ui/components/CanvasView.kt`（LayerTouchOverlay） |
| 移除毛玻璃/霓虹/反色按钮 | 从 + 菜单删除（背景工具栏仍有完整滤镜面板可用） | `TopAppBar.kt`、`MainActivity.kt` |
| 自由画笔直接画布绘制 | + 菜单「画布直接绘制」→ 浮动工具栏(颜色/粗细/橡皮/完成)，手指在画布直接画，一笔一涂鸦图层 | `CanvasView.kt`（手势+实时预览）、`ViewModel.addCanvasDrawStroke`、`MainActivity.DrawOnCanvasToolbar` |
| 模板保存白底修复 | **根因**：BitmapExporter 缺失模板特效导出分支 → 模板文字导出时特效丢失退化为普通文字。已补同一套 TemplateEffectsRenderer 渲染 | `util/BitmapExporter.kt` |
| OTA 弹窗文案写死 | 更新弹窗固定显示「叮咚~我们又又又更新啦！/快更新看看新增了一些什么功能把~/如果你有什么好的想法建议/请联系作者」（每行一条） | `ui/dialogs/UpdateCheckDialog.kt` |

## 二、★ PS 图层样式新字段（TextProperties 增量）

```kotlin
strokePosition: StrokePosition   // INNER / CENTER / OUTER
shadowSpread: Float              // 0~100 阴影扩展
hasInnerShadow / innerShadowColorArgb / innerShadowRadius / innerShadowDx / innerShadowDy / innerShadowOpacity
hasInnerGlow / innerGlowColorArgb / innerGlowRadius / innerGlowOpacity
hasColorOverlay / colorOverlayColorArgb / colorOverlayOpacity
glowOpacity                      // 外发光强度
```

**渲染技术要点**（Canvas/Skia 原生，无第三方库）：
- 内阴影 / 内发光 / 颜色叠加：`canvas.saveLayer(bounds)` + 白色 fill 文字作 mask + `PorterDuffXfermode(SRC_IN)` + `BlurMaskFilter` → 效果只在文字内部显示。
- 内描边：同 mask 思路，stroke 文字用 SRC_IN。
- 阴影扩展：主阴影随 fill 绘制（保持透明度正确）+ 额外外圈模糊层。
- **CanvasRenderer（预览）与 BitmapExporter（导出）必须同步**，否则出现"预览与导出不一致"。改渲染务必两端都改。

**FX 预设**（高级样式页签底部）：黑描边/白描边/霓虹发光/金属文字/立体文字/长阴影，一键套用；一键清除全部效果。

## 三、设置中心

- 入口：顶栏 ⋮ →「⚙️ 设置中心」。
- 作者 QQ：`307779523`（联系作者、支付宝投喂都用此号，点击复制到剪贴板）。
- 微信：先加 QQ 获取。
- 应用分享：ACTION_SEND 文本分享。
- 隐私说明：静态文案（数据本地/无账号/仅 OTA 网络）。
- 关于软件：展示 `R.drawable.ic_app_logo`（1024×1024 应用图标）。⚠️ 用户提到过"附件图片"，但**当前仓库只有 ic_app_logo.jpg**；若作者提供专属头像/收款码图片，放入 `res/drawable/` 并在对应页面替换即可（SettingsDialog 中支付宝/QQ/微信卡片可加图片位）。

## 四、自由画笔模式

- `ViewModel.isDrawMode / drawColorArgb / drawStrokeWidth / drawIsEraser` 四个 StateFlow。
- `CanvasView` 在 `isDrawMode` 时：关闭图层触摸覆盖（LayerTouchOverlay 早退），画布 Box 上 `detectDragGestures` 收集相对画布中心的 `DrawPoint`，实时 polyline 预览，抬手调用 `onDrawStroke → addCanvasDrawStroke` 一笔一图层。
- 浮动工具栏 `DrawOnCanvasToolbar`（MainActivity 内）：8 色、粗细 Slider(2~40)、橡皮擦、完成。
- 注意：画笔模式下需手动退出（完成按钮）；缩放/平移在画笔模式下未处理（点坐标基于未缩放画布）。

## 五、OTA（v2.2 → 旧版本 2.1/2.0）

- `backend/update.json`：versionCode=4 > 旧版 3/2 → 旧版本检测到更新 → 弹窗（文案已写死）→ 进度条 → 下载 `PixelLab-v2.2.apk`（Release 已上传，`releases/latest/download/` 有效）→ 安装替换。
- 已验证：update.json HTTP 200、apkUrl HTTP 200、Release v2.2 资产在。

## 六、构建与测试

```bash
export JAVA_HOME=~/tools/jdk17 PATH ANDROID_HOME ANDROID_SDK_ROOT（见 v2.1 交接）
./gradlew :app:assembleDebug :app:testDebugUnitTest   # 14 测试全绿
KEYSTORE_PATH=... STORE_PASSWORD=... KEY_PASSWORD=... ./gradlew :app:assembleRelease
```

## 七、已知限制 / 后续可做

1. **模板多图层**：数据结构已支持，可做第 11、12 组（多文字层模板）。
2. **曲线弧度 curveAngle**：FX 面板可调但渲染器未实现，是已知 TODO。
3. **内阴影/内发光性能**：每次 drawTextLayer 都 saveLayer，图层多时可能略卡，可用离屏缓存优化。
4. **画布画笔模式坐标**：在 zoom 状态下手绘坐标未做逆变换，建议仅普通状态使用。
5. **预设持久化**：FX 内置预设已做，自定义保存预设未做（可用 Room/DataStore 存 JSON）。
6. **正式签名**：Release APK 为临时签名，上架需自有 keystore。

## 八、快速自测清单

```
1. ⋮ 菜单 → 设置中心 → 各子页（关于/联系/投喂/分享/隐私）正常，QQ 复制可用
2. 文字选中 → 无四角按钮 → 双击文字可编辑；双指捏合缩放文字
3. FX 面板 → 高级样式 → 内阴影/内发光/颜色叠加/描边位置/FX 预设/一键清除 实时生效
4. 导出图片 → 内阴影等效果保留，模板文字特效保留（无白底）
5. 贴纸弹窗 → 选颜色后贴纸染色
6. + 菜单 → 画布直接绘制 → 浮动工具栏选色/粗细 → 画布手绘 → 完成
7. 关于 → 检查更新 → 弹窗显示写死文案 → 进度条 → 安装
```
