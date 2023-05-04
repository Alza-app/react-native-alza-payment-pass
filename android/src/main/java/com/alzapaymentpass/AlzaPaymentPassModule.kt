package com.alzapaymentpass

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.nfc.NfcManager
import android.nfc.cardemulation.CardEmulation
import android.view.View
import com.facebook.react.bridge.*
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tapandpay.TapAndPay
import com.google.android.gms.tapandpay.TapAndPayClient
import com.google.android.gms.tapandpay.TapAndPayStatusCodes.*
import com.google.android.gms.tapandpay.issuer.IsTokenizedRequest
import com.google.android.gms.tapandpay.issuer.PushTokenizeRequest
import com.google.android.gms.tapandpay.issuer.UserAddress
import java.nio.charset.Charset
import java.util.logging.Level
import java.util.logging.Logger


class AlzaPaymentPassModule(reactContext: ReactApplicationContext) :
  ReactContextBaseJavaModule(reactContext) {

  var requestPaymentPromise: Promise? = null

  override fun getName(): String {
    return NAME
  }

  private val activityEventListener: ActivityEventListener = object : BaseActivityEventListener() {
    override fun onActivityResult(
      activity: Activity,
      requestCode: Int,
      resultCode: Int,
      data: Intent?
    ) {
      if (requestCode == REQUEST_CODE_PUSH_TOKENIZE) {
        when (resultCode) {
          TAP_AND_PAY_ATTESTATION_ERROR -> requestPaymentPromise?.reject(
            "TAP_AND_PAY_ATTESTATION_ERROR",
            "Tap and pay error: attestation error"
          )
          Activity.RESULT_OK -> requestPaymentPromise?.resolve(PAYMENT_PASS_RESULT_SUCCESSFUL)
          Activity.RESULT_CANCELED -> requestPaymentPromise?.reject("CANCELED", "Canceled")
          TAP_AND_PAY_INVALID_TOKEN_STATE -> requestPaymentPromise?.reject(
            "TAP_AND_PAY_INVALID_TOKEN_STATE",
            "Tap and pay error: invalid token state"
          )
          TAP_AND_PAY_NO_ACTIVE_WALLET -> requestPaymentPromise?.reject(
            "TAP_AND_PAY_NO_ACTIVE_WALLET",
            "Tap and pay error: no active wallet"
          )
          TAP_AND_PAY_TOKEN_NOT_FOUND -> requestPaymentPromise?.reject(
            "TAP_AND_PAY_TOKEN_NOT_FOUND",
            "Tap and pay error: token not found"
          )
          TAP_AND_PAY_UNAVAILABLE -> requestPaymentPromise?.reject(
            "TAP_AND_PAY_UNAVAILABLE",
            "Tap and pay error: unavailable"
          )
          else -> requestPaymentPromise?.resolve("DEFAULT")
        }
      }
    }
  }

  init {
    reactContext.addActivityEventListener(activityEventListener);
  }

  /**
   * This method shows how to check whether or not Google Pay is the default HCE wallet for NFC
   * payments. See the documentation here:
   * https://developers.google.com/pay/issuers/apis/push-provisioning/android/set-nfc-wallet
   *
   * @return - true if Google Pay is the default, false otherwise
   */
  private fun isDefaultWallet(): Boolean {
    val nfcManager = currentActivity?.getSystemService(Context.NFC_SERVICE) as? NfcManager
    val adapter = nfcManager?.defaultAdapter
    // if nfc hardware is not available, this will be null
    return if (adapter != null) {
      val emulation = CardEmulation.getInstance(adapter)
      emulation.isDefaultServiceForCategory(
        ComponentName(GoogleApiAvailability.GOOGLE_PLAY_SERVICES_PACKAGE, GOOGLE_PAY_TP_HCE_SERVICE),
        CardEmulation.CATEGORY_PAYMENT
      )
    } else {
      false
    }
  }

  private fun cardAlreadyAddedToWallet(tapAndPayClient: TapAndPayClient, cardNetwork: Int, tokenServiceProvider: Int, last4: String, promise: Promise) {
    val request = IsTokenizedRequest.Builder()
      .setIdentifier(last4)
      .setNetwork(cardNetwork)
      .setTokenServiceProvider(tokenServiceProvider)
      .build()

    tapAndPayClient
      .isTokenized(request)
      .addOnCompleteListener {
        if (it.isSuccessful) {
          if (it.result) {
            promise.resolve(CAN_ADD_PAYMENT_PASS_RESULT_ALREADY_ADDED)
          } else {
            promise.resolve(CAN_ADD_PAYMENT_PASS_RESULT_CAN_ADD)
          }
        } else {
          logger.log(Level.WARNING, "exception when calling isTokenized: ${it.exception}")
          promise.resolve(CAN_ADD_PAYMENT_PASS_RESULT_BLOCKED)
        }
      }
  }

  @ReactMethod
  fun canAddPaymentPass(options: ReadableMap, promise: Promise) {
    // TODO: Do we need to also check that Google Pay is the default HCE wallet for NFC payments?
    if (currentActivity == null) {
      logger.log(Level.WARNING, "currentActivity is null, cannot request provision")
      promise.resolve(CAN_ADD_PAYMENT_PASS_RESULT_BLOCKED)
      return
    }
//    if (!isDefaultWallet()) {
//      logger.log(Level.WARNING, "isDefaultWallet() returned false, cannot request provision")
//      promise.resolve(CAN_ADD_PAYMENT_PASS_RESULT_BLOCKED)
//      return
//    }

    val cardNetwork = options.requireInt("cardNetwork")
    val tokenProvider = options.requireInt("tokenProvider")
    val lastDigits = options.requireString("lastDigits")

    if (cardNetwork != TapAndPay.CARD_NETWORK_MASTERCARD) {
      promise.reject(CAN_ADD_PAYMENT_PASS_RESULT_BLOCKED, "cardNetwork must be 3 (CARD_NETWORK_MASTERCARD)")
    }
    if (tokenProvider != TapAndPay.TOKEN_PROVIDER_MASTERCARD) {
      promise.reject(CAN_ADD_PAYMENT_PASS_RESULT_BLOCKED, "tokenProvider must be 3 (TOKEN_PROVIDER_MASTERCARD)")
    }

    val tapAndPayClient = TapAndPay.getClient(currentActivity!!)

    val onSuccess = fun () = run { cardAlreadyAddedToWallet(tapAndPayClient, cardNetwork, tokenProvider, lastDigits, promise) }

    tapAndPayClient
      .activeWalletId
      .addOnCompleteListener {
        if (it.isSuccessful) {
          onSuccess()
        } else {
          val apiException = it.exception as ApiException
          if (apiException.statusCode == TAP_AND_PAY_NO_ACTIVE_WALLET) {
            tapAndPayClient.createWallet(currentActivity!!, REQUEST_CREATE_WALLET)
            canAddPaymentPass(options, promise)
          } else {
            onSuccess()
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
    try {
      val tapAndPayClient = TapAndPay.getClient(currentActivity!!)
      val opc = options.requireString("opc").toByteArray(Charset.defaultCharset())
      val cardNetwork = options.requireInt("cardNetwork")
      val tokenProvider = options.requireInt("tokenProvider")
      val displayName = options.requireString("displayName")
      val lastDigits = options.requireString("lastDigits")
      val userAddressMap = options.getMap("userAddress")
        ?: throw IllegalArgumentException("Missing required argument userAddress")
      if (cardNetwork != TapAndPay.CARD_NETWORK_MASTERCARD) {
        promise.reject(PAYMENT_PASS_RESULT_FAILED, "cardNetwork must be 3 (CARD_NETWORK_MASTERCARD)")
      }
      if (tokenProvider != TapAndPay.TOKEN_PROVIDER_MASTERCARD) {
        promise.reject(PAYMENT_PASS_RESULT_FAILED, "tokenProvider must be 3 (TOKEN_PROVIDER_MASTERCARD)")
      }
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
      this.requestPaymentPromise = promise
    } catch (e: Exception) {
      promise.reject(
        "TAP_AND_PAY_START_PUSH_PROVISION_ERROR",
        "Tap and pay addPassToGoogle exception: ${e.message}"
      )
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
    const val NAME = "AlzaPaymentPass"
    const val GOOGLE_PAY_TP_HCE_SERVICE =
      "com.google.android.gms.tapandpay.hce.service.TpHceService"
    private const val REQUEST_CODE_PUSH_TOKENIZE = 3
    private const val REQUEST_CREATE_WALLET = 4
    private const val PAYMENT_PASS_RESULT_FAILED = "PAYMENT_PASS_RESULT_FAILED"
    private const val PAYMENT_PASS_RESULT_SUCCESSFUL = "PAYMENT_PASS_RESULT_SUCCESSFUL"
    private const val CAN_ADD_PAYMENT_PASS_RESULT_CAN_ADD = "CAN_ADD"
    private const val CAN_ADD_PAYMENT_PASS_RESULT_ALREADY_ADDED = "ALREADY_ADDED"
    private const val CAN_ADD_PAYMENT_PASS_RESULT_BLOCKED = "BLOCKED"
    private val logger = Logger.getLogger("AlzaPaymentPassModule")
  }
}
