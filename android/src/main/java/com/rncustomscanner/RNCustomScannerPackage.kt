package com.rncustomscanner

import com.facebook.react.TurboReactPackage
import com.facebook.react.bridge.NativeModule
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.module.model.ReactModuleInfo
import com.facebook.react.module.model.ReactModuleInfoProvider
import com.facebook.react.uimanager.ViewManager

class RNCustomScannerPackage : TurboReactPackage() {

    override fun createViewManagers(
        reactContext: ReactApplicationContext,
    ): List<ViewManager<*, *>> {
        return listOf(ScannerViewManager(reactContext))
    }

    override fun getModule(
        name: String,
        reactContext: ReactApplicationContext,
    ): NativeModule? {
        return if (name == ScannerModule.NAME) {
            ScannerModule(reactContext)
        } else {
            null
        }
    }

    override fun getReactModuleInfoProvider(): ReactModuleInfoProvider {
        val isTurboModule = BuildConfig.IS_NEW_ARCHITECTURE_ENABLED
        return ReactModuleInfoProvider {
            mapOf(
                ScannerModule.NAME to ReactModuleInfo(
                    ScannerModule.NAME,
                    ScannerModule.NAME,
                    false,
                    false,
                    false,
                    isTurboModule,
                ),
            )
        }
    }
}
