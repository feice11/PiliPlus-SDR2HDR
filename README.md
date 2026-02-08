# PiliPlus-SDR2HDR (Android HDR Build)

> **目标**：在 PiliPlus（Android）中启用 **SDR→HDR 实时播放**，并保留原生 HDR 直通。此仓库用于发布带 HDR 渲染能力的 APK 与详细文档。

---

## 目录
- [简介](#简介)
- [功能一览](#功能一览)
- [适用范围](#适用范围)
- [快速安装](#快速安装)
- [设置与开关](#设置与开关)
- [HDR 渲染管线概述](#hdr-渲染管线概述)
- [性能与流畅度建议](#性能与流畅度建议)
- [常见问题](#常见问题)
- [构建说明](#构建说明)
- [调试与日志](#调试与日志)
- [已知限制](#已知限制)
- [路线图](#路线图)
- [致谢](#致谢)
- [许可证](#许可证)

---

## 简介
本仓库是 **PiliPlus 的 HDR 实验构建**：
- Android 端播放 SDR 内容时，通过 OpenGL 实时进行 SDR→HDR 映射；
- HDR 内容保持 **直通（不做转换）**；
- 提供 HDR 开关与高级参数（非推荐）。

> 适合用于 **HDR 设备** 上提升 SDR 观看体验。功能仍处于实验状态。

---

## 功能一览
- **HDR 开关（Android-only）**：设置中可开启/关闭 HDR 渲染。
- **SDR→HDR 实时播放**：基于 MediaKit + OpenGL 的管线。
- **HDR 直通**：HDR 内容不做转换，直接播放。
- **高级调参**（可选）：峰值亮度 / 强度 / 饱和度 / 高光增强。
- **降级策略**：HDR 不可用时，**每次提示**是否回退 SDR。

---

## 适用范围
- **仅 Android**（Flutter 多平台项目中，非 Android 不受影响）。
- 需要 HDR 显示硬件支持（HDR10 / PQ）。
- 建议 Android 10+（API 29+）。

---

## 快速安装
1. 打开 Releases 页面下载 `app-release.apk`
2. 安装后进入设置 → 视频设置 → 打开 **HDR 渲染（实验）**
3. 播放 SDR 视频观察亮度与高光变化

---

## 设置与开关
### HDR 渲染（实验）
- **路径**：设置 → 视频设置
- **说明**：Android 专用。开启后，SDR 视频走 HDR 渲染；HDR 视频直通。

### 高级 HDR 转换参数（不建议修改）
- **峰值亮度 (nits)**：调高会提升整体亮度（默认 1000）
- **强度**：SDR→HDR 映射强度（默认 1.0）
- **饱和度**：控制颜色扩展（默认 1.0）
- **高光增强**：更强的高光强调（默认 1.0）

> 提醒：过高参数可能引发色彩偏色或细节丢失。

---

## HDR 渲染管线概述
- **解码播放**：media_kit 负责解复用与音频同步
- **渲染输出**：SurfaceView + EGL HDR colorspace
- **OpenGL Shader**：
  1. BT.709 → 线性
  2. 逆色调映射（SDR→HDR）
  3. BT.2020 + PQ 输出
- HDR 内容 **不走转换**，直通播放

---

## 性能与流畅度建议
HDR 实时渲染会增加 GPU 负载，可能导致评论区/推荐页面掉帧：
- 已做 **UI 更新节流**（进度/缓冲更新频率降低）
- 如果仍掉帧，可考虑：
  - 关闭 HDR
  - 降低显示刷新率
  - 关闭弹幕

---

## 常见问题
### 1. 为什么提示 HDR 不可用？
通常是 **EGL HDR colorspace 不支持** 或设备驱动限制导致。
即使系统检测 HDR10 支持，也可能缺少 `EGL_EXT_gl_colorspace_bt2020_pq` 扩展。

### 2. SDR 播放效果不明显？
检查：
- 是否开启 HDR 渲染开关
- 参数强度/峰值亮度是否被调低
- 设备是否处于 HDR 省电模式

### 3. 切换视频黑屏？
已做修复（重建播放控制器与 Surface 绑定），如仍复现请提交日志。

---

## 构建说明
**Flutter 版本**：3.38.6

```bash
# 进入项目
cd PiliPlus

# 拉依赖
D:\flutter\3.38.6\flutter\bin\flutter.bat pub get

# 生成 release APK
D:\flutter\3.38.6\flutter\bin\flutter.bat build apk --release --no-shrink
```

输出路径：
```
PiliPlus/build/app/outputs/flutter-apk/app-release.apk
```

---

## 调试与日志
- 建议使用 `adb logcat` 过滤关键字：
  - `Hdr`, `EGL`, `MediaKit`, `VideoOutput`

---

## 已知限制
- HDR 渲染为实验功能，不保证所有机型可用
- 部分机型会出现 HDR 可用检测不一致
- 多任务或高负载情况下 UI 可能掉帧

---

## 路线图
- 改善 HDR 检测兼容性
- 优化 HDR 渲染性能（降低功耗）
- 加入自动调参模式

---

## 致谢
- 原项目：**PiliPlus**
- HDR 渲染与媒体播放基础：media_kit / OpenGL / Android EGL

---

## 许可证
本仓库遵循 **GPL-3.0**（与上游一致）。

