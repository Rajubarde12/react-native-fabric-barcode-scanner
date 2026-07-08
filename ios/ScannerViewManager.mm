#import "RNCustomScanner-Swift.h"
#import <React/RCTViewComponentView.h>
#import <react/renderer/components/RNCustomScannerSpec/ComponentDescriptors.h>
#import <react/renderer/components/RNCustomScannerSpec/EventEmitters.h>
#import <react/renderer/components/RNCustomScannerSpec/Props.h>
#import <react/renderer/components/RNCustomScannerSpec/RCTComponentViewHelpers.h>

using namespace facebook::react;

@interface ScannerView : RCTViewComponentView <RCTScannerViewViewProtocol>
@end

@implementation ScannerView {
  RNScannerView *_scannerView;
}

+ (facebook::react::ComponentDescriptorProvider)componentDescriptorProvider {
  return concreteComponentDescriptorProvider<ScannerViewComponentDescriptor>();
}

- (instancetype)initWithFrame:(CGRect)frame {
  if (self = [super initWithFrame:frame]) {
    static const auto defaultProps = std::make_shared<const ScannerViewProps>();
    _props = defaultProps;

    _scannerView = [[RNScannerView alloc] initWithFrame:self.bounds];
    __weak ScannerView *weakSelf = self;
    _scannerView.onBarcodeScanned = ^(NSDictionary<NSString *, id> *payload) {
      [weakSelf emitBarcodeScanned:payload];
    };
    self.contentView = _scannerView;
  }
  return self;
}

- (void)updateProps:(Props::Shared const &)props oldProps:(Props::Shared const &)oldProps {
  const auto &newProps = *std::static_pointer_cast<const ScannerViewProps>(props);

  _scannerView.isActive = newProps.isActive;
  _scannerView.torchOn = newProps.torchOn;

  if (!newProps.scanFormats.empty()) {
    NSMutableArray<NSString *> *formats = [NSMutableArray new];
    for (const auto &format : newProps.scanFormats) {
      [formats addObject:[NSString stringWithUTF8String:format.c_str()]];
    }
    _scannerView.scanFormats = formats;
  }

  [super updateProps:props oldProps:oldProps];
}

- (void)emitBarcodeScanned:(NSDictionary<NSString *, id> *)payload {
  if (!_eventEmitter) return;
  const auto emitter = std::static_pointer_cast<const ScannerViewEventEmitter>(_eventEmitter);
  emitter->onBarcodeScanned(ScannerViewEventEmitter::OnBarcodeScanned{
    .value = std::string([payload[@"value"] UTF8String]),
    .type = std::string([payload[@"type"] UTF8String]),
    .timestamp = [payload[@"timestamp"] intValue]
  });
}

@end

Class<RCTComponentViewProtocol> ScannerViewCls(void) {
  return ScannerView.class;
}