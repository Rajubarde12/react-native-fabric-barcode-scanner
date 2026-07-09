import ScannerModule from './NativeScannerModule';
import ScannerViewNative from './ScannerViewNativeComponent';
import type { BarcodeScannedEvent } from './ScannerViewNativeComponent';

export const ScannerView = ScannerViewNative;
export type { BarcodeScannedEvent };

export {
  getCameraPermissionState,
  requestCameraPermission,
  openCameraSettings,
  type CameraPermissionState,
  type CameraPermissionStatus,
} from './cameraPermission';

export {
  useCameraPermission,
  type UseCameraPermissionResult,
} from './useCameraPermission';

export function setTorchEnabled(enabled: boolean): void {
  ScannerModule.setTorchEnabled(enabled);
}
