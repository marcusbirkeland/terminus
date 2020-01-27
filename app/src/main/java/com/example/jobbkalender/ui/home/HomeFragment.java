package com.example.jobbkalender.ui.home;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.EventLog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.content.Intent;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.example.jobbkalender.CreateEvent;
import com.example.jobbkalender.DataClasses.WorkdayEvent;
import com.example.jobbkalender.EventListAdapter;
import com.example.jobbkalender.R;
import com.example.jobbkalender.ViewEvent;
import com.github.sundeepk.compactcalendarview.CompactCalendarView;
import com.github.sundeepk.compactcalendarview.domain.Event;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.w3c.dom.Text;

import java.lang.reflect.Type;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import static com.example.jobbkalender.MainActivity.DELETE_EVENT;

public class HomeFragment extends Fragment{

    public static final int CREATE_EVENT = 2;
    List<WorkdayEvent> workdayEvents = new ArrayList<>();
    private int selectedDay,selectedMonth,selectedYear;
    private DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE;

    private String monthToString(int date){
        String currentDate = "";
        switch(date){
            case 1:
                currentDate = "Januar";
                break;
            case 2:
                currentDate = "Februar";
                break;
            case 3:
                currentDate = "Mars";
                break;
            case 4:
                currentDate = "April";
                break;
            case 5:
                currentDate = "Mai";
                break;
            case 6:
                currentDate = "Juni";
                break;
            case 7:
                currentDate = "Juli";
                break;
            case 8:
                currentDate = "August";
                break;
            case 9:
                currentDate = "Sepember";
                break;
            case 10:
                currentDate = "Oktober";
                break;

            case 11:
                currentDate = "November";
                break;

            case 12:
                currentDate = "Desember";
                break;
        }
        return currentDate;
    }

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
    private void loadCalendarEvents(){
        final CompactCalendarView compactCalendarView = getView().findViewById(R.id.compactCalendarView);
        compactCalendarView.removeAllEvents();
        loadEvents();
        for (WorkdayEvent event : workdayEvents){
            LocalDate eventDate = LocalDate.parse( event.getDate(), dateTimeFormatter.ofPattern("yyyy-MM-dd"));
            Instant instant = eventDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
            int accent = ContextCompat.getColor(getContext(), R.color.colorAccent);
            Event calendarEvent = new Event(accent, instant.toEpochMilli());
            compactCalendarView.addEvent(calendarEvent);
        }
    }
    private List<WorkdayEvent> searchEvents (String date){
        List<WorkdayEvent> matchingEvents = new ArrayList<>();
        String selectedDate = dateToString(selectedDay,selectedMonth,selectedYear);
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
        loadCalendarEvents();
        LocalDate currentDate = java.time.LocalDate.now();
        selectedDay = currentDate.getDayOfMonth();
        selectedMonth = currentDate.getMonthValue();
        selectedYear= currentDate.getYear();

        TextView textViewSelectedMonth = getView().findViewById(R.id.textViewCalendarSelectedMonth);
        textViewSelectedMonth.setText(monthToString(selectedMonth) + " " + selectedYear);

        // Finn events på gjeldende dato og oppdater listview
        String date = dateToString(selectedDay,selectedMonth,selectedYear);
        final List<WorkdayEvent> events = searchEvents(date);
        EventListAdapter eventListAdapter = new EventListAdapter(getContext(),0,events);
        final ListView eventListView = view.findViewById(R.id.listViewEventList);
        eventListView.setAdapter(eventListAdapter);
        eventListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String date = dateToString(selectedDay,selectedMonth,selectedYear);
                List<WorkdayEvent> events = searchEvents(date);
                startViewEvent(events.get(position));
            }
        });
        // Legg til ny event
        Button buttonAddEvent = view.findViewById(R.id.buttonAddEvent);
        buttonAddEvent.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), CreateEvent.class);
                intent.putExtra("DATE", dateToString(selectedDay,selectedMonth,selectedYear));
                startActivityForResult(intent,CREATE_EVENT);
            }
        });
        // Endre dato og oppdater liste med events
        CompactCalendarView calendarView = view.findViewById(R.id.compactCalendarView);
        calendarView.setUseThreeLetterAbbreviation(true);
        calendarView.setListener(new CompactCalendarView.CompactCalendarViewListener() {
            @Override
            public void onDayClick(Date dateClicked) {
                Calendar cal = Calendar.getInstance(TimeZone.getTimeZone(ZoneId.systemDefault()));
                cal.setTime(dateClicked);
                selectedDay = cal.get(Calendar.DAY_OF_MONTH);
                selectedMonth = cal.get(Calendar.MONTH) + 1;
                selectedYear = cal.get(Calendar.YEAR);
                Log.d("DAY CLICKED", "DAY: " + selectedDay + "Month: " + selectedMonth + " Year: " + selectedYear );
                loadEvents();
                // Finn events på gjeldende dato og oppdater listview
                String date = dateToString(selectedDay,selectedMonth,selectedYear);
                List<WorkdayEvent> events = searchEvents(date);
                EventListAdapter eventListAdapter = new EventListAdapter(getContext(),0,events);
                eventListView.setAdapter(eventListAdapter);
            }

            @Override
            public void onMonthScroll(Date firstDayOfNewMonth) {

                Calendar cal = Calendar.getInstance(TimeZone.getTimeZone(ZoneId.systemDefault()));
                cal.setTime(firstDayOfNewMonth);
                String currentDate = monthToString(cal.get(Calendar.MONTH)+1);
                currentDate += " " + cal.get(Calendar.YEAR);
                TextView textViewSelectedMonth = getView().findViewById(R.id.textViewCalendarSelectedMonth);
                textViewSelectedMonth.setText(currentDate);

                selectedDay = cal.get(Calendar.DAY_OF_MONTH);
                selectedMonth = cal.get(Calendar.MONTH) + 1;
                selectedYear = cal.get(Calendar.YEAR);
                Log.d("DAY CLICKED", "DAY: " + selectedDay + "Month: " + selectedMonth + " Year: " + selectedYear );
                loadEvents();
                // Finn events på gjeldende dato og oppdater listview
                String date = dateToString(selectedDay,selectedMonth,selectedYear);
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
        loadEvents();
        // Finn events på gjeldende dato og oppdater listview
        String date = dateToString(selectedDay,selectedMonth,selectedYear);
        List<WorkdayEvent> events = searchEvents(date);
        EventListAdapter eventListAdapter = new EventListAdapter(getContext(),0,events);
        eventListView.setAdapter(eventListAdapter);
        // Legg til event
        loadCalendarEvents();
    }



    private void startViewEvent(WorkdayEvent event) {
        Bundle bundle = new Bundle();
        bundle.putSerializable("EVENT",event);
        Intent intent = new Intent(getContext(), ViewEvent.class);
        intent.putExtra("EVENTBUNDLE",bundle);
        startActivityForResult(intent,DELETE_EVENT);
    }

    @Override
    public void onResume() {
        super.onResume();
        CompactCalendarView compactCalendarView = getView().findViewById(R.id.compactCalendarView);
        Date date = compactCalendarView.getFirstDayOfCurrentMonth();
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone(ZoneId.systemDefault()));
        cal.setTime(date);
        TextView textViewSelectedMonth = getView().findViewById(R.id.textViewCalendarSelectedMonth);
        textViewSelectedMonth.setText(monthToString(cal.get(Calendar.MONTH) +1) + " " + cal.get(Calendar.YEAR));
    }
}