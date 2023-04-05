import ExpoModulesCore
import PassKit

public class AlzaReactNativePaymentPassModule: Module {
    //    private var pickingContext: PickingContext?
    
    public func definition() -> ModuleDefinition {
        Name("AlzaReactNativePaymentPass")
        
        View(AlzaReactNativePaymentPassView.self) {
            Events("onAddButtonPress")

            Prop("iosButtonStyle") { (view, style: iosButtonStyle) in
                print ("prop:iosButtonStyle", style)
                switch style {
                case .black:
                    view.iosButtonStyle = PKAddPassButtonStyle.black
                case .blackOutline:
                    view.iosButtonStyle = PKAddPassButtonStyle.blackOutline
                }
            }
        }
        
        AsyncFunction("asyncCanAddPaymentPass") { (paymentReferenceID: String, promise: Promise) in
            if PKAddPaymentPassViewController.canAddPaymentPass() {
                if PKPassLibrary().canAddPaymentPass(withPrimaryAccountIdentifier: paymentReferenceID) {
                    promise.resolve("CAN_ADD")
                } else {
                    promise.resolve("ALREADY_ADDED")
                }
            } else {
                promise.resolve("UNABLE_TO_CHECK")
            }
        }
        
        AsyncFunction("addPaymentPassToAppleWallet") { (cardholderName: String, last4: String, paymentReferenceID: String, promise: Promise) in
            guard let currentVc = appContext?.utilities?.currentViewController() else {
                throw MissingViewControllerException()
            }
            guard let requestConfiguration = PKAddPaymentPassRequestConfiguration(encryptionScheme: .ECC_V2) else {
                promise.reject("Unable to init PKAddPaymentPassRequestConfiguration", "Error")
                return
            }
            requestConfiguration.cardholderName = cardholderName
            requestConfiguration.primaryAccountSuffix = last4
            requestConfiguration.primaryAccountIdentifier = paymentReferenceID
            debugPrint (requestConfiguration)
            
            let delegate = AddPaymentPassViewDelegate()
            
            guard let addPaymentPassViewController = PKAddPaymentPassViewController(requestConfiguration:
                                                                        requestConfiguration, delegate: delegate) else {
                promise.reject("Unable to init PKAddPaymentPassViewController", "Error")
                return
            }
            
            currentVc.present(addPaymentPassViewController, animated: true, completion: nil)
        }
    }
    enum iosButtonStyle: String, Enumerable {
        case black
        case blackOutline
    }
    enum CanAddPaymentPassResult: String, Enumerable {
        case CAN_ADD
        case ALREADY_ADDED
        case UNABLE_TO_CHECK
    }
}
