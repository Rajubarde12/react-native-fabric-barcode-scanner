import type { ViewProps, HostComponent } from 'react-native';
import codegenNativeComponent from 'react-native/Libraries/Utilities/codegenNativeComponent';
import type {
  Int32,
  WithDefault,
  BubblingEventHandler,
} from 'react-native/Libraries/Types/CodegenTypes';

export interface BarcodeScannedEvent {
  value: string;
  type: string; // e.g. 'qr', 'ean13', 'code128', 'pdf417', 'upc-e', etc.
  timestamp: Int32;
}

export interface NativeProps extends ViewProps {
  isActive?: WithDefault<boolean, true>;
  torchOn?: WithDefault<boolean, false>;

  // comma-free array prop: list of formats to actively decode
  // e.g. ['qr','ean13','ean8','upc-a','upc-e','code128','code39','pdf417','aztec','datamatrix']
  scanFormats?: string[];

  onBarcodeScanned?: BubblingEventHandler<BarcodeScannedEvent>;
}

export default codegenNativeComponent<NativeProps>(
  'ScannerView',
) as HostComponent<NativeProps>;
