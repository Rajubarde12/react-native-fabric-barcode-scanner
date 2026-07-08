package com.rncustomscanner.events

import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.WritableMap
import com.facebook.react.uimanager.events.Event

class BarcodeScannedEvent(
    surfaceId: Int,
    viewId: Int,
    private val value: String,
    private val type: String,
    private val timestamp: Int,
) : Event<BarcodeScannedEvent>(surfaceId, viewId) {

    override fun getEventName(): String = EVENT_NAME

    override fun getEventData(): WritableMap =
        Arguments.createMap().apply {
            putString("value", value)
            putString("type", type)
            putInt("timestamp", timestamp)
        }

    override fun canCoalesce(): Boolean = false

    companion object {
        const val EVENT_NAME = "topBarcodeScanned"
    }
}
