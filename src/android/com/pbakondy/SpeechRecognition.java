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
import android.os.Build;
import android.Manifest;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;

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

  // android.speech.extra.MAX_RESULTS
  private static final int MAX_RESULTS = 5;

  private static final String NOT_AVAILABLE = "Speech recognition service is available on the system.";
  private static final String MISSING_PERMISSION = "Missing permission";

  private static final String RECORD_AUDIO_PERMISSION = Manifest.permission.RECORD_AUDIO;

  private CallbackContext callbackContext;
  private LanguageDetailsChecker languageDetailsChecker;
  private Activity activity;
  private Context context;

  @Override
  public void initialize(CordovaInterface cordova, CordovaWebView webView) {
    super.initialize(cordova, webView);

    activity = cordova.getActivity();
    context = webView.getContext();
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

        String lang = args.optString(0, Locale.getDefault().toString());
        int matches = args.optInt(1, MAX_RESULTS);
        String prompt = args.optString(2, "");

        startListening(lang, matches, prompt);

        return true;
      }

      if (STOP_LISTENING.equals(action)) {
        this.callbackContext.success();
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

  private void startListening(String language, int matches, String prompt) {
    Log.d(LOG_TAG, "startListening() language: " + language + ", matches: " + matches + ", prompt: " + prompt);

    Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
    intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
    intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, language);
    intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, matches);

    if (!("".equals(prompt))) {
      intent.putExtra(RecognizerIntent.EXTRA_PROMPT, prompt);
    }

    cordova.startActivityForResult(this, intent, REQUEST_CODE_SPEECH);
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
    return (PackageManager.PERMISSION_GRANTED ==
      ContextCompat.checkSelfPermission(activity, type));
  }

  private void requestPermission(String type) {
    if (!audioPermissionGranted(type)) {
      ActivityCompat.requestPermissions(activity, new String[]{type}, REQUEST_CODE_PERMISSION);
    }
    this.callbackContext.success();
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

}
