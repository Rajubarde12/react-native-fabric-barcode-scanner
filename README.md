# react-native-fabric-barcode-scanner

Native barcode scanner for React Native built with **TurboModules** and **Fabric** (New Architecture). Uses AVFoundation on iOS and CameraX + ML Kit on Android.

[![npm version](https://img.shields.io/npm/v/react-native-fabric-barcode-scanner.svg)](https://www.npmjs.com/package/react-native-fabric-barcode-scanner)
[![license](https://img.shields.io/npm/l/react-native-fabric-barcode-scanner.svg)](https://github.com/Rajubarde12/react-native-fabric-barcode-scanner/blob/main/LICENSE)

## Features

- Fabric `ScannerView` component with continuous barcode scanning
- **`useCameraPermission()`** hook — recommended permission UX (auto-refresh on foreground)
- TurboModule `ScannerModule` for camera permission and torch control
- New Architecture support on iOS and Android
- Configurable barcode formats (`qr`, `ean13`, `code128`, and more)
- No permission dialogs from the view — permission is handled in JS via the module / hook

## Requirements

| Platform | Minimum |
|----------|---------|
| React Native | **0.74.0+** (New Architecture recommended; default on RN 0.76+) |
| React | **18.2+** (peer) |
| Node | **18+** (library build / tooling) |
| iOS | **13.4+** library fallback; host RN sets floor (**15.1** on RN 0.76+) via `min_ios_version_supported` |
| Xcode | **≥ 15.1** (RN 0.74–0.80); **≥ 16.1** (RN 0.81+ / 0.86 helpers) |
| Android | **API 24+** (Android 7.0) |
| Kotlin (host app) | Align with RN template (library default fallback **2.1.20**) |
| Swift | **5.0** language mode |
| CameraX (Android) | **1.6.1** (bundled by library) |
| ML Kit barcode (Android) | **17.3.0** bundled model (see `DEPENDENCY_AUDIT.md` for unbundled option) |

Enable New Architecture in `android/gradle.properties`:

```properties
newArchEnabled=true
```

> Full dependency audit, upgrade table, and migration notes: see [`DEPENDENCY_AUDIT.md`](./DEPENDENCY_AUDIT.md) (also mirrored in internal `doc.md`).

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

Also set `newArchEnabled=true` in the app's `android/gradle.properties` (see Requirements above).

## Usage

### Which permission API should I use?

| I need… | Use |
|---------|-----|
| Permission UI in a React screen | **`useCameraPermission()`** (recommended) |
| One-shot check outside React | `getCameraPermissionState()` |
| Anything else | See [MIGRATION_PERMISSION_API.md](./MIGRATION_PERMISSION_API.md) |

```tsx
import { Button, Text, View } from 'react-native';
import {
  ScannerView,
  useCameraPermission,
} from 'react-native-fabric-barcode-scanner';

function ScannerScreen() {
  const {
    isGranted,
    canRequest,
    canOpenSettings,
    isLoading,
    request,
    openSettings,
  } = useCameraPermission();

  if (isLoading) {
    return null;
  }

  if (isGranted) {
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
      {canRequest ? (
        <Button title="Allow camera" onPress={() => { void request(); }} />
      ) : null}

      {canOpenSettings ? (
        <>
          <Text style={{ marginBottom: 12, textAlign: 'center' }}>
            Camera access is blocked. Open settings to enable it.
          </Text>
          <Button title="Open settings" onPress={openSettings} />
        </>
      ) : null}
    </View>
  );
}
```

The hook refreshes automatically when the app returns to the foreground (e.g. after Settings).

## API

### `ScannerView` props

| Prop | Type | Default | Description |
|------|------|---------|-------------|
| `isActive` | `boolean` | `true` | Start/stop the camera session |
| `torchOn` | `boolean` | `false` | Enable device torch |
| `scanFormats` | `string[]` | all supported | Barcode formats to decode |
| `onBarcodeScanned` | `(event) => void` | — | Fired continuously while a code is visible |

### Camera permission — recommended: `useCameraPermission()`

| Field / method | Type | Description |
|----------------|------|-------------|
| `status` | `CameraPermissionStatus` | `granted` \| `not-determined` \| `denied` \| `blocked` \| `restricted` |
| `isGranted` | `boolean` | `status === 'granted'` |
| `canRequest` | `boolean` | Show “Allow camera” |
| `canOpenSettings` | `boolean` | Show “Open settings” |
| `isLoading` | `boolean` | True until first native check finishes |
| `request()` | `() => Promise<status>` | System permission dialog (when allowed) |
| `openSettings()` | `() => void` | Opens OS app settings |
| `refresh()` | `() => Promise<state>` | Manual re-check |

| Status | Meaning | UI |
|--------|---------|-----|
| `granted` | Camera allowed | Show `ScannerView` |
| `not-determined` / `denied` | Can still prompt | `request()` |
| `blocked` / `restricted` | Must use Settings | `openSettings()` |

**Platform mapping (same JS on iOS & Android):**

| Scenario | iOS | Android |
|----------|-----|---------|
| Never asked | `not-determined` | `not-determined` |
| Allowed | `granted` | `granted` |
| Denied, can ask again | — | `denied` |
| Permanently denied | `blocked` | `blocked` |
| Device policy | `restricted` | — |

### Advanced / low-level permission API

Prefer the hook above. These remain for non-React or legacy code:

| Method | Use when |
|--------|----------|
| `getCameraPermissionState()` | Imperative full state `{ status, canRequest, canOpenSettings }` |
| `requestCameraPermission()` | Imperative request (hook exposes `request()`) |
| `openCameraSettings()` | Imperative settings (hook exposes `openSettings()`) |

**Deprecated** (still exported; `__DEV__` warns once): `checkCameraPermission`, `isCameraPermissionGranted`, `canRequestCameraPermission`, `shouldOpenCameraSettings` — see [MIGRATION_PERMISSION_API.md](./MIGRATION_PERMISSION_API.md).

### Other

| Method | Description |
|--------|-------------|
| `setTorchEnabled(enabled)` | Toggle torch outside the view (prefer `ScannerView` `torchOn` when mounted) |

### Supported formats

`qr`, `ean13`, `ean8`, `upce`, `code128`, `code39`, `code93`, `pdf417`, `aztec`, `datamatrix`, `itf14`, `interleaved2of5`

## New Architecture

This library is built for React Native's New Architecture:

- **TurboModule**: `ScannerModule`
- **Fabric component**: `ScannerView`
- Codegen spec: `RNCustomScannerSpec`

Legacy architecture is supported via paper fallbacks on Android (`android/src/oldarch`). Those sources share the same CameraX / ML Kit dependencies — no separate Old-Arch-only Maven artifacts.

## Dependency policy

See **[DEPENDENCY_AUDIT.md](./DEPENDENCY_AUDIT.md)** for the full upgrade table, ML Kit bundled vs unbundled tradeoffs, and migration notes. Dependabot is configured in `.github/dependabot.yml`.

## License

MIT — see [LICENSE](LICENSE).

## Repository

- **GitHub**: [Rajubarde12/react-native-fabric-barcode-scanner](https://github.com/Rajubarde12/react-native-fabric-barcode-scanner)
- **npm**: [react-native-fabric-barcode-scanner](https://www.npmjs.com/package/react-native-fabric-barcode-scanner)
- **Issues**: [Report a bug](https://github.com/Rajubarde12/react-native-fabric-barcode-scanner/issues)
