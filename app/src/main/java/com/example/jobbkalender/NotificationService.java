package com.example.jobbkalender;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class NotificationService extends Service {
    public static final String NOTIFICATION_CHANNEL_ID = "10001" ;
    private final static String default_notification_channel_id = "default" ;
    Timer timer ;
    TimerTask timerTask ;
    String TAG = "Timers" ;
    int timerInterval = 30 ;
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
        Log. e ( TAG , "onCreate" ) ;
    }
    @Override
    public void onDestroy () {
        Log. e ( TAG , "onDestroy" ) ;
        stopTimerTask() ;
        super .onDestroy() ;
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction("restartservice");
        broadcastIntent.setClass(this, ServiceRestarter.class);
        this.sendBroadcast(broadcastIntent);
    }
    //we are going to use a handler to be able to run in our TimerTask
    final Handler handler = new Handler() ;
    public void startTimer () {
        timer = new Timer() ;
        initializeTimerTask() ;
        timer .schedule( timerTask , 5000 , timerInterval * 1000 ) ; //
    }
    public void stopTimerTask () {
        if ( timer != null ) {
            timer .cancel() ;
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
                        createNotification(event) ;
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
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "CalendarEvent")
                .setSmallIcon(R.drawable.coin_icon).setLargeIcon(icon)
                .setContentTitle(event.getJob().getName())
                .setContentText(event.getStartTime() + " til " + event.getEndTime())
                .setSubText("Lønn: " + event.getSalary())
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setCategory(NotificationCompat.CATEGORY_EVENT)
                .setOnlyAlertOnce(true);
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        // notificationId is a unique int for each notification that you must define
        notificationManager.notify(1123, builder.build());
    }
    private WorkdayEvent getTodaysEvent(){
        loadEvents();
        LocalDate localDate = LocalDate.now();
        for (WorkdayEvent event : workdayEvents){
            if(event.getDate().equals(localDate.toString())){
                return event;
            }
        }
        return null;
    }
}
