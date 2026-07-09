# Dependency Audit — react-native-fabric-barcode-scanner

**Date:** 2026-07-09  
**Scope:** JS/npm, Android Gradle, iOS CocoaPods  
**npm audit:** `0 vulnerabilities` (after install)

---

## Summary of changes applied

| Area | Change |
|------|--------|
| CameraX | `1.4.2` → **`1.6.1`** (latest stable) |
| AppCompat | **Removed** — `ScannerView` now binds via `LifecycleOwner` |
| lifecycle-runtime-ktx | **Removed explicit pin** — comes transitively via CameraX |
| ML Kit barcode | **Hold** at bundled `17.3.0` (latest bundled); unbundled option documented |
| peer `react` | `*` → **`>=18.2.0`** |
| peer `react-native` | keep **`>=0.74.0`** |
| TypeScript | pin **`~5.9.3`** (stable 5.x; TS 7 available but hold for RN ecosystem) |
| Build pipeline | **`react-native-builder-bob`** → emits `lib/` (CJS + ESM + `.d.ts`) |
| Dependabot | added `.github/dependabot.yml` |
| Podspec | Unpinned RN deps + `install_modules_dependencies`; `min_ios_version_supported` when available; `AVFoundation`/`UIKit`; static framework |
| iOS deployment | Tracks host RN (`13.4` fallback / `15.1` on RN 0.76+) via `min_ios_version_supported` |

---

## 1. Dependency comparison table

### JS / npm

| Name | Current (before) | Latest stable (audit) | Breaking if upgraded? | Recommendation |
|------|------------------|----------------------|------------------------|----------------|
| `react` (peer) | `*` | 19.x (app-owned) | N/A (peer) | **Upgrade range** → `>=18.2.0` (done). Do not bundle. |
| `react-native` (peer) | `>=0.74.0` | 0.86.x | Library APIs still target 0.74+ New Arch | **Hold** `>=0.74.0`. Raising floor to 0.76 would drop iOS 13.4 consumers. |
| `typescript` (dev) | `^5.0.0` | 5.9.3 / 7.0.2 | TS 7 may need tooling updates | **Upgrade now** to `~5.9.3`. Hold TS 7 until RN templates catch up. |
| `@types/react` (dev) | `^19.0.0` | Align with RN 0.76 peer (`react@18`) | Peer conflict if React 19 + RN 0.76 | **Pin `^18.3.12`** with `react@18.3.1` for local bob/typecheck. Apps on RN 0.79+ / React 19 can still consume via peers. |
| `react-native-builder-bob` (dev) | — (missing) | 0.40.x | New pipeline | **Add now** — ships compiled `lib/` instead of raw `.tsx` entry for npm consumers. |
| ESLint / Prettier | — | — | — | **Hold** — not required for library publish; add later if team wants lint CI. |
| Codegen CLI | — | via host RN | — | **No separate dep** — RN app runs codegen. |

**peer vs dependencies:** `react` / `react-native` correctly stay **peer only** (never `dependencies`). No runtime npm `dependencies` needed for this native module.

**Duplicate / overlapping:** Previously none. After bob, ensure only one `typescript` in lockfile (dev).

**Entry fields (before → after):**

| Field | Before | After |
|-------|--------|-------|
| `main` | `src/index.tsx` | `lib/commonjs/index.js` |
| `module` | — | `lib/module/index.js` |
| `types` | `src/index.tsx` | `lib/typescript/src/index.d.ts` |
| `react-native` / `source` | `src/index.tsx` | `src/index.tsx` (Metro still uses source) |

---

### Android

| Name | Current (before) | Latest stable | Breaking? | Recommendation |
|------|------------------|---------------|-----------|----------------|
| CameraX (`camera-core/camera2/lifecycle/view`) | 1.4.2 | **1.6.1** | Low–medium (API polish; default minSdk trend 23) | **Upgrade now** to 1.6.1 (done). Test preview bind + torch. |
| ML Kit `barcode-scanning` (bundled) | 17.3.0 | **17.3.0** | — | **Hold** — already latest bundled. |
| Play Services ML Kit barcode (unbundled) | — | 18.3.1 | Smaller APK; needs Play Services model download | **Optional** — document only; keep bundled for offline/reliability. |
| `appcompat` | 1.7.0 | 1.7.1 | — | **Remove** (done) — was only used for `AppCompatActivity` cast. |
| `lifecycle-runtime-ktx` | 2.8.7 | 2.11.0 | Possible AGP/Compose coupling in 2.11 | **Remove explicit** (done); rely on CameraX transitive. |
| `kotlin-stdlib` | host / 2.1.20 fallback | host-owned | — | **Keep** `safeExtGet("kotlinVersion", ...)` — do not pin above host RN. |
| `react-android` | host | host | — | **Keep** unresolved version from RN Gradle plugin. |
| minSdk | 24 | CameraX OK ≥23; RN 0.76 floor 24 | — | **Hold API 24**. |
| compileSdk / targetSdk fallback | 36 | 36 | — | **Hold** (host overrides). |

