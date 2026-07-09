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

/** @deprecated Use {@link getCameraPermissionState} and read `.status`, or {@link useCameraPermission}. */
export { checkCameraPermission } from './cameraPermission';

/** @deprecated Use `state.status === 'granted'` or `useCameraPermission().isGranted`. */
export { isCameraPermissionGranted } from './cameraPermission';

/** @deprecated Use `getCameraPermissionState().canRequest` or `useCameraPermission().canRequest`. */
export { canRequestCameraPermission } from './cameraPermission';

/** @deprecated Use `getCameraPermissionState().canOpenSettings` or `useCameraPermission().canOpenSettings`. */
export { shouldOpenCameraSettings } from './cameraPermission';

export {
  useCameraPermission,
  type UseCameraPermissionResult,
} from './useCameraPermission';

export function setTorchEnabled(enabled: boolean): void {
  ScannerModule.setTorchEnabled(enabled);
}
