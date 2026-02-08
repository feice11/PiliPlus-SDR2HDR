// ignore_for_file: non_constant_identifier_names

import 'package:PiliPlus/common/widgets/pair.dart';
import 'package:PiliPlus/models/common/sponsor_block/segment_type.dart';
import 'package:PiliPlus/models/common/sponsor_block/skip_type.dart';

class SegmentModel {
  SegmentModel({
    required this.UUID,
    required this.segmentType,
    required this.segment,
    required this.skipType,
  });
  String UUID;
  SegmentType segmentType;
  Pair<int, int> segment;
  SkipType skipType;
  late bool hasSkipped = false;
}
