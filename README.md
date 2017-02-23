# cordova-plugin-speechrecognition

[![npm](https://img.shields.io/npm/v/cordova-plugin-speechrecognition.svg)](https://www.npmjs.com/package/cordova-plugin-speechrecognition)
![Platform](https://img.shields.io/badge/platform-android%20%7C%20ios-lightgrey.svg)
[![Donate](https://img.shields.io/badge/Donate-PayPal-green.svg)](https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=R7STJ6V2PNEMA)

This is a cordova plugin for Speech Recognition.


## Installation

```
cordova plugin add cordova-plugin-speechrecognition
```

## Supported Platforms

- Android
- iOS

## Usage

This plugin requires internet connection.

### isRecognitionAvailable()

```js
window.plugins.speechRecognition.isRecognitionAvailable(
  Function successCallback, Function errorCallback)
```

Result of success callback is a `Boolean`.

### startListening()

```js

let options = {
  String language,
  Number matches,
  String prompt,      // Android only
  Boolean showPopup,  // Android only
  Boolean showPartial // iOS only
}

window.plugins.speechRecognition.startListening(
  Function successCallback, Function errorCallback, Object options)
```

This method has an options parameter with the following optional values:

- `language` {String} used language for recognition (default `"en-US"`)
- `matches` {Number} number of return matches (default `5`, on iOS: maximum number of matches)
- `prompt` {String} displayed prompt of listener popup window (default `""`, Android only)
- `showPopup` {Boolean} display listener popup window with prompt (default `true`, Android only)
- `showPartial` {Boolean} Allow partial results to be returned (default `false`, iOS only)

Result of success callback is an `Array` of recognized terms.

There is a difference between Android and iOS platforms. On Android speech recognition stops when the speaker finishes speaking (at end of sentence). On iOS the user has to stop manually the recognition process by calling stopListening() method.

If you set `showPartial` to `true` on iOS the success callback will be called multiple times until `stopListening()` called.

### stopListening()

iOS only method.

```js
window.plugins.speechRecognition.stopListening(
  Function successCallback, Function errorCallback)
```

Stop the recognition process. No return value.

### getSupportedLanguages()

```js
window.plugins.speechRecognition.getSupportedLanguages(
  Function successCallback, Function errorCallback)
```

Result of success callback is an `Array` of supported languages.

```js
// getSupportedLanguages result on Android:

let supportedLanguagesAndroid =
  ["af-ZA", "id-ID", "ms-MY", "ca-ES", "cs-CZ", "da-DK", "de-DE", "en-AU", "en-CA",
  "en-001", "en-IN", "en-IE", "en-NZ", "en-PH", "en-ZA", "en-GB", "en-US", "es-AR",
  "es-BO", "es-CL", "es-CO", "es-CR", "es-EC", "es-US", "es-SV", "es-ES", "es-GT",
  "es-HN", "es-MX", "es-NI", "es-PA", "es-PY", "es-PE", "es-PR", "es-DO", "es-UY",
  "es-VE", "eu-ES", "fil-PH", "fr-FR", "gl-ES", "hr-HR", "zu-ZA", "is-IS", "it-IT",
  "lt-LT", "hu-HU", "nl-NL", "nb-NO", "pl-PL", "pt-BR", "pt-PT", "ro-RO", "sl-SI",
  "sk-SK", "fi-FI", "sv-SE", "vi-VN", "tr-TR", "el-GR", "bg-BG", "ru-RU", "sr-RS",
  "uk-UA", "he-IL", "ar-IL", "ar-JO", "ar-AE", "ar-BH", "ar-DZ", "ar-SA", "ar-KW",
  "ar-MA", "ar-TN", "ar-OM", "ar-PS", "ar-QA", "ar-LB", "ar-EG", "fa-IR", "hi-IN",
  "th-TH", "ko-KR", "cmn-Hans-CN", "cmn-Hans-HK", "cmn-Hant-TW", "yue-Hant-HK",
  "ja-JP"];


// getSupportedLanguages result on iOS:

let supportedLanguagesIOS =
  ["nl-NL","es-MX","zh-TW","fr-FR","it-IT","vi-VN","en-ZA","ca-ES","es-CL","ko-KR",
  "ro-RO","fr-CH","en-PH","en-CA","en-SG","en-IN","en-NZ","it-CH","fr-CA","da-DK",
  "de-AT","pt-BR","yue-CN","zh-CN","sv-SE","es-ES","ar-SA","hu-HU","fr-BE","en-GB",
  "ja-JP","zh-HK","fi-FI","tr-TR","nb-NO","en-ID","en-SA","pl-PL","id-ID","ms-MY",
  "el-GR","cs-CZ","hr-HR","en-AE","he-IL","ru-RU","de-CH","en-AU","de-DE","nl-BE",
  "th-TH","pt-PT","sk-SK","en-US","en-IE","es-CO","uk-UA","es-US"];

```

### hasPermission()

```js
window.plugins.speechRecognition.hasPermission(
  Function successCallback, Function errorCallback)
```

Result of success callback is a `Boolean`.

### requestPermission()

```js
window.plugins.speechRecognition.requestPermission(
  Function successCallback, Function errorCallback)
```

This method requests access permission to system resources if it was not granted before.


### Ionic 2 Usage

```typescript
import { SpeechRecognition } from 'ionic-native';

// Check feature available
SpeechRecognition.isRecognitionAvailable()
  .then((available: boolean) => console.log(available))

// Start the recognition process
SpeechRecognition.startListening(options)
  .subscribe(
    (matches: Array<string>) => console.log(matches),
    (onerror) => console.log('error:', onerror)
  )

// Stop the recognition process (iOS only)
SpeechRecognition.stopListening()

// Get the list of supported languages
SpeechRecognition.getSupportedLanguages()
  .then(
    (languages: Array<string>) => console.log(languages),
    (error) => console.log(error)
  )

// Check permission
SpeechRecognition.hasPermission()
  .then((hasPermission: boolean) => console.log(hasPermission))

// Request permissions
SpeechRecognition.requestPermission()
  .then(
    () => console.log('Granted'),
    () => console.log('Denied')
  )
```

Required: [ionic-native](https://www.npmjs.com/package/ionic-native) v2.3.0

See [Ionic Native documentation](https://ionicframework.com/docs/v2/native/speechrecognition/).


## Android Quirks

### Requirements

- cordova-android v5.0.0
- Android API level 14
- `<android:launchMode>` must not be `singleInstance`. It can be `singleTask`, `standard`, `singleTop`.
- [RECORD_AUDIO](https://developer.android.com/reference/android/Manifest.permission.html#RECORD_AUDIO) permission

### How it works

It uses [Speech API](https://cloud.google.com/speech/) of Google. The Android public API recognizes when the speech is over and automatically begins the parsing process. The delay of result depends on the network quality.

### Further readings

- https://developer.android.com/reference/android/speech/package-summary.html
- https://developer.android.com/reference/android/speech/SpeechRecognizer.html

## iOS Quirks

### Requirements

- XCode 8.0 (requires 10.12+ macOS Sierra or 10.11.5+ OS X El Capitan)
- iOS 10
- [NSMicrophoneUsageDescription](https://developer.apple.com/library/content/documentation/General/Reference/InfoPlistKeyReference/Articles/CocoaKeys.html#//apple_ref/doc/uid/TP40009251-SW25) permission
- [NSSpeechRecognitionUsageDescription](https://developer.apple.com/library/content/documentation/General/Reference/InfoPlistKeyReference/Articles/CocoaKeys.html#//apple_ref/doc/uid/TP40009251-SW52) permission

### How it works

The Speech APIs perform speech recognition by communicating with Apple's servers or using an on-device speech recognizer, if available.

Because your app may need to connect to the servers to perform recognition, it's essential that you respect the privacy of your users and treat their utterances as sensitive data. For this reason, you must get the user's explicit permission before you initiate speech recognition.

The plugin works in [AVAudioSessionCategoryPlayAndRecord](https://developer.apple.com/reference/avfoundation/avaudiosessioncategoryplayandrecord) mode to enable playing audio.


### Further readings

- https://developer.apple.com/reference/speech?language=objc
- https://developer.apple.com/reference/speech/sfspeechrecognizer?language=objc

## Author

### Peter Bakondy

- https://github.com/pbakondy


## LICENSE

**cordova-plugin-speechrecognition** is licensed under the MIT Open Source license. For more information, see the LICENSE file in this repository.
