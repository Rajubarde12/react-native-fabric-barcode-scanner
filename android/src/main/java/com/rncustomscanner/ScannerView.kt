package com.rncustomscanner

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Color
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import com.facebook.react.bridge.LifecycleEventListener
import com.facebook.react.uimanager.ThemedReactContext
import com.facebook.react.uimanager.UIManagerHelper
import com.rncustomscanner.events.BarcodeScannedEvent
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@SuppressLint("ViewConstructor")
class ScannerView(context: ThemedReactContext) : FrameLayout(context) {

    private val reactContext: ThemedReactContext = context
    private val previewView = PreviewView(context)
    private val cameraExecutor: ExecutorService = Executors.newSingleThreadExecutor()

    private var cameraProvider: ProcessCameraProvider? = null
    private var camera: Camera? = null
    private var preview: Preview? = null
    private var imageAnalysis: ImageAnalysis? = null
    private var barcodeAnalyzer: BarcodeAnalyzer? = null

    private var isActive: Boolean = true
    private var torchOn: Boolean = false
    private var scanFormats: List<String> = BarcodeFormatMapper.supportedFormats
    private var isCameraBound = false

    private val lifecycleListener = object : LifecycleEventListener {
        override fun onHostResume() {
            if (isActive && hasCameraPermission()) {
                isCameraBound = false
                scheduleCameraStart()
            }
        }

        override fun onHostPause() {
            stopCamera()
        }

        override fun onHostDestroy() {
            stopCamera()
        }
    }

    init {
        setBackgroundColor(Color.BLACK)
        previewView.layoutParams = LayoutParams(
            LayoutParams.MATCH_PARENT,
            LayoutParams.MATCH_PARENT,
        )
        previewView.scaleType = PreviewView.ScaleType.FILL_CENTER
        installHierarchyFitter(previewView)
        addView(previewView)

        reactContext.addLifecycleEventListener(lifecycleListener)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        scheduleCameraStart()
    }

    override fun onDetachedFromWindow() {
        reactContext.removeLifecycleEventListener(lifecycleListener)
        stopCamera()
        barcodeAnalyzer?.close()
        barcodeAnalyzer = null
        cameraExecutor.shutdown()
        super.onDetachedFromWindow()
    }

    override fun onWindowFocusChanged(hasWindowFocus: Boolean) {
        super.onWindowFocusChanged(hasWindowFocus)
        if (hasWindowFocus && isActive && hasCameraPermission() && !isCameraBound) {
            previewView.post { scheduleCameraStart() }
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (w > 0 && h > 0 && !isCameraBound) {
            scheduleCameraStart()
        }
    }

    fun setIsActive(active: Boolean) {
        isActive = active
        if (active) {
            scheduleCameraStart()
        } else {
            stopCamera()
        }
    }

    fun setTorchOn(enabled: Boolean) {
        torchOn = enabled
        camera?.cameraControl?.enableTorch(enabled)
    }

    fun setScanFormats(formats: List<String>?) {
        scanFormats = formats?.takeIf { it.isNotEmpty() } ?: BarcodeFormatMapper.supportedFormats
        barcodeAnalyzer?.updateFormats(BarcodeFormatMapper.toMlKitFormats(scanFormats))
    }

    private fun scheduleCameraStart() {
        if (!isActive || !hasCameraPermission() || width <= 0 || height <= 0) {
            return
        }
        previewView.post { configureCameraIfNeeded() }
    }

    private fun hasCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
            PackageManager.PERMISSION_GRANTED
    }

    private fun configureCameraIfNeeded() {
        if (!isActive || !hasCameraPermission() || width <= 0 || height <= 0 || isCameraBound) {
            return
        }

        val activity = reactContext.currentActivity
        if (activity == null) {
            Log.w(TAG, "Activity not available yet, retrying camera start")
            previewView.postDelayed({ configureCameraIfNeeded() }, 100)
            return
        }

        val cameraProviderFuture = ProcessCameraProvider.getInstance(activity)
        cameraProviderFuture.addListener({
            try {
                cameraProvider = cameraProviderFuture.get()
                bindCameraUseCases()
            } catch (exc: Exception) {
                Log.e(TAG, "Failed to initialize camera", exc)
                isCameraBound = false
            }
        }, ContextCompat.getMainExecutor(activity))
    }

    private fun bindCameraUseCases() {
        val provider = cameraProvider ?: return
        val activity = reactContext.currentActivity as? AppCompatActivity ?: return

        provider.unbindAll()

        preview = Preview.Builder().build()

        val formats = BarcodeFormatMapper.toMlKitFormats(scanFormats)
        barcodeAnalyzer = BarcodeAnalyzer { barcodes ->
            val barcode = barcodes.firstOrNull() ?: return@BarcodeAnalyzer
            val value = barcode.rawValue ?: return@BarcodeAnalyzer
            emitBarcodeScanned(
                value = value,
                type = BarcodeFormatMapper.fromMlKitFormat(barcode.format),
                timestamp = System.currentTimeMillis().toInt(),
            )
        }.also {
            it.updateFormats(formats)
        }

        imageAnalysis = ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()
            .also { analysis ->
                analysis.setAnalyzer(cameraExecutor, barcodeAnalyzer!!)
            }

        try {
            val useCases = listOfNotNull(preview, imageAnalysis)
            camera = provider.bindToLifecycle(
                activity,
                CameraSelector.DEFAULT_BACK_CAMERA,
                *useCases.toTypedArray(),
            )
            camera?.cameraControl?.enableTorch(torchOn)
            // Attach surface AFTER bind — required for PreviewView inside React Native.
            preview?.setSurfaceProvider(previewView.surfaceProvider)
            isCameraBound = true
            Log.d(TAG, "Camera bound successfully (${width}x${height})")
        } catch (exc: Exception) {
            Log.e(TAG, "Use case binding failed", exc)
            isCameraBound = false
        }
    }

    private fun stopCamera() {
        cameraProvider?.unbindAll()
        camera = null
        preview = null
        imageAnalysis = null
        isCameraBound = false
    }

    // React Native does not always layout TextureView children correctly — without this
    // the PreviewView stays black. See: https://github.com/facebook/react-native/issues/17968
    private fun installHierarchyFitter(view: ViewGroup) {
        view.setOnHierarchyChangeListener(object : OnHierarchyChangeListener {
            override fun onChildViewRemoved(parent: View?, child: View?) = Unit

            override fun onChildViewAdded(parent: View?, child: View?) {
                parent?.measure(
                    MeasureSpec.makeMeasureSpec(measuredWidth, MeasureSpec.EXACTLY),
                    MeasureSpec.makeMeasureSpec(measuredHeight, MeasureSpec.EXACTLY),
                )
                parent?.layout(0, 0, parent.measuredWidth, parent.measuredHeight)
            }
        })
    }

    private fun emitBarcodeScanned(value: String, type: String, timestamp: Int) {
        val surfaceId = UIManagerHelper.getSurfaceId(reactContext)
        UIManagerHelper
            .getEventDispatcherForReactTag(reactContext, id)
            ?.dispatchEvent(
                BarcodeScannedEvent(
                    surfaceId,
                    id,
                    value,
                    type,
                    timestamp,
                ),
            )
    }

    companion object {
        private const val TAG = "ScannerView"
    }
}
