package com.example.jobbkalender;

import android.app.Notification;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class AlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("ALARM RECEIVER", "Alarm received");
        Intent i = new Intent(context, NotificationService.class);
        context.startService(i);
    }
}
