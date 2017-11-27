# Changelog

## v1.2.0

- Android: add `stopListening` [by Simone Compagnone]
- Android: enable partial results [by Simone Compagnone]

## v1.1.2

- iOS: `startListening` do not run in background
- iOS: `startListening` check existing process

## v1.1.1

- Android: use Cordova Permissions API
- Android: drop Android Support Library requirement

## v1.1.0

- Android: recognition is runnable in the background without popup window ( option `showPopup` )
- modify signature of function `startListening`
- Android: real callbacks for `requestPermission`

## v1.0.5

- iOS: use `AVAudioSessionCategoryPlayAndRecord` mode
- iOS: run `startListening` and `stopListening` in the background

## v1.0.4

- iOS: return partial results ( option `showPartial` )

## v1.0.3

- initial release of `cordova-plugin-speechrecognition`
