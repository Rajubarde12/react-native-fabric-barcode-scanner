import AVFoundation
import UIKit

private final class MetadataOutputHandler: NSObject, AVCaptureMetadataOutputObjectsDelegate {
  var onBarcodeScanned: ((NSDictionary) -> Void)?

  func metadataOutput(_ output: AVCaptureMetadataOutput,
                       didOutput metadataObjects: [AVMetadataObject],
                       from connection: AVCaptureConnection) {
    for object in metadataObjects {
      guard let readable = object as? AVMetadataMachineReadableCodeObject,
            let value = readable.stringValue else { continue }
      onBarcodeScanned?([
        "value": value,
        "type": RNScannerHelper.name(for: readable.type),
        "timestamp": Int32(Date().timeIntervalSince1970 * 1000)
      ] as NSDictionary)
    }
  }
}

@objc(RNScannerView)
public class RNScannerView: UIView {

  private let session = AVCaptureSession()
  private var previewLayer: AVCaptureVideoPreviewLayer?
  private let metadataOutput = AVCaptureMetadataOutput()
  private let metadataHandler = MetadataOutputHandler()
  private let sessionQueue = DispatchQueue(label: "com.rncustomscanner.session")
  private var isSessionConfigured = false

  @objc public var isActive: Bool = true {
    didSet {
      sessionQueue.async {
        // Re-try session setup when permission may have been granted after mount.
        if !self.isSessionConfigured {
          self.configureSessionIfNeeded()
        } else {
          self.updateSessionState()
        }
      }
    }
  }
  @objc public var torchOn: Bool = false { didSet { RNScannerHelper().setTorchEnabled(torchOn) } }
  @objc public var scanFormats: [String] = RNScannerHelper.supportedFormats { didSet { applyMetadataFilters() } }

  // Wired up by ScannerViewManager to emit the Fabric event
  @objc public var onBarcodeScanned: ((NSDictionary) -> Void)? {
    didSet { metadataHandler.onBarcodeScanned = onBarcodeScanned }
  }

  public override init(frame: CGRect) {
    super.init(frame: frame)
    sessionQueue.async {
      self.configureSessionIfNeeded()
    }
  }
  public required init?(coder: NSCoder) { fatalError("init(coder:) not implemented") }

  // Only builds the AVCaptureSession if permission is already granted.
  // Requesting permission is entirely JS's responsibility via
  // ScannerModule.requestCameraPermission() — this view never triggers
  // the system permission dialog itself.
  private func configureSessionIfNeeded() {
    guard !isSessionConfigured else {
      updateSessionState()
      return
    }
    guard AVCaptureDevice.authorizationStatus(for: .video) == .authorized else {
      return
    }
    buildSessionGraph()
  }

  private func buildSessionGraph() {
    session.beginConfiguration()
    guard let device = AVCaptureDevice.default(for: .video),
          let input = try? AVCaptureDeviceInput(device: device),
          session.canAddInput(input) else {
      session.commitConfiguration()
      return
    }
    session.addInput(input)
    guard session.canAddOutput(metadataOutput) else {
      session.commitConfiguration()
      return
    }
    session.addOutput(metadataOutput)
    metadataOutput.setMetadataObjectsDelegate(metadataHandler, queue: DispatchQueue.main)
    session.commitConfiguration()
    isSessionConfigured = true

    DispatchQueue.main.async { [weak self] in
      guard let self = self else { return }
      self.applyMetadataFilters()
      let layer = AVCaptureVideoPreviewLayer(session: self.session)
      layer.videoGravity = .resizeAspectFill
      layer.frame = self.bounds
      self.layer.addSublayer(layer)
      self.previewLayer = layer
      self.updateSessionState()
    }
  }

  private func applyMetadataFilters() {
    let requested = scanFormats.compactMap { RNScannerHelper.avFormat(for: $0) }
    let available = metadataOutput.availableMetadataObjectTypes
    metadataOutput.metadataObjectTypes = requested.filter { available.contains($0) }
  }

  private func updateSessionState() {
    sessionQueue.async {
      guard self.isSessionConfigured else { return }
      if self.isActive && !self.session.isRunning {
        self.session.startRunning()
      } else if !self.isActive && self.session.isRunning {
        self.session.stopRunning()
      }
    }
  }

  public override func layoutSubviews() {
    super.layoutSubviews()
    previewLayer?.frame = bounds
    // If the view stayed mounted while JS obtained permission, start the session.
    if !isSessionConfigured && isActive {
      sessionQueue.async { [weak self] in
        self?.configureSessionIfNeeded()
      }
    }
  }

  public override func didMoveToWindow() {
    super.didMoveToWindow()
    guard window != nil, isActive else { return }
    sessionQueue.async { [weak self] in
      guard let self = self else { return }
      if !self.isSessionConfigured {
        self.configureSessionIfNeeded()
      } else {
        self.updateSessionState()
      }
    }
  }

  public override func removeFromSuperview() {
    sessionQueue.async { [session] in
      if session.isRunning { session.stopRunning() }
    }
    super.removeFromSuperview()
  }
}
