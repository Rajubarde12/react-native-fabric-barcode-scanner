package com.rncustomscanner

import com.facebook.react.BaseReactPackage
import com.facebook.react.bridge.NativeModule
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.module.model.ReactModuleInfo
import com.facebook.react.module.model.ReactModuleInfoProvider
import com.facebook.react.uimanager.ViewManager

class RNCustomScannerPackage : BaseReactPackage() {

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

    override fun getReactModuleInfoProvider(): ReactModuleInfoProvider =
        ReactModuleInfoProvider {
            mapOf(
                ScannerModule.NAME to ReactModuleInfo(
                    name = ScannerModule.NAME,
                    className = ScannerModule::class.java.name,
                    canOverrideExistingModule = false,
                    needsEagerInit = false,
                    isCxxModule = false,
                    // Must be true for Bridgeless / New Arch — do not gate on BuildConfig.
                    isTurboModule = ReactModuleInfo.classIsTurboModule(ScannerModule::class.java),
                ),
            )
        }
}
