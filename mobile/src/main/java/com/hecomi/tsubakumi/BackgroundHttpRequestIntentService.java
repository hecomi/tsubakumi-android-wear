package com.hecomi.tsubakumi;

import android.app.IntentService;
import android.content.Intent;

import com.hecomi.tsubakumi.util.HttpRequest;

public class BackgroundHttpRequestIntentService extends IntentService {
    public BackgroundHttpRequestIntentService() {
        super("BackgroundHttpRequestIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        HttpRequest.Get(intent.getStringExtra("url"));
    }
}
