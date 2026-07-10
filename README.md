# react-native-fabric-barcode-scanner

**High-performance native barcode scanner for React Native** — Fabric `ScannerView` + TurboModule, with AVFoundation (iOS) and CameraX + ML Kit (Android).

[![npm version](https://img.shields.io/npm/v/react-native-fabric-barcode-scanner.svg)](https://www.npmjs.com/package/react-native-fabric-barcode-scanner)
[![license](https://img.shields.io/npm/l/react-native-fabric-barcode-scanner.svg)](./LICENSE)
[![platform](https://img.shields.io/badge/platform-iOS%20%7C%20Android-blue.svg)](https://github.com/Rajubarde12/react-native-fabric-barcode-scanner)

```bash
npm install react-native-fabric-barcode-scanner
cd ios && pod install
```

> **New Architecture recommended.** Enable `newArchEnabled=true` on Android. You own the permission UI — the library only normalizes camera status.

---

## Table of contents

- [Features](#features)
- [Why this package?](#why-this-package)
- [Requirements](#requirements)
- [Installation](#installation)
- [Quick start](#quick-start)
- [Basic example](#basic-example)
- [API](#api)
- [Supported formats](#supported-formats)
- [Architecture](#architecture)
- [Best practices](#best-practices)
- [Troubleshooting](#troubleshooting)
- [FAQ](#faq)
- [Migration](#migration)
- [Contributing](#contributing)
- [License](#license)

---

## Features

| Feature | Details |
|---------|---------|
| **Fabric `ScannerView`** | Continuous barcode scanning as a native view |
| **`useCameraPermission()`** | Normalized permission status for your UI |
| **TurboModule `ScannerModule`** | Permission + torch control |
| **Configurable formats** | `qr`, `ean13`, `code128`, and more |
| **New Architecture** | Codegen Fabric component + TurboModule |
| **Old Arch fallback** | Android paper sources under `android/src/oldarch` |
| **Android activity-safe** | Waits for `currentActivity` before requesting permission |

---

## Why this package?

Most barcode libraries either wrap a WebView, ship heavy permission UIs you must restyle, or lag behind New Architecture.

| Approach | New Arch (Fabric) | You own permission UI | Native camera stack |
|----------|-------------------|------------------------|---------------------|
| WebView / JS-only scanners | Varies | Yes | Limited |
| UI-heavy scanner kits | Varies | Often no | Yes |
| **This package** | First-class | Yes | AVFoundation / CameraX + ML Kit |

Use it when you want a **native preview**, **format filters**, and **permission primitives** without a baked-in permission screen.

---

## Requirements

| | Minimum |
|--|---------|
| React Native | **0.74.0+** (New Architecture recommended; default on RN 0.76+) |
| React | **18.2+** |
| Node | **18+** (library tooling) |
| iOS | Library fallback **13.4+**; host RN floor often **15.1+** (RN 0.76+) via `min_ios_version_supported` |
| Xcode | **≥ 15.1** (RN 0.74–0.80); **≥ 16.1** (RN 0.81+) |
| Android | **API 24+** (Android 7.0) |
| Kotlin (host) | Align with RN template (library fallback **2.1.20**) |
| Swift | **5.0** language mode |
| CameraX | **1.6.1** (bundled) |
| ML Kit barcode | **17.3.0** bundled model |

Enable New Architecture (Android):

```properties
# android/gradle.properties
newArchEnabled=true
```

Full upgrade table, ML Kit bundled vs unbundled, and dependency notes: **[DEPENDENCY_AUDIT.md](./DEPENDENCY_AUDIT.md)**.

---

## Installation

```bash
npm install react-native-fabric-barcode-scanner
# or
yarn add react-native-fabric-barcode-scanner
# or
pnpm add react-native-fabric-barcode-scanner
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

The library declares camera permission. Ensure it merges into your app (or add explicitly):

```xml
<uses-permission android:name="android.permission.CAMERA" />
```

Confirm `newArchEnabled=true` in `android/gradle.properties`.

<details>
<summary>Local <code>file:</code> / Metro link (advanced)</summary>

If you link this repo with `file:../path` and `watchFolders`, Metro can resolve the library’s nested `node_modules/react-native` and break TurboModules (`PlatformConstants` / `ScannerModule` not found). Force the **app’s** single copy:

```js
// metro.config.js
const path = require('path');
const localScannerPath = path.resolve(__dirname, '../react-native-custom-scanner');

module.exports = {
  // ...
  resolver: {
    nodeModulesPaths: [path.resolve(__dirname, 'node_modules')],
    extraNodeModules: {
      'react-native-fabric-barcode-scanner': localScannerPath,
      react: path.resolve(__dirname, 'node_modules/react'),
      'react-native': path.resolve(__dirname, 'node_modules/react-native'),
    },
    blockList: [
      new RegExp(
        `${localScannerPath.replace(/[/\\]/g, '[/\\\\]')}/node_modules/react(/.*)?$`
      ),
      new RegExp(
        `${localScannerPath.replace(/[/\\]/g, '[/\\\\]')}/node_modules/react-native(/.*)?$`
      ),
    ],
  },
};
```

Adjust `localScannerPath` to your checkout. Rebuild the native app after Metro changes.

</details>

---

## Quick start

1. Install the package and run `pod install` (iOS).
2. Add `NSCameraUsageDescription` / confirm Android `CAMERA` permission.
3. Enable New Architecture on Android.
4. Request permission with `useCameraPermission()`, then mount `ScannerView`.

```tsx
import { ScannerView, useCameraPermission } from 'react-native-fabric-barcode-scanner';

const { isGranted, isLoading, canRequest, request } = useCameraPermission();

if (isLoading) return null;
if (!isGranted && canRequest) {
  return <Button title="Allow camera" onPress={() => { void request(); }} />;
}
if (!isGranted) return null;

return (
  <ScannerView
    style={{ flex: 1 }}
    isActive
    scanFormats={['qr', 'ean13']}
    onBarcodeScanned={(e) => console.log(e.nativeEvent.value)}
  />
);
```

---

## Basic example

The library normalizes **permission status**. You build the **UI** and decide when to call `request()`.

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
    return null; // or your spinner
  }

  if (isGranted) {
    return (
      <ScannerView
        style={{ flex: 1 }}
        isActive
        scanFormats={['qr', 'ean13', 'code128']}
        onBarcodeScanned={(event) => {
          const { value, type, timestamp } = event.nativeEvent;
          console.log(value, type, timestamp);
        }}
      />
    );
  }

  if (canRequest) {
    return (
      <Button title="Allow camera" onPress={() => { void request(); }} />
    );
  }

  if (canOpenSettings) {
    return (
      <View>
        <Text>Camera access is blocked. Enable it from settings.</Text>
        <Button title="Open settings" onPress={openSettings} />
      </View>
    );
  }

  return null;
}
```

The hook refreshes automatically when the app returns to the foreground (e.g. after Settings).

---

## API

### `ScannerView` props

| Prop | Type | Default | Description |
|------|------|---------|-------------|
| `isActive` | `boolean` | `true` | Start / stop the camera session |
| `torchOn` | `boolean` | `false` | Enable device torch |
| `scanFormats` | `string[]` | all supported | Formats to decode |
| `onBarcodeScanned` | `(event) => void` | — | Fired while a code is visible |
| `style` / `ViewProps` | — | — | Standard React Native view props |

**`event.nativeEvent`:**

| Field | Type | Description |
|-------|------|-------------|
| `value` | `string` | Decoded payload |
| `type` | `string` | Format id (e.g. `qr`, `ean13`) |
| `timestamp` | `number` | Event timestamp |

```tsx
onBarcodeScanned={(event) => {
  const { value, type, timestamp } = event.nativeEvent;
}}
```

> Scans fire **continuously** while a code stays in frame. Debounce or gate in JS if you need a single accept.

### `useCameraPermission()`

| Field / method | Type | Description |
|----------------|------|-------------|
| `status` | `CameraPermissionStatus` | See table below |
| `isGranted` | `boolean` | `status === 'granted'` |
| `canRequest` | `boolean` | Show your Allow UI → `request()` |
| `canOpenSettings` | `boolean` | Show your Settings UI → `openSettings()` |
| `isLoading` | `boolean` | True until first check finishes |
| `request()` | `() => Promise<status>` | System permission dialog |
| `openSettings()` | `() => void` | Opens OS app settings |
| `refresh()` | `() => Promise<state>` | Manual re-check |

| Status | Meaning | Typical UI |
|--------|---------|------------|
| `granted` | Camera allowed | `<ScannerView />` |
| `not-determined` | Never asked | Allow → `request()` |
| `denied` | Soft deny (can ask again) | Allow → `request()` |
| `blocked` / `restricted` | Must use Settings | Settings → `openSettings()` |

**Platform mapping (same JS API):**

| Scenario | iOS | Android |
|----------|-----|---------|
| Never asked | `not-determined` | `not-determined` |
| Allowed | `granted` | `granted` |
| Denied, can ask again | — | `denied` |
| Permanently denied | `blocked` | `blocked` |
| Device policy | `restricted` | — |

### Imperative permission API

Prefer the hook. These remain for non-React or legacy code:

| Method | Description |
|--------|-------------|
| `getCameraPermissionState()` | `{ status, canRequest, canOpenSettings }` |
| `requestCameraPermission()` | Imperative request |
| `openCameraSettings()` | Imperative settings |

### Torch

| Method | Description |
|--------|-------------|
| `ScannerView` `torchOn` | Prefer when the view is mounted |
| `setTorchEnabled(enabled)` | Toggle torch outside the view |

```tsx
import { setTorchEnabled } from 'react-native-fabric-barcode-scanner';

setTorchEnabled(true);
```

### Exports

```ts
import {
  ScannerView,
  useCameraPermission,
  getCameraPermissionState,
  requestCameraPermission,
  openCameraSettings,
  setTorchEnabled,
  type BarcodeScannedEvent,
  type CameraPermissionStatus,
  type CameraPermissionState,
  type UseCameraPermissionResult,
} from 'react-native-fabric-barcode-scanner';
```

---

## Supported formats

`qr` · `ean13` · `ean8` · `upce` · `code128` · `code39` · `code93` · `pdf417` · `aztec` · `datamatrix` · `itf14` · `interleaved2of5`

```tsx
scanFormats={['qr', 'ean13', 'code128']}
```

Omit `scanFormats` to allow all supported formats (subject to native decoder support).

---

## Architecture

| Layer | Name |
|-------|------|
| TurboModule | `ScannerModule` |
| Fabric component | `ScannerView` |
| Codegen spec | `RNCustomScannerSpec` |

**Native stacks**

| Platform | Stack |
|----------|--------|
| iOS | AVFoundation |
| Android | CameraX **1.6.1** + ML Kit barcode **17.3.0** (bundled) |

Legacy architecture: Android paper fallbacks in `android/src/oldarch` share the same CameraX / ML Kit dependencies — no separate Old-Arch-only Maven artifacts.

Dependency policy & Dependabot: **[DEPENDENCY_AUDIT.md](./DEPENDENCY_AUDIT.md)** · `.github/dependabot.yml`.

---

## Best practices

1. **Gate the camera** — only mount `ScannerView` when `isGranted` is true.
2. **Own the permission UX** — match your brand; the library does not ship screens.
3. **Debounce scans** — `onBarcodeScanned` can fire repeatedly for the same code.
4. **Pause when hidden** — set `isActive={false}` on blur / modal dismiss to free the camera.
5. **Limit formats** — smaller `scanFormats` lists can reduce work on busy frames.
6. **Test real devices** — simulators / emulators often lack reliable camera + barcode paths.
7. **New Architecture** — keep `newArchEnabled=true` aligned with how you develop and ship.

---

## Troubleshooting

### `ScannerModule` / `PlatformConstants` could not be found

Usually a **duplicate React Native** when using a local `file:` link. Apply the [Metro `extraNodeModules` / `blockList`](#installation) setup, clear Metro cache, and rebuild the native app.

```bash
npx react-native start --reset-cache
```

### Camera permission never appears (Android)

Ensure an Activity is ready before `request()` (the library waits for `currentActivity`). Confirm `CAMERA` is in the merged manifest and New Arch is enabled if you rely on the Fabric path.

### iOS crash / missing usage string

Add `NSCameraUsageDescription` to `Info.plist`, then clean and rebuild.

### Black preview / no scans

- Confirm `isGranted` and `isActive={true}`
- Check `scanFormats` includes the symbology on the label
- Test lighting and focus on a physical device
- Try omitting `scanFormats` once to rule out filter mismatch

### Torch does nothing

Some devices / front cameras lack a torch. Prefer `torchOn` on `ScannerView` while mounted; use `setTorchEnabled` only when appropriate.

### Pods / codegen issues after upgrade

```bash
cd ios && pod install --repo-update
# Xcode: Product → Clean Build Folder, then rebuild
```

---

## FAQ

**Do I need New Architecture?**  
Strongly recommended. The library is built for Fabric + TurboModules. Android includes old-arch fallbacks, but New Arch is the supported path going forward.

**Does this package render a permission screen?**  
No. Use `useCameraPermission()` and build your own UI.

**Why do I get many `onBarcodeScanned` events?**  
Scanning is continuous while a code is visible. Debounce or accept-once in your screen.

**Can I use this with Expo?**  
Use a **dev client / prebuild** workflow with native modules. It is not a pure Expo Go JS module.

**Where is the full dependency upgrade matrix?**  
See [DEPENDENCY_AUDIT.md](./DEPENDENCY_AUDIT.md).

**I used old boolean permission helpers — what changed?**  
See [MIGRATION_PERMISSION_API.md](./MIGRATION_PERMISSION_API.md).

---

## Migration

Permission API changes (removed boolean helpers → status-based API):

→ **[MIGRATION_PERMISSION_API.md](./MIGRATION_PERMISSION_API.md)**

Dependency / ML Kit notes:

→ **[DEPENDENCY_AUDIT.md](./DEPENDENCY_AUDIT.md)**

---

## Contributing

Issues and PRs: [github.com/Rajubarde12/react-native-fabric-barcode-scanner](https://github.com/Rajubarde12/react-native-fabric-barcode-scanner)

```bash
git clone https://github.com/Rajubarde12/react-native-fabric-barcode-scanner.git
cd react-native-fabric-barcode-scanner
npm install
npm run typecheck
npm run build
```

Please open an issue before large native changes. Keep PRs focused and document API changes.

<details>
<summary>Maintainer: publish to npm</summary>

```bash
npm whoami
npm run typecheck
npm run build
npm publish --access public

# verify
npm view react-native-fabric-barcode-scanner version
```

Current package version is in `package.json` (e.g. `2.0.1`). Use `npm version patch|minor|major` for later releases.

</details>

---

## License

MIT — see [LICENSE](./LICENSE).

---

## Links

- **npm:** [react-native-fabric-barcode-scanner](https://www.npmjs.com/package/react-native-fabric-barcode-scanner)
- **GitHub:** [Rajubarde12/react-native-fabric-barcode-scanner](https://github.com/Rajubarde12/react-native-fabric-barcode-scanner)
- **Issues:** [Report a bug](https://github.com/Rajubarde12/react-native-fabric-barcode-scanner/issues)

---

## Docs wishlist

| Item | Status |
|------|--------|
| Screenshots / GIF of scan flow | Not included yet |
| Before/after permission UI examples | Example code only |
| Performance notes (format filters, frame rate) | Best-practices summary only |
| Expo config plugin | Not shipped |
| Comparison vs `vision-camera` / other scanners | High-level table above |
