require "json"

package = JSON.parse(File.read(File.join(__dir__, "package.json")))

# Host RN defines this when Podfile has required react_native_pods.rb (normal RN apps).
# Fallback 13.4 keeps the library installable if the helper is missing (unusual).
ios_deployment_target =
  if defined?(min_ios_version_supported)
    min_ios_version_supported
  else
    "13.4"
  end

Pod::Spec.new do |s|
  s.name         = "react-native-fabric-barcode-scanner"
  s.module_name  = "RNCustomScanner"
  s.version      = package["version"]
  s.summary      = package["description"]
  s.homepage     = package["homepage"]
  s.license      = package["license"]
  s.authors      = package["author"]
  # Tracks host RN floor (13.4 on RN 0.74–0.75, 15.1 on RN 0.76+).
  s.platforms    = { :ios => ios_deployment_target }
  repo_url = package["repository"]["url"].sub(/^git\+/, "")
  s.source       = { :git => repo_url, :tag => "v#{s.version}" }
  # Static framework works with RN's default CocoaPods setup and with
  # `use_frameworks! :linkage => :static`. Dynamic frameworks may need
  # extra header search path tweaks — prefer static linkage.
  # Note: RN's `spm_dependency` helper requires dynamic frameworks; this
  # library has no third-party SPM deps, so static remains correct.
  s.static_framework = true

  s.source_files = "ios/**/*.{h,m,mm,swift}"
  s.resource_bundles = {
    "RNCustomScannerPrivacyInfo" => ["ios/PrivacyInfo.xcprivacy"]
  }
  s.frameworks   = "AVFoundation", "UIKit"

  s.pod_target_xcconfig = {
    "DEFINES_MODULE" => "YES",
    # Swift 5 language mode (Xcode 15.1+ / 16.1+ toolchains used by RN 0.74+)
    "SWIFT_VERSION" => "5.0"
  }

  # ---------------------------------------------------------------------------
  # CocoaPods dependencies — INTENTIONALLY UNPINNED
  # ---------------------------------------------------------------------------
  # Do NOT write:
  #   s.dependency "React-Core", "~> 0.74"
  #   s.dependency "RCT-Folly", "2024.11.18.00"
  #   s.dependency "React-Codegen" / "ReactCodegen"
  #
  # Reasons:
  # 1. RN pods are path-based from node_modules, not CocoaPods CDN versions.
  # 2. Pinning Folly/Codegen to a fixed string breaks on every RN minor (classic
  #    "Could not find compatible versions for pod RCT-Folly" failure mode).
  # 3. install_modules_dependencies(s) (RN ≥ 0.71) injects the correct New Arch
  #    graph for the *host* RN version — including React-Core, React-RCTFabric,
  #    ReactCodegen, RCTRequired, RCTTypeSafety, ReactCommon/turbomodule/*,
  #    Fabric/graphics/utils, Yoga, JS engine, and third-party (RCT-Folly or
  #    ReactNativeDependencies prebuild).
  #
  # See DEPENDENCY_AUDIT.md § iOS for the resolved version table from the
  # consumer app (rajhans_admin @ RN 0.86.0).
  # ---------------------------------------------------------------------------
  if defined?(install_modules_dependencies)
    install_modules_dependencies(s)
  else
    # Legacy fallback only — should not run on RN ≥ 0.71 apps.
    s.dependency "React-Core"
  end
end
