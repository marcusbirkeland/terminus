package com.example.jobbkalender;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

public class ServiceRestarter extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(new Intent(context, NotificationService.class));
        } else {
            context.startService(new Intent(context, NotificationService.class));
        }
    }
}
