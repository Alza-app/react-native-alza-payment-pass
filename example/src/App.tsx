import * as React from 'react';
import { useCallback, useEffect, useRef, useState } from 'react';

import {
  AppState,
  Platform,
  Pressable,
  StyleSheet,
  Text,
  View,
} from 'react-native';
import {
  CanAddPaymentPassResult,
  addPassToAppleWallet,
  addPassToGoogle,
  canAddPaymentPass,
} from 'react-native-alza-payment-pass';

export const useCanAddCardToDigitalWallet =
  (): null | CanAddPaymentPassResult => {
    const [canAdd, setCanAdd] = useState<null | CanAddPaymentPassResult>(null);
    const appState = useRef(AppState.currentState);
    const [appStateVisible, setAppStateVisible] = useState(appState.current);

    useEffect(() => {
      const check = async () => {
        // should really check that this is an emulator since that won't work on android

        let result = null;
        switch (Platform.OS) {
          case 'ios':
            result = await canAddPaymentPass('ref');
            break;
          case 'android':
            result = await canAddPaymentPass({
              cardNetwork: 3,
              tokenProvider: 3,
              lastDigits: '4242',
            });
            break;
          default:
            throw new Error('Unsupported platform: ' + Platform.OS);
        }

        console.log('result', result);
        switch (result) {
          case 'CAN_ADD':
          case 'ALREADY_ADDED':
          case 'BLOCKED':
            setCanAdd(result);
            break;
          default:
            console.error('Unexpected result from canAddPaymentPass', result);
            setCanAdd('BLOCKED');
        }
      };
      check();
    }, [appStateVisible]);

    useEffect(() => {
      const subscription = AppState.addEventListener(
        'change',
        (nextAppState) => {
          appState.current = nextAppState;
          setAppStateVisible(appState.current);
          console.log('AppState', appState.current);
        }
      );

      return () => {
        subscription.remove();
      };
    }, []);
    return canAdd;
  };

// Make sure you set the OPC in your .env file
const OPC = process.env.OPC;

export default function App() {
  const canAdd = useCanAddCardToDigitalWallet();

  const addToGoogle = useCallback(async () => {
    if (!OPC) {
      throw new Error('OPC not set');
    }
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
      <Text>Can add payment pass? {canAdd}</Text>
      <Pressable style={styles.button} onPress={onPress}>
        <Text style={styles.buttonText}>Add pass to {walletName}</Text>
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
