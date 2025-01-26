import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:socket_io_client/socket_io_client.dart' as IO;
import 'utils.dart';

class ScreenSharePage extends StatefulWidget {
  const ScreenSharePage({super.key, required this.title});

  final String title;

  @override
  State<ScreenSharePage> createState() => _ScreenSharePageState();
}

class _ScreenSharePageState extends State<ScreenSharePage> {
  static const platform =
      MethodChannel('com.example.flutter_app/screen_capture');
  bool isSharing = false;
  late IO.Socket socket;
  String deviceId = '';
  String lastError = '';

  @override
  void initState() {
    super.initState();
    _initSocket();
    _setupMethodChannel();
  }

  @override
  void dispose() {
    socket.dispose();
    super.dispose();
  }

  void _initSocket() async {
    // HACK: hardcode the server IP address
    socket = IO.io('http://10.0.2.2:3000', <String, dynamic>{
      'transports': ['websocket'],
      'autoConnect': true,
    });

    socket.onConnect((_) async {
      setState(() {
        deviceId = socket.id ?? '';
      });

      socket.emit('register-device', await getDevicesInfo());
    });

    socket.onDisconnect((_) {
      setState(() {
        deviceId = '';
      });
    });

    socket.onConnectError((err) {
      setState(() {
        lastError = '伺服器連線失敗';
      });
    });
  }

  void _setupMethodChannel() {
    platform.setMethodCallHandler((call) async {
      switch (call.method) {
        case 'onScreenCaptureStarted':
          setState(() {
            isSharing = true;
            lastError = '';
          });
          break;
        case 'onScreenCaptureDenied':
          setState(() {
            isSharing = false;
            lastError = '螢幕擷取權限被拒絕';
          });
          break;
        case 'onScreenData':
          if (socket.connected && deviceId.isNotEmpty) {
            final String base64Image = call.arguments['imageData'];
            socket.emit('screen-data', {
              'deviceId': deviceId,
              'frameData': 'data:image/jpeg;base64,$base64Image',
            });
          }
          break;
      }
    });
  }

  Future<void> _startScreenShare() async {
    try {
      await platform.invokeMethod('startScreenCapture');
    } on PlatformException catch (e) {
      setState(() {
        lastError = '無法啟動螢幕擷取';
        isSharing = false;
      });
    }
  }

  Future<void> _stopScreenShare() async {
    try {
      await platform.invokeMethod('stopScreenCapture');
      setState(() {
        isSharing = false;
        lastError = '';
      });
    } on PlatformException catch (e) {
      setState(() {
        lastError = '無法停止螢幕擷取';
      });
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        backgroundColor: Theme.of(context).colorScheme.inversePrimary,
        title: Text(widget.title),
      ),
      body: Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: <Widget>[
            Text(
              isSharing ? '正在分享螢幕...' : '螢幕分享已停止',
              style: Theme.of(context).textTheme.headlineMedium,
            ),
            if (lastError.isNotEmpty) ...[
              const SizedBox(height: 10),
              Text(
                lastError,
                style: const TextStyle(color: Colors.red),
              ),
            ],
            const SizedBox(height: 20),
            ElevatedButton(
              onPressed: isSharing ? _stopScreenShare : _startScreenShare,
              child: Text(isSharing ? '停止分享' : '開始分享'),
            ),
          ],
        ),
      ),
    );
  }
}
