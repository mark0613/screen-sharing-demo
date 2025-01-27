package com.example.rne_app

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.projection.MediaProjectionManager
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.facebook.react.bridge.*
import com.facebook.react.modules.core.DeviceEventManagerModule

class ScreenCaptureModule(
  reactContext: ReactApplicationContext
) : ReactContextBaseJavaModule(reactContext), ActivityEventListener {

  companion object {
    private const val MODULE_NAME = "ScreenCapture"
    private const val PERMISSION_CODE = 1001
    private const val TAG = "ScreenCaptureModule"
    private const val SCREEN_CAPTURE_EVENT = "SCREEN_CAPTURE_EVENT"
  }

  private var permissionPromise: Promise? = null
  private var broadcastReceiver: BroadcastReceiver? = null

  init {
    reactContext.addActivityEventListener(this)
    setupBroadcastReceiver()
  }

  private fun setupBroadcastReceiver() {
    broadcastReceiver = object : BroadcastReceiver() {
      override fun onReceive(context: Context, intent: Intent) {
        try {
          val eventName = intent.getStringExtra("eventName") as String
          val bundle = intent.getBundleExtra("params")
          val params = Arguments.fromBundle(bundle)
          sendEvent(eventName, params)
        } catch (e: Exception) {
          Log.e(TAG, "Error handling broadcast: ${e.message}")
        }
      }
    }

    LocalBroadcastManager.getInstance(reactApplicationContext).registerReceiver(
      broadcastReceiver!!,
      IntentFilter(SCREEN_CAPTURE_EVENT)
    )
  }

  override fun getName(): String = MODULE_NAME

  override fun onActivityResult(activity: Activity, requestCode: Int, resultCode: Int, data: Intent?) {
    if (requestCode == PERMISSION_CODE) {
      handleProjectionResult(resultCode, data)
    }
  }

  override fun onNewIntent(intent: Intent?) {
    // Not used but required by ActivityEventListener
  }

  private fun handleProjectionResult(resultCode: Int, data: Intent?) {
    try {
      when {
        resultCode == Activity.RESULT_OK && data != null -> {
          val currentActivity = currentActivity
          if (currentActivity != null) {
            val serviceIntent = Intent(currentActivity, ScreenCaptureService::class.java).apply {
              putExtra("resultCode", resultCode)
              putExtra("data", data)
            }

            currentActivity.startForegroundService(serviceIntent)

            sendEvent("screenCaptureEvent", Arguments.createMap().apply {
              putString("type", "started")
            })
            permissionPromise?.resolve(null)
          } else {
            throw IllegalStateException("Activity is not available")
          }
        }
        resultCode == Activity.RESULT_CANCELED -> {
          sendEvent("screenCaptureEvent", Arguments.createMap().apply {
            putString("type", "denied")
            putString("reason", "User denied permission")
          })
          permissionPromise?.reject("PERMISSION_DENIED", "Screen capture permission denied by user")
        }
        else -> {
          sendEvent("screenCaptureEvent", Arguments.createMap().apply {
            putString("type", "error")
            putString("message", "Failed to get screen capture permission")
          })
          permissionPromise?.reject("PERMISSION_ERROR", "Failed to get screen capture permission")
        }
      }
    } catch (e: Exception) {
      val errorMessage = "Error handling permission result: ${e.message}"
      sendEvent("screenCaptureEvent", Arguments.createMap().apply {
        putString("type", "error")
        putString("message", errorMessage)
      })
      permissionPromise?.reject("PERMISSION_ERROR", errorMessage, e)
    } finally {
      permissionPromise = null
    }
  }

  override fun getConstants(): Map<String, Any> {
    return mapOf("PERMISSION_CODE" to PERMISSION_CODE)
  }

  override fun invalidate() {
    super.invalidate()
    broadcastReceiver?.let {
      LocalBroadcastManager.getInstance(reactApplicationContext)
        .unregisterReceiver(it)
    }
  }

  @ReactMethod
  fun addListener(eventName: String) {
    // Required for RN event emitter
  }

  @ReactMethod
  fun removeListeners(count: Int) {
    // Required for RN event emitter
  }

  @ReactMethod
  fun startScreenCapture(promise: Promise) {
    val currentActivity = currentActivity
    if (currentActivity == null) {
      promise.reject("ERR_ACTIVITY_NOT_FOUND", "Activity is not available")
      return
    }
    permissionPromise = promise
    val mediaProjectionManager = currentActivity.getSystemService(
      Context.MEDIA_PROJECTION_SERVICE
    ) as MediaProjectionManager
    val permissionIntent = mediaProjectionManager.createScreenCaptureIntent()
    currentActivity.startActivityForResult(permissionIntent, PERMISSION_CODE)
  }

  @ReactMethod
  fun stopScreenCapture(promise: Promise) {
    val currentActivity = currentActivity
    if (currentActivity == null) {
      promise.reject("ERR_ACTIVITY_NOT_FOUND", "Activity is not available")
      return
    }
    val serviceIntent = Intent(currentActivity, ScreenCaptureService::class.java)
    currentActivity.stopService(serviceIntent)

    sendEvent("screenCaptureEvent", Arguments.createMap().apply {
      putString("type", "stopped")
    })
    promise.resolve(null)
  }

  private fun sendEvent(eventName: String, params: WritableMap) {
    try {
      reactApplicationContext
        .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
        .emit(eventName, params)
    } catch (e: Exception) {
      Log.e(TAG, "Error sending event: $eventName", e)
    }
  }
}
