// http://stackoverflow.com/a/10548680

package com.pbakondy;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;

import org.apache.cordova.CallbackContext;
import org.json.JSONArray;

import java.util.List;

public class LanguageDetailsChecker extends BroadcastReceiver {

    private static final String ERROR = "Could not get list of languages";

    private List<String> supportedLanguages;
    private String languagePreference;
    private CallbackContext callbackContext;

    public LanguageDetailsChecker(CallbackContext callbackContext) {
        super();
        this.callbackContext = callbackContext;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle results = getResultExtras(true);

        if (results.containsKey(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE)) {
            languagePreference = results.getString(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE);
        }

        if (results.containsKey(RecognizerIntent.EXTRA_SUPPORTED_LANGUAGES)) {
            supportedLanguages = results.getStringArrayList(RecognizerIntent.EXTRA_SUPPORTED_LANGUAGES);

            JSONArray languages = new JSONArray(supportedLanguages);
            callbackContext.success(languages);
            return;
        }

        callbackContext.error(ERROR);
    }

    public List<String> getSupportedLanguages() {
        return supportedLanguages;
    }

    public String getLanguagePreference() {
        return languagePreference;
    }
}
