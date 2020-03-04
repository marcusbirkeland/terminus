package com.birkeland.terminus.ui.earnings;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.birkeland.terminus.DataClasses.Job;
import com.birkeland.terminus.DataClasses.WorkdayEvent;
import com.birkeland.terminus.PayCalculator;
import com.birkeland.terminus.R;
import com.birkeland.terminus.dialogFragments.EditTaxDialogFragment;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import static android.content.Context.MODE_PRIVATE;
import static com.birkeland.terminus.dialogFragments.EditTaxDialogFragment.TAX_SELECTED;

public class EarningsFragment extends Fragment {

    private EarningsViewModel earningsViewModel;
    private List<WorkdayEvent> workdayEvents = new ArrayList<>();
    private List<Job> jobs = new ArrayList<>();
    List<String> jobNames = new ArrayList<>();
    private Job selectedJob;
    private String currency;
    private int currentEarnings;
    private int monthOffset = 0;
    private float taxPercentage;
    private int spinnerPosition;
    private String selectedTable = "";
    private static final double feriepengFaktor= 0.12;

    private String loadCurrency(){
        SharedPreferences locale = getActivity().getSharedPreferences("LOCALE",MODE_PRIVATE);
        return locale.getString("CURRENCY",getString(R.string.currency));
    }
    PayCalculator payCalculator = new PayCalculator(workdayEvents,getContext());

    private int calculateYearlyEarningsNet(int totalEarnings){
        SharedPreferences pref = getActivity().getSharedPreferences("SHARED PREFERENCES",MODE_PRIVATE);
        boolean isTaxTable = pref.getBoolean("ISTAXTABLE",false);
        PayCalculator payCalculator = new PayCalculator(workdayEvents,getActivity());
        if(!isTaxTable){
            return payCalculator.getNetEarningsWithPercentage(totalEarnings,pref.getFloat("TAXPERCENTAGE",0));
        }else{
            return payCalculator.getYearlyNetEarningsWithTable(workdayEvents,pref.getString("TAXTABLE","7700"));
        }
    }

