import 'package:PiliPlus/pages/setting/models/hdr_settings.dart';
import 'package:flutter/material.dart';

class HdrSetting extends StatelessWidget {
  const HdrSetting({super.key, this.showAppBar = true});

  final bool showAppBar;

  @override
  Widget build(BuildContext context) {
    final padding = MediaQuery.viewPaddingOf(context);
    return Scaffold(
      resizeToAvoidBottomInset: false,
      appBar: showAppBar ? AppBar(title: const Text('HDR 渲染设置')) : null,
      body: ListView(
        padding: EdgeInsets.only(
          left: showAppBar ? padding.left : 0,
          right: showAppBar ? padding.right : 0,
          bottom: padding.bottom + 100,
        ),
        children: const [
          HdrSettingsPanel(),
        ],
      ),
    );
  }
}
