package app.alza.reactnativepaymentpass

import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.bridge.ReadableMap
import com.google.android.gms.tapandpay.TapAndPay
import com.google.android.gms.tapandpay.issuer.PushTokenizeRequest
import com.google.android.gms.tapandpay.issuer.UserAddress
import java.nio.charset.Charset
import java.util.logging.Logger
import java.util.logging.Level

class AlzaPaymentPassModule(context: ReactApplicationContext) : ReactContextBaseJavaModule(context) {
  override fun getName(): String {
    return "AlzaReactNativePaymentPassModule"
  }

  @ReactMethod
  fun canAddPaymentPass(promise: Promise) {
    promise.resolve("MAYBE_WE_CAN_v2")
  }

  @ReactMethod
  fun addPassToGoogle(options: ReadableMap, promise: Promise) {
    logger.log(Level.INFO, "addPassToGoogle")
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
    if (currentActivity == null) {
      logger.log(Level.INFO, "currentActivity is null")
    }
    currentActivity?.let {
      val tapAndPayClient = TapAndPay.getClient(it);
      val pushTokenizeRequest = PushTokenizeRequest.Builder()
          .setOpaquePaymentCard(OPC.toByteArray(Charset.defaultCharset()))
          .setNetwork(cardNetwork)
          .setTokenServiceProvider(tokenProvider)
          .setDisplayName(displayName)
          .setLastDigits(lastDigits)
          .setUserAddress(userAddress)
          .build()
      tapAndPayClient.pushTokenize(it, pushTokenizeRequest, REQUEST_CODE_PUSH_TOKENIZE);
      logger.log(Level.INFO, "push tokenize request sent")
    }
    promise.resolve("ALL_DONE")
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
    private val OPC = "eyJjYXJkSW5mbyI6eyJlbmNyeXB0ZWREYXRhIjoiMkQ3QTZENjY1Q0YyMDQyODdDRjZGRkI1NDg2QzAxQTBBRUM3OTMzRDZDOEMwOTg5MUZGMzFFRDNERUM1RTNEMjc3RjYxQjVCNzhBNDVGQTQxNEI5N0MzNUU0NjQ4QkZCRTFBMDJGRjg1RjFDOEM2MjQ3QUVENUZDMUUwMDk1ODdFNzUzM0M1RTcxMUQ5MDc2OEFBODVDRjlGNjY2MUZGQUE4QTYyNUFBMTc1NkMxODc4OEZBMUM3NEUyOEI2ODg4RTc4MjUxOUI3QkM1NDBFNTY0MDJERkQ5RkFFRTk1QkIxNTRFMDNFQjI5RTBGM0UwNzY5QjA2N0Y1MzYzQTMxQUZDNjlBNzU2OTI1NkRFNkZFNzg5NDIzNjg5NTlDNDg2IiwiZW5jcnlwdGVkS2V5IjoiMzdERTAxNjBERjlDQUFCNzA0MUNGNTRCNkUwRkJBOTQ5RjkxMkJGMzY4RjYyNUExMDZGNDhCRUYzMTQ3NzZGNDEyMURBQjFEMEREQzgxMEI4MTYzRTEyMkU4MTYyMDY5MUMwMjg4M0JFNTM3QkIwMTkyQjJEQTRDN0Y3QkM1NjczMDFDNjAzMzIwRDg2QzE1Q0I4M0ZDQTNCRjUwRjEwNzM0M0I4ODk5OEE5OUEzQzI5NEJCN0M4NjQwMjg1MkYyQzhDOEI1QUJGQ0Y4QzMwNThBQzYxMTgxOTBFRUFGN0ZFMTNGNDhBNDREQzU1RkZDOUZGRDIzRkZGNkRDNjFCNzQzNzc2QTY2RjM5MDc3RThGNDAzQTZDQjY5QjFDMzYzOTM2Q0Q3MUYzNzlFOTVBNkM2NThCRjM4QzcwMTI1QjJDQzIzRUZEMEJENjQ3NUMwMjk3M0Y1MjQ1MUNFMjUzOTU3QkI2NTBBMTY0NDZDREZGNkY5MjUxNTVFQzNFRENEN0I1MDhGRTI2MkU3OEQzQzczQTcyREIzQTE3NzBEOTQxQTYyOTlGNzA5NzhBNzNERDQ2MUFGM0VBRTEyOTY1NkYzRkM2RUVCMDlDRDA0NUFGQjU5NzZEMkFEQUQ4ODIwNTYwMjVDRjIwQTQzM0RBMUM3QkJDQzc5NDE2RjAxOTUiLCJpdiI6IjM1RkEzNDRBREJEOEU0MUNEMzMwRUVGN0RFQ0Y1RTc2Iiwib2FlcEhhc2hpbmdBbGdvcml0aG0iOiJTSEEyNTYiLCJwdWJsaWNLZXlGaW5nZXJwcmludCI6IkY3Q0FBNzAyM0RENkZDRTk5Q0Q0NEM1RUEzOUY2N0FGRUM0Rjg0NzUifSwidG9rZW5pemF0aW9uQXV0aGVudGljYXRpb25WYWx1ZSI6ImV5SmtZWFJoVm1Gc2FXUlZiblJwYkZScGJXVnpkR0Z0Y0NJNklqSXdNak10TURRdE1EVlVNVGs2TXpRNk1qVmFJaXdpYVc1amJIVmtaV1JHYVdWc1pITkpiazl5WkdWeUlqb2laR0YwWVZaaGJHbGtWVzUwYVd4VWFXMWxjM1JoYlhCOFlXTmpiM1Z1ZEU1MWJXSmxjbnhoWTJOdmRXNTBSWGh3YVhKNUlpd2ljMmxuYm1GMGRYSmxJam9pUkVFM1ZUWk9jV1JST1hOVVdsRlpjaTlqU1ZkdVlVbDBhbkZOTmxoUWVEUXlOM1l3ZFVJemNWbHJSMVY1YTNkd1pXdHJVMGhQVmtabWN6QTBZaXRYUTBkSVEyeDNOWGd2U2pRdllWZHhXRkJQWldKV1NUbEJhSHB3ZWk5SmJEZGFVa1prVFN0RGIzQklSMFEzY1RWemIyMTRTSEVyZW5ZMlkwMUJkVWhpVEVVclFWRkhSRU53ZURSNWVXeEdlV2RoYWxab1ZqSm9SREJ6Y0ZGT1lreHNhMlZhWmsxVVdTODJTWGxhUXpoWFJGRmpZako1UTBSTFlVSTJlVkl3SzBkSFR6QnhVVEJ4VGtWeGQwZ3JlVk14Y1hreVJIZEdTRm80WTNwclQxaE5ia296VEZsbFVqSlJWRzFuZERaNE4wTldhekpTTUVGWVZWQTNXV2hHVUhVNVJraDVhVVY1VUhsbFFtOW9ZV0ZLYWtaVldVSjRNRFpWTlRGUlEwSjBOR3BaT0hCaFdUUnBLMHBGUVRSRmFURklaSE0zY1RKNGVXaDZjVEJhWm5kcmRuRk9UR1ptVVhaclVGTk5ieTkwWlc5MFYwdDVUMmh4TkRSQ1ZUTlNWMDFuUlU1MVpUVlZZV055Wmt4cE9FTmFUMlJTVkdSMk1HRlBXRE00Y0ZCdkwxTXZVbEJrTld0RU5qSkhibFUzV0VWNldYTnRjVEZEUjJ4R1duTnpXbUptUnk5dWJHTnhkVlpwZGtGSWRYWkVhSFZ3Um5CRFdEZHZjaXRXSzFZeGNYQmxlRUV3ZUZOdVpYWlZUalZyWVZseVZDOW5WbGhZTXpkeFpGQnJRVEJtTlRJMlJtUnBPVU5ZY0dGVVFXcFRWRE00ZVc5UlkwSmlPR1Z5VEZWalFuSlVjM2xEUm5WQ1VrTldTRlpwWlVKaEwwSk9SMHN5ZFVjNVN6TnlXSGhVZFdvMFpUZDBXRmRRT1Vsb1ZHcDZNbFJLYVU1allXSlplRWRvTlZWbk5FeDBibTkzU0ZvclVuZHJNMmhKTVhVelZrOUtNMFZvV2pORGNWWlJPSHBwYldSNFRrTk9VbVpNYjBoQ05qVlFTbGROYUVKMmJ6VklRMHdyTTJOUE5rSmtiazAwWTNJNFJVeHNZVWhhWldneGJ6ZHFTR0pRVVZSVkwxZEVVRUZyY1RGcGMxSTJUMncyVGxadVJuY3ZXbk05SWl3aWMybG5ibUYwZFhKbFFXeG5iM0pwZEdodElqb2lVbE5CTFZOSVFUSTFOaUlzSW5abGNuTnBiMjRpT2lJekluMD0ifQ=="
    private val logger = Logger.getLogger("AlzaPaymentPassModule")
  }
}