    private void calculateMonthlyEarnings(int position){
        try {
            selectedJob = getJobByName(jobNames.get(position));
            TextView textViewMonthlyEarningsGross = getView().findViewById(R.id.textViewMonthlyEarningsGross);
            TextView textViewMonthPeriod = getView().findViewById(R.id.textViewMonthPeriod);
            TextView textViewMonthlyEarningsNet = getView().findViewById(R.id.textViewMonthlyEarningsNet);
            int monthlyGrossPay = payCalculator.getPaycheckEarnings(workdayEvents, selectedJob, monthOffset);
            textViewMonthPeriod.setText(payCalculator.getStartDateStr() + " - " + payCalculator.getEndDateStr());
            double feriepeng = monthlyGrossPay*feriepengFaktor;
            textViewMonthlyEarningsGross.setText(monthlyGrossPay +  " " + currency);
            SharedPreferences pref = getActivity().getSharedPreferences("SHARED PREFERENCES", MODE_PRIVATE);
            boolean isTaxTable = pref.getBoolean("ISTAXTABLE", false);
            PayCalculator payCalculator = new PayCalculator(workdayEvents, getActivity());
            float monthlyNetPay;
            if (isTaxTable) {
                monthlyNetPay = payCalculator.getMonthlyNetEarningsWithTable((int)monthlyGrossPay, pref.getString("TAXTABLE", "7700"));
            } else {
                monthlyNetPay = payCalculator.getNetEarningsWithPercentage((int)monthlyGrossPay, pref.getFloat("TAXPERCENTAGE", 0));
            }
            textViewMonthlyEarningsNet.setText((int)monthlyNetPay + " " + currency);
        }catch (IndexOutOfBoundsException i){
            Log.d("OUT OF BOUNDS",i + "");
        }
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
        final TextView textViewTotalEarningsGross = getView().findViewById(R.id.textViewGrossCurrentEarnings);
        final TextView textViewTotalEarningsNet = getView().findViewById(R.id.textViewNetCurrentEarnings);
        final TextView textViewTotalHours = getView().findViewById(R.id.textViewTotalHours);
        loadEvents();
        loadJobs();
        if(jobs != null) {
            for (Job job : jobs) {
                jobNames.add(job.getName());
            }
        }
        currency = loadCurrency();
        setTaxText();
        calculateMonthlyEarnings(spinnerPosition);
        textViewTotalHours.setText("" + payCalculator.totalHours);
        currentEarnings = payCalculator.getYearlyEarnings(workdayEvents);
        textViewTotalEarningsGross.setText("" + currentEarnings + " " + currency);
        // Setter feriepenger
        //double feriepeng = currentEarnings*feriepengFaktor;
        float netEarnings = calculateYearlyEarningsNet(currentEarnings);
        textViewTotalEarningsNet.setText("" + (int) netEarnings + " " + currency);
        final Spinner jobSpinner = getView().findViewById(R.id.spinnerSelectJob);
        // Gjør spinner scrollable
        try {
            Field popup = Spinner.class.getDeclaredField("mPopup");
            popup.setAccessible(true);
            // Get private mPopup member variable and try cast to ListPopupWindow
            android.widget.ListPopupWindow popupWindow = (android.widget.ListPopupWindow) popup.get(jobSpinner);
            // Set maxwidth for spinner window
            if(jobs  != null) {
                popupWindow.setHeight(jobs.size() * 200);
                if (jobs.size() > 3) {
                    popupWindow.setHeight(420);
                }
            }
        }
        catch (NoClassDefFoundError | ClassCastException | NoSuchFieldException | IllegalAccessException e) {
        }
        ArrayAdapter<String> spinnerAdapter= new ArrayAdapter<>(getActivity(),R.layout.spinner_item,jobNames);
        jobSpinner.setAdapter(spinnerAdapter);
        jobSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                spinnerPosition = position;
                monthOffset = 0;
                calculateMonthlyEarnings(position);
                textViewTotalHours.setText("" + payCalculator.totalHours);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedJob = null;
            }
        });
        // Endre skatteprosent eller tabell
        Button buttonEditTax = getView().findViewById(R.id.buttonEditTaxSettings);
        buttonEditTax.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTaxDialog();
            }
        });
        // Endre dato for lønnslipp
        Button buttonPrevMonth = getView().findViewById(R.id.buttonPrevMonthEarnings);
        Button buttonNextMonth = getView().findViewById(R.id.buttonNextMonthEarnings);
        buttonPrevMonth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                monthOffset--;
                calculateMonthlyEarnings(spinnerPosition);
                textViewTotalHours.setText("" + payCalculator.totalHours);
            }
        });
        buttonNextMonth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                monthOffset++;
                calculateMonthlyEarnings(spinnerPosition);
                textViewTotalHours.setText("" + payCalculator.totalHours);
            }
        });

    }

    private void showTaxDialog(){
        EditTaxDialogFragment editTaxDialogFragment = new EditTaxDialogFragment();
        editTaxDialogFragment.setTargetFragment(this,0);
        editTaxDialogFragment.show(this.getFragmentManager(),"Edit Tax dialog");
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode==TAX_SELECTED){
            setTaxText();
            calculateMonthlyEarnings(spinnerPosition);
            currentEarnings = payCalculator.getYearlyEarnings(workdayEvents);
            TextView textViewTotalEarningsNet = getView().findViewById(R.id.textViewNetCurrentEarnings);
            float netEarnings = calculateYearlyEarningsNet(currentEarnings);
            textViewTotalEarningsNet.setText("" + (int) netEarnings + " " + currency);
        }
    }

    private void setTaxText(){
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("SHARED PREFERENCES",MODE_PRIVATE);
        boolean isTaxTable = sharedPreferences.getBoolean("ISTAXTABLE",false);
        TextView textViewTaxPercentage = getView().findViewById(R.id.textViewTaxPercentage);
        if(isTaxTable){
            String table = sharedPreferences.getString("TAXTABLE" ,"");
            textViewTaxPercentage.setText(getString(R.string.table) + " " + table);
        }else{
            float percentage = sharedPreferences.getFloat("TAXPERCENTAGE", 0);
            textViewTaxPercentage.setText(percentage +"%");
        }
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