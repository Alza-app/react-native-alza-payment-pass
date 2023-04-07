import * as React from 'react';
import { useCallback } from 'react';

import { Pressable, StyleSheet, Text, View } from 'react-native';
import {
  addPassToGoogle,
  canAddPaymentPass,
  PAYMENT_PASS_RESULT_SUCCESSFUL,
} from 'react-native-alza-payment-pass';

// Make sure you set the OPC in your .env file
const OPC = process.env.OPC;

export default function App() {
  const [canAdd, setCanAdd] = React.useState(false);
  const [result, setResult] = React.useState<string | null>(null);

  React.useEffect(() => {
    if (!result) {
      canAddPaymentPass().then((canAddResult) => {
        setCanAdd(canAddResult === PAYMENT_PASS_RESULT_SUCCESSFUL);
        setResult(canAddResult);
      });
    }
  }, [result]);

  const onPress = useCallback(async () => {
    console.log('add button pressed');

    addPassToGoogle({
      opc: OPC,
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

  return (
    <View style={styles.container}>
      <Text style={styles.canAdd}>
        canAddPaymentPass(): {canAdd ? 'YES' : 'NO'}
      </Text>
      <Pressable style={styles.button} onPress={onPress}>
        <Text style={styles.buttonText}>Add to Google Pay</Text>
      </Pressable>
    </View>
  );
}

const styles = StyleSheet.create({
  canAdd: {
    fontSize: 20,
    fontFamily: 'monospace',
  },
  button: {
    height: 100,
    paddingHorizontal: 30,
    backgroundColor: '#09f',
    marginTop: 20,
    borderRadius: 5,
  },
  buttonText: {
    textAlign: 'center',
    marginTop: 35,
    fontSize: 20,
    fontWeight: 'bold',
  },
  container: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
  },
});
