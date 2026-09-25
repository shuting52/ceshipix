# PixelLab 后端更新程序部署指南

本目录包含 App 的 OTA 更新后端模板，用于"检查更新 → 下载新 APK → 安装"。

## 工作原理

App 内的「关于 → 检查更新」会请求一个 JSON 接口（默认 `https://example.com/pixellab/update.json`），
与当前版本号比较，若有新版则提示下载（通过 FileProvider 安装）。

## 部署步骤（任意一种方式）

### 方式一：GitHub 托管（免费推荐）

1. 在 GitHub 新建仓库（或使用现有 ceshipix 仓库），把 `backend/update.json` 上传到仓库根目录
2. 把新版本的 APK 上传到仓库 Releases 或仓库文件
3. 编辑 `update.json`：
   - `versionCode`：必须大于 App 当前版本（当前为 1），每次发版 +1
   - `apkUrl`：填写 APK 的可直链地址（GitHub 建议用 `https://github.com/<用户名>/<仓库>/raw/main/<文件名>.apk`）
   - `changelog`：本次更新说明
   - `force`：`true` 时弹窗不可关闭（强制更新），平时 `false`
4. 在 App 源码 `app/src/main/java/com/landesheji/util/UpdateManager.kt` 中，
   把 `UPDATE_URL` 常量改成你的 JSON 地址，重新打包。

### 方式二：任意静态托管（OSS/云存储/CDN）

把 `update.json` 与 APK 传到任意可公网访问的静态服务器，同样修改 `UPDATE_URL`。

## update.json 字段说明

```json
{
  "versionCode": 2,            // 新版本整数代号（必须 > 当前 App 版本）
  "versionName": "2.0",       // 用户可见的版本名
  "apkUrl": "https://...apk", // APK 直链
  "changelog": "更新说明文本",
  "force": false              // 是否强制更新
}
```

## 发版流程建议

1. 开发完新功能后执行构建：`gradle :app:assembleDebug`（或 release）
2. 将 APK 上传，把 `backend/update.json` 的 versionCode +1 并填写新 URL/说明
3. 用户打开 App → 关于 → 检查更新 → 即可收到更新提示