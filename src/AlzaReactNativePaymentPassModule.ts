import { requireNativeModule } from "expo-modules-core";
import { AddCardToGooglePayOptions } from "./AlzaReactNativePaymentPass.types";

const AlzaReactNativePaymentPass = requireNativeModule(
  "AlzaReactNativePaymentPass"
);

export type CanAddPaymentPassResult =
  | "CAN_ADD"
  | "ALREADY_ADDED"
  | "UNABLE_TO_CHECK";

export async function asyncCanAddPaymentPass(
  paymentReferenceID: string
): Promise<CanAddPaymentPassResult> {
  return await AlzaReactNativePaymentPass.asyncCanAddPaymentPass(
    paymentReferenceID
  );
}

export async function addPassToGoogle(
  options: AddCardToGooglePayOptions
): Promise<boolean> {
  console.log('sending request: ', options);
  return await AlzaReactNativePaymentPass.addPassToGoogle(options);
}

export async function addPaymentPassToAppleWallet(
  cardholderName: String, last4: String, paymentReferenceID: String
): Promise<boolean> {
  console.log('sending request: ', cardholderName, last4, paymentReferenceID);
  return await AlzaReactNativePaymentPass.addPaymentPassToAppleWallet(cardholderName, last4, paymentReferenceID);
}
