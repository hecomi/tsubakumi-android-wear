package com.hecomi.tsubakumi;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import com.hecomi.tsubakumi.util.HttpRequest;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.concurrent.atomic.AtomicInteger;

public class Tsubakumi {
    private Context context;
    private static final String TAG = Tsubakumi.class.getSimpleName();
    public static final String EXTRA_VOICE_REPLY = "extra_voice_reply";
    AtomicInteger msgId = new AtomicInteger();

    Tsubakumi(Context parent) {
        context = parent;
    }

    public void OnServerMessageReceived(Bundle message) {
        sendNotification(message);
    }

    private void sendNotification(final Bundle message) {
        try {
            final JSONObject json = new JSONObject(message.getString("json"));
            final NotificationManagerCompat manager = NotificationManagerCompat.from(context);
            if (json.has("largeIcon")) {
                HttpRequest.GetImage(json.getString("largeIcon"), new HttpRequest.BitmapCallback() {
                    @Override
                    public void onFinish(Bitmap bitmap) {
                        manager.notify(msgId.incrementAndGet(), CreateNotification(json, bitmap));
                    }
                });
            } else {
                manager.notify(msgId.incrementAndGet(), CreateNotification(json));
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }

    Notification CreateNotification(JSONObject json, Bitmap largeIcon) {
        android.support.v4.app.NotificationCompat.Builder builder =
                new android.support.v4.app.NotificationCompat.Builder(context)
                .setDefaults(Notification.DEFAULT_ALL)
                .setSmallIcon(R.drawable.ic_launcher);

        NotificationCompat.WearableExtender extender = new NotificationCompat.WearableExtender();
        extender.setHintHideIcon(true);

        try {
            if (largeIcon != null) {
                builder.setLargeIcon(largeIcon);
            }
            if (json.has("title")) {
                builder.setContentTitle(json.getString("title"));
            }
            if (json.has("summary")) {
                builder.setContentText(json.getString("summary"));
            }
            if (json.has("message")) {
                android.support.v4.app.NotificationCompat.BigTextStyle bigStyle =
                        new android.support.v4.app.NotificationCompat.BigTextStyle();
                bigStyle.bigText(json.getString("message"));
                builder.setStyle(bigStyle);
            }
            if (json.has("actions")) {
                JSONArray actions = json.getJSONArray("actions");
                for (int i = 0; i < actions.length(); ++i) {
                    JSONObject actionJson = actions.getJSONObject(i);
                    String title = actionJson.getString("title");
                    String command = actionJson.getString("command");
                    String value = actionJson.getString("value");
                    String icon = actionJson.getString("icon");
                    Log.i(TAG, title + " " + command + " " + value + " " + icon);
                    switch (command) {
                        case "get":
                            int iconId = Resources.getSystem().getIdentifier(icon , "drawable", "android");
                            NotificationCompat.Action getAction =
                                    new NotificationCompat.Action.Builder(iconId, title, CreateHttpGetIntent(value))
                                            .build();
                            extender.addAction(getAction);
                            break;
                        case "voice":
                            AddVoiceRecognitionExtender(actionJson, extender);
                            break;
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }

        builder.extend(extender);
        return builder.build();
    }

    Notification CreateNotification(JSONObject message) {
        return CreateNotification(message, null);
    }

    PendingIntent CreateHttpGetIntent(String url) {
        Intent intent = new Intent(context, BackgroundHttpRequestIntentService.class);
        intent.putExtra("url", url);
        return PendingIntent.getService(context, 0, intent, 0);
    }

    void AddVoiceRecognitionExtender(JSONObject json, NotificationCompat.WearableExtender extender) {
        try {
            String title = json.getString("title");
            String command = json.getString("command");
            String icon = json.getString("icon");
            int iconId = Resources.getSystem().getIdentifier(icon , "drawable", "android");

            JSONObject value = json.getJSONObject("value");
            android.support.v4.app.RemoteInput.Builder builder =
                    new android.support.v4.app.RemoteInput.Builder(EXTRA_VOICE_REPLY);
            if (value.has("label")) {
                builder.setLabel(value.getString("label"));
            }

            if (value.has("choices")) {
                JSONArray choices = value.getJSONArray("choices");
                String choicesStrArr[] = new String[choices.length()];
                for (int i = 0; i < choices.length(); ++i) {
                    choicesStrArr[i] = choices.getString(i);
                }
                builder.setChoices(choicesStrArr);
            }

            Intent replyIntent = new Intent(context, VoiceInputReceiveIntentService.class);
            replyIntent.putExtra("urlTemplate", value.getString("url"));
            PendingIntent replyPendingIntent = PendingIntent.getService(context, 0, replyIntent, 0);

            NotificationCompat.Action action =
                    new NotificationCompat.Action.Builder(iconId, title, replyPendingIntent)
                            .addRemoteInput(builder.build())
                            .build();

            extender.addAction(action);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }
}
