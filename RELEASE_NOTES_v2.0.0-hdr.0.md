# PiliPlus SDR2HDR v2.0.0-hdr.0 (Major Upgrade)

MAJOR UPGRADE: HDR 动态范围扩展重构，移除映射前降亮度入口。

## Highlights
- 移除播放控件中的“映射前降亮度”滑条与会话调节。
- 移除 HDR 设置页中的“默认映射前降亮度（新视频生效）”。
- HDR 参数链保持兼容，`preDarken` 固定为 `0.0`，避免旧版本联动回归。
- 动态范围扩展成为唯一 HDR 观感调节入口，保持实时预览。
- 保持现有 HDR 主算法与画质策略，不做分辨率/帧率降级。

## Compatibility
- 保留历史存储键 `hdrToneMapDefaultPreDarken`（仅兼容，不再生效）。
- MethodChannel `SetToneMapOptions` 仍可接收 `preDarken` 字段。

## Notes
- 平台聚焦 Android HDR 渲染路径。
- 旧版本用户升级后无需迁移操作。
