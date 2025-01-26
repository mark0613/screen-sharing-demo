import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:socket_io_client/socket_io_client.dart' as IO;
import 'dart:async';
import 'utils.dart';

class ScreenSharePage extends StatefulWidget {
  const ScreenSharePage({super.key, required this.title});

  final String title;

  @override
  State<ScreenSharePage> createState() => _ScreenSharePageState();
}

class _ScreenSharePageState extends State<ScreenSharePage> {
  static const _methodChannel = MethodChannel('com.example.flutter_app/screen_control');
  static const _eventChannel = EventChannel('com.example.flutter_app/screen_stream');
  
  bool isSharing = false;
  late IO.Socket socket;
  String deviceId = '';
  String lastError = '';
  StreamSubscription<dynamic>? _screenSubscription;

  @override
  void initState() {
    super.initState();
    _initSocket();
    _initEventChannel();
  }

  @override
  void dispose() {
    _screenSubscription?.cancel();
    socket.dispose();
    super.dispose();
  }

  void _initSocket() async {
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

  void _initEventChannel() {
    _screenSubscription = _eventChannel.receiveBroadcastStream().listen(
      (dynamic event) {
        switch (event['type']) {
          case 'started':
            setState(() {
              isSharing = true;
              lastError = '';
            });
            break;
          case 'denied':
            setState(() {
              isSharing = false;
              lastError = '螢幕擷取權限被拒絕';
            });
            break;
          case 'stopped':
            setState(() {
              isSharing = false;
              lastError = '';
            });
            break;
          case 'error':
            setState(() {
              lastError = event['message'] ?? '未知錯誤';
              isSharing = false;
            });
            break;
          case 'screenData':
            if (socket.connected && deviceId.isNotEmpty) {
              socket.emit('screen-data', {
                'deviceId': deviceId,
                'frameData': event['imageBytes'],
              });
            }
            break;
        }
      }
    );
  }

  Future<void> _startScreenShare() async {
    try {
      await _methodChannel.invokeMethod('startScreenCapture');
    } on PlatformException catch (e) {
      setState(() {
        lastError = '無法啟動螢幕擷取';
        isSharing = false;
      });
    }
  }

  Future<void> _stopScreenShare() async {
    try {
      await _methodChannel.invokeMethod('stopScreenCapture');
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
