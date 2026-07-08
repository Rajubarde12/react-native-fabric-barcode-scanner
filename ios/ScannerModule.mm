#import "RNCustomScannerSpec.h"
#import "RNCustomScanner-Swift.h"

using namespace facebook::react;

@interface ScannerModule : NativeScannerModuleSpecBase <NativeScannerModuleSpec>
@end

@implementation ScannerModule {
  RNScannerHelper *_helper;
}

RCT_EXPORT_MODULE(ScannerModule)

+ (BOOL)requiresMainQueueSetup {
  return NO;
}

- (instancetype)init {
  if (self = [super init]) {
    _helper = [RNScannerHelper new];
  }
  return self;
}

- (void)checkCameraPermission:(RCTPromiseResolveBlock)resolve
                        reject:(RCTPromiseRejectBlock)reject {
  resolve([_helper checkCameraPermission]);
}

- (void)requestCameraPermission:(RCTPromiseResolveBlock)resolve
                          reject:(RCTPromiseRejectBlock)reject {
  [_helper requestCameraPermission:^(NSString *result) {
    resolve(result);
  }];
}

- (void)setTorchEnabled:(BOOL)enabled {
  [_helper setTorchEnabled:enabled];
}

- (void)openCameraSettings {
  [_helper openCameraSettings];
}

- (facebook::react::ModuleConstants<JS::NativeScannerModule::Constants>)getConstants {
  std::vector<NSString *> formats;
  for (NSString *f in RNScannerHelper.supportedFormats) {
    formats.push_back(f);
  }
  return facebook::react::typedConstants<JS::NativeScannerModule::Constants>({
    .SUPPORTED_FORMATS = formats
  });
}

- (facebook::react::ModuleConstants<JS::NativeScannerModule::Constants>)constantsToExport {
  return [self getConstants];
}

- (std::shared_ptr<facebook::react::TurboModule>)getTurboModule:
    (const facebook::react::ObjCTurboModule::InitParams &)params {
  return std::make_shared<facebook::react::NativeScannerModuleSpecJSI>(params);
}

@end