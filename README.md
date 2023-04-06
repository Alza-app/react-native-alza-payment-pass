# react-native-alza-payment-pass

sample

## Installation

```sh
npm install react-native-alza-payment-pass
```

## Running example app

`yarn example ios` or `yarn example android`

## Usage

```js
import {
  addPassToGoogle,
  canAddPaymentPass,
} from 'react-native-alza-payment-pass';

const result = await canAddPaymentPass();
await addPassToGoogle({
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
```

## Troubleshooting

If the app insta-crashes on iOS, make sure you run `bundle exec pod install` under `example/ios` and try running again.

For Android, make sure you have the tapandpay library under `~/m2/repository`, so Gradle can find it with `mavenLocal()`
