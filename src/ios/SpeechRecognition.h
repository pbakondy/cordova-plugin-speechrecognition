#import <Cordova/CDV.h>
#import <Speech/Speech.h>

@interface SpeechRecognition : CDVPlugin

- (void)isRecognitionAvailable:(CDVInvokedUrlCommand*)command;
- (void)startListening:(CDVInvokedUrlCommand*)command;
- (void)stopListening:(CDVInvokedUrlCommand*)command;
- (void)getSupportedLanguages:(CDVInvokedUrlCommand*)command;
- (void)hasPermission:(CDVInvokedUrlCommand*)command;
- (void)requestPermission:(CDVInvokedUrlCommand*)command;

@end
