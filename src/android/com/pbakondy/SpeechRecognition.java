// https://developer.android.com/reference/android/speech/SpeechRecognizer.html

package com.pbakondy;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.PluginResult;

import org.json.JSONArray;
import org.json.JSONException;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;

import android.Manifest;
import android.os.Build;
import android.os.Bundle;

import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;

import android.util.Log;
import android.view.View;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SpeechRecognition extends CordovaPlugin {

  private static final String LOG_TAG = "SpeechRecognition";

  private static final int REQUEST_CODE_PERMISSION = 2001;
  private static final int REQUEST_CODE_SPEECH = 2002;
  private static final String IS_RECOGNITION_AVAILABLE = "isRecognitionAvailable";
  private static final String START_LISTENING = "startListening";
  private static final String STOP_LISTENING = "stopListening";
  private static final String GET_SUPPORTED_LANGUAGES = "getSupportedLanguages";
  private static final String HAS_PERMISSION = "hasPermission";
  private static final String REQUEST_PERMISSION = "requestPermission";
  private static final int MAX_RESULTS = 5;
  private static final String NOT_AVAILABLE = "Speech recognition service is not available on the system.";
  private static final String MISSING_PERMISSION = "Missing permission";

  private JSONArray mLastPartialResults = new JSONArray();

  private static final String RECORD_AUDIO_PERMISSION = Manifest.permission.RECORD_AUDIO;

  private CallbackContext callbackContext;
  private LanguageDetailsChecker languageDetailsChecker;
  private Activity activity;
  private Context context;
  private View view;
  private SpeechRecognizer recognizer;

  @Override
  public void initialize(CordovaInterface cordova, CordovaWebView webView) {
    super.initialize(cordova, webView);

    activity = cordova.getActivity();
    context = webView.getContext();
    view = webView.getView();

    view.post(new Runnable() {
      @Override
      public void run() {
        recognizer = SpeechRecognizer.createSpeechRecognizer(activity);
        SpeechRecognitionListener listener = new SpeechRecognitionListener();
        recognizer.setRecognitionListener(listener);
      }
    });
  }

  @Override
  public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
    this.callbackContext = callbackContext;

    Log.d(LOG_TAG, "execute() action " + action);

    try {
      if (IS_RECOGNITION_AVAILABLE.equals(action)) {
        boolean available = isRecognitionAvailable();
        PluginResult result = new PluginResult(PluginResult.Status.OK, available);
        callbackContext.sendPluginResult(result);
        return true;
      }

      if (START_LISTENING.equals(action)) {
        if (!isRecognitionAvailable()) {
          callbackContext.error(NOT_AVAILABLE);
          return true;
        }
        if (!audioPermissionGranted(RECORD_AUDIO_PERMISSION)) {
          callbackContext.error(MISSING_PERMISSION);
          return true;
        }

        String lang = args.optString(0);
        if (lang == null || lang.isEmpty() || lang.equals("null")) {
          lang = Locale.getDefault().toString();
        }

        int matches = args.optInt(1, MAX_RESULTS);

        String prompt = args.optString(2);
        if (prompt == null || prompt.isEmpty() || prompt.equals("null")) {
          prompt = null;
        }

        mLastPartialResults = new JSONArray();
        Boolean showPartial = args.optBoolean(3, false);
        Boolean showPopup = args.optBoolean(4, true);
        startListening(lang, matches, prompt,showPartial, showPopup);

        return true;
      }

      if (STOP_LISTENING.equals(action)) {
        final CallbackContext callbackContextStop = this.callbackContext;
        view.post(new Runnable() {
          @Override
          public void run() {
            if(recognizer != null) {
              recognizer.stopListening();
            }
            callbackContextStop.success();
          }
        });
        return true;
      }

      if (GET_SUPPORTED_LANGUAGES.equals(action)) {
        getSupportedLanguages();
        return true;
      }

      if (HAS_PERMISSION.equals(action)) {
        hasAudioPermission();
        return true;
      }

      if (REQUEST_PERMISSION.equals(action)) {
        requestAudioPermission();
        return true;
      }

    } catch (Exception e) {
      e.printStackTrace();
      callbackContext.error(e.getMessage());
    }

    return false;
  }

  private boolean isRecognitionAvailable() {
    return SpeechRecognizer.isRecognitionAvailable(context);
  }

  private void startListening(String language, int matches, String prompt, final Boolean showPartial, Boolean showPopup) {
    Log.d(LOG_TAG, "startListening() language: " + language + ", matches: " + matches + ", prompt: " + prompt + ", showPartial: " + showPartial + ", showPopup: " + showPopup);

    final Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
    intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
    intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, language);
    intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, matches);
    intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE,
            activity.getPackageName());
    intent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, showPartial);
    intent.putExtra("android.speech.extra.DICTATION_MODE", showPartial);

    if (prompt != null) {
      intent.putExtra(RecognizerIntent.EXTRA_PROMPT, prompt);
    }

    if (showPopup) {
      cordova.startActivityForResult(this, intent, REQUEST_CODE_SPEECH);
    } else {
      view.post(new Runnable() {
        @Override
        public void run() {
          recognizer.startListening(intent);
        }
      });
    }
  }

  private void getSupportedLanguages() {
    if (languageDetailsChecker == null) {
      languageDetailsChecker = new LanguageDetailsChecker(callbackContext);
    }

    List<String> supportedLanguages = languageDetailsChecker.getSupportedLanguages();
    if (supportedLanguages != null) {
      JSONArray languages = new JSONArray(supportedLanguages);
      callbackContext.success(languages);
      return;
    }

    Intent detailsIntent = new Intent(RecognizerIntent.ACTION_GET_LANGUAGE_DETAILS);
    activity.sendOrderedBroadcast(detailsIntent, null, languageDetailsChecker, null, Activity.RESULT_OK, null, null);
  }

  private void hasAudioPermission() {
    PluginResult result = new PluginResult(PluginResult.Status.OK, audioPermissionGranted(RECORD_AUDIO_PERMISSION));
    this.callbackContext.sendPluginResult(result);
  }

  private void requestAudioPermission() {
    requestPermission(RECORD_AUDIO_PERMISSION);
  }

  private boolean audioPermissionGranted(String type) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
      return true;
    }
    return cordova.hasPermission(type);
  }

  private void requestPermission(String type) {
    if (!audioPermissionGranted(type)) {
      cordova.requestPermission(this, 23456, type);
    } else {
      this.callbackContext.success();
    }
  }

  @Override
  public void onRequestPermissionResult(int requestCode, String[] permissions, int[] grantResults) throws JSONException {
    if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
      this.callbackContext.success();
    } else {
      this.callbackContext.error("Permission denied");
    }
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    Log.d(LOG_TAG, "onActivityResult() requestCode: " + requestCode + ", resultCode: " + resultCode);

    if (requestCode == REQUEST_CODE_SPEECH) {
      if (resultCode == Activity.RESULT_OK) {
        try {
          ArrayList<String> matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
          JSONArray jsonMatches = new JSONArray(matches);
          this.callbackContext.success(jsonMatches);
        } catch (Exception e) {
          e.printStackTrace();
          this.callbackContext.error(e.getMessage());
        }
      } else {
        this.callbackContext.error(Integer.toString(resultCode));
      }
      return;
    }

    super.onActivityResult(requestCode, resultCode, data);
  }


  private class SpeechRecognitionListener implements RecognitionListener {

    @Override
    public void onBeginningOfSpeech() {
    }

    @Override
    public void onBufferReceived(byte[] buffer) {
    }

    @Override
    public void onEndOfSpeech() {
    }

    @Override
    public void onError(int errorCode) {
      String errorMessage = getErrorText(errorCode);
      Log.d(LOG_TAG, "Error: " + errorMessage);
      callbackContext.error(errorMessage);
    }

    @Override
    public void onEvent(int eventType, Bundle params) {
    }

    @Override
    public void onPartialResults(Bundle bundle) {
      ArrayList<String> matches = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
      Log.d(LOG_TAG, "SpeechRecognitionListener partialResults: " + matches);
      JSONArray matchesJSON = new JSONArray(matches);
      try {
        if (matches != null
                && matches.size() > 0
                        && !mLastPartialResults.equals(matchesJSON)) {
          mLastPartialResults = matchesJSON;
          PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, matchesJSON);
          pluginResult.setKeepCallback(true);
          callbackContext.sendPluginResult(pluginResult);
        }
      } catch (Exception e) {
        e.printStackTrace();
        callbackContext.error(e.getMessage());
      }
    }

    @Override
    public void onReadyForSpeech(Bundle params) {
      Log.d(LOG_TAG, "onReadyForSpeech");
    }

    @Override
    public void onResults(Bundle results) {
      ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
      Log.d(LOG_TAG, "SpeechRecognitionListener results: " + matches);
      try {
        JSONArray jsonMatches = new JSONArray(matches);
        callbackContext.success(jsonMatches);
      } catch (Exception e) {
        e.printStackTrace();
        callbackContext.error(e.getMessage());
      }
    }

    @Override
    public void onRmsChanged(float rmsdB) {
    }

    private String getErrorText(int errorCode) {
      String message;
      switch (errorCode) {
        case SpeechRecognizer.ERROR_AUDIO:
          message = "Audio recording error";
          break;
        case SpeechRecognizer.ERROR_CLIENT:
          message = "Client side error";
          break;
        case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
          message = "Insufficient permissions";
          break;
        case SpeechRecognizer.ERROR_NETWORK:
          message = "Network error";
          break;
        case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
          message = "Network timeout";
          break;
        case SpeechRecognizer.ERROR_NO_MATCH:
          message = "No match";
          break;
        case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
          message = "RecognitionService busy";
          break;
        case SpeechRecognizer.ERROR_SERVER:
          message = "error from server";
          break;
        case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
          message = "No speech input";
          break;
        default:
          message = "Didn't understand, please try again.";
          break;
      }
      return message;
    }
  }

}
