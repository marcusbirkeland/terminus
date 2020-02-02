package com.birkeland.terminus;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.SystemClock;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.birkeland.terminus.DataClasses.WorkdayEvent;
import com.birkeland.terminus.R;
import com.birkeland.terminus.broadcastReceivers.AlarmReceiver;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class NotificationService extends IntentService {
    public static final String NOTIFICATION_CHANNEL_ID = "10001" ;
    private static final String notification_channel_name = "Varsler for arbeidsdag";
    List<WorkdayEvent> workdayEvents = new ArrayList<>();

    public NotificationService() {
        super("notificationService");

    }

    private void loadEvents(){
        // Laster listen med lagrede jobber.
        SharedPreferences pref = getSharedPreferences("SHARED PREFERENCES", Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String json = pref.getString("EVENTLIST",null);
        Type type = new TypeToken<ArrayList<WorkdayEvent>>(){}.getType();
        try {
            workdayEvents = gson.fromJson(json,type);
        } catch (Exception e){
            Log.e("Error","Failed to load events");
        }
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        createNotificationChannel();
        createNotification(getTodaysEvent());
        Intent alarmIntent = new Intent(this, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, MainActivity.CREATE_ALARM,alarmIntent,0);
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        // Setter ny alarm som restarter denne tjenesten.
        long interval = 60*60*1000; // 1 time.
        alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + interval,pendingIntent);
        stopSelf();
    }

    private void createNotification(WorkdayEvent event){
        if (event == null)
            return;
        Bitmap icon;
        try{
            icon = BitmapFactory.decodeFile(event.getJob().getImage());
        } catch (NullPointerException e){
            icon = null;
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_action_name).setLargeIcon(icon)
                .setContentTitle(event.getJob().getName())
                .setContentText("Fra " + event.getStartTime() + " til " + event.getEndTime())
                .setSubText("I dag")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setCategory(NotificationCompat.CATEGORY_EVENT)
                .setOnlyAlertOnce(true);
        if(event.isNightShift()){
            builder.setContentText("Fra " + event.getStartTime() + " i kveld" + " til " + event.getEndTime() + " i morgen");
        }
        // notificationId is a unique int for each notification that you must define
        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.notify(Integer.parseInt(NOTIFICATION_CHANNEL_ID),builder.build());
    }
    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = notification_channel_name;
            String description = "Viser varsel for dagens vakt";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private WorkdayEvent getTodaysEvent(){
        loadEvents();
        LocalDate localDate = LocalDate.now();
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm");
        try {

            for (WorkdayEvent event : workdayEvents) {
                LocalTime localTimeEvent = LocalTime.parse(event.getStartTime(),dtf);
                // Henter kun arbeidsdag helt fram til starten av event
                if (event.getDate().equals(localDate.toString()) && LocalTime.now().isBefore(localTimeEvent)){
                    Log.d("Notification Service", "Getting event: " + event.getJob().getName());
                    return event;
                }
            }
        } catch (NullPointerException n){
            Log.e("NotificationService","No events in list.");
        }
        return null;
    }
}
