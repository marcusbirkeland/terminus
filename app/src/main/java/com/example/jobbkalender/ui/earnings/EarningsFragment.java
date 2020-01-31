package com.example.jobbkalender.ui.earnings;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

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
    List<String> jobNames = new ArrayList<>();
    private Job selectedJob;
    private int currentEarnings;
    private float taxPercentage;
    private int spinnerPosition;

    PayCalculator payCalculator = new PayCalculator(workdayEvents);

    private void calculateMonthlyEarnings(int position){
        selectedJob = getJobByName(jobNames.get(position));
        TextView textViewMonthlyEarningsGross = getView().findViewById(R.id.textViewMonthlyEarningsGross);
        TextView textViewMonthPeriod = getView().findViewById(R.id.textViewMonthPeriod);
        TextView textViewMonthlyEarningsNet = getView().findViewById(R.id.textViewMonthlyEarningsNet);
        int monthlyGrossPay = payCalculator.getMonthlyEarnings(workdayEvents,selectedJob);
        textViewMonthPeriod.setText("Lønn i perioden  " + payCalculator.getStartDateStr() + " - " + payCalculator.getEndDateStr());
        textViewMonthlyEarningsGross.setText(monthlyGrossPay + " kr");
        Log.d("Selected job",selectedJob.toString());
        float monthlyNetPay = monthlyGrossPay*(1-taxPercentage/100);
        textViewMonthlyEarningsNet.setText((int) monthlyNetPay + " kr");
    }

    private void saveTaxPercentage (float percentage){
        SharedPreferences pref = getActivity().getSharedPreferences("SHARED PREFERENCES", MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putFloat("TAXPERCENTAGE", percentage);
        editor.apply();
    }
    private float loadTaxPercentage(){
        SharedPreferences pref = getActivity().getSharedPreferences("SHARED PREFERENCES",MODE_PRIVATE);
        float percentage;
        percentage = pref.getFloat("TAXPERCENTAGE", 0.0f);
        Log.d("LOAD TAX","TAX:" + percentage);
        return percentage;
    }
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
        taxPercentage = loadTaxPercentage();
        EditText editTextTaxPercentage = getView().findViewById(R.id.editTextTaxPercentage);
        editTextTaxPercentage.setText(taxPercentage+ "");
        TextView textViewTotalEarningsGross = getView().findViewById(R.id.textViewGrossCurrentEarnings);
        currentEarnings = payCalculator.getYearlyEarnings(workdayEvents);
        textViewTotalEarningsGross.setText("" + currentEarnings + " kr");
        TextView textViewTotalEarningsNet = getView().findViewById(R.id.textViewNetCurrentEarnings);
        float netEarnings = currentEarnings*(1-(taxPercentage/100));
        textViewTotalEarningsNet.setText("" + (int) netEarnings + " kr");
        final Spinner jobSpinner = getView().findViewById(R.id.spinnerSelectJob);
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
                spinnerPosition = position;
                calculateMonthlyEarnings(position);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedJob = null;
            }
        });
        Button buttonSaveTaxPercentage = getView().findViewById(R.id.buttonSaveTaxPercentage);
        buttonSaveTaxPercentage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText editTextTaxPercentage = getView().findViewById(R.id.editTextTaxPercentage);
                TextView textViewTotalEarningsNet = getView().findViewById(R.id.textViewNetCurrentEarnings);
                if(editTextTaxPercentage.getText() != null && !editTextTaxPercentage.getText().toString().equals("")) {
                    taxPercentage = Float.parseFloat(editTextTaxPercentage.getText().toString());
                    if (taxPercentage > 100 ){
                        taxPercentage = 100;
                        // Lagrer skatteprosent i sharedprefs
                        editTextTaxPercentage.setText("100");
                    }
                    saveTaxPercentage(taxPercentage);
                    Toast toast =  Toast.makeText(getActivity(), "Skatteprosent lagret!", Toast.LENGTH_LONG);
                    toast.show();
                    // Setter netto-årslønn
                    float netEarnings = currentEarnings*(1-(taxPercentage/100));
                    textViewTotalEarningsNet.setText("" + (int) netEarnings);
                    if(jobs != null)
                        calculateMonthlyEarnings(spinnerPosition);
                }
            }
        });
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        earningsViewModel =
                ViewModelProviders.of(this).get(EarningsViewModel.class);
        View root = inflater.inflate(R.layout.fragment_earnings, container, false);
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