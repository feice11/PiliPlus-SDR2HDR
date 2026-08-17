# Contributing to PiliPlus SDR2HDR

Thanks for helping improve the Android HDR experiment. Device compatibility reports, focused bug fixes, tests, and documentation corrections are especially valuable.

## Before opening an issue

1. Search the existing issues and test the latest release.
2. Confirm whether the problem disappears when HDR rendering is disabled.
3. Record the app version, device model, Android version, GPU, display mode, and reproduction steps.
4. Remove account tokens, cookies, personal identifiers, and private media URLs from logs.

Use the repository's bug-report form for defects and the feature-request form for proposals. Security-sensitive reports belong in the process described by [SECURITY.md](SECURITY.md).

## Development setup

The project pins Flutter 3.38.6 in `.fvmrc`.

```bash
flutter pub get
flutter analyze
flutter build apk --debug
```

The HDR implementation spans these areas:

- `hdr_player/lib/`: Flutter-facing controller and platform API
- `hdr_player/android/`: EGL surface, rendering thread, and OpenGL shader code
- `lib/plugin/pl_player/`: player lifecycle and HDR/SDR switching
- `lib/pages/setting/`: user-facing HDR controls

## Pull requests

- Keep changes focused and explain the device or rendering behavior they affect.
- Do not combine unrelated formatting or upstream synchronization with an HDR fix.
- Include manual test results and identify the tested device/GPU.
- Add or update documentation when behavior, defaults, or compatibility changes.
- Never commit signing keys, credentials, cookies, access tokens, or personal logs.
- Preserve the existing GPL-3.0 licensing and upstream attribution.

For shader or tone-mapping changes, describe the input content, output display mode, expected visual effect, and any observed clipping, banding, color shift, frame drops, or power impact.

## Commit style

Short conventional prefixes make the history easier to scan:

- `feat(android): ...`
- `fix(hdr): ...`
- `perf(shader): ...`
- `docs: ...`
- `ci: ...`

By contributing, you agree that your contribution is licensed under the repository's GPL-3.0 license.
