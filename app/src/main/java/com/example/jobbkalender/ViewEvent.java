package com.example.jobbkalender;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.jobbkalender.DataClasses.Job;
import com.example.jobbkalender.DataClasses.WorkdayEvent;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.w3c.dom.Text;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class ViewEvent extends AppCompatActivity {

    private List<WorkdayEvent> workdayEvents = new ArrayList<>();

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
                Log.d("WAHOOO","FOUND INDEX: " + i);
                return i;
            }
        }
        return -1;
    }

        @Override
        protected void onCreate (Bundle savedInstanceState){
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_view_event);
            Intent intent = getIntent();
            Bundle bundle = intent.getBundleExtra("EVENTBUNDLE");
            final WorkdayEvent event = (WorkdayEvent) bundle.getSerializable("EVENT");
            TextView textViewDate = findViewById(R.id.textViewViewEventDate);
            TextView textViewTimeFrom = findViewById(R.id.timeInputFromViewEvent);
            TextView textViewTimeTo = findViewById(R.id.timeInputToViewEvent);
            TextView textViewJobName = findViewById(R.id.textViewViewEventJobName);
            ImageView imageView = findViewById(R.id.imageViewViewEvent);

            textViewDate.setText(event.getDate());
            textViewTimeFrom.setText(event.getStartTime());
            textViewTimeTo.setText(event.getEndTime());
            textViewJobName.setText(event.getJob().getName());
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
                    loadEvents();
                    int index = getEventIndex(event);
                    if(index > -1) {
                        workdayEvents.remove(index);
                    }
                    saveEvents();
                    setResult(RESULT_OK);
                    finish();
                }
            });
        }
    }
