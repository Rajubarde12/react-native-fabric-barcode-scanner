package com.rncustomscanner

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.facebook.react.bridge.ReactApplicationContext

object CameraPermissionHelper {

    const val STATUS_GRANTED = "granted"
    const val STATUS_NOT_DETERMINED = "not-determined"
    const val STATUS_DENIED = "denied"
    const val STATUS_BLOCKED = "blocked"
    const val STATUS_RESTRICTED = "restricted"

    private const val PREFS_NAME = "rncustomscanner_permissions"
    private const val KEY_CAMERA_REQUESTED = "camera_requested"
    private const val KEY_CAMERA_DENY_COUNT = "camera_deny_count"

    fun checkPermission(context: ReactApplicationContext): String {
        if (isGranted(context)) {
            return STATUS_GRANTED
        }

        if (!wasPermissionRequested(context)) {
            return STATUS_NOT_DETERMINED
        }

        if (shouldShowRationale(context)) {
            return STATUS_DENIED
        }

        // Unified with iOS: only blocked when the system will not show the dialog again.
        // Android may deny 1-2 times before reaching this state.
        return if (getDenyCount(context) >= 2) {
            STATUS_BLOCKED
        } else {
            STATUS_DENIED
        }
    }

    fun resolveRequestResult(
        context: ReactApplicationContext,
        grantResults: IntArray,
    ): String {
        markPermissionRequested(context)

        val granted = grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED

        if (granted) {
            clearDenyCount(context)
            return STATUS_GRANTED
        }

        incrementDenyCount(context)
        return resolveDeniedStatus(context)
    }

    fun canRequestPermission(status: String): Boolean {
        return status == STATUS_NOT_DETERMINED || status == STATUS_DENIED
    }

    fun markPermissionRequested(context: ReactApplicationContext) {
        getPrefs(context)
            .edit()
            .putBoolean(KEY_CAMERA_REQUESTED, true)
            .apply()
    }

    fun openAppSettings(context: ReactApplicationContext) {
        val intent = Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.fromParts("package", context.packageName, null),
        ).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    private fun resolveDeniedStatus(context: ReactApplicationContext): String {
        if (shouldShowRationale(context)) {
            return STATUS_DENIED
        }

        // First denial: always "denied" (Android can still show the dialog again).
        // Second+ denial without rationale: "blocked" (same as iOS after one deny).
        return if (getDenyCount(context) >= 2) {
            STATUS_BLOCKED
        } else {
            STATUS_DENIED
        }
    }

    private fun isGranted(context: ReactApplicationContext): Boolean {
        val activity = context.currentActivity
        val checkContext = activity ?: context
        return ContextCompat.checkSelfPermission(checkContext, Manifest.permission.CAMERA) ==
            PackageManager.PERMISSION_GRANTED
    }

    private fun shouldShowRationale(context: ReactApplicationContext): Boolean {
        val activity = context.currentActivity ?: return false
        return ActivityCompat.shouldShowRequestPermissionRationale(
            activity,
            Manifest.permission.CAMERA,
        )
    }

    private fun wasPermissionRequested(context: ReactApplicationContext): Boolean {
        return getPrefs(context).getBoolean(KEY_CAMERA_REQUESTED, false)
    }

    private fun getDenyCount(context: ReactApplicationContext): Int {
        return getPrefs(context).getInt(KEY_CAMERA_DENY_COUNT, 0)
    }

    private fun incrementDenyCount(context: ReactApplicationContext) {
        val count = getDenyCount(context)
        getPrefs(context)
            .edit()
            .putInt(KEY_CAMERA_DENY_COUNT, count + 1)
            .apply()
    }

    private fun clearDenyCount(context: ReactApplicationContext) {
        getPrefs(context)
            .edit()
            .putInt(KEY_CAMERA_DENY_COUNT, 0)
            .apply()
    }

    private fun getPrefs(context: ReactApplicationContext) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
}
