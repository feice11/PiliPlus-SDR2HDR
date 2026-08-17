# PiliPlus SDR2HDR

[![GPL-3.0](https://img.shields.io/github/license/feice11/PiliPlus-SDR2HDR)](LICENSE)
[![Latest release](https://img.shields.io/github/v/release/feice11/PiliPlus-SDR2HDR)](https://github.com/feice11/PiliPlus-SDR2HDR/releases/latest)
[![Android](https://img.shields.io/badge/platform-Android-3DDC84?logo=android&logoColor=white)](#compatibility)

An experimental Android build of [PiliPlus](https://github.com/bggRGjQaUbCoE/PiliPlus) with a real-time SDR-to-HDR rendering pipeline.

PiliPlus SDR2HDR converts SDR video from BT.709 into a BT.2020/PQ output surface using MediaKit, Android EGL, and OpenGL shaders. Native HDR video remains unmodified, and unsupported devices can fall back to standard SDR playback.

**[中文](#中文说明) · [Download](https://github.com/feice11/PiliPlus-SDR2HDR/releases/latest) · [Report a bug](https://github.com/feice11/PiliPlus-SDR2HDR/issues/new/choose) · [Roadmap](#roadmap)**

> [!IMPORTANT]
> HDR rendering is experimental and device-dependent. A system advertising HDR10 support does not guarantee that its GPU driver exposes the EGL extensions required by this project.

## Highlights

- Real-time SDR-to-HDR tone mapping during Android video playback
- Native HDR passthrough without applying a second conversion
- BT.709 linearization, inverse tone mapping, BT.2020 conversion, and PQ output
- Optional peak-brightness, strength, saturation, and highlight controls
- Runtime HDR capability checks with an explicit SDR fallback
- Android integration across Flutter, Kotlin, MediaKit, EGL, and OpenGL ES

## Rendering pipeline

```text
SDR video (BT.709)
        |
        v
MediaKit decoder -> SurfaceTexture -> OpenGL shader
                                      |
                    BT.709 -> linear light
                    inverse tone mapping
                    gamut mapping -> BT.2020
                    ST 2084 / PQ encoding
                                      |
                                      v
                          EGL BT.2020 PQ surface
```

Native HDR content bypasses the SDR expansion stage. If the device cannot create a BT.2020/PQ EGL surface, the player reports the limitation and offers SDR playback instead.

## Compatibility

| Requirement | Status |
| --- | --- |
| Platform | Android only for the HDR pipeline |
| Recommended OS | Android 10 (API 29) or newer |
| Display | HDR10/PQ-capable panel |
| Graphics stack | Working `EGL_EXT_gl_colorspace_bt2020_pq` support |
| Other platforms | PiliPlus remains usable; this HDR pipeline is not enabled |

Vendor firmware and GPU drivers vary. Please include the device model, Android version, GPU, app version, and relevant logs when reporting compatibility problems.

## Install

1. Download `app-release.apk` from the [latest release](https://github.com/feice11/PiliPlus-SDR2HDR/releases/latest).
2. Install the APK on an HDR-capable Android device.
3. Open **Settings -> Video settings -> HDR rendering (experimental)**.
4. Play SDR content and compare the result with the HDR toggle disabled.

Release assets include a SHA-256 checksum when available. Verify it before installing builds downloaded through mirrors or third parties.

## Settings

The default values are designed to produce a restrained result. Advanced controls are intended for testing and device-specific tuning:

- **Peak brightness:** target output brightness in nits (default: 1000)
- **Strength:** amount of SDR dynamic-range expansion (default: 1.0)
- **Saturation:** color expansion multiplier (default: 1.0)
- **Highlight enhancement:** emphasis applied to bright detail (default: 1.0)

Aggressive values can clip detail, distort color, increase power consumption, or expose driver-specific rendering problems.

## Build from source

The pinned toolchain is Flutter 3.38.6 (also recorded in `.fvmrc`).

```bash
flutter pub get
flutter build apk --release --no-shrink
```

The APK is written to `build/app/outputs/flutter-apk/app-release.apk`.

For reproducible release work, use the repository's GitHub Actions workflow. Signing credentials are not required for pull-request validation and must never be committed to the repository.

## Troubleshooting

### HDR is reported as unavailable

The display may support HDR10 while the graphics driver lacks a usable BT.2020/PQ EGL colorspace. Capture logs with:

```bash
adb logcat | grep -Ei "Hdr|EGL|MediaKit|VideoOutput"
```

### Switching video produces a black frame

Retry with HDR disabled and attach a short reproduction, device details, and filtered logs to a [bug report](https://github.com/feice11/PiliPlus-SDR2HDR/issues/new/choose).

### Playback or UI becomes less smooth

Real-time conversion adds GPU load. Disable HDR, reduce the display refresh rate, or disable danmaku to isolate the bottleneck.

## Roadmap

- Expand the tested-device compatibility matrix
- Improve EGL capability detection and fallback diagnostics
- Add regression tests for tone-mapping parameters and platform messages
- Reduce GPU cost and power consumption
- Automate build verification and release checksums

## Contributing and security

Bug reports, device compatibility results, documentation improvements, and focused patches are welcome. Read [CONTRIBUTING.md](CONTRIBUTING.md) before opening a pull request.

For vulnerabilities or reports containing sensitive information, follow [SECURITY.md](SECURITY.md) instead of opening a public issue.

## Upstream and license

This repository is an experimental derivative of [PiliPlus](https://github.com/bggRGjQaUbCoE/PiliPlus). PiliPlus provides the application foundation; this repository maintains the Android HDR rendering integration and related user controls. It is not affiliated with or endorsed by Bilibili.

Distributed under the [GNU General Public License v3.0](LICENSE), consistent with the upstream project. See the repository history for authorship of individual changes.

---

## 中文说明

PiliPlus SDR2HDR 是 [PiliPlus](https://github.com/bggRGjQaUbCoE/PiliPlus) 的 Android HDR 实验版本。它通过 MediaKit、Android EGL 与 OpenGL Shader，将 SDR 视频从 BT.709 实时映射到 BT.2020/PQ 输出；原生 HDR 内容保持直通，设备不支持时可回退到普通 SDR 播放。

### 使用条件

- 仅 Android 端启用此 HDR 渲染管线，建议 Android 10 及以上。
- 设备需要 HDR10/PQ 屏幕以及可用的 `EGL_EXT_gl_colorspace_bt2020_pq` 驱动扩展。
- 系统标注“支持 HDR”不代表 EGL 驱动一定兼容，实际支持情况取决于机型和固件。

### 快速使用

1. 从 [Releases](https://github.com/feice11/PiliPlus-SDR2HDR/releases/latest) 下载 APK。
2. 安装后进入 **设置 -> 视频设置 -> HDR 渲染（实验）**。
3. 播放 SDR 视频并通过开关对比效果。

遇到问题请使用 [Issue 模板](https://github.com/feice11/PiliPlus-SDR2HDR/issues/new/choose)，并附上机型、Android 版本、GPU、应用版本、复现步骤和相关日志。参与贡献前请阅读 [CONTRIBUTING.md](CONTRIBUTING.md)。
