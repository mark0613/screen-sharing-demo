package com.example.flutter_app

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.os.Bundle
import android.util.Log
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel

class MainActivity : FlutterActivity() {
    private val TAG = "MainActivity"
    private val CHANNEL = "com.example.flutter_app/screen_capture"
    private val PERMISSION_CODE = 1001
    private var methodChannel: MethodChannel? = null
    private var resultCallback: MethodChannel.Result? = null

    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)
        
        methodChannel = MethodChannel(flutterEngine.dartExecutor.binaryMessenger, CHANNEL)
        ScreenCaptureService.methodChannel = methodChannel
        
        methodChannel?.setMethodCallHandler { call, result ->
            when (call.method) {
                "startScreenCapture" -> {
                    resultCallback = result
                    startScreenCapture()
                }
                "stopScreenCapture" -> {
                    stopScreenCapture()
                    result.success(null)
                }
                else -> {
                    result.notImplemented()
                }
            }
        }
    }

    override fun onDestroy() {
        stopScreenCapture()
        super.onDestroy()
    }

    private fun startScreenCapture() {
        val mediaProjectionManager = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        startActivityForResult(
            mediaProjectionManager.createScreenCaptureIntent(),
            PERMISSION_CODE
        )
    }

    private fun stopScreenCapture() {
        val serviceIntent = Intent(this, ScreenCaptureService::class.java)
        stopService(serviceIntent)
        methodChannel?.invokeMethod("onScreenCaptureStopped", null)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        
        if (requestCode == PERMISSION_CODE) {
            if (resultCode == Activity.RESULT_OK && data != null) {
                val serviceIntent = Intent(this, ScreenCaptureService::class.java).apply {
                    putExtra("resultCode", resultCode)
                    putExtra("data", data)
                }
                startForegroundService(serviceIntent)
                methodChannel?.invokeMethod("onScreenCaptureStarted", null)
                resultCallback?.success(null)
            } else {
                methodChannel?.invokeMethod("onScreenCaptureDenied", null)
                resultCallback?.error(
                    "PERMISSION_DENIED",
                    "Screen capture permission denied",
                    null
                )
            }
            resultCallback = null
        }
    }
}
