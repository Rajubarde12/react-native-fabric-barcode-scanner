# react-native-fabric-barcode-scanner

Native barcode scanner for React Native built with **TurboModules** and **Fabric** (New Architecture). Uses AVFoundation on iOS and CameraX + ML Kit on Android.

[![npm version](https://img.shields.io/npm/v/react-native-fabric-barcode-scanner.svg)](https://www.npmjs.com/package/react-native-fabric-barcode-scanner)
[![license](https://img.shields.io/npm/l/react-native-fabric-barcode-scanner.svg)](https://github.com/Rajubarde12/react-native-fabric-barcode-scanner/blob/main/LICENSE)

## Features

- Fabric `ScannerView` component with continuous barcode scanning
- TurboModule `ScannerModule` for camera permission and torch control
- New Architecture support on iOS and Android
- Configurable barcode formats (`qr`, `ean13`, `code128`, and more)
- No permission dialogs from the view — permission is handled in JS via the module

## Requirements

- React Native **0.74+**
- New Architecture enabled (`newArchEnabled=true` recommended)
- iOS 13.4+
- Android API 24+

## Installation

```bash
npm install react-native-fabric-barcode-scanner
# or
yarn add react-native-fabric-barcode-scanner
```

### iOS

```bash
cd ios && pod install
```

Add camera usage description to `Info.plist`:

```xml
<key>NSCameraUsageDescription</key>
<string>Camera access is required to scan barcodes</string>
```

### Android

Camera permission is declared by the library. Ensure your app manifest includes camera access if not merged automatically:

```xml
<uses-permission android:name="android.permission.CAMERA" />
```

Enable New Architecture in `android/gradle.properties`:

```properties
newArchEnabled=true
```

## Usage

```tsx
import { useEffect, useState } from 'react';
import { Button, Text, View } from 'react-native';
import {
  ScannerView,
  getCameraPermissionState,
  requestCameraPermission,
  openCameraSettings,
  type CameraPermissionStatus,
} from 'react-native-fabric-barcode-scanner';

function ScannerScreen() {
  const [status, setStatus] = useState<CameraPermissionStatus>('not-determined');

  const refreshPermission = async () => {
    const state = await getCameraPermissionState();
    setStatus(state.status);
    return state;
  };

  useEffect(() => {
    refreshPermission();
  }, []);

  if (status === 'granted') {
    return (
      <ScannerView
        style={{ flex: 1 }}
        isActive
        torchOn={false}
        scanFormats={['qr', 'ean13', 'code128']}
        onBarcodeScanned={(event) => {
          console.log(event.nativeEvent.value, event.nativeEvent.type);
        }}
      />
    );
  }

  return (
    <View style={{ flex: 1, justifyContent: 'center', alignItems: 'center', padding: 24 }}>
      {status === 'not-determined' || status === 'denied' ? (
        <Button
          title="Allow camera"
          onPress={async () => {
            const next = await requestCameraPermission();
            setStatus(next);
          }}
        />
      ) : null}

      {status === 'blocked' || status === 'restricted' ? (
        <>
          <Text style={{ marginBottom: 12, textAlign: 'center' }}>
            Camera access is {status}. Open settings to enable it.
          </Text>
          <Button title="Open settings" onPress={openCameraSettings} />
        </>
      ) : null}
    </View>
  );
}
```

## API

### `ScannerView` props

| Prop | Type | Default | Description |
|------|------|---------|-------------|
| `isActive` | `boolean` | `true` | Start/stop the camera session |
| `torchOn` | `boolean` | `false` | Enable device torch |
| `scanFormats` | `string[]` | all supported | Barcode formats to decode |
| `onBarcodeScanned` | `(event) => void` | — | Fired continuously while a code is visible |

### Camera permission (same API on iOS & Android)

Use **one flow** for both platforms:

```tsx
if (status === 'not-determined' || status === 'denied') {
  // Show "Allow camera" → requestCameraPermission()
} else if (status === 'blocked' || status === 'restricted') {
  // Show "Open settings" → openCameraSettings()
} else {
  // granted → show ScannerView
}
```

| Status | Meaning | Developer action |
|--------|---------|------------------|
| `granted` | Camera allowed | Show `ScannerView` |
| `not-determined` | Never asked yet | `requestCameraPermission()` |
| `denied` | Denied, system dialog can still appear | `requestCameraPermission()` |
| `blocked` | System will not ask again | `openCameraSettings()` |
| `restricted` | Restricted by device policy (iOS) | `openCameraSettings()` |

**How each platform maps to the same status:**

| Scenario | iOS returns | Android returns |
|----------|-------------|-----------------|
| Never asked | `not-determined` | `not-determined` |
| User allowed | `granted` | `granted` |
| Denied, can ask again | — (iOS does not re-prompt) | `denied` |
| Denied permanently | `blocked` (after 1 deny) | `blocked` (after 2+ denies or "Don't ask again") |
| Device restricted | `restricted` | — |

iOS blocks after **one** deny. Android allows **2–3** prompts, then returns `blocked` — but your JS code stays the same on both platforms.

### `ScannerModule` methods

| Method | Returns | Description |
|--------|---------|-------------|
| `getCameraPermissionState()` | `Promise<{ status, canRequest, canOpenSettings }>` | Full permission state for UI |
| `checkCameraPermission()` | `Promise<CameraPermissionStatus>` | Current camera permission status |
| `requestCameraPermission()` | `Promise<CameraPermissionStatus>` | Request permission (only when allowed) |
| `openCameraSettings()` | `void` | Open app settings so user can enable camera |
| `canRequestCameraPermission(status)` | `boolean` | `true` for `not-determined` or `denied` |
| `shouldOpenCameraSettings(status)` | `boolean` | `true` for `blocked` or `restricted` |
| `isCameraPermissionGranted(status)` | `boolean` | `true` when `granted` |
| `setTorchEnabled(enabled)` | `void` | Toggle torch outside the view |

### Supported formats

`qr`, `ean13`, `ean8`, `upce`, `code128`, `code39`, `code93`, `pdf417`, `aztec`, `datamatrix`, `itf14`, `interleaved2of5`

## New Architecture

This library is built for React Native's New Architecture:

- **TurboModule**: `ScannerModule`
- **Fabric component**: `ScannerView`
- Codegen spec: `RNCustomScannerSpec`

Legacy architecture is supported via paper fallbacks on Android.

## License

MIT — see [LICENSE](LICENSE).

## Repository

- **GitHub**: [Rajubarde12/react-native-fabric-barcode-scanner](https://github.com/Rajubarde12/react-native-fabric-barcode-scanner)
- **npm**: [react-native-fabric-barcode-scanner](https://www.npmjs.com/package/react-native-fabric-barcode-scanner)
- **Issues**: [Report a bug](https://github.com/Rajubarde12/react-native-fabric-barcode-scanner/issues)
