package app.alza.reactnativepaymentpass

import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.bridge.ReadableMap
import com.google.android.gms.tapandpay.TapAndPay
import com.google.android.gms.tapandpay.issuer.PushTokenizeRequest
import com.google.android.gms.tapandpay.issuer.UserAddress
import java.util.logging.Logger
import java.util.logging.Level

class AlzaPaymentPassModule(context: ReactApplicationContext) : ReactContextBaseJavaModule(context) {
  override fun getName(): String {
    return "AlzaReactNativePaymentPassModule"
  }

  @ReactMethod
  private fun canAddPaymentPass(promise: Promise) {
    promise.resolve("MAYBE_WE_CAN_v2")
  }

  @ReactMethod
  private fun addPassToGoogle(options: ReadableMap) {
    logger.log(Level.INFO, "addPassToGoogle")
    val opc = options.requireString("opc").toByteArray()
    val cardNetwork = options.requireInt("cardNetwork")
    val tokenProvider = options.requireInt("tokenProvider")
    val displayName = options.requireString("displayName")
    val lastDigits = options.requireString("lastDigits")
    val userAddressMap = options.getMap("userAddress")
        ?: throw IllegalArgumentException("Missing required argument userAddress")
    val userAddress = UserAddress.newBuilder()
        .setName(userAddressMap.requireString("name"))
        .setAddress1(userAddressMap.requireString("address1"))
        .setLocality(userAddressMap.requireString("locality"))
        .setAdministrativeArea(userAddressMap.requireString("administrativeArea"))
        .setCountryCode(userAddressMap.requireString("countryCode"))
        .setPostalCode(userAddressMap.requireString("postalCode"))
        .setPhoneNumber(userAddressMap.requireString("phoneNumber"))
        .build()
    logger.log(Level.INFO, "opc: ${opc.contentToString()}")
    if (currentActivity == null) {
      Logger.log(Level.INFO, "currentActivity is null")
    }
    currentActivity?.let {
      val tapAndPayClient = TapAndPay.getClient(it);
      val pushTokenizeRequest: PushTokenizeRequest = PushTokenizeRequest.Builder()
          .setOpaquePaymentCard(opc)
          .setNetwork(cardNetwork)
          .setTokenServiceProvider(tokenProvider)
          .setDisplayName(displayName)
          .setLastDigits(lastDigits)
          .setUserAddress(userAddress)
          .build()
      tapAndPayClient.pushTokenize(it, pushTokenizeRequest, REQUEST_CODE_PUSH_TOKENIZE);
    }
  }

  private fun ReadableMap.requireString(name: String): String {
    return getString(name) ?: throw IllegalArgumentException("Missing required argument $name")
  }

  private fun ReadableMap.requireInt(name: String): Int {
    return if (hasKey(name)) getInt(name)
    else throw IllegalArgumentException("Missing required argument $name")
  }

  companion object {
    private const val REQUEST_CODE_PUSH_TOKENIZE = 3
    private val logger = Logger.getLogger("AlzaPaymentPassModule")
  }
}
