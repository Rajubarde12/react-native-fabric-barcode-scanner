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

export default TurboModuleRegistry.getEnforcing<Spec>('ScannerModule');
