import ScannerModule from './NativeScannerModule';
import type { CameraPermissionStatus } from './NativeScannerModule';
import ScannerViewNative from './ScannerViewNativeComponent';
import type { BarcodeScannedEvent } from './ScannerViewNativeComponent';

export const ScannerView = ScannerViewNative;
export type { BarcodeScannedEvent, CameraPermissionStatus };

export interface CameraPermissionState {
  status: CameraPermissionStatus;
  canRequest: boolean;
  canOpenSettings: boolean;
}

export function isCameraPermissionGranted(
  status: CameraPermissionStatus,
): boolean {
  return status === 'granted';
}

export function canRequestCameraPermission(
  status: CameraPermissionStatus,
): boolean {
  return status === 'not-determined' || status === 'denied';
}

export function shouldOpenCameraSettings(
  status: CameraPermissionStatus,
): boolean {
  return status === 'blocked' || status === 'restricted';
}

export async function getCameraPermissionState(): Promise<CameraPermissionState> {
  const status = await ScannerModule.checkCameraPermission();
  return {
    status,
    canRequest: canRequestCameraPermission(status),
    canOpenSettings: shouldOpenCameraSettings(status),
  };
}

export async function checkCameraPermission(): Promise<CameraPermissionStatus> {
  return ScannerModule.checkCameraPermission();
}

export async function requestCameraPermission(): Promise<CameraPermissionStatus> {
  const current = await ScannerModule.checkCameraPermission();

  if (current === 'granted') {
    return 'granted';
  }

  if (!canRequestCameraPermission(current)) {
    return current;
  }

  return ScannerModule.requestCameraPermission();
}

export function openCameraSettings(): void {
  ScannerModule.openCameraSettings();
}

export function setTorchEnabled(enabled: boolean): void {
  ScannerModule.setTorchEnabled(enabled);
}
