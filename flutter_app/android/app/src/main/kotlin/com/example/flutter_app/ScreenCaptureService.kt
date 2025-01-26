package com.example.flutter_app

import android.app.Activity
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.IBinder
import android.util.Base64
import android.util.DisplayMetrics
import android.util.Log
import android.view.WindowManager
import io.flutter.plugin.common.MethodChannel
import java.io.ByteArrayOutputStream
import java.util.Timer
import java.util.TimerTask

class ScreenCaptureService : Service() {
    private val TAG = "ScreenCaptureService"
    private val NOTIFICATION_ID = 1
    private val CHANNEL_ID = "ScreenCapture"
    private var mediaProjection: MediaProjection? = null
    private var virtualDisplay: VirtualDisplay? = null
    private var imageReader: ImageReader? = null
    private var timer: Timer? = null
    private var displayWidth: Int = 0
    private var displayHeight: Int = 0
    private var displayDensity: Int = 0

    companion object {
        var methodChannel: MethodChannel? = null
    }

    override fun onCreate() {
        super.onCreate()
        val metrics = DisplayMetrics()
        val windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        windowManager.defaultDisplay.getMetrics(metrics)
        displayWidth = metrics.widthPixels
        displayHeight = metrics.heightPixels
        displayDensity = metrics.densityDpi

        createNotificationChannel()
        startForeground(NOTIFICATION_ID, createNotification())
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent == null) return START_NOT_STICKY

        val resultCode = intent.getIntExtra("resultCode", Activity.RESULT_OK)
        val data = intent.getParcelableExtra<Intent>("data")!!
        
        val projectionManager = getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        mediaProjection = projectionManager.getMediaProjection(resultCode, data)
        mediaProjection?.registerCallback(object : MediaProjection.Callback() {
            override fun onStop() {
                virtualDisplay?.release()
                imageReader?.close()
                timer?.cancel()
                stopSelf()
            }
        }, null)
        setupVirtualDisplay()
        startCapturing()

        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        timer?.cancel()
        virtualDisplay?.release()
        imageReader?.close()
        mediaProjection?.stop()
        stopForeground(true)
        super.onDestroy()
    }

    private fun setupVirtualDisplay() {
        imageReader = ImageReader.newInstance(
            displayWidth, displayHeight,
            PixelFormat.RGBA_8888, 2
        )

        virtualDisplay = mediaProjection?.createVirtualDisplay(
            "ScreenCapture",
            displayWidth, displayHeight, displayDensity,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
            imageReader?.surface, null, null
        )
    }

    private fun startCapturing() {
        timer = Timer()
        timer?.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                captureScreen()
            }
        }, 0, 100)  // TODO: modify the interval
    }

    private fun captureScreen() {
        try {
            val image = imageReader?.acquireLatestImage()
            image?.use { 
                val planes = it.planes
                val buffer = planes[0].buffer
                val pixelStride = planes[0].pixelStride
                val rowStride = planes[0].rowStride
                val rowPadding = rowStride - pixelStride * displayWidth

                val bitmap = Bitmap.createBitmap(
                    displayWidth + rowPadding / pixelStride,
                    displayHeight, Bitmap.Config.ARGB_8888
                )
                bitmap.copyPixelsFromBuffer(buffer)

                val outputStream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.WEBP, 70, outputStream)
                val imageBytes = outputStream.toByteArray()

                android.os.Handler(android.os.Looper.getMainLooper()).post {
                    methodChannel?.invokeMethod("onScreenData", mapOf(
                        "imageBytes" to imageBytes
                    ))
                }

                bitmap.recycle()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error capturing screen", e)
        }
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Screen Capture Service",
            NotificationManager.IMPORTANCE_DEFAULT
        )
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }

    private fun createNotification(): Notification {
        return Notification.Builder(this, CHANNEL_ID)
            .setContentTitle("Screen Capture Service")
            .setContentText("Recording your screen...")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .build()
    }
}
