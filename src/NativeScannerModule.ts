import type { TurboModule } from 'react-native';
import { TurboModuleRegistry } from 'react-native';

export type CameraPermissionStatus =
  | 'granted'
  | 'not-determined'
  | 'denied'
  | 'blocked'
  | 'restricted';

export interface Spec extends TurboModule {
  checkCameraPermission(): Promise<CameraPermissionStatus>;
  requestCameraPermission(): Promise<CameraPermissionStatus>;
  openCameraSettings(): void;
  setTorchEnabled(enabled: boolean): void;
  getConstants(): {
    SUPPORTED_FORMATS: string[];
  };
}

function getScannerModule(): Spec {
  const mod = TurboModuleRegistry.get<Spec>('ScannerModule');
  if (mod == null) {
    throw new Error(
      "TurboModule 'ScannerModule' is not registered in the native binary. " +
        'Rebuild the Android app after linking react-native-fabric-barcode-scanner:\n' +
        '  cd android && ./gradlew clean && cd .. && npx react-native run-android\n' +
        'Ensure android/gradle.properties has newArchEnabled=true.',
    );
  }
  return mod;
}

/** Lazy proxy — import does not crash; first native call validates registration. */
const ScannerModuleProxy: Spec = {
  getConstants: () => getScannerModule().getConstants(),
  checkCameraPermission: () => getScannerModule().checkCameraPermission(),
  requestCameraPermission: () => getScannerModule().requestCameraPermission(),
  openCameraSettings: () => getScannerModule().openCameraSettings(),
  setTorchEnabled: (enabled: boolean) =>
    getScannerModule().setTorchEnabled(enabled),
};

export default ScannerModuleProxy;
