# Permission API — migration guide

**Change type:** Additive (Option B) — non-breaking.  
**Recommended entry point:** `useCameraPermission()`  
**Version:** ship with library **1.1.0** (or next minor).

---

## Before → after (exports)

| Export | Before | After |
|--------|--------|-------|
| `useCameraPermission` | — | **New — recommended** |
| `getCameraPermissionState` | Public | Public (imperative / non-React) |
| `requestCameraPermission` | Public | Public (prefer hook `request()`) |
| `openCameraSettings` | Public | Public (prefer hook `openSettings()`) |
| `checkCameraPermission` | Public | **@deprecated** → use `.status` from state/hook |
| `isCameraPermissionGranted` | Public | **@deprecated** → `status === 'granted'` / `isGranted` |
| `canRequestCameraPermission` | Public | **@deprecated** → `canRequest` field |
| `shouldOpenCameraSettings` | Public | **@deprecated** → `canOpenSettings` field |
| `ScannerView` / `setTorchEnabled` | Unchanged | Unchanged |

Deprecated helpers still work; in `__DEV__` they log a one-time `console.warn` per symbol.

---

## Which function should I use?

| I need… | Use |
|---------|-----|
| Permission UI in a React screen | **`useCameraPermission()`** |
| One-shot check outside React | **`getCameraPermissionState()`** |
| Open OS settings without the hook | `openCameraSettings()` |
| Raw status string only | `getCameraPermissionState()` then `.status` (not `checkCameraPermission`) |
| Boolean helpers | Prefer fields on the state object / hook — not the deprecated top-level functions |

---

## Migrate existing code

### From boolean helpers

```diff
- import {
-   checkCameraPermission,
-   isCameraPermissionGranted,
-   canRequestCameraPermission,
-   shouldOpenCameraSettings,
-   requestCameraPermission,
-   openCameraSettings,
- } from 'react-native-fabric-barcode-scanner';
-
- const status = await checkCameraPermission();
- if (isCameraPermissionGranted(status)) { ... }
- if (canRequestCameraPermission(status)) await requestCameraPermission();
- if (shouldOpenCameraSettings(status)) openCameraSettings();

+ import { useCameraPermission, ScannerView } from 'react-native-fabric-barcode-scanner';
+
+ const { isGranted, canRequest, canOpenSettings, request, openSettings } =
+   useCameraPermission();
+ if (isGranted) return <ScannerView ... />;
+ if (canRequest) return <Button onPress={request} ... />;
+ if (canOpenSettings) return <Button onPress={openSettings} ... />;
```

### From `getCameraPermissionState` + `useEffect` (still valid)

No change required. Optionally switch to the hook to get AppState refresh for free (Settings → back).

### Imperative / non-React

```ts
const { status, canRequest, canOpenSettings } = await getCameraPermissionState();
```

---

## Hook return shape

```ts
{
  status: CameraPermissionStatus;
  canRequest: boolean;
  canOpenSettings: boolean;
  isLoading: boolean;
  isGranted: boolean;
  refresh: () => Promise<CameraPermissionState>;
  request: () => Promise<CameraPermissionStatus>;
  openSettings: () => void;
}
```

Auto-refreshes when `AppState` becomes `'active'` (covers return-from-Settings).
