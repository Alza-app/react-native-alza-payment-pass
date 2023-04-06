package app.alza.reactnativepaymentpass

import android.view.View
import com.facebook.react.ReactPackage
import com.facebook.react.bridge.NativeModule
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.uimanager.ReactShadowNode
import com.facebook.react.uimanager.ViewManager

class AlzaReactPackage : ReactPackage {
  override fun createNativeModules(reactContext: ReactApplicationContext): List<NativeModule> {
    return listOf(AlzaPaymentPassModule(reactContext))
  }

  override fun createViewManagers(p0: ReactApplicationContext): List<ViewManager<View, ReactShadowNode<*>>> {
    return listOf()
  }
}