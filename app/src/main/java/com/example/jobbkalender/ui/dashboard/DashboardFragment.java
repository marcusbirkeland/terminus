package com.example.jobbkalender.ui.dashboard;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.example.jobbkalender.DataClasses.WorkdayEvent;
import com.example.jobbkalender.R;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.w3c.dom.Text;

import java.lang.reflect.Type;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.Temporal;
import java.util.ArrayList;
import java.util.List;

public class DashboardFragment extends Fragment {

    private DashboardViewModel dashboardViewModel;
    private List<WorkdayEvent> workdayEvents = new ArrayList<>();
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
    private double calculateTotalEarningsGross (){
        double sum=0;
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ISO_LOCAL_TIME;
        for(WorkdayEvent event : workdayEvents){
            LocalTime startTime = LocalTime.parse(event.getStartTime(),dateTimeFormatter.ofPattern("HH:mm"));
            LocalTime endTime = LocalTime.parse(event.getEndTime(),dateTimeFormatter.ofPattern("HH:mm"));
            int startTimeMinutes = startTime.getHour()*60 + startTime.getMinute();
            int endTimeMinutes = endTime.getHour()*60 + endTime.getMinute();
            // Ganger antall minutter med timelønna delt på 60
            sum += (endTimeMinutes-startTimeMinutes)* (event.getJob().getSalary()/60);
        }
        return sum;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        TextView textViewtotalEarningsGross = getView().findViewById(R.id.textViewGrossCurrentEarnings);
        TextView textViewtotalEarningsNet = getView().findViewById(R.id.textViewNetCurrentEarnings);
        TextView textViewtotalTaxes = getView().findViewById(R.id.textViewTotalTaxes);
        TextView textViewmonthlyEarnings = getView().findViewById(R.id.textViewMonthlySalary);
        TextView textViewmonthlyTaxes = getView().findViewById(R.id.textViewMonthlyTax);
        Button refresh = getView().findViewById(R.id.buttonRefreshEarnings);

        refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadEvents();
            }
        });

    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        dashboardViewModel =
                ViewModelProviders.of(this).get(DashboardViewModel.class);
        View root = inflater.inflate(R.layout.fragment_dashboard, container, false);
        final TextView textView = root.findViewById(R.id.text_dashboard);
        dashboardViewModel.getText().observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });
        return root;
    }
}