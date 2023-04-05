import {
  asyncCanAddPaymentPass,
  addPassToGoogle,
  addPaymentPassToAppleWallet,
  CardNetwork,
  TokenProvider,
  AlzaReactNativePaymentPassView,
} from "alza-react-native-payment-pass";
import { Platform } from "expo-modules-core";
import { useCallback, useEffect, useState } from "react";
import { Pressable, Text, View } from "react-native";

export default function App() {
  const [canAdd, setCanAdd] = useState("");

  useEffect(() => {
    (async () => {
      const c = await asyncCanAddPaymentPass("ref");
      console.log("asyncCanAddPaymentPass", c);
      setCanAdd(c);
    })();
  }, []);

  const addCardToGoogleWallet = useCallback(async () => {
    console.log("add button pressed");

    const result = await addPassToGoogle({
      opc: "",
      cardNetwork: CardNetwork.MasterCard,
      tokenProvider: TokenProvider.MasterCard,
      displayName: "David Meadows",
      lastDigits: "4242",
      userAddress: {
        name: "",
        address1: "",
        locality: "",
        administrativeArea: "",
        countryCode: "",
        postalCode: "",
        phoneNumber: "",
      },
    });
    console.log("result", result);
  }, []);

  const addCardToApple = useCallback(async () => {
    console.log("add button pressed");

    const result = await addPaymentPassToAppleWallet(
      "David Meadows",
      "4242",
      "ref"
    );
    console.log("result", result);
  }, []);

  return (
    <View style={{ flex: 1, justifyContent: "center", alignItems: "center" }}>
      <Text>Async can add payment pass: {canAdd}</Text>
      <Text>Add button to wallet</Text>
      <Pressable
        style={{ height: 100, width: 200 }}
        onPress={addCardToGoogleWallet}
      ></Pressable>
      {Platform.OS === "ios" && (
        <>
          <Text>iOS Add to Apple Wallet Button</Text>
          <View style={{ margin: 24, height: 60, width: 194 }}>
            <AlzaReactNativePaymentPassView
              iosButtonStyle="blackOutline"
              style={{ flex: 1 }}
              onAddButtonPress={() => {
                addCardToApple();
              }}
            />
          </View>
        </>
        // todo: android button
      )}
    </View>
  );
}
