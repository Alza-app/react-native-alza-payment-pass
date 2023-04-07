import { NativeModules, Platform } from 'react-native';

const LINKING_ERROR =
  `The package 'react-native-alza-payment-pass' doesn't seem to be linked. Make sure: \n\n` +
  Platform.select({ ios: "- You have run 'pod install'\n", default: '' }) +
  '- You rebuilt the app after installing the package\n' +
  '- You are not using Expo Go\n';

const AlzaPaymentPass = NativeModules.AlzaPaymentPass
  ? NativeModules.AlzaPaymentPass
  : new Proxy(
      {},
      {
        get() {
          throw new Error(LINKING_ERROR);
        },
      }
    );

export function canAddPaymentPass(): Promise<number> {
  return AlzaPaymentPass.canAddPaymentPass();
}

export function addPassToGoogle(options: any): Promise<number> {
  return AlzaPaymentPass.addPassToGoogle(options);
}
