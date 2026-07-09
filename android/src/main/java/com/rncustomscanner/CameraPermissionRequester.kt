package com.rncustomscanner

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Handler
import android.os.Looper
import androidx.core.content.ContextCompat
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.modules.core.PermissionAwareActivity
import com.facebook.react.modules.core.PermissionListener

/**
 * Requests CAMERA permission, waiting briefly for [ReactApplicationContext.currentActivity]
 * when the JS layer calls too early after navigation.
 *
 * Requires a [PermissionAwareActivity] (React Native's default [ReactActivity]) so the
 * result is delivered via [PermissionListener] — never resolves before the user answers.
 */
internal object CameraPermissionRequester {

    private const val REQUEST_CODE = 0x5343414E
    private const val MAX_ACTIVITY_WAIT_MS = 2000L
    private const val ACTIVITY_POLL_MS = 100L

    fun request(reactContext: ReactApplicationContext, promise: Promise) {
        val current = CameraPermissionHelper.checkPermission(reactContext)

        if (current == CameraPermissionHelper.STATUS_GRANTED) {
            promise.resolve(current)
            return
        }

        if (!CameraPermissionHelper.canRequestPermission(current)) {
            promise.resolve(current)
            return
        }

        waitForActivity(reactContext) { activity ->
            if (activity == null) {
                // Do NOT mark as requested — keep status not-determined so JS can retry.
                promise.resolve(CameraPermissionHelper.STATUS_NOT_DETERMINED)
                return@waitForActivity
            }

            if (
                ContextCompat.checkSelfPermission(activity, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED
            ) {
                promise.resolve(CameraPermissionHelper.STATUS_GRANTED)
                return@waitForActivity
            }

            val permissionActivity = activity as? PermissionAwareActivity
            if (permissionActivity == null) {
                promise.reject(
                    "E_NO_PERMISSION_AWARE_ACTIVITY",
                    "Current activity does not support permission requests. " +
                        "Use ReactActivity (or another PermissionAwareActivity).",
                )
                return@waitForActivity
            }

            val listener = PermissionListener { requestCode, _, grantResults ->
                if (requestCode != REQUEST_CODE) {
                    return@PermissionListener false
                }
                // Mark requested only after the system dialog returns a result.
                promise.resolve(
                    CameraPermissionHelper.resolveRequestResult(reactContext, grantResults),
                )
                true
            }

            permissionActivity.requestPermissions(
                arrayOf(Manifest.permission.CAMERA),
                REQUEST_CODE,
                listener,
            )
        }
    }

    private fun waitForActivity(
        reactContext: ReactApplicationContext,
        onReady: (Activity?) -> Unit,
    ) {
        val existing = reactContext.currentActivity
        if (existing != null) {
            onReady(existing)
            return
        }

        val handler = Handler(Looper.getMainLooper())
        val startedAt = System.currentTimeMillis()

        val poll = object : Runnable {
            override fun run() {
                val activity = reactContext.currentActivity
                if (activity != null) {
                    onReady(activity)
                    return
                }
                if (System.currentTimeMillis() - startedAt >= MAX_ACTIVITY_WAIT_MS) {
                    onReady(null)
                    return
                }
                handler.postDelayed(this, ACTIVITY_POLL_MS)
            }
        }

        handler.post(poll)
    }
}
