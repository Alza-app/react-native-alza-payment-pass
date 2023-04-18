import Foundation
import PassKit

var pkAddPaymentErrorCallback: RCTResponseSenderBlock? = nil
var pkAddPaymentSuccessCallback: RCTResponseSenderBlock? = nil
var pkFinaliseSuccessCallback: RCTResponseSenderBlock? = nil
var pkFinaliseErrorCallback: RCTResponseSenderBlock? = nil
var pkCompletionHandler: ((PKAddPaymentPassRequest) -> Void)? = nil

@objc(AlzaPaymentPass)
class AlzaPaymentPass: NSObject {
    override init() {}
    
    @objc static func requiresMainQueueSetup() -> Bool {
        return false
    }

    @objc(isCardInWallet:resolve:rejecter:)
    func isCardInWallet(_ uniqueCardReferenceID: String, resolve: RCTPromiseResolveBlock, reject: RCTPromiseRejectBlock) -> Void {
        let passLibrary = PKPassLibrary()
        let allPaymentPasses = passLibrary.passes(of: .payment).compactMap({$0 as? PKPaymentPass}) + passLibrary.remotePaymentPasses()
        let hasPaymentPass = allPaymentPasses.contains(where: {$0.primaryAccountIdentifier == uniqueCardReferenceID})
        resolve(hasPaymentPass);
    }
    
    @objc(canAddPaymentPass:resolve:rejecter:)
    func canAddPaymentPass(_ uniqueCardReferenceID: String, resolve: RCTPromiseResolveBlock, reject: RCTPromiseRejectBlock) -> Void {
        if PKAddPaymentPassViewController.canAddPaymentPass() {
            if PKPassLibrary().canAddPaymentPass(withPrimaryAccountIdentifier: uniqueCardReferenceID) {
                resolve("CAN_ADD")
            } else {
                resolve("ALREADY_ADDED")
            }
        } else {
            resolve("BLOCKED")
        }
    }
    
    @objc(addPassToAppleWallet:lastFour:paymentReferenceId:successCallback:errorCallback:)
    func addPassToAppleWallet(_ cardHolderName: String, lastFour: String, uniqueCardReferenceID: String = "", successCallback: @escaping RCTResponseSenderBlock, errorCallback: @escaping RCTResponseSenderBlock) -> Void {
        pkAddPaymentErrorCallback = errorCallback
        pkAddPaymentSuccessCallback = successCallback
        
        DispatchQueue.main.async {
            let rootViewController = UIApplication.shared.delegate?.window??.rootViewController
            guard let requestConfiguration = PKAddPaymentPassRequestConfiguration(encryptionScheme: .ECC_V2) else {
                errorCallback(["BLOCKED"])
                return
            }
            requestConfiguration.cardholderName = cardHolderName
            requestConfiguration.primaryAccountSuffix = lastFour
            requestConfiguration.primaryAccountIdentifier = uniqueCardReferenceID
            
            guard let addPaymentPassViewController = PKAddPaymentPassViewController(requestConfiguration:
                                                                                        requestConfiguration, delegate: self) else {
                errorCallback(["BLOCKED"])
                return
            }
            rootViewController?.present(addPaymentPassViewController, animated: true, completion: nil)
        }
    }
    
    @objc(finalizeAddPassToAppleWallet:activationData:ephemeralPublicKey:successCallback:errorCallback:)
    func finalizeAddPassToAppleWallet(_ encryptedPassData: String, activationData: String, ephemeralPublicKey: String, successCallback: @escaping RCTResponseSenderBlock,
                         errorCallback: @escaping RCTResponseSenderBlock) -> Void {
        pkFinaliseErrorCallback = errorCallback
        pkFinaliseSuccessCallback = successCallback
        
        let addPaymentPassRequest = PKAddPaymentPassRequest()
        addPaymentPassRequest.encryptedPassData = Data(base64Encoded: encryptedPassData, options: [])
        addPaymentPassRequest.activationData = Data(base64Encoded: activationData, options: [])
        addPaymentPassRequest.ephemeralPublicKey = Data(base64Encoded: ephemeralPublicKey, options: [])
        
        
        pkCompletionHandler?(addPaymentPassRequest)
        pkAddPaymentSuccessCallback = nil
        pkAddPaymentErrorCallback = nil
    }
    
    @objc(removeSuspendedCard:resolve:rejecter:)
    func removeSuspendedCard(_ panReferenceId: String, resolve: RCTPromiseResolveBlock, reject: RCTPromiseRejectBlock) -> Void {
        let passLibrary = PKPassLibrary()
        let allPaymentPasses = passLibrary.passes(of: .payment).compactMap({$0 as? PKPaymentPass}) + passLibrary.remotePaymentPasses()
        let passesToRemove = allPaymentPasses.filter({$0.primaryAccountIdentifier == panReferenceId})
        passesToRemove.forEach(passLibrary.removePass(_:))
        resolve(nil)
    }
}

extension AlzaPaymentPass: PKAddPaymentPassViewControllerDelegate {
    func addPaymentPassViewController(_ controller: PKAddPaymentPassViewController, didFinishAdding pass: PKPaymentPass?, error: Error?) {
        if error == nil {
            pkFinaliseSuccessCallback?([])
        } else {
            pkFinaliseErrorCallback?([error?.localizedDescription ?? ""])
            pkAddPaymentErrorCallback?([error?.localizedDescription ?? ""])
        }
        
        pkFinaliseErrorCallback = nil
        pkFinaliseSuccessCallback = nil
        pkCompletionHandler = nil
        
        controller.dismiss(animated: true, completion: nil)
    }
    
    func addPaymentPassViewController(_ controller: PKAddPaymentPassViewController,
                                      generateRequestWithCertificateChain certificates: [Data],
                                      nonce: Data,
                                      nonceSignature: Data,
                                      completionHandler handler: @escaping (PKAddPaymentPassRequest) -> Void)
    {
        pkCompletionHandler = handler
        let appVersion = Bundle.main.object(forInfoDictionaryKey: "CFBundleShortVersionString") as! String
        var certArray: [String] = []
        for cert in certificates {
            certArray.append(cert.base64EncodedString())
        }
        
        pkAddPaymentSuccessCallback?([["certificates": certArray, "nonce": nonce.base64EncodedString(), "nonce_signature": nonceSignature.base64EncodedString(), "provisioning_app_version": appVersion, "device_type": "MOBILE_PHONE"]])
    }
}
