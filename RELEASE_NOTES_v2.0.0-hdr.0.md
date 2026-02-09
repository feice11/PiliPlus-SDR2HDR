# PiliPlus SDR2HDR v2.0.0-hdr.0 (Major Upgrade)

MAJOR UPGRADE: HDR 动态范围扩展重构，移除映射前降亮度入口。

## Highlights
- 移除播放控件中的“映射前降亮度”滑条与会话调节。
- 移除 HDR 设置页中的“默认映射前降亮度（新视频生效）”。
- HDR 参数链保持兼容，`preDarken` 固定为 `0.0`，避免旧版本联动回归。
- 动态范围扩展成为唯一 HDR 观感调节入口，保持实时预览与实时生效。
- 保持现有 HDR 主算法与画质策略，不做分辨率/帧率降级。

## 观感提升（本次重点）
- 在 `preDarken=0` 的前提下，动态范围扩展拉高后，高光亮度提升更明显，不再依赖“先整体压暗”才能保细节。
- 天空、云层、灯牌、白墙等高亮区域层次更容易看清，亮处纹理更完整，不再容易糊成一片白。
- 中亮到高亮过渡更通透，画面“更亮但不炸白”，主观清晰度和立体感明显提升。
- 与早期“硬拉亮度”不同：本次是亮度域扩展 + shoulder 保护，目标是增加动态范围而不是单纯把整帧提亮。

## Compatibility
- 保留历史存储键 `hdrToneMapDefaultPreDarken`（仅兼容，不再生效）。
- MethodChannel `SetToneMapOptions` 仍可接收 `preDarken` 字段。

## Notes
- 平台聚焦 Android HDR 渲染路径。
- 旧版本用户升级后无需迁移操作。
