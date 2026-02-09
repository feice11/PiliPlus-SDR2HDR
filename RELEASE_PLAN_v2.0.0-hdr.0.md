# Release Plan: v2.0.0-hdr.0 (Major Upgrade)

## Major Upgrade
MAJOR UPGRADE: HDR 动态范围扩展重构，移除映射前降亮度入口。

## Scope
- 移除播放控件中的“映射前降亮度”入口与会话调节。
- 移除设置页中的“默认映射前降亮度（新视频生效）”。
- `preDarken` 在运行时固定为 `0.0`（保留参数链兼容）。
- 保留并强化“动态范围扩展”作为唯一 HDR 观感调节项。
- 版本升级到 `2.0.0+2`，发布标签 `v2.0.0-hdr.0`。

## Validation Checklist
1. 播放控件仅显示“动态范围扩展”。
2. HDR 设置页仅显示“默认动态范围扩展（新视频生效）”及高级参数。
3. `SetToneMapOptions` 仍下发 `preDarken`，且值恒为 `0.0`。
4. `flutter build apk --release --no-shrink` 成功，产物存在。
5. 安装启动正常，HDR 渲染/切换逻辑无回退。

## Release Steps
1. `git add -A`
2. `git commit -m "feat(android): major hdr upgrade remove pre-darken controls"`
3. `git push origin main`
4. `flutter build apk --release --no-shrink`
5. 生成 `app-release.apk.sha256`
6. 创建 GitHub Release:
- Tag: `v2.0.0-hdr.0`
- Title: `PiliPlus SDR2HDR v2.0.0-hdr.0 (Major Upgrade)`
- `prerelease=false`
- 资产: `app-release.apk`, `app-release.apk.sha256`

## Rollback
- 若发现回归，回退到 `v1.1.6-hdr.1`。
