package com.hecomi.tsubakumi.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class HttpRequest {
    private static final String TAG = HttpRequest.class.getSimpleName();
    public interface Callback extends AsyncTaskWithCallback.Callback<HttpResponse> {}
    public interface BitmapCallback extends AsyncTaskWithCallback.Callback<Bitmap> {}

    public static void Get(String url) {
        Get(url, null);
    }

    public static void Get(String url, Callback callback) {
        new AsyncTaskWithCallback<String, Void, HttpResponse>(callback) {
            @Override
            protected HttpResponse doInBackground(String... params) {
                HttpResponse response = null;
                try {
                    String url = params[0];
                    Log.i(TAG, "URL: " + url);
                    HttpGet httpGet = new HttpGet(url);
                    DefaultHttpClient client = new DefaultHttpClient();
                    response = client.execute(httpGet);
                    client.getConnectionManager().shutdown();
                    int status = response.getStatusLine().getStatusCode();
                    Log.i(TAG, "Response: " + status);
                } catch (Exception ex) {
                    Log.e(TAG, "Error :" + ex.getMessage());
                }
                return response;
            }
        }.execute(url);
    }

    public static void GetImage(String url, final BitmapCallback callback) {
        new AsyncTaskWithCallback<String, Void, Void>() {
            @Override
            protected Void doInBackground(String... params) {
                Bitmap bitmap = null;
                try {
                    Log.i(TAG, "Get Image: " + params[0]);
                    URL url = new URL(params[0]);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setDoInput(true);
                    connection.connect();
                    bitmap = BitmapFactory.decodeStream(connection.getInputStream());
                } catch (IOException e) {
                    Log.e(TAG, e.getMessage());
                }
                callback.onFinish(bitmap);
                return null;
            }
        }.execute(url);
    }
}