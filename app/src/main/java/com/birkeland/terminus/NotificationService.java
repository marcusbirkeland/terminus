package com.birkeland.terminus;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.SystemClock;
import android.util.DisplayMetrics;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.birkeland.terminus.DataClasses.WorkdayEvent;
import com.birkeland.terminus.broadcastReceivers.AlarmReceiver;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static com.birkeland.terminus.MainActivity.ENGLISH;
import static com.birkeland.terminus.MainActivity.NORWEGIAN;

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
        int language = loadLanguage();
        // Setter språk på notifikasjonen.
        // Dette skjer også automatisk, men har med if statement for å styre brukervalg over systemdefault.
        if(language == NORWEGIAN){
            setLanguage("nb");
        }else if (language == ENGLISH){
            setLanguage("en-rUS");
        }
        createNotificationChannel();
        createNotification(getTodaysEvent());
        Intent alarmIntent = new Intent(this, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, MainActivity.CREATE_ALARM,alarmIntent,0);
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        // Setter ny alarm som restarter denne tjenesten etter *interval* millisekunder.
        long interval = 2*60*60*1000; // 2 timer.
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

        // Intent for å starte appen når man trykker på notifikasjonen. kode for dette hentet fra google documentasjon
        Intent resultIntent = new Intent(this, MainActivity.class);
        // Create the TaskStackBuilder and add the intent, which inflates the back stack
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addNextIntentWithParentStack(resultIntent);
        // Get the PendingIntent containing the entire back stack
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        // Bygger notifikasjonen
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_action_name).setLargeIcon(icon)
                .setContentTitle(event.getJob().getName())
                .setContentText(getString(R.string.from) + " " + event.getStartTime() + " " + getString(R.string.to).toLowerCase()+" " + event.getEndTime())
                .setSubText(getString(R.string.today))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setCategory(NotificationCompat.CATEGORY_EVENT)
                .setOnlyAlertOnce(true)
                .setContentIntent(resultPendingIntent);
        if(event.isNightShift()){
            builder.setContentText(getString(R.string.from) + " " + event.getStartTime() + " " +
                    getString(R.string.tonight)+ " " + getString(R.string.to).toLowerCase() + " " + event.getEndTime() + " " + getString(R.string.tomorrow));
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

    private int loadLanguage(){
        SharedPreferences sharedPreferences = getSharedPreferences("LOCALE",MODE_PRIVATE);
        return sharedPreferences.getInt("LANGUAGE", 0);
    }
    private void setLanguage(String countryCode){
        Resources resources = getResources();
        DisplayMetrics displayMetrics = resources.getDisplayMetrics();
        Configuration configuration = resources.getConfiguration();
        configuration.setLocale(new Locale(countryCode.toLowerCase()));
        resources.updateConfiguration(configuration, displayMetrics);
        configuration.locale = new Locale(countryCode.toLowerCase());
        resources.updateConfiguration(configuration, displayMetrics);
    }
}