**AppCompat still needed?** No — after casting to `LifecycleOwner`. RN `ReactActivity` is a `LifecycleOwner`.

**Version catalog:** For a single-module library, a top-of-`build.gradle` version block is enough. Full `libs.versions.toml` is optional; Dependabot Gradle ecosystem covers Maven coords.

**Transitive conflicts:** Run in a host app:

```bash
./gradlew :react-native-fabric-barcode-scanner:dependencies --configuration releaseRuntimeClasspath
```

(Standalone library folder has no Gradle wrapper — audit in the consuming RN app.)

---

### iOS / CocoaPods — detailed

#### Podspec source of truth (what we declare)

File: `react-native-fabric-barcode-scanner.podspec`

| Declared in podspec | Version pin? | Notes |
|---------------------|--------------|-------|
| `s.platforms[:ios]` | Dynamic | `min_ios_version_supported` from host RN when available, else `"13.4"` |
| `s.frameworks` | System | `AVFoundation`, `UIKit` (no CocoaPods version) |
| `s.static_framework` | `true` | Compatible with default RN + `use_frameworks! :linkage => :static` |
| `SWIFT_VERSION` | `"5.0"` | Language mode, not a Swift toolchain pin |
| `s.dependency "React-Core"` | **None** (unpinned) | Only used in legacy fallback if helper missing |
| `install_modules_dependencies(s)` | N/A | Injects full New Arch graph from **host** RN |

**There are no `~> 0.74` / `= 0.86.0` / Folly date pins in this podspec.** That is intentional.

#### What `install_modules_dependencies` actually adds (RN 0.86 helper)

From `node_modules/react-native/scripts/cocoapods/new_architecture.rb` → `NewArchitectureHelper.install_modules_dependencies`:

| Injected dependency | Role | Version source |
|---------------------|------|----------------|
| `React-Core` | Core RN | Host RN (`0.86.0` in consumer) |
| `React-RCTFabric` | Fabric components | Host RN |
| `ReactCodegen` | Codegen artifacts for this app | Host RN (name is `ReactCodegen`, not legacy `React-Codegen`) |
| `RCTRequired` | Required native types | Host RN |
| `RCTTypeSafety` | Type safety | Host RN |
| `ReactCommon/turbomodule/bridging` | TurboModule bridging | Host RN |
| `ReactCommon/turbomodule/core` | TurboModule core | Host RN |
| `React-NativeModulesApple` | Apple TM runtime | Host RN |
| `Yoga` | Layout | Host RN (`0.0.0` placeholder in lock) |
| `React-Fabric` | Fabric renderer | Host RN |
| `React-graphics` | Graphics | Host RN |
| `React-utils` / `React-featureflags` / `React-debug` | Shared RN libs | Host RN |
| `React-ImageManager` / `React-rendererdebug` / `React-renderercss` / `React-jsi` | Renderer / JSI | Host RN |
| JS engine (`depend_on_js_engine`) | Hermes (default) | Host RN (`hermes-engine` prebuilt) |
| Third-party (`add_rn_third_party_dependencies`) | Folly **or** prebuilt umbrella | See below |

**Third-party / Folly on modern RN (0.76+ prebuilds):**

| Mode | Dependency | Resolved in `rajhans_admin` (RN 0.86.0) |
|------|------------|----------------------------------------|
| Prebuilt deps (default, `RCT_USE_RN_DEP=1`) | `ReactNativeDependencies` | **`0.86.0`** (umbrella; Folly/fmt/glog/etc. inside) |
| Build-from-source | `RCT-Folly` (+ `/Fabric` when Fabric) | Folly config version **`2024.11.18.00`** in RN 0.86 `helpers.rb` |

Pinning `RCT-Folly` yourself to that date string **will break** consumers on other RN minors and on prebuild mode (pod may not exist as a standalone spec).

#### Consumer dump — `rajhans_admin` (`react-native@0.86.0`)

| Item | Value |
|------|-------|
| App | `/Users/forebear/Desktop/Flutter/rajhans_admin` |
| CocoaPods | **1.16.2** (`Podfile.lock`) |
| Podfile platform | `platform :ios, min_ios_version_supported` → **15.1** (RN 0.86) |
| Xcode on machine | **26.2** (Build 17C52) — above RN 0.86 min **16.1** |
| Scanner in `Podfile.lock`? | **Not yet** — npm has package, but iOS pods not re-installed after add; run `pod install` to link |

**Resolved RN pod versions (from `ios/Podfile.lock`, all path-based → `../node_modules/react-native/`):**

