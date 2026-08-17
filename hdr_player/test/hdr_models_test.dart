import 'package:flutter_test/flutter_test.dart';
import 'package:hdr_player/hdr_player.dart';

void main() {
  group('ToneMapOptions', () {
    test('serializes stable defaults for the Android platform channel', () {
      expect(
        const ToneMapOptions().toMap(),
        <String, dynamic>{
          'targetPeakNits': 1000.0,
          'strength': 1.0,
          'saturation': 1.0,
          'highlightBoost': 1.0,
          'preDarken': 0.0,
          'highlightProtect': 0.65,
        },
      );
    });

    test('preserves custom values across the platform boundary', () {
      const options = ToneMapOptions(
        targetPeakNits: 1500.0,
        strength: 0.8,
        saturation: 1.1,
        highlightBoost: 1.2,
        preDarken: 0.05,
        highlightProtect: 0.75,
      );

      expect(options.toMap(), <String, dynamic>{
        'targetPeakNits': 1500.0,
        'strength': 0.8,
        'saturation': 1.1,
        'highlightBoost': 1.2,
        'preDarken': 0.05,
        'highlightProtect': 0.75,
      });
    });
  });

  group('HdrSupportResult', () {
    test('parses Android platform-channel values', () {
      final result = HdrSupportResult.fromMap(<String, dynamic>{
        'displaySupported': true,
        'eglSupported': true,
        'reason': 'supported',
        'hdrTypes': <int>[2, 3],
      });

      expect(result.displaySupported, isTrue);
      expect(result.eglSupported, isTrue);
      expect(result.reason, 'supported');
      expect(result.hdrTypes, <int>[2, 3]);
      expect(result.isSupported, isTrue);
      expect(result.isEglSupported, isTrue);
    });

    test('uses safe defaults for missing or loosely typed values', () {
      final result = HdrSupportResult.fromMap(<String, dynamic>{
        'displaySupported': 'yes',
        'eglSupported': null,
        'hdrTypes': <Object>[1, 'invalid', 4],
      });

      expect(result.displaySupported, isFalse);
      expect(result.eglSupported, isFalse);
      expect(result.reason, isEmpty);
      expect(result.hdrTypes, <int>[1, 4]);
      expect(result.isSupported, isFalse);
      expect(result.isEglSupported, isFalse);
    });
  });
}
