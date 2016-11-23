module.exports = {
  isRecognitionAvailable: function(successCallback, errorCallback) {
    cordova.exec(successCallback, errorCallback, 'SpeechRecognition', 'isRecognitionAvailable', []);
  },
  startListening: function(successCallback, errorCallback, options) {
    options = options || {};
    cordova.exec(successCallback, errorCallback, 'SpeechRecognition', 'startListening', [ options.language, options.matches, options.prompt, options.showPartial, options.showPopup ]);
  },
  stopListening: function(successCallback, errorCallback) {
    cordova.exec(successCallback, errorCallback, 'SpeechRecognition', 'stopListening', []);
  },
  getSupportedLanguages: function(successCallback, errorCallback) {
    cordova.exec(successCallback, errorCallback, 'SpeechRecognition', 'getSupportedLanguages', []);
  },
  hasPermission: function(successCallback, errorCallback) {
    cordova.exec(successCallback, errorCallback, 'SpeechRecognition', 'hasPermission', []);
  },
  requestPermission: function(successCallback, errorCallback) {
    cordova.exec(successCallback, errorCallback, 'SpeechRecognition', 'requestPermission', []);
  }
};