| Pod | Resolved version | Behind? |
|-----|------------------|---------|
| `React-Core` | 0.86.0 | No — matches `package.json` |
| `React-Core-prebuilt` | 0.86.0 | No |
| `ReactCodegen` | 0.86.0 | No |
| `React-RCTFabric` | 0.86.0 | No |
| `RCTRequired` / `RCTTypeSafety` | 0.86.0 | No |
| `ReactCommon` (+ turbomodule) | 0.86.0 | No |
| `React-Fabric` / `React-graphics` / … | 0.86.0 | No |
| `ReactNativeDependencies` | 0.86.0 | No (prebuilt Folly stack) |
| `hermes-engine` | 250829098.0.14 | Tied to this RN build |
| `Yoga` | 0.0.0 | Normal RN lock placeholder |
| `FBLazyVector` | 0.86.0 | No |

**`pod outdated`:** Could not complete in this environment (`use_native_modules!` needs write access to `ios/build/generated/autolinking`). For path-based React Native pods, `pod outdated` is usually **not meaningful** anyway — versions come from `node_modules/react-native`, not the CocoaPods trunk. To refresh:

```bash
cd ios
bundle install   # if Gemfile pins cocoapods
bundle exec pod install
# Optional CDN check (third-party pods only):
bundle exec pod outdated
```

Upgrade RN with npm/yarn (`react-native@x.y.z`), then `pod install` — do **not** try to bump `React-Core` via CocoaPods alone.

#### Pinning analysis (too loose vs too tight)

| Strategy | Example | Verdict for this library |
|----------|---------|---------------------------|
| Unpinned + `install_modules_dependencies` | current | **Correct** — follows host RN |
| Loose CocoaPods range | `s.dependency "React-Core", "~> 0.74"` | **Bad** — RN pods are path specs; range fights the lockfile / breaks minors |
| Tight pin | `s.dependency "RCT-Folly", "2024.11.18.00"` | **Bad** — classic Folly mismatch; breaks prebuild (`ReactNativeDependencies`) |
| Duplicate manual New Arch list | `React-Codegen`, `RCT-Folly`, … | **Bad on RN 0.84+** — specs renamed/removed from CDN |

**Recommendation:** keep unpinned; never add Folly/Codegen version strings to this podspec.

#### CocoaPods vs Swift Package Manager (SPM)

| Question | Answer |
|----------|--------|
| Does RN still use CocoaPods for libraries? | **Yes** — primary integration path through 0.86+ |
| Does RN support SPM *dependencies inside* a podspec? | **Yes from RN 0.75** via `spm_dependency(...)` helper |
| Does this library need SPM? | **No today** — only system frameworks (`AVFoundation`, `UIKit`) + RN pods |
| Should we add a `Package.swift` for the scanner itself? | **Hold / future** — RN has not replaced CocoaPods for third-party native modules yet; dual-publish is optional future-proofing |
| Conflict with `static_framework`? | RN docs: SPM helper works best with **dynamic** frameworks. Our `static_framework = true` is fine **because we declare no SPM deps**. If we later add SPM-only SDKs, revisit linkage. |

Future-proof stub (only if a real SPM-only dependency appears):

```ruby
if defined?(spm_dependency)
  # spm_dependency(s, url: '...', requirement: {kind: 'upToNextMajorVersion', minimumVersion: 'x.y.z'}, products: ['Product'])
end
```

#### Xcode / Swift / iOS floor matrix

| RN version | Min iOS (RN) | Min Xcode (RN support table / helpers) | This library |
|------------|--------------|----------------------------------------|--------------|
| 0.74 – 0.75 | 13.4 | Xcode **15.1** | Podspec fallback `13.4`; Swift 5.0 OK |
| 0.76 – 0.80 | **15.1** | Xcode **15.1** | Host `min_ios_version_supported` → 15.1 |
| 0.81 – 0.86 | **15.1** | Xcode **16.1** (`helpers.rb` on 0.86) | Same; consumer machine has Xcode 26.2 ✓ |

**Mismatch risk:** Documenting “iOS 13.4 + New Arch + Xcode 14” would be wrong for RN 0.74+ (needs **Xcode ≥ 15.1**). For RN 0.86 consumers, require **Xcode ≥ 16.1**. Swift 5 language mode remains valid on those Xcodes.

#### iOS comparison table (same shape as Android)

