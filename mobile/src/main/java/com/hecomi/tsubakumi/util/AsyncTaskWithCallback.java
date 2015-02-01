package com.hecomi.tsubakumi.util;

import android.os.AsyncTask;

public abstract class AsyncTaskWithCallback<T1, T2, T3> extends AsyncTask<T1, T2, T3> {
    public interface Callback<T3> {
        public void onFinish(T3 result);
    }
    private Callback<T3> callback = null;

    public AsyncTaskWithCallback() {
    }

    public AsyncTaskWithCallback(Callback<T3> _callback) {
        callback = _callback;
    }

    @Override
    protected void onPostExecute(T3 result) {
        super.onPostExecute(result);
        if (callback != null) {
            callback.onFinish(result);
        }
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
        if (callback != null) {
            callback.onFinish(null);
        }
    }
}
