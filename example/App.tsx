import { useCallback, useEffect, useState } from "react";
import { NativeModules, Pressable, Text, View } from 'react-native';

const { AlzaReactNativePaymentPassModule } = NativeModules;

export default function App() {
  const [canAdd, setCanAdd] = useState("");

  console.log("App rendering");

  useEffect(() => {
    AlzaReactNativePaymentPassModule.canAddPaymentPass().then(setCanAdd);
  }, []);

  const onPress = useCallback(async () => {
    console.log("add button pressed");

    AlzaReactNativePaymentPassModule.addPassToGoogle({
      cardNetwork: 3,
      tokenProvider: 3,
      displayName: "David Meadows",
      lastDigits: "4242",
      userAddress: {
        name: "John Doe",
        address1: "1 Infinite Loop",
        locality: "Cupertino",
        administrativeArea: "CA",
        countryCode: "US",
        postalCode: "98103",
        phoneNumber: "415 769 7137",
      },
    }).then((result: any) => console.log(result)).catch((error: any) => console.log(error));
  }, []);

  return (
    <View style={{ flex: 1, justifyContent: "center", alignItems: "center" }}>
      <Text>Async can add payment pass: {canAdd}</Text>
      <Pressable
        style={{ height: 200, width: 200, backgroundColor: "red" }}
        onPress={onPress}
      >
      </Pressable>
    </View>
  );
}