| Name | Declared by library | Resolved @ consumer RN 0.86 | Latest / note | Recommendation |
|------|---------------------|----------------------------|---------------|----------------|
| Deployment target | `min_ios_version_supported` or 13.4 | **15.1** | RN-owned | **Track host** (done) |
| Swift language mode | 5.0 | 5.x toolchain | — | **Hold** |
| `React-Core` | via helper (unpinned) | 0.86.0 | = app RN | **Do not pin** |
| `ReactCodegen` | via helper | 0.86.0 | = app RN | **Do not pin** |
| `React-RCTFabric` | via helper | 0.86.0 | = app RN | **Do not pin** |
| `RCTRequired` / `RCTTypeSafety` | via helper | 0.86.0 | = app RN | **Do not pin** |
| `ReactCommon/turbomodule/*` | via helper | 0.86.0 | = app RN | **Do not pin** |
| `ReactNativeDependencies` | via helper (prebuild) | 0.86.0 | replaces standalone Folly | **Do not pin Folly** |
| Folly (source mode) | via helper | config `2024.11.18.00` | RN-owned | **Do not pin** |
| `hermes-engine` | via helper | 250829098.0.14 | RN-owned | **Do not pin** |
| `AVFoundation` / `UIKit` | system frameworks | OS | — | **Keep** |
| Third-party CocoaPods | none | — | — | N/A |
| SPM packages | none | — | — | **Hold** until needed |

**Deprecated / unmaintained?** No iOS third-party pods. Relying on manual `React-Codegen` / CDN `RCT-Folly` pins **is** the deprecated pattern — we avoid it.

---

## 2. ML Kit: bundled vs unbundled

| | Bundled `com.google.mlkit:barcode-scanning:17.3.0` | Unbundled `play-services-mlkit-barcode-scanning:18.3.1` |
|--|---------------------------------------------------|--------------------------------------------------------|
| APK size | Larger (~model in APK) | Smaller |
| Offline | Works offline | Needs Play Services download (first use may fail until ready) |
| Devices | All | Google Play devices preferred |
| This library | **Default (kept)** | Swap in `android/build.gradle` if product needs smaller APK |

---

## 3. Migration notes (consumers)

### From previous published layout (raw `src/index.tsx` as `main`)

1. **No app code changes** if you import public API the same way.
2. After upgrade, npm package includes `lib/` — Metro still prefers `"react-native": "src/index.tsx"`.
3. TypeScript consumers get proper `.d.ts` from `types` field.
4. Rebuild host app after Android CameraX bump:
   ```bash
   cd android && ./gradlew clean && cd ..
   ```
5. iOS: `pod install` (podspec comment-only + frameworks; usually no API break).
6. If you relied on **AppCompat** being pulled only by this library — unlikely; RN apps already ship it. We no longer force AppCompat.

### Breaking for library maintainers

- `npm publish` / `prepare` runs `bob build` — Node 18+ required.
- Commit or generate `lib/` on publish (CI should run `npm run prepare`).
- `lib/` is gitignored; publish tarball still includes it via `"files": ["lib", ...]`.

---

## 4. Automated dependency updates

File: `.github/dependabot.yml`

- **npm** — weekly (groups TS / bob; ignores peer `react` / `react-native`)
- **gradle** — weekly under `/android`
- **github-actions** — monthly

Optional later: Renovate with same ignore rules if the org prefers it over Dependabot.

---

## 5. Minimum supported versions (canonical)

| Constraint | Value |
|------------|-------|
| React Native | ≥ 0.74.0 |
| React | ≥ 18.2.0 |
| Node (build) | ≥ 18 |
| iOS (library podspec) | `min_ios_version_supported` or ≥ 13.4 |
| iOS (RN 0.76+ apps) | ≥ 15.1 (app Podfile / RN helper) |
| Xcode (RN 0.74–0.80) | ≥ 15.1 |
| Xcode (RN 0.81–0.86) | ≥ 16.1 |
| Android minSdk | ≥ 24 |
| New Architecture | Recommended; default on RN 0.76+ |
| Kotlin (fallback) | 2.1.20 (host overrides) |
| Swift language mode | 5.0 |
| CameraX | 1.6.1 |
| ML Kit barcode (bundled) | 17.3.0 |

---

## 6. Follow-ups (not done in this pass)

- [ ] Run CameraX 1.6.1 on device: cold start, resume, torch, multi-format scan.
- [ ] Play Console 16 KB page-size check on release AAB (CameraX ≥1.4.2 helps; re-verify with 1.6.1).
- [ ] Optional: switch to unbundled ML Kit if APK size is critical.
- [ ] Consumer `cd ios && pod install` so `react-native-fabric-barcode-scanner` appears in `Podfile.lock` (currently npm-linked but not pod-installed in rajhans_admin).
- [ ] Optional: raise peer RN floor to 0.76 when dropping iOS 13–14 / RN 0.74–0.75.
- [ ] Optional: remove `android/src/oldarch` when dropping Paper support.
- [ ] Optional: add `Package.swift` only if RN ecosystem moves libraries fully to SPM (not required now).
- [ ] Add example app CI that runs `./gradlew :app:dependencies` for conflict detection.
