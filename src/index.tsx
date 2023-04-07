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

export const PAYMENT_PASS_RESULT_SUCCESSFUL = 'PAYMENT_PASS_RESULT_SUCCESSFUL';
export const PAYMENT_PASS_RESULT_FAILED = 'PAYMENT_PASS_RESULT_FAILED';

export function canAddPaymentPass(): Promise<string> {
  return AlzaPaymentPass.canAddPaymentPass();
}

export function addPassToGoogle(options: any): Promise<string> {
  return AlzaPaymentPass.addPassToGoogle(options);
}
