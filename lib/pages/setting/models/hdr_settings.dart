import 'dart:async';

import 'package:PiliPlus/pages/setting/models/model.dart';
import 'package:PiliPlus/utils/storage.dart';
import 'package:PiliPlus/utils/storage_key.dart';
import 'package:PiliPlus/utils/storage_pref.dart';
import 'package:flutter/material.dart';
import 'package:get/get.dart';

List<SettingsModel> get hdrSettings => [
  NormalModel(
    title: 'HDR 渲染设置',
    leading: const Icon(Icons.hdr_on_outlined),
    subtitle: '仅 Android，页内直接调整',
    onTap: (context, setState) => Get.toNamed('/hdrSetting'),
  ),
  NormalModel(
    title: '启用 HDR 渲染（Android）',
    leading: const Icon(Icons.hdr_on_outlined),
    getSubtitle: () => Pref.enableHdrRenderAndroid ? '当前：开启' : '当前：关闭',
    onTap: (context, setState) => Get.toNamed('/hdrSetting'),
  ),
  NormalModel(
    title: '启用 HDR 转换参数（高级）',
    leading: const Icon(Icons.tune),
    getSubtitle: () => Pref.enableHdrToneMapCustom ? '当前：开启' : '当前：关闭',
    onTap: (context, setState) => Get.toNamed('/hdrSetting'),
  ),
  NormalModel(
    title: '默认动态范围扩展（新视频生效）',
    leading: const Icon(Icons.light_mode_outlined),
    getSubtitle: () =>
        '当前：${(Pref.hdrToneMapDefaultDynamicRange.clamp(0.0, 1.0) * 100).round()}%',
    onTap: (context, setState) => Get.toNamed('/hdrSetting'),
  ),
];

class HdrSettingsPanel extends StatefulWidget {
  const HdrSettingsPanel({super.key});

  @override
  State<HdrSettingsPanel> createState() => _HdrSettingsPanelState();
}

class _HdrSettingsPanelState extends State<HdrSettingsPanel> {
  late bool _enableRender;
  late bool _enableCustom;
  late double _peak;
  late double _strength;
  late double _saturation;
  late double _highlight;
  late double _highlightProtect;

  @override
  void initState() {
    super.initState();
    _enableRender = Pref.enableHdrRenderAndroid;
    _enableCustom = Pref.enableHdrToneMapCustom;
    _peak = Pref.hdrToneMapPeakNits;
    _strength = Pref.hdrToneMapStrength;
    _saturation = Pref.hdrToneMapSaturation;
    _highlight = Pref.hdrToneMapHighlightBoost;
    _highlightProtect = Pref.hdrToneMapDefaultDynamicRange
        .clamp(0.0, 1.0)
        .toDouble();
  }

  Future<void> _saveBool(String key, bool value) async {
    await GStorage.setting.put(key, value);
  }

  Future<void> _saveDouble(String key, double value) async {
    await GStorage.setting.put(key, value);
  }

  Widget _buildSlider({
    required String title,
    required double value,
    required double min,
    required double max,
    required int divisions,
    required String Function(double) format,
    required ValueChanged<double> onChanged,
  }) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Padding(
          padding: const EdgeInsets.fromLTRB(16, 12, 16, 0),
          child: Text('$title：${format(value)}'),
        ),
        Slider(
          value: value.clamp(min, max),
          min: min,
          max: max,
          divisions: divisions,
          onChanged: onChanged,
        ),
      ],
    );
  }

  @override
  Widget build(BuildContext context) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        SwitchListTile(
          title: const Text('启用 HDR 渲染（Android）'),
          subtitle: const Text('仅 Android，需 HDR 显示支持；不支持可降级 SDR'),
          value: _enableRender,
          onChanged: (value) {
            setState(() => _enableRender = value);
            unawaited(_saveBool(SettingBoxKey.enableHdrRenderAndroid, value));
          },
        ),
        SwitchListTile(
          title: const Text('启用 HDR 转换参数（高级）'),
          subtitle: const Text('不建议更改，修改不当可能导致偏色/色带'),
          value: _enableCustom,
          onChanged: (value) {
            setState(() => _enableCustom = value);
            unawaited(_saveBool(SettingBoxKey.enableHdrToneMapCustom, value));
          },
        ),
        _buildSlider(
          title: '默认动态范围扩展（新视频生效）',
          value: _highlightProtect,
          min: 0.0,
          max: 1.0,
          divisions: 100,
          format: (v) => '${(v * 100).round()}%',
          onChanged: (value) {
            setState(() => _highlightProtect = value);
            unawaited(
              _saveDouble(
                SettingBoxKey.hdrToneMapDefaultHighlightProtect,
                value,
              ),
            );
          },
        ),
        if (_enableCustom) ...[
          _buildSlider(
            title: '峰值亮度（nits）',
            value: _peak,
            min: 300,
            max: 4000,
            divisions: 37,
            format: (v) => v.toStringAsFixed(0),
            onChanged: (value) {
              setState(() => _peak = value);
              unawaited(_saveDouble(SettingBoxKey.hdrToneMapPeakNits, value));
            },
          ),
          _buildSlider(
            title: '强度',
            value: _strength,
            min: 0.0,
            max: 1.2,
            divisions: 24,
            format: (v) => v.toStringAsFixed(2),
            onChanged: (value) {
              setState(() => _strength = value);
              unawaited(_saveDouble(SettingBoxKey.hdrToneMapStrength, value));
            },
          ),
          _buildSlider(
            title: '饱和度',
            value: _saturation,
            min: 0.6,
            max: 1.2,
            divisions: 30,
            format: (v) => v.toStringAsFixed(2),
            onChanged: (value) {
              setState(() => _saturation = value);
              unawaited(_saveDouble(SettingBoxKey.hdrToneMapSaturation, value));
            },
          ),
          _buildSlider(
            title: '高光增强',
            value: _highlight,
            min: 0.5,
            max: 2.0,
            divisions: 30,
            format: (v) => v.toStringAsFixed(2),
            onChanged: (value) {
              setState(() => _highlight = value);
              unawaited(
                _saveDouble(SettingBoxKey.hdrToneMapHighlightBoost, value),
              );
            },
          ),
        ],
        Padding(
          padding: const EdgeInsets.fromLTRB(16, 8, 16, 16),
          child: Text(
            '不建议更改高级参数，可能导致偏色/色带。',
            style: TextStyle(
              fontSize: 12,
              color: Theme.of(context).colorScheme.outline,
            ),
          ),
        ),
      ],
    );
  }
}
