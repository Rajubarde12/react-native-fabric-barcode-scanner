import AVFoundation
import Foundation
import UIKit

@objc(RNScannerHelper)
public class RNScannerHelper: NSObject {

  @objc public static var supportedFormats: [String] {
    return ["qr", "ean13", "ean8", "upce", "code128", "code39",
            "code93", "pdf417", "aztec", "datamatrix", "itf14", "interleaved2of5"]
  }

  // NOTE: intentionally NOT @objc — AVMetadataObject.ObjectType does not
  // bridge cleanly to Objective-C in the generated -Swift.h header and
  // causes "expected a type" build errors if exposed. These are internal
  // Swift-only helpers; call them only from other Swift code.
  internal static func avFormat(for name: String) -> AVMetadataObject.ObjectType? {
    switch name {
    case "qr": return .qr
    case "ean13": return .ean13
    case "ean8": return .ean8
    case "upce": return .upce
    case "code128": return .code128
    case "code39": return .code39
    case "code93": return .code93
    case "pdf417": return .pdf417
    case "aztec": return .aztec
    case "datamatrix": return .dataMatrix
    case "itf14": return .itf14
    case "interleaved2of5": return .interleaved2of5
    default: return nil
    }
  }

  internal static func name(for avType: AVMetadataObject.ObjectType) -> String {
    switch avType {
    case .qr: return "qr"
    case .ean13: return "ean13"
    case .ean8: return "ean8"
    case .upce: return "upce"
    case .code128: return "code128"
    case .code39: return "code39"
    case .code93: return "code93"
    case .pdf417: return "pdf417"
    case .aztec: return "aztec"
    case .dataMatrix: return "datamatrix"
    case .itf14: return "itf14"
    case .interleaved2of5: return "interleaved2of5"
    default: return "unknown"
    }
  }

  public override init() {
    super.init()
  }

  @objc public func checkCameraPermission() -> String {
    switch AVCaptureDevice.authorizationStatus(for: .video) {
    case .authorized: return "granted"
    case .denied: return "blocked"
    case .restricted: return "restricted"
    case .notDetermined: return "not-determined"
    @unknown default: return "not-determined"
    }
  }

  @objc public func requestCameraPermission(_ completion: @escaping (String) -> Void) {
    let status = AVCaptureDevice.authorizationStatus(for: .video)

    switch status {
    case .authorized:
      completion("granted")
    case .denied:
      completion("blocked")
    case .restricted:
      completion("restricted")
    case .notDetermined:
      AVCaptureDevice.requestAccess(for: .video) { granted in
        DispatchQueue.main.async {
          completion(granted ? "granted" : "blocked")
        }
      }
    @unknown default:
      completion("not-determined")
    }
  }

  @objc public func openCameraSettings() {
    DispatchQueue.main.async {
      guard let url = URL(string: UIApplication.openSettingsURLString) else { return }
      UIApplication.shared.open(url)
    }
  }

  @objc public func setTorchEnabled(_ enabled: Bool) {
    guard let device = AVCaptureDevice.default(for: .video), device.hasTorch else { return }
    do {
      try device.lockForConfiguration()
      device.torchMode = enabled ? .on : .off
      device.unlockForConfiguration()
    } catch {
      // Best-effort: torch failures are non-fatal for scanning
    }
  }
}