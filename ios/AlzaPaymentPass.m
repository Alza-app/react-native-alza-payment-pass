#import <React/RCTBridgeModule.h>

@interface RCT_EXTERN_MODULE(AlzaPaymentPass, NSObject)
RCT_EXTERN_METHOD(
                  canAddPaymentPass: (NSString *) paymentRefrenceId
                  resolve: (RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject
                  )

RCT_EXTERN_METHOD(
                  addPassToAppleWallet: (NSString *) cardHolderName
                  lastFour: (NSString *) lastFour
                  paymentReferenceId: (NSString *) paymentReferenceId
                  successCallback: (RCTResponseSenderBlock)successCallback
                  errorCallback: (RCTResponseSenderBlock) errorCallback
                  )

RCT_EXTERN_METHOD(
                  finalizeAddPassToAppleWallet: (NSString *) encryptedPassData
                  activationData: (NSString *) activationData
                  ephemeralPublicKey: (NSString *) ephemeralPublicKey
                  successCallback: (RCTResponseSenderBlock)successCallback
                  errorCallback: (RCTResponseSenderBlock) errorCallback
                  )

RCT_EXTERN_METHOD(
                  removeSuspendedCard: (NSString *) paymentReferenceId
                  resolve: (RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject
                  )

RCT_EXTERN_METHOD(
                  isCardInWallet: (NSString *) uniqueCardReferenceID
                  resolve: (RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject
                  )
@end
