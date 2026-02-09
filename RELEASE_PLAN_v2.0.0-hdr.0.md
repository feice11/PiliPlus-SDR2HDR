# Release Plan: v2.0.0-hdr.0 (Major Upgrade)

## Major Upgrade
MAJOR UPGRADE: HDR 动态范围扩展重构，移除映射前降亮度入口。

## Scope
- 移除播放控件中的“映射前降亮度”入口与会话调节。
- 移除设置页中的“默认映射前降亮度（新视频生效）”。
- `preDarken` 在运行时固定为 `0.0`（保留参数链兼容）。
- 保留并强化“动态范围扩展”作为唯一 HDR 观感调节项。
- 版本升级到 `2.0.0+2`，发布标签 `v2.0.0-hdr.0`。

## Upgrade Impact (Visual)
- 目标不是“整体变暗再保细节”，而是直接在 `preDarken=0` 下提升高光可用动态范围。
- 高光区域（天空/灯牌/白色反光）应表现为更亮且更有层次，细节可辨性明显提升。
- 过渡区域（中亮到高亮）更通透，减少“发灰”或“一片白”两端问题。
- 该提升来自曲线重构与保护段重分配，不是分辨率、帧率或精度降级换来的。

## Validation Checklist
1. 播放控件仅显示“动态范围扩展”。
2. HDR 设置页仅显示“默认动态范围扩展（新视频生效）”及高级参数。
3. `SetToneMapOptions` 仍下发 `preDarken`，且值恒为 `0.0`。
4. `flutter build apk --release --no-shrink` 成功，产物存在。
5. 安装启动正常，HDR 渲染/切换逻辑无回退。
6. 同片段对比中，动态范围扩展从低到高可看到高光更亮且细节更清晰，而不是整帧一同变亮/变暗。

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
