package com.hecomi.tsubakumi;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import java.io.IOException;


public class MainActivity extends ActionBarActivity {
    static final String TAG = MainActivity.class.getSimpleName();

    GoogleCloudMessaging gcm;
    Context context;
    String regId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initGCM();

        WebView webview = (WebView)findViewById(R.id.webView);
        webview.setWebViewClient(new WebViewClient());
        webview.loadUrl(Common.INITIAL_WEB_URI);
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkPlayServices();
    }

    private boolean checkPlayServices() {
        try {
            int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
            if (resultCode != ConnectionResult.SUCCESS) {
                if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                    GooglePlayServicesUtil.getErrorDialog(
                            resultCode, this, Common.PLAY_SERVICES_RESOLUTION_REQUEST).show();
                } else {
                    Log.i(TAG, "This device is not supported.");
                    finish();
                }
                return false;
            }
        } catch (Exception e) {
            Log.d(TAG, e.toString());
        }
        return true;
    }

    private void initGCM() {

        if (checkPlayServices()) {
            gcm = GoogleCloudMessaging.getInstance(this);
            regId = getRegistrationId(this);

            if (regId.isEmpty()) {
                registerInBackground();
            } else {
                Log.i(TAG, "registrationID = " + regId);
            }
        } else {
            Log.i(TAG, "No valid Google Play Services APK found.");
        }
    }

    private String getRegistrationId(Context context) {
        final SharedPreferences prefs = getGCMPreferences(context);
        String registrationId = prefs.getString(Common.PROPERTY_REG_ID, "");
        if (registrationId.isEmpty()) {
            Log.i(TAG, "Registration not found.");
            return "";
        }
        int registeredVersion = prefs.getInt(Common.PROPERTY_APP_VERSION, Integer.MIN_VALUE);
        int currentVersion = getAppVersion(context);
        if (registeredVersion != currentVersion) {
            Log.i(TAG, "App version changed.");
            return "";
        }
        return registrationId;
    }

    private SharedPreferences getGCMPreferences(Context context) {
        return getSharedPreferences(MainActivity.class.getSimpleName(),
                Context.MODE_PRIVATE);
    }

    private static int getAppVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            throw new RuntimeException("Could not get package name: " + e);
        }
    }

    private void registerInBackground() {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                String msg = "";
                try {
                    if (gcm == null) {
                        gcm = GoogleCloudMessaging.getInstance(MainActivity.this);
                    }
                    regId = gcm.register(Common.SENDER_ID);
                    msg = "Device registered, registration ID=" + regId;

                    // sendRegistrationIdToBackend();
                    storeRegistrationId(MainActivity.this, regId);
                } catch (IOException ex) {
                    msg = "Error :" + ex.getMessage();
                }
                return msg;
            }

            @Override
            protected void onPostExecute(String msg) {
                Log.i(TAG, msg + "\n");
            }
        }.execute();
    }

    private void storeRegistrationId(Context context, String regId) {
        final SharedPreferences prefs = getGCMPreferences(context);
        int appVersion = getAppVersion(context);
        Log.i(TAG, "Saving regId on app version " + appVersion);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(Common.PROPERTY_REG_ID, regId);
        editor.putInt(Common.PROPERTY_APP_VERSION, appVersion);
        editor.apply();
    }
}
