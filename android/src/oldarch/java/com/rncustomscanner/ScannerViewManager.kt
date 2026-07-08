package com.rncustomscanner

import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReadableArray
import com.facebook.react.uimanager.SimpleViewManager
import com.facebook.react.uimanager.ThemedReactContext
import com.facebook.react.uimanager.annotations.ReactProp
import com.rncustomscanner.events.BarcodeScannedEvent

class ScannerViewManager(
    @Suppress("UNUSED_PARAMETER") context: ReactApplicationContext,
) : SimpleViewManager<ScannerView>() {

    override fun getName(): String = REACT_CLASS

    override fun createViewInstance(context: ThemedReactContext): ScannerView {
        return ScannerView(context)
    }

    override fun getExportedCustomBubblingEventTypeConstants(): Map<String, Any> {
        return mapOf(
            BarcodeScannedEvent.EVENT_NAME to mapOf(
                "registrationName" to "onBarcodeScanned",
            ),
        )
    }

    @ReactProp(name = "isActive", defaultBoolean = true)
    fun setIsActive(view: ScannerView, value: Boolean) {
        view.setIsActive(value)
    }

    @ReactProp(name = "torchOn", defaultBoolean = false)
    fun setTorchOn(view: ScannerView, value: Boolean) {
        view.setTorchOn(value)
    }

    @ReactProp(name = "scanFormats")
    fun setScanFormats(view: ScannerView, value: ReadableArray?) {
        if (value == null) {
            view.setScanFormats(null)
            return
        }
        val formats = ArrayList<String>(value.size())
        for (i in 0 until value.size()) {
            value.getString(i)?.let { formats.add(it) }
        }
        view.setScanFormats(formats)
    }

    companion object {
        const val REACT_CLASS = "ScannerView"
    }
}
