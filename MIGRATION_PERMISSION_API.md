# Permission API

**Recommended:** `useCameraPermission()`  
**Imperative:** `getCameraPermissionState()` / `requestCameraPermission()` / `openCameraSettings()`

The hook is a thin React wrapper: it reads native status, normalizes it into UI flags, and refreshes on AppState → active. It does **not** auto-show the system dialog — call `request()` from your UI.

Legacy helpers (`checkCameraPermission`, `isCameraPermissionGranted`, `canRequestCameraPermission`, `shouldOpenCameraSettings`) were **removed**.

---

## Which function should I use?

| I need… | Use |
|---------|-----|
| Permission in a React screen | **`useCameraPermission()`** |
| One-shot check outside React | **`getCameraPermissionState()`** |
| Open OS settings without the hook | `openCameraSettings()` |

---

## Migrate from removed helpers

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
+ if (canRequest) return <Button onPress={request} ... />; // your UI
+ if (canOpenSettings) return <Button onPress={openSettings} ... />; // your UI
```

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

---

## Platform status mapping

| Scenario | iOS | Android |
|----------|-----|---------|
| Never asked | `not-determined` | `not-determined` |
| Allowed | `granted` | `granted` |
| Soft deny (can ask again) | — | `denied` |
| Permanent deny / Don't ask again | `blocked` | `blocked` |
| Device / parental policy | `restricted` | — |

On iOS, the first user deny is permanent for the system dialog → `blocked` (`canOpenSettings`).  
On Android, soft deny is `denied` (`canRequest`); "Don't ask again" is `blocked`.
