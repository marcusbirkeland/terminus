package com.birkeland.terminus;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.birkeland.terminus.Adapters.EventListAdapter;
import com.birkeland.terminus.DataClasses.WorkdayEvent;
import com.birkeland.terminus.R;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class ViewAllEventsActivity extends AppCompatActivity {

    private static final int VIEW_DATE = 1;
    List<WorkdayEvent> savedEvents;

    private void loadEvents(){
        // Laster lagrede events fra applagring
        try {
        SharedPreferences pref = getSharedPreferences("SHARED PREFERENCES", Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String json = pref.getString("EVENTLIST",null);
        Type type = new TypeToken<ArrayList<WorkdayEvent>>(){}.getType();

            savedEvents = gson.fromJson(json,type);
        } catch (Exception e){
            Log.e("Error","Failed to load events");
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences pref = this.getSharedPreferences("DARKMODE",MODE_PRIVATE);
        boolean isDarkMode = pref.getBoolean("isDarkMode",false);
        if(isDarkMode){
            setTheme(R.style.AppThemeDark);
        }else{
            setTheme(R.style.AppTheme);
        }
        loadEvents();
        setContentView(R.layout.activity_view_all_events);
        if(savedEvents == null)
            return;
        ListView listView = findViewById(R.id.listViewViewAllEvents);
        try {
            EventListAdapter listAdapter = new EventListAdapter(this, VIEW_DATE, savedEvents);
            listView.setAdapter(listAdapter);
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    startViewEvent(savedEvents.get(position));
                }
            });
        }catch (NullPointerException n){
            Log.e("View all events","No events to view");

        }
    }
    private void startViewEvent(WorkdayEvent event) {
        Bundle bundle = new Bundle();
        bundle.putSerializable("EVENT",event);
        Intent intent = new Intent(this, ViewEventActivity.class);
        intent.putExtra("EVENTBUNDLE",bundle);
        startActivityForResult(intent, MainActivity.DELETE_EVENT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        updateList();
    }

    private void updateList(){
        // Oppdaterer elementer i ListView
        try {
            loadEvents();
            ListView listView = findViewById(R.id.listViewViewAllEvents);
            EventListAdapter listAdapter = new EventListAdapter(this, VIEW_DATE, savedEvents);
            listView.setAdapter(listAdapter);
        }catch (NullPointerException n){
            Log.e("View all events","No events to view");
        }
    }
}
