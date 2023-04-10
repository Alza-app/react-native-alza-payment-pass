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

export function canAddPaymentPass(
  uniqueCardReferenceID: string
): Promise<string> {
  return AlzaPaymentPass.canAddPaymentPass(uniqueCardReferenceID);
}

export function addPassToGoogle(options: any): Promise<string> {
  return AlzaPaymentPass.addPassToGoogle(options);
}

function noop(): void {}

export interface AppleWalletProvisionRequestParams {
  device_type: string;
  certificates: string[];
  nonce: string;
  nonce_signature: string;
  app_version: string;
}

export function addPassToAppleWallet(
  cardHolderName: string,
  lastFour: string,
  uniqueCardReferenceID: string,
  successCallback: (params: AppleWalletProvisionRequestParams) => void,
  errorCallback?: (error: string) => void
): Promise<string> {
  return AlzaPaymentPass.addPassToAppleWallet(
    cardHolderName,
    lastFour,
    uniqueCardReferenceID,
    successCallback,
    errorCallback
  );
}

export const finalizeAddPassToAppleWallet = (
  encryptedPassData: string,
  activationData: string,
  ephemeralPublicKey: string,
  successCallback: () => void,
  errorCallback?: (error: string) => void
) => {
  AlzaPaymentPass.finalizeAddPassToAppleWallet(
    encryptedPassData,
    activationData,
    ephemeralPublicKey,
    successCallback,
    errorCallback ? errorCallback : noop
  );
};
