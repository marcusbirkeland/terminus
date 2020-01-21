package com.example.jobbkalender.ui.earnings;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.example.jobbkalender.DataClasses.Job;
import com.example.jobbkalender.DataClasses.WorkdayEvent;
import com.example.jobbkalender.PayCalculator;
import com.example.jobbkalender.R;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import static android.content.Context.MODE_PRIVATE;

public class EarningsFragment extends Fragment {

    private EarningsViewModel earningsViewModel;
    private List<WorkdayEvent> workdayEvents = new ArrayList<>();
    private List<Job> jobs = new ArrayList<>();
    private Job selectedJob;

    PayCalculator payCalculator = new PayCalculator(workdayEvents);
    private void loadJobs(){
        // Laster listen med lagrede jobber.
        SharedPreferences pref = getActivity().getSharedPreferences("SHARED PREFERENCES", MODE_PRIVATE);
        Gson gson = new Gson();
        String json = pref.getString("JOBLIST",null);
        Log.d("JSON","Json read: " + json);
        Type type = new TypeToken<ArrayList<Job>>(){}.getType();
        try {
            jobs= gson.fromJson(json,type);
        } catch (Exception e){
            Log.e("Error","Failed to load jobs");
        }
    }
    private Job getJobByName(String name){
        // Finn Job klasse etter navn i en liste. Brukes for å søke i shared preferences.
        for (Job job: jobs
        ) {
            if(job.getName().equals(name)){
                return job;
            }
        }
        return null;
    }

    private void loadEvents(){
        // Laster listen med lagrede events
        SharedPreferences pref = getActivity().getSharedPreferences("SHARED PREFERENCES", MODE_PRIVATE);
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


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        loadEvents();
        loadJobs();
        TextView textViewTotalEarningsGross = getView().findViewById(R.id.textViewGrossCurrentEarnings);
        textViewTotalEarningsGross.setText("" + payCalculator.getEarnings(workdayEvents));
        Spinner jobSpinner = getView().findViewById(R.id.spinnerSelectJob);
        // Gjør spinner scrollable
        try {
            Field popup = Spinner.class.getDeclaredField("mPopup");
            popup.setAccessible(true);
            // Get private mPopup member variable and try cast to ListPopupWindow
            android.widget.ListPopupWindow popupWindow = (android.widget.ListPopupWindow) popup.get(jobSpinner);
            // Set maxwidth for spinner window
            if(jobs  != null) {
                popupWindow.setHeight(jobs.size() * 130);
                if (jobs.size() > 390) {
                    popupWindow.setHeight(390);
                }
            }
        }
        catch (NoClassDefFoundError | ClassCastException | NoSuchFieldException | IllegalAccessException e) {
        }

        final List<String> jobNames = new ArrayList<>();
        if(jobs != null) {
            for (Job job : jobs) {
                jobNames.add(job.getName());
            }
        }
        ArrayAdapter<String> spinnerAdapter= new ArrayAdapter<>(getActivity(),R.layout.spinner_item,jobNames);
        jobSpinner.setAdapter(spinnerAdapter);
        jobSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedJob = getJobByName(jobNames.get(position));
                TextView textViewMonthlyEarningsGross = getView().findViewById(R.id.textViewMonthlyEarningsGross);
                TextView textViewMonthPeriod = getView().findViewById(R.id.textViewMonthPeriod);
                TextView textViewMonthlyEarningsNet = getView().findViewById(R.id.textViewMonthlyEarningsNet);
                int monthlyGrossPay = payCalculator.getMonthlyEarnings(workdayEvents,selectedJob);
                textViewMonthPeriod.setText("Fra " + payCalculator.getStartDateStr() + " til " + payCalculator.getEndDateStr());
                textViewMonthlyEarningsGross.setText("" + monthlyGrossPay);
                Log.d("Selected job",selectedJob.toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedJob = null;
            }
        });

    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        earningsViewModel =
                ViewModelProviders.of(this).get(EarningsViewModel.class);
        View root = inflater.inflate(R.layout.fragment_dashboard, container, false);
        final TextView textView = root.findViewById(R.id.text_dashboard);
        earningsViewModel.getText().observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });
        return root;
    }
}