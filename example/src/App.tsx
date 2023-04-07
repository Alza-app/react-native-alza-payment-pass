import * as React from 'react';

import { StyleSheet, View, Text, Pressable } from 'react-native';
import {
  addPassToGoogle,
  canAddPaymentPass,
} from 'react-native-alza-payment-pass';
import { useCallback } from 'react';

const OPC =
  'eyJjYXJkSW5mbyI6eyJlbmNyeXB0ZWREYXRhIjoiMkQ3QTZENjY1Q0YyMDQyODdDRjZGRkI1NDg2QzAxQTBBRUM3OTMzRDZDOEMwOTg5MUZGMzFFRDNERUM1RTNEMjc3RjYxQjVCNzhBNDVGQTQxNEI5N0MzNUU0NjQ4QkZCRTFBMDJGRjg1RjFDOEM2MjQ3QUVENUZDMUUwMDk1ODdFNzUzM0M1RTcxMUQ5MDc2OEFBODVDRjlGNjY2MUZGQUE4QTYyNUFBMTc1NkMxODc4OEZBMUM3NEUyOEI2ODg4RTc4MjUxOUI3QkM1NDBFNTY0MDJERkQ5RkFFRTk1QkIxNTRFMDNFQjI5RTBGM0UwNzY5QjA2N0Y1MzYzQTMxQUZDNjlBNzU2OTI1NkRFNkZFNzg5NDIzNjg5NTlDNDg2IiwiZW5jcnlwdGVkS2V5IjoiMzdERTAxNjBERjlDQUFCNzA0MUNGNTRCNkUwRkJBOTQ5RjkxMkJGMzY4RjYyNUExMDZGNDhCRUYzMTQ3NzZGNDEyMURBQjFEMEREQzgxMEI4MTYzRTEyMkU4MTYyMDY5MUMwMjg4M0JFNTM3QkIwMTkyQjJEQTRDN0Y3QkM1NjczMDFDNjAzMzIwRDg2QzE1Q0I4M0ZDQTNCRjUwRjEwNzM0M0I4ODk5OEE5OUEzQzI5NEJCN0M4NjQwMjg1MkYyQzhDOEI1QUJGQ0Y4QzMwNThBQzYxMTgxOTBFRUFGN0ZFMTNGNDhBNDREQzU1RkZDOUZGRDIzRkZGNkRDNjFCNzQzNzc2QTY2RjM5MDc3RThGNDAzQTZDQjY5QjFDMzYzOTM2Q0Q3MUYzNzlFOTVBNkM2NThCRjM4QzcwMTI1QjJDQzIzRUZEMEJENjQ3NUMwMjk3M0Y1MjQ1MUNFMjUzOTU3QkI2NTBBMTY0NDZDREZGNkY5MjUxNTVFQzNFRENEN0I1MDhGRTI2MkU3OEQzQzczQTcyREIzQTE3NzBEOTQxQTYyOTlGNzA5NzhBNzNERDQ2MUFGM0VBRTEyOTY1NkYzRkM2RUVCMDlDRDA0NUFGQjU5NzZEMkFEQUQ4ODIwNTYwMjVDRjIwQTQzM0RBMUM3QkJDQzc5NDE2RjAxOTUiLCJpdiI6IjM1RkEzNDRBREJEOEU0MUNEMzMwRUVGN0RFQ0Y1RTc2Iiwib2FlcEhhc2hpbmdBbGdvcml0aG0iOiJTSEEyNTYiLCJwdWJsaWNLZXlGaW5nZXJwcmludCI6IkY3Q0FBNzAyM0RENkZDRTk5Q0Q0NEM1RUEzOUY2N0FGRUM0Rjg0NzUifSwidG9rZW5pemF0aW9uQXV0aGVudGljYXRpb25WYWx1ZSI6ImV5SmtZWFJoVm1Gc2FXUlZiblJwYkZScGJXVnpkR0Z0Y0NJNklqSXdNak10TURRdE1EVlVNVGs2TXpRNk1qVmFJaXdpYVc1amJIVmtaV1JHYVdWc1pITkpiazl5WkdWeUlqb2laR0YwWVZaaGJHbGtWVzUwYVd4VWFXMWxjM1JoYlhCOFlXTmpiM1Z1ZEU1MWJXSmxjbnhoWTJOdmRXNTBSWGh3YVhKNUlpd2ljMmxuYm1GMGRYSmxJam9pUkVFM1ZUWk9jV1JST1hOVVdsRlpjaTlqU1ZkdVlVbDBhbkZOTmxoUWVEUXlOM1l3ZFVJemNWbHJSMVY1YTNkd1pXdHJVMGhQVmtabWN6QTBZaXRYUTBkSVEyeDNOWGd2U2pRdllWZHhXRkJQWldKV1NUbEJhSHB3ZWk5SmJEZGFVa1prVFN0RGIzQklSMFEzY1RWemIyMTRTSEVyZW5ZMlkwMUJkVWhpVEVVclFWRkhSRU53ZURSNWVXeEdlV2RoYWxab1ZqSm9SREJ6Y0ZGT1lreHNhMlZhWmsxVVdTODJTWGxhUXpoWFJGRmpZako1UTBSTFlVSTJlVkl3SzBkSFR6QnhVVEJ4VGtWeGQwZ3JlVk14Y1hreVJIZEdTRm80WTNwclQxaE5ia296VEZsbFVqSlJWRzFuZERaNE4wTldhekpTTUVGWVZWQTNXV2hHVUhVNVJraDVhVVY1VUhsbFFtOW9ZV0ZLYWtaVldVSjRNRFpWTlRGUlEwSjBOR3BaT0hCaFdUUnBLMHBGUVRSRmFURklaSE0zY1RKNGVXaDZjVEJhWm5kcmRuRk9UR1ptVVhaclVGTk5ieTkwWlc5MFYwdDVUMmh4TkRSQ1ZUTlNWMDFuUlU1MVpUVlZZV055Wmt4cE9FTmFUMlJTVkdSMk1HRlBXRE00Y0ZCdkwxTXZVbEJrTld0RU5qSkhibFUzV0VWNldYTnRjVEZEUjJ4R1duTnpXbUptUnk5dWJHTnhkVlpwZGtGSWRYWkVhSFZ3Um5CRFdEZHZjaXRXSzFZeGNYQmxlRUV3ZUZOdVpYWlZUalZyWVZseVZDOW5WbGhZTXpkeFpGQnJRVEJtTlRJMlJtUnBPVU5ZY0dGVVFXcFRWRE00ZVc5UlkwSmlPR1Z5VEZWalFuSlVjM2xEUm5WQ1VrTldTRlpwWlVKaEwwSk9SMHN5ZFVjNVN6TnlXSGhVZFdvMFpUZDBXRmRRT1Vsb1ZHcDZNbFJLYVU1allXSlplRWRvTlZWbk5FeDBibTkzU0ZvclVuZHJNMmhKTVhVelZrOUtNMFZvV2pORGNWWlJPSHBwYldSNFRrTk9VbVpNYjBoQ05qVlFTbGROYUVKMmJ6VklRMHdyTTJOUE5rSmtiazAwWTNJNFJVeHNZVWhhWldneGJ6ZHFTR0pRVVZSVkwxZEVVRUZyY1RGcGMxSTJUMncyVGxadVJuY3ZXbk05SWl3aWMybG5ibUYwZFhKbFFXeG5iM0pwZEdodElqb2lVbE5CTFZOSVFUSTFOaUlzSW5abGNuTnBiMjRpT2lJekluMD0ifQ==';

export default function App() {
  const [canAdd, setCanAdd] = React.useState(false);
  const [result, setResult] = React.useState<number>(-1);

  React.useEffect(() => {
    if (result === -1) {
      canAddPaymentPass().then((canAddResult) => {
        setCanAdd(canAddResult === 1);
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
      <Text style={{ fontSize: 20, fontFamily: 'monospace' }}>
        canAddPaymentPass(): {canAdd ? 'YES' : 'NO'}
      </Text>
      <Pressable
        style={{
          height: 100,
          paddingHorizontal: 30,
          backgroundColor: '#09f',
          marginTop: 20,
          borderRadius: 5,
        }}
        onPress={onPress}
      >
        <Text
          style={{
            textAlign: 'center',
            marginTop: 35,
            fontSize: 20,
            fontWeight: 'bold',
          }}
        >
          Add to Google Pay
        </Text>
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
  box: {
    width: 60,
    height: 60,
    marginVertical: 20,
  },
});
