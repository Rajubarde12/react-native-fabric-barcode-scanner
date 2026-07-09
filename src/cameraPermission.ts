import ScannerModule from './NativeScannerModule';
import type { CameraPermissionStatus } from './NativeScannerModule';

export type { CameraPermissionStatus };

export interface CameraPermissionState {
  status: CameraPermissionStatus;
  canRequest: boolean;
  canOpenSettings: boolean;
}

const deprecatedWarned = new Set<string>();

function warnDeprecatedOnce(name: string, replacement: string): void {
  if (typeof __DEV__ !== 'undefined' && !__DEV__) {
    return;
  }
  if (deprecatedWarned.has(name)) {
    return;
  }
  deprecatedWarned.add(name);
  // eslint-disable-next-line no-console
  console.warn(
    `[react-native-fabric-barcode-scanner] ${name} is deprecated. ${replacement}`,
  );
}

/**
 * Prefer {@link useCameraPermission} in React components, or this helper in
 * non-React / imperative code.
 */
export async function getCameraPermissionState(): Promise<CameraPermissionState> {
  const status = await ScannerModule.checkCameraPermission();
  return {
    status,
    canRequest: status === 'not-determined' || status === 'denied',
    canOpenSettings: status === 'blocked' || status === 'restricted',
  };
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

/**
 * @deprecated Use {@link getCameraPermissionState} and read `.status`, or
 * {@link useCameraPermission}. Will be removed in a future major version.
 */
export async function checkCameraPermission(): Promise<CameraPermissionStatus> {
  warnDeprecatedOnce(
    'checkCameraPermission()',
    'Use getCameraPermissionState() (or useCameraPermission) instead.',
  );
  return ScannerModule.checkCameraPermission();
}

/**
 * @deprecated Use `state.status === 'granted'` or `useCameraPermission().isGranted`.
 */
export function isCameraPermissionGranted(
  status: CameraPermissionStatus,
): boolean {
  warnDeprecatedOnce(
    'isCameraPermissionGranted()',
    'Use status === "granted" or useCameraPermission().isGranted.',
  );
  return status === 'granted';
}

/**
 * @deprecated Use `getCameraPermissionState().canRequest` or
 * `useCameraPermission().canRequest`.
 */
export function canRequestCameraPermission(
  status: CameraPermissionStatus,
): boolean {
  warnDeprecatedOnce(
    'canRequestCameraPermission()',
    'Use getCameraPermissionState().canRequest or useCameraPermission().canRequest.',
  );
  return status === 'not-determined' || status === 'denied';
}

/**
 * @deprecated Use `getCameraPermissionState().canOpenSettings` or
 * `useCameraPermission().canOpenSettings`.
 */
export function shouldOpenCameraSettings(
  status: CameraPermissionStatus,
): boolean {
  warnDeprecatedOnce(
    'shouldOpenCameraSettings()',
    'Use getCameraPermissionState().canOpenSettings or useCameraPermission().canOpenSettings.',
  );
  return status === 'blocked' || status === 'restricted';
}
