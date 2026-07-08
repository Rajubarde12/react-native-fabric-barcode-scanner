package com.rncustomscanner

import android.Manifest
import android.content.pm.PackageManager
import android.hardware.camera2.CameraManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.module.annotations.ReactModule
import com.facebook.react.modules.core.PermissionAwareActivity
import com.facebook.react.modules.core.PermissionListener

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
        val current = CameraPermissionHelper.checkPermission(reactContext)

        if (current == CameraPermissionHelper.STATUS_GRANTED) {
            promise.resolve(current)
            return
        }

        if (!CameraPermissionHelper.canRequestPermission(current)) {
            promise.resolve(current)
            return
        }

        val activity = reactContext.currentActivity
        if (activity == null) {
            promise.resolve(CameraPermissionHelper.STATUS_DENIED)
            return
        }

        if (
            ContextCompat.checkSelfPermission(activity, Manifest.permission.CAMERA) ==
            PackageManager.PERMISSION_GRANTED
        ) {
            promise.resolve(CameraPermissionHelper.STATUS_GRANTED)
            return
        }

        val listener = PermissionListener { requestCode, _, grantResults ->
            if (requestCode != CAMERA_PERMISSION_REQUEST_CODE) {
                return@PermissionListener false
            }
            promise.resolve(
                CameraPermissionHelper.resolveRequestResult(reactContext, grantResults),
            )
            true
        }

        CameraPermissionHelper.markPermissionRequested(reactContext)

        if (activity is PermissionAwareActivity) {
            activity.requestPermissions(
                arrayOf(Manifest.permission.CAMERA),
                CAMERA_PERMISSION_REQUEST_CODE,
                listener,
            )
        } else {
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(Manifest.permission.CAMERA),
                CAMERA_PERMISSION_REQUEST_CODE,
            )
            promise.resolve(CameraPermissionHelper.checkPermission(reactContext))
        }
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
        const val CAMERA_PERMISSION_REQUEST_CODE = 0x5343414E
    }
}
