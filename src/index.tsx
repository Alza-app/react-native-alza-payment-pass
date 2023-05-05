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

export type CanAddPaymentPassResult = 'CAN_ADD' | 'ALREADY_ADDED' | 'BLOCKED';

export type CanAddPaymentPassArguments =
  // android args
  | {
      cardNetwork: number;
      tokenProvider: number;
      lastDigits: string;
    }
  // ios arg (uniqueCardReferenceID)
  | string;

export function canAddPaymentPass(
  options: CanAddPaymentPassArguments
): Promise<CanAddPaymentPassResult> {
  return AlzaPaymentPass.canAddPaymentPass(options);
}

export type AddPassToGoogleArguments = {
  opc: string;
  cardNetwork: number;
  tokenProvider: number;
  displayName: string;
  lastDigits: string;
  userAddress: {
    name: string;
    address1: string;
    locality: string;
    administrativeArea: string;
    countryCode: string;
    postalCode: string;
    phoneNumber: string;
  };
};

export type AddPassToGoogleResult =
  | 'PAYMENT_PASS_RESULT_SUCCESSFUL'
  | 'PAYMENT_PASS_RESULT_FAILED';

export function addPassToGoogle(
  options: AddPassToGoogleArguments
): Promise<AddPassToGoogleResult> {
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
): void {
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
): void => {
  AlzaPaymentPass.finalizeAddPassToAppleWallet(
    encryptedPassData,
    activationData,
    ephemeralPublicKey,
    successCallback,
    errorCallback ? errorCallback : noop
  );
};
