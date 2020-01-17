package com.example.jobbkalender.ui.home;

import android.os.Bundle;
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
import com.example.jobbkalender.CreateJobActivity;
import com.example.jobbkalender.DataClasses.Job;
import com.example.jobbkalender.DataClasses.SalaryRule;
import com.example.jobbkalender.DataClasses.WorkdayEvent;
import com.example.jobbkalender.EventListAdapter;
import com.example.jobbkalender.MainActivity;
import com.example.jobbkalender.R;

import org.w3c.dom.Text;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class HomeFragment extends Fragment {

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

    private HomeViewModel homeViewModel;
    int day,selectedMonth,selectedYear;
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        LocalDate currentDate = java.time.LocalDate.now();
        day = currentDate.getDayOfMonth();
        selectedMonth = currentDate.getMonthValue();
        selectedYear= currentDate.getYear();
        super.onViewCreated(view, savedInstanceState);
        CalendarView calendarView = view.findViewById(R.id.calendarView);

        Button buttonAddEvent = view.findViewById(R.id.buttonAddEvent);
        buttonAddEvent.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), CreateEvent.class);
                intent.putExtra("DATE", dateToString(day,selectedMonth,selectedYear));
                startActivity(intent);
            }
        });
        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView view, int year, int month, int dayOfMonth) {
                day = dayOfMonth;
                selectedMonth = month+1;
                selectedYear = year;
            }
        });

        List<WorkdayEvent> events = new ArrayList<>();
        events.add(new WorkdayEvent("","16:00","20:00",15, new Job("Pizzabakeren",140,new ArrayList<SalaryRule>())));
        events.add(new WorkdayEvent("","12:00","20:00",30, new Job("Meny",160,new ArrayList<SalaryRule>())));
        events.add(new WorkdayEvent("","06:00","14:00",30, new Job("Cowboy Bobb Asker",69,new ArrayList<SalaryRule>())));

        ListView eventListView = view.findViewById(R.id.listViewEventList);
        EventListAdapter eventListAdapter = new EventListAdapter(getContext(),0,events);
        eventListView.setAdapter(eventListAdapter);

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


}