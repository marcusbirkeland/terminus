package com.birkeland.terminus.broadcastReceivers;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.birkeland.terminus.NotificationService;

import java.util.List;

public class AlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("ALARM RECEIVER", "Alarm received");
        Intent i = new Intent(context, NotificationService.class);
            try {
                Log.d("AlarmReceiver", "Start service background");
                context.startService(i);
            }catch (IllegalStateException illegalState){
                // Starter service i forgrunnen dersom appen ikke kjører
                Log.d("AlarmReceiver","Start service foreground");
                context.startForegroundService(i);
            }


    }
    private boolean appIsRunning(Context appContext){
        ActivityManager activityManager = (ActivityManager) appContext.getSystemService(Context.ACTIVITY_SERVICE);
        final List<ActivityManager.RunningAppProcessInfo> processInfos = activityManager.getRunningAppProcesses();
        if(processInfos!= null){
            for(final  ActivityManager.RunningAppProcessInfo processInfo : processInfos){
                if(processInfo.processName.equals("terminus_main")){
                    return true;
                }
            }
        }
        return false;
    }
}
