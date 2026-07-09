import { useCallback, useEffect, useState } from 'react';
import { AppState } from 'react-native';
import type { AppStateStatus } from 'react-native';
import {
  getCameraPermissionState,
  openCameraSettings,
  requestCameraPermission,
  type CameraPermissionState,
  type CameraPermissionStatus,
} from './cameraPermission';

export interface UseCameraPermissionResult extends CameraPermissionState {
  /** True until the first native permission check completes. */
  isLoading: boolean;
  /** Convenience: `status === 'granted'`. */
  isGranted: boolean;
  /** Re-read permission from the OS (also runs on AppState → active). */
  refresh: () => Promise<CameraPermissionState>;
  /** Request permission when `canRequest` is true; updates local state. */
  request: () => Promise<CameraPermissionStatus>;
  /** Open app settings when `canOpenSettings` is true. */
  openSettings: () => void;
}

/**
 * Thin React wrapper around native camera permission.
 * Normalizes OS status into `{ status, canRequest, canOpenSettings }`.
 *
 * @example
 * ```tsx
 * const { isGranted, canRequest, canOpenSettings, request, openSettings, isLoading } =
 *   useCameraPermission();
 *
 * if (isLoading) return null;
 * if (isGranted) return <ScannerView ... />;
 * if (canRequest) return <Button title="Allow camera" onPress={request} />;
 * if (canOpenSettings) return <Button title="Open settings" onPress={openSettings} />;
 * ```
 */
export function useCameraPermission(): UseCameraPermissionResult {
  const [state, setState] = useState<CameraPermissionState>({
    status: 'not-determined',
    canRequest: true,
    canOpenSettings: false,
  });
  const [isLoading, setIsLoading] = useState(true);

  const refresh = useCallback(async () => {
    const next = await getCameraPermissionState();
    setState(next);
    setIsLoading(false);
    return next;
  }, []);

  const request = useCallback(async () => {
    const status = await requestCameraPermission();
    const next = await getCameraPermissionState();
    setState(next);
    setIsLoading(false);
    return status;
  }, []);

  const openSettings = useCallback(() => {
    openCameraSettings();
  }, []);

  useEffect(() => {
    let cancelled = false;

    const run = async () => {
      const next = await getCameraPermissionState();
      if (!cancelled) {
        setState(next);
        setIsLoading(false);
      }
    };

    void run();

    const onChange = (nextAppState: AppStateStatus) => {
      if (nextAppState === 'active') {
        void run();
      }
    };

    const subscription = AppState.addEventListener('change', onChange);
    return () => {
      cancelled = true;
      subscription.remove();
    };
  }, []);

  return {
    status: state.status,
    canRequest: state.canRequest,
    canOpenSettings: state.canOpenSettings,
    isLoading,
    isGranted: state.status === 'granted',
    refresh,
    request,
    openSettings,
  };
}
