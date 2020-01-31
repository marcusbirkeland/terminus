package com.example.jobbkalender;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.jobbkalender.DataClasses.Job;
import com.example.jobbkalender.DataClasses.WorkdayEvent;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class NotificationService extends Service {
    public static final String NOTIFICATION_CHANNEL_ID = "10001" ;
    private static final String notification_channel_name = "Varsler for arbeidsdag";
    Timer timer ;
    TimerTask timerTask ;
    String TAG = "Timers" ;
    int timerInterval = 1800;
    List<WorkdayEvent> workdayEvents = new ArrayList<>();

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
    public IBinder onBind (Intent arg0) {
        return null;
    }
    @Override
    public int onStartCommand (Intent intent , int flags , int startId) {
        Log. e ( TAG , "onStartCommand" ) ;
        super .onStartCommand(intent , flags , startId) ;
        startTimer() ;
        return START_STICKY ;
    }
    @Override
    public void onCreate () {
        Log. e ( TAG , "NotificationService created" ) ;
        createNotificationChannel();
    }
    @Override
    public void onDestroy () {
        Log. e ( TAG , "NotificationService destroyed");
        super .onDestroy() ;
        stopTimerTask() ;
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction("restartservice");
        broadcastIntent.setClass(this, ServiceRestarter.class);
        this.sendBroadcast(broadcastIntent);
    }
    final Handler handler = new Handler() ;
    public void startTimer () {
        timer = new Timer() ;
        initializeTimerTask() ;
        timer .schedule( timerTask , 5000 , timerInterval * 1000 ) ; //
    }
    public void stopTimerTask () {
        if ( timer != null ) {
            timer.cancel() ;
            timer = null;
        }
    }
    public void initializeTimerTask () {
        timerTask = new TimerTask() {
            public void run () {
                handler .post( new Runnable() {
                    public void run () {
                        WorkdayEvent event = getTodaysEvent();
                        try{
                            Log.d("Notification Service","Making notification");
                            createNotification(event) ;
                            timerInterval = 1800;
                    }catch (NullPointerException e){
                            Log.e("NULL", "No events today");
                        }
                    }
                }) ;
            }
        } ;
    }

    private void createNotification(WorkdayEvent event){
        Bitmap icon = BitmapFactory.decodeFile(event.getJob().getImage());
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.coin_icon).setLargeIcon(icon)
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
