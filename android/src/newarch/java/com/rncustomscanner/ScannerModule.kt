package com.rncustomscanner

import android.hardware.camera2.CameraManager
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.module.annotations.ReactModule

@ReactModule(name = ScannerModule.NAME)
class ScannerModule(
    private val reactContext: ReactApplicationContext,
) : NativeScannerModuleSpec(reactContext) {

    override fun getName(): String = NAME

    override fun getTypedExportedConstants(): Map<String, Any> {
        return mapOf("SUPPORTED_FORMATS" to BarcodeFormatMapper.supportedFormats)
    }

    override fun checkCameraPermission(promise: Promise) {
        promise.resolve(CameraPermissionHelper.checkPermission(reactContext))
    }

    override fun requestCameraPermission(promise: Promise) {
        CameraPermissionRequester.request(reactContext, promise)
    }

    override fun openCameraSettings() {
        CameraPermissionHelper.openAppSettings(reactContext)
    }

    override fun setTorchEnabled(enabled: Boolean) {
        val cameraManager =
            reactContext.getSystemService(CameraManager::class.java) ?: return
        try {
            val cameraId = cameraManager.cameraIdList.firstOrNull() ?: return
            cameraManager.setTorchMode(cameraId, enabled)
        } catch (_: Exception) {
            // Best-effort: torch failures are non-fatal for scanning
        }
    }

    companion object {
        const val NAME = "ScannerModule"
    }
}
