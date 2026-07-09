package com.rncustomscanner

import android.hardware.camera2.CameraManager
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.module.annotations.ReactModule

@ReactModule(name = ScannerModule.NAME)
class ScannerModule(
    private val reactContext: ReactApplicationContext,
) : ReactContextBaseJavaModule(reactContext) {

    override fun getName(): String = NAME

    override fun getConstants(): Map<String, Any> {
        return mapOf("SUPPORTED_FORMATS" to BarcodeFormatMapper.supportedFormats)
    }

    @ReactMethod
    fun checkCameraPermission(promise: Promise) {
        promise.resolve(CameraPermissionHelper.checkPermission(reactContext))
    }

    @ReactMethod
    fun requestCameraPermission(promise: Promise) {
        CameraPermissionRequester.request(reactContext, promise)
    }

    @ReactMethod
    fun openCameraSettings() {
        CameraPermissionHelper.openAppSettings(reactContext)
    }

    @ReactMethod
    fun setTorchEnabled(enabled: Boolean) {
        val cameraManager =
            reactContext.getSystemService(CameraManager::class.java) ?: return
        try {
            val cameraId = cameraManager.cameraIdList.firstOrNull() ?: return
            cameraManager.setTorchMode(cameraId, enabled)
        } catch (_: Exception) {
            // Best-effort
        }
    }

    companion object {
        const val NAME = "ScannerModule"
    }
}
