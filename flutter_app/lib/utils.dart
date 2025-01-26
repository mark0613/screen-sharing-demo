import 'package:device_info_plus/device_info_plus.dart';
import 'dart:io';

Future<Map<String, String>> getDevicesInfo() async {
  final deviceInfoPlugin = DeviceInfoPlugin();
  String model = 'Unknown';
  String osVersion = 'Unknown';
  String platform = Platform.operatingSystem;

  if (Platform.isAndroid) {
    AndroidDeviceInfo androidInfo = await deviceInfoPlugin.androidInfo;
    model = androidInfo.model ?? 'Unknown';
    osVersion = androidInfo.version.release ?? 'Unknown';
  } else if (Platform.isIOS) {
    IosDeviceInfo iosInfo = await deviceInfoPlugin.iosInfo;
    model = iosInfo.utsname.machine ?? 'Unknown';
    osVersion = iosInfo.systemVersion ?? 'Unknown';
  }

  return {
    'platform': platform,
    'model': model,
    'osVersion': osVersion,
  };
}
