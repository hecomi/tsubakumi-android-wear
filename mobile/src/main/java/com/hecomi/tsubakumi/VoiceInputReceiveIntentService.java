package com.hecomi.tsubakumi;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.RemoteInput;
import android.util.Log;

import com.hecomi.tsubakumi.util.HttpRequest;

public class VoiceInputReceiveIntentService extends IntentService {
    private static final String TAG = VoiceInputReceiveIntentService.class.getSimpleName();

    public VoiceInputReceiveIntentService() {
        super("VoiceInputReceiveIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Bundle remoteInput = RemoteInput.getResultsFromIntent(intent);
        String urlTemplate = intent.getStringExtra("urlTemplate");
        if (urlTemplate != null) {
            CharSequence message = remoteInput.getCharSequence(Tsubakumi.EXTRA_VOICE_REPLY);
            String url = urlTemplate.replace("%s", message);
            Log.i(TAG, url);
            HttpRequest.Get(url);
        } else {
            Log.e(TAG, "urlTemplate is null");
        }
    }
}
