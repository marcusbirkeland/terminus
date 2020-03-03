package com.birkeland.terminus;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.birkeland.terminus.DataClasses.WorkdayEvent;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.text.DateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ViewEventActivity extends AppCompatActivity {

    private List<WorkdayEvent> workdayEvents = new ArrayList<>();
    private int eventIndex = -1;
    private String currency;
    private String loadCurrency(){
        SharedPreferences locale = getSharedPreferences("LOCALE",MODE_PRIVATE);
        return locale.getString("CURRENCY",getString(R.string.currency));
    }
    private String formatDate(LocalDate date){
        Instant instant = date.atStartOfDay(ZoneId.systemDefault()).toInstant();
        Date date1 = new Date(instant.toEpochMilli());
        DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT);
        return df.format(date1);
    }

    private void loadEvents() {
        // Laster listen med lagrede jobber.
        SharedPreferences pref = getSharedPreferences("SHARED PREFERENCES", Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String json = pref.getString("EVENTLIST", null);
        Log.d("JSON", "Json read: " + json);
        Type type = new TypeToken<ArrayList<WorkdayEvent>>() {
        }.getType();
        try {
            workdayEvents = gson.fromJson(json, type);
        } catch (Exception e) {
            Log.e("Error", "Failed to load events");
        }
    }
    private void saveEvents(){
        SharedPreferences pref = getSharedPreferences("SHARED PREFERENCES", MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        Gson gson = new Gson();
        Type type = new TypeToken<ArrayList<WorkdayEvent>>(){}.getType();
        String eventInfo = gson.toJson(workdayEvents,type);
        editor.putString("EVENTLIST", eventInfo);
        Log.d("Saving to sharedprefs: ", eventInfo);
        editor.apply();
    }

    private int getEventIndex(WorkdayEvent event){
        for (int i = 0; i<workdayEvents.size();i++){
            WorkdayEvent e = workdayEvents.get(i);
            if(e.getDate().equals(event.getDate()) &&
                    e.getJob().getName().equals(event.getJob().getName()) &&
                    e.getStartTime().equals(event.getStartTime()) &&
                    e.getEndTime().equals(event.getEndTime())
            ){
                return i;
            }
        }
        return -1;
    }
    private int getEqualEventIndex(WorkdayEvent event) {
        for (int i = 0; i<workdayEvents.size();i++){
            WorkdayEvent e = workdayEvents.get(i);
            if(e.getDayOfWeek().equals(event.getDayOfWeek()) &&
                    e.getJob().getName().equals(event.getJob().getName()) &&
                    e.getStartTime().equals(event.getStartTime()) &&
                    e.getEndTime().equals(event.getEndTime())
            ){
                return i;
            }
        }
        return -1;
    }

    private int getEventPay(){
        final List<WorkdayEvent> eventToCalculate = new ArrayList<>();
        eventToCalculate.add(workdayEvents.get(eventIndex));
        PayCalculator payCalculator = new PayCalculator(eventToCalculate, this);
        return  payCalculator.getEarnings(eventToCalculate);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == CreateJobActivity.DELETE_JOB)
            finish();
        loadEvents();
        final WorkdayEvent event = workdayEvents.get(eventIndex);
        final TextView textViewJobName = findViewById(R.id.textViewViewEventJobName);
        final TextView textViewSalary = findViewById(R.id.textViewViewEventSalary);
        final ImageView imageView = findViewById(R.id.imageViewViewEvent);

        textViewJobName.setText(event.getJob().getName());
        // Regn ut total lønn for arbeidsdag
        textViewSalary.setText(getEventPay()+ currency);
        // Finner bilde for imageView
        try {
            Uri uri = Uri.parse(event.getJob().getImage());
            imageView.setImageURI(uri);
        } catch (Exception e){
            Log.e("No image", "No image file to open");
        }

        // Laster inn det nye "Job" objektet
        LinearLayout linearLayoutJobView = findViewById(R.id.linearLayoutJobViewCreateEvent);
        linearLayoutJobView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadEvents();
                Intent intent = new Intent(getApplicationContext(),CreateJobActivity.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable("JOB",event.getJob());
                bundle.putBoolean("EDITMODE",true);
                intent.putExtra("BUNDLE",bundle);
                startActivityForResult(intent, 1);
            }
        });
    }

    @SuppressLint("SetTextI18n")
    @Override
        protected void onCreate (Bundle savedInstanceState){
            super.onCreate(savedInstanceState);
            currency = loadCurrency();
            SharedPreferences pref = this.getSharedPreferences("DARKMODE",MODE_PRIVATE);
            boolean isDarkMode = pref.getBoolean("isDarkMode",false);
            if(isDarkMode){
                setTheme(R.style.AppThemeDark);
            }else{
                setTheme(R.style.AppTheme);
            }
            setContentView(R.layout.activity_view_event);
            loadEvents();
            Intent intent = getIntent();
            Bundle bundle = intent.getBundleExtra("EVENTBUNDLE");
            final WorkdayEvent eventIn = (WorkdayEvent) bundle.getSerializable("EVENT");
            eventIndex = getEventIndex(eventIn);
            final WorkdayEvent event = workdayEvents.get(eventIndex);

            final TextView textViewDate = findViewById(R.id.textViewViewEventDate);
            final TextView textViewTimeFrom = findViewById(R.id.timeInputFromViewEvent);
            final TextView textViewTimeTo = findViewById(R.id.timeInputToViewEvent);
            final TextView textViewJobName = findViewById(R.id.textViewViewEventJobName);
            final TextView textViewSalary = findViewById(R.id.textViewViewEventSalary);
            final TextView textViewBreak = findViewById(R.id.textViewViewEventBreak);
            final ImageView imageView = findViewById(R.id.imageViewViewEvent);
            final TextView textViewOvertimeLabel = findViewById(R.id.textViewOvertimeLabel);
            final TextView textViewOvertimePercentage = findViewById(R.id.textViewViewEventOvertimePercentage);
            final TextView textViewOvertimeMessage = findViewById(R.id.textViewOvertimeMessage);

            if(!event.isOvertime()){
               textViewOvertimeLabel.setVisibility(View.GONE);
               textViewOvertimePercentage.setVisibility(View.GONE);
               textViewOvertimeMessage.setVisibility(View.GONE);
            }else{
                textViewOvertimePercentage.setText(event.getOvertimePercentage() + " %");
            }

            LocalDate eventDate = LocalDate.parse(event.getDate(), DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            if (event.isNightShift()){
                textViewDate.setText(formatDate(eventDate) + " - " + formatDate(eventDate.plusDays(1)));
            }else{
                textViewDate.setText(formatDate(eventDate));
            }
            textViewTimeFrom.setText(event.getStartTime());
            textViewTimeTo.setText(event.getEndTime());
            textViewJobName.setText(event.getJob().getName());
            textViewBreak.setText(event.getBreakTime() + " min");
            // Regn ut total lønn for arbeidsdag

            textViewSalary.setText(getEventPay() + " " + currency);
            try {
                Uri uri = Uri.parse(event.getJob().getImage());
                imageView.setImageURI(uri);
            } catch (Exception e){
                Log.e("No image", "No image file to open");
            }

            final Button deleteEvent = findViewById(R.id.buttonViewEventDelete);
            deleteEvent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Dialog deleteDialog = makeDeleteEventDialog(event);
                    deleteDialog.show();
                }
            });
            final Button editEvent = findViewById(R.id.buttonViewEventEdit);
            editEvent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });
            LinearLayout linearLayoutJobView = findViewById(R.id.linearLayoutJobViewCreateEvent);
            linearLayoutJobView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    loadEvents();
                    Intent intent = new Intent(getApplicationContext(),CreateJobActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("JOB",event.getJob());
                    bundle.putBoolean("EDITMODE",true);
                    intent.putExtra("BUNDLE",bundle);
                    startActivityForResult(intent, 1);
                }
            });

        }
        private void deleteEvent(WorkdayEvent event){
            loadEvents();
            int index = getEventIndex(event);
            if(index > -1) {
                workdayEvents.remove(index);
            }
            saveEvents();
            setResult(RESULT_OK);
            finish();
        }

        private void deleteEqualEvents (WorkdayEvent event){
            loadEvents();
            int index = getEqualEventIndex(event);
            while (index != -1){
                workdayEvents.remove(index);
                index = getEqualEventIndex(event);
            }
            saveEvents();
            setResult(RESULT_OK);
            finish();
        }

        private Dialog makeDeleteEventDialog (final WorkdayEvent event){
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(getString(R.string.delete_shift) + "?")
                    .setNegativeButton(getString(R.string.delete_shift), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            deleteEvent(event);
                        }
                    })
                    .setPositiveButton(getString(R.string.delete_equal_shifts), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteEqualEvents(event);
                    }
                    });
            return builder.create();
        }
    }
