package com.alzapaymentpass

import com.facebook.react.bridge.*
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tapandpay.TapAndPay
import com.google.android.gms.tapandpay.TapAndPayStatusCodes.TAP_AND_PAY_NO_ACTIVE_WALLET
import com.google.android.gms.tapandpay.issuer.PushTokenizeRequest
import com.google.android.gms.tapandpay.issuer.UserAddress
import java.nio.charset.Charset
import java.util.logging.Level
import java.util.logging.Logger

class AlzaPaymentPassModule(reactContext: ReactApplicationContext) :
  ReactContextBaseJavaModule(reactContext) {

  override fun getName(): String {
    return NAME
  }

  @ReactMethod
  fun canAddPaymentPass(promise: Promise) {
    // TODO: Do we need to also check that Google Pay is the default HCE wallet for NFC payments?
    if (currentActivity == null) {
      logger.log(Level.WARNING, "currentActivity is null, cannot request provision")
      promise.resolve(PAYMENT_PASS_RESULT_FAILED)
      return
    }
    val tapAndPayClient = TapAndPay.getClient(currentActivity!!)
    tapAndPayClient
      .activeWalletId
      .addOnCompleteListener {
        if (it.isSuccessful) {
          promise.resolve(PAYMENT_PASS_RESULT_SUCCESSFUL)
        } else {
          val apiException = it.exception as ApiException
          if (apiException.statusCode == TAP_AND_PAY_NO_ACTIVE_WALLET) {
            tapAndPayClient.createWallet(currentActivity!!, REQUEST_CREATE_WALLET)
            canAddPaymentPass(promise)
          } else {
            promise.resolve(PAYMENT_PASS_RESULT_SUCCESSFUL)
          }
        }
      }
  }

  @ReactMethod
  fun addPassToGoogle(options: ReadableMap, promise: Promise) {
    if (currentActivity == null) {
      logger.log(Level.WARNING, "currentActivity is null, cannot request provision")
      promise.resolve(PAYMENT_PASS_RESULT_FAILED)
      return
    }
    val tapAndPayClient = TapAndPay.getClient(currentActivity!!)
    val opc = options.requireString("opc").toByteArray(Charset.defaultCharset())
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
    val pushTokenizeRequest = PushTokenizeRequest.Builder()
      .setOpaquePaymentCard(opc)
      .setNetwork(cardNetwork)
      .setTokenServiceProvider(tokenProvider)
      .setDisplayName(displayName)
      .setLastDigits(lastDigits)
      .setUserAddress(userAddress)
      .build()
    tapAndPayClient.pushTokenize(
      currentActivity!!,
      pushTokenizeRequest,
      REQUEST_CODE_PUSH_TOKENIZE
    );
    logger.log(Level.INFO, "push tokenize request sent")
    promise.resolve(PAYMENT_PASS_RESULT_SUCCESSFUL)
  }

  private fun ReadableMap.requireString(name: String): String {
    return getString(name) ?: throw IllegalArgumentException("Missing required argument $name")
  }

  private fun ReadableMap.requireInt(name: String): Int {
    return if (hasKey(name)) getInt(name)
    else throw IllegalArgumentException("Missing required argument $name")
  }

  companion object {
    const val NAME = "AlzaPaymentPass"
    private const val REQUEST_CODE_PUSH_TOKENIZE = 3
    private const val REQUEST_CREATE_WALLET = 4
    private const val PAYMENT_PASS_RESULT_FAILED = 0
    private const val PAYMENT_PASS_RESULT_SUCCESSFUL = 1
    private val logger = Logger.getLogger("AlzaPaymentPassModule")
  }
}
