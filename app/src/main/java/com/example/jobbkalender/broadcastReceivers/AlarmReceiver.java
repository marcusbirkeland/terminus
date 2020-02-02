package com.example.jobbkalender.broadcastReceivers;

import android.app.Notification;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.jobbkalender.NotificationService;

public class AlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("ALARM RECEIVER", "Alarm received");
        Intent i = new Intent(context, NotificationService.class);
        context.startForegroundService(i);
    }
}
