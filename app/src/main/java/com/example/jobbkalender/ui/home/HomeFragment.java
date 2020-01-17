package com.example.jobbkalender.ui.home;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.content.Intent;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CalendarView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.example.jobbkalender.CreateEvent;
import com.example.jobbkalender.DataClasses.Job;
import com.example.jobbkalender.DataClasses.WorkdayEvent;
import com.example.jobbkalender.EventListAdapter;
import com.example.jobbkalender.MainActivity;
import com.example.jobbkalender.R;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class HomeFragment extends Fragment {

    public static final int CREATE_EVENT = 2;
    List<WorkdayEvent> workdayEvents = new ArrayList<>();
    private int day,selectedMonth,selectedYear;

    private String dateToString(int day, int month, int year){
        String daystring = day + "";
        String monthstring = month + "";
        if(day < 10){
            daystring = "0"+day;
        }
        if (month < 10){
            monthstring = "0" + month;
        }
        return daystring+monthstring+year;
    }

    private void loadEvents(){
        // Laster listen med lagrede jobber.
        SharedPreferences pref = getActivity().getSharedPreferences("SHARED PREFERENCES", Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String json = pref.getString("EVENTLIST",null);
        Log.d("JSON","Json read: " + json);
        Type type = new TypeToken<ArrayList<WorkdayEvent>>(){}.getType();
        try {
            workdayEvents = gson.fromJson(json,type);
        } catch (Exception e){
            Log.e("Error","Failed to load events");
        }
    }

    private List<WorkdayEvent> searchEvents (String date){
        List<WorkdayEvent> matchingEvents = new ArrayList<>();
        String selectedDate = dateToString(day,selectedMonth,selectedYear);
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ISO_LOCAL_TIME;
        LocalDate eventDate = LocalDate.parse(selectedDate,dateTimeFormatter.ofPattern("ddMMyyyy"));
        selectedDate = eventDate.toString();
        Log.d("SELECTED DATE", selectedDate);
        try {
            for (WorkdayEvent event : workdayEvents) {
                if (event.getDate().equals(selectedDate)) {
                    matchingEvents.add(event);
                   Log.d("Add event to list;",event.toString());
                }
            }
        }
        catch (NullPointerException e){
            Log.d("Null", "No workday events in list");
        }
        return  matchingEvents;
    }

    private HomeViewModel homeViewModel;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        loadEvents();
        final ListView eventListView = view.findViewById(R.id.listViewEventList);
        LocalDate currentDate = java.time.LocalDate.now();
        day = currentDate.getDayOfMonth();
        selectedMonth = currentDate.getMonthValue();
        selectedYear= currentDate.getYear();
        // Finn events på gjeldende dato og oppdater listview
        String date = dateToString(day,selectedMonth,selectedYear);
        List<WorkdayEvent> events = searchEvents(date);
        EventListAdapter eventListAdapter = new EventListAdapter(getContext(),0,events);
        eventListView.setAdapter(eventListAdapter);
        // Legg til ny event
        Button buttonAddEvent = view.findViewById(R.id.buttonAddEvent);
        buttonAddEvent.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), CreateEvent.class);
                intent.putExtra("DATE", dateToString(day,selectedMonth,selectedYear));
                startActivityForResult(intent,CREATE_EVENT);
            }
        });
        // Endre dato og oppdater liste med events
        CalendarView calendarView = view.findViewById(R.id.calendarView);
        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView view, int year, int month, int dayOfMonth) {
                loadEvents();
                day = dayOfMonth;
                selectedMonth = month+1;
                selectedYear = year;
                // Finn events på gjeldende dato og oppdater listview
                String date = dateToString(day,selectedMonth,selectedYear);
                List<WorkdayEvent> events = searchEvents(date);
                EventListAdapter eventListAdapter = new EventListAdapter(getContext(),0,events);
                eventListView.setAdapter(eventListAdapter);
            }
        });
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel =
                ViewModelProviders.of(this).get(HomeViewModel.class);
        View root = inflater.inflate(R.layout.fragment_home, container, false);
        final TextView textView = root.findViewById(R.id.text_home);
        final ListView listView = root.findViewById(R.id.listViewEventList);
        homeViewModel.getText().observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });
        return root;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        final ListView eventListView = getView().findViewById(R.id.listViewEventList);
        // Finn events på gjeldende dato og oppdater listview
        loadEvents();
        String date = dateToString(day,selectedMonth,selectedYear);
        List<WorkdayEvent> events = searchEvents(date);
        EventListAdapter eventListAdapter = new EventListAdapter(getContext(),0,events);
        eventListView.setAdapter(eventListAdapter);
    }
}