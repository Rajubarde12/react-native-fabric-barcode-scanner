import ScannerModule from './NativeScannerModule';
import type { CameraPermissionStatus } from './NativeScannerModule';

export type { CameraPermissionStatus };

export interface CameraPermissionState {
  status: CameraPermissionStatus;
  canRequest: boolean;
  canOpenSettings: boolean;
}

function toPermissionState(status: CameraPermissionStatus): CameraPermissionState {
  return {
    status,
    canRequest: status === 'not-determined' || status === 'denied',
    canOpenSettings: status === 'blocked' || status === 'restricted',
  };
}

/**
 * Prefer {@link useCameraPermission} in React components, or this helper in
 * non-React / imperative code.
 */
export async function getCameraPermissionState(): Promise<CameraPermissionState> {
  const status = await ScannerModule.checkCameraPermission();
  return toPermissionState(status);
}

/**
 * Request the system camera permission dialog when allowed.
 * Prefer `request()` from {@link useCameraPermission} in UI code.
 */
export async function requestCameraPermission(): Promise<CameraPermissionStatus> {
  const current = await ScannerModule.checkCameraPermission();

  if (current === 'granted') {
    return 'granted';
  }

  if (current !== 'not-determined' && current !== 'denied') {
    return current;
  }

  return ScannerModule.requestCameraPermission();
}

/**
 * Opens the OS app settings screen so the user can enable camera access.
 * Prefer `openSettings()` from {@link useCameraPermission} in UI code.
 */
export function openCameraSettings(): void {
  ScannerModule.openCameraSettings();
}
