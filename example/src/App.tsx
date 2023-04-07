import * as React from 'react';

import { StyleSheet, View, Text, Pressable, Platform } from 'react-native';
import {
  addPassToAppleWallet,
  addPassToGoogle,
  canAddPaymentPass,
} from 'react-native-alza-payment-pass';
import { useCallback } from 'react';

export default function App() {
  const [result, setResult] = React.useState<string | undefined>();

  React.useEffect(() => {
    canAddPaymentPass('ref').then(setResult);
  }, []);

  const addToGoogle = useCallback(async () => {
    console.log('add button pressed');

    addPassToGoogle({
      cardNetwork: 3,
      tokenProvider: 3,
      displayName: 'David Meadows',
      lastDigits: '4242',
      userAddress: {
        name: 'David Meadows',
        address1: '1 Infinite Loop',
        locality: 'Cupertino',
        administrativeArea: 'CA',
        countryCode: 'US',
        postalCode: '98103',
        phoneNumber: '415 769 7137',
      },
    })
      .then((r) => console.log(r))
      .catch((error: any) => console.log(error));
  }, []);

  const addToIos = useCallback(async () => {
    console.log('add button pressed');

    addPassToAppleWallet(
      'Jenny Rosen',
      '4242',
      'ref',
      (params) => {
        console.log('success', params);
      },
      (error) => {
        console.log('error', error);
      }
    );
  }, []);

  const onPress = useCallback(() => {
    if (Platform.OS === 'ios') {
      addToIos();
    } else if (Platform.OS === 'android') {
      addToGoogle();
    }
  }, [addToGoogle, addToIos]);

  const walletName = Platform.OS === 'ios' ? 'Apple Wallet' : 'Google Pay';

  return (
    <View style={styles.container}>
      <Text>Can add payment pass? {result}</Text>
      <Pressable style={styles.button} onPress={onPress}>
        <Text style={styles.buttonText}>Add pass to {walletName}</Text>
      </Pressable>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
  },
  button: {
    height: 100,
    width: 200,
    backgroundColor: 'black',
    borderRadius: 20,
  },
  buttonText: { textAlign: 'center', marginTop: 40, color: 'white' },
});
