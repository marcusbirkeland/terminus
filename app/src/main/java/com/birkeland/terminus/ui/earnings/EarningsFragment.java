package com.birkeland.terminus.ui.earnings;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.birkeland.terminus.DataClasses.Job;
import com.birkeland.terminus.DataClasses.WorkdayEvent;
import com.birkeland.terminus.PayCalculator;
import com.birkeland.terminus.R;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.io.InputStream;
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
    private String currency;
    private int currentEarnings;
    private float taxPercentage;
    private int spinnerPosition;

    private String loadCurrency(){
        SharedPreferences locale = getActivity().getSharedPreferences("LOCALE",MODE_PRIVATE);
        return locale.getString("CURRENCY",getString(R.string.currency));
    }
    PayCalculator payCalculator = new PayCalculator(workdayEvents,getContext());

    private void calculateMonthlyEarnings(int position){
        selectedJob = getJobByName(jobNames.get(position));
        TextView textViewMonthlyEarningsGross = getView().findViewById(R.id.textViewMonthlyEarningsGross);
        TextView textViewMonthPeriod = getView().findViewById(R.id.textViewMonthPeriod);
        TextView textViewMonthlyEarningsNet = getView().findViewById(R.id.textViewMonthlyEarningsNet);
        int monthlyGrossPay = payCalculator.getMonthlyEarnings(workdayEvents,selectedJob);
        textViewMonthPeriod.setText(getString(R.string.paycheck_period) + " " + payCalculator.getStartDateStr() + " - " + payCalculator.getEndDateStr());
        textViewMonthlyEarningsGross.setText(monthlyGrossPay + " " + currency);
        Log.d("Selected job",selectedJob.toString());
        float monthlyNetPay = monthlyGrossPay*(1-taxPercentage/100);
        textViewMonthlyEarningsNet.setText((int) monthlyNetPay + " " + currency);
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
        currency = loadCurrency();
        taxPercentage = loadTaxPercentage();
        TextView editTextTaxPercentage = getView().findViewById(R.id.textViewTaxPercentage);
        editTextTaxPercentage.setText(taxPercentage+ "");
        TextView textViewTotalEarningsGross = getView().findViewById(R.id.textViewGrossCurrentEarnings);
        currentEarnings = payCalculator.getYearlyEarnings(workdayEvents);
        textViewTotalEarningsGross.setText("" + currentEarnings + " " + currency);
        TextView textViewTotalEarningsNet = getView().findViewById(R.id.textViewNetCurrentEarnings);
        float netEarnings = currentEarnings*(1-(taxPercentage/100));
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
        Button buttonEditTax = getView().findViewById(R.id.buttonEditTaxSettings);
        buttonEditTax.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                makeEditTaxDialog().show();
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

    private Dialog makeEditTaxDialog(){
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        final View dialogView = View.inflate(getContext(),R.layout.select_tax_dialog_layout,null);
        final TextView textViewErrorMessage = dialogView.findViewById(R.id.textViewSelectTaxError);
        final TextView textViewSetTaxTable = dialogView.findViewById(R.id.textViewPickTaxTable);
        final RadioButton radioButtonPercentage = dialogView.findViewById(R.id.radioButtonTaxPercentage);
        final RadioButton radioButtonTable = dialogView.findViewById(R.id.radioButtonTaxTable);
        // Har ikke satt radio buttons i noen gruppe, derfor må vi programmere de her:
        radioButtonPercentage.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(radioButtonPercentage.isChecked())
                    radioButtonTable.setChecked(false);
            }
        });
        radioButtonTable.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(radioButtonTable.isChecked())
                    radioButtonPercentage.setChecked(false);
            }
        });

        textViewSetTaxTable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Åpne tabellpicker
                    makeTablePicker().show();
            }
        });

        builder.setMessage(getString(R.string.choose_tax_method))
                .setView(dialogView)
                .setPositiveButton(getString(R.string.save),new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        final EditText editTextTaxPercentage = dialogView.findViewById(R.id.editTextTaxPercentage);
                        if(radioButtonPercentage.isChecked()){
                            if(editTextTaxPercentage.getText().toString().equals("")){
                                textViewErrorMessage.setText(getString(R.string.error_eneter_tax_percentage));
                                return;
                            }
                            setTaxPercentage(editTextTaxPercentage.getText().toString());

                        }else if(radioButtonTable.isChecked()){
                            if(false){
                                textViewErrorMessage.setText(getString(R.string.error_eneter_tax_table));
                                return;
                            }
                        }else{
                            textViewErrorMessage.setText(getString(R.string.error_choose_a_tax_option));
                        }
                    }
                }).setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Avbryt
            }
        });
        return builder.create();
    }

    private String[] reverseArray(String[] arrIn){
        String[] reversedArray = arrIn.clone();
        for(int i = 0;i < reversedArray.length;i++){
            int j = reversedArray.length -(1+ i);
               reversedArray[i] = arrIn[j];
        }
        return reversedArray;
    }

    private Dialog makeTablePicker() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        AssetManager assetManager = getContext().getAssets();
        String[] tableIDs;
        try {
            tableIDs = assetManager.list("tabellene2020");
            int i = 0;
            for (String ID : tableIDs){
                tableIDs[i]= ID.substring(0,ID.length()-4);
                i++;
            }

        builder.setTitle(getString(R.string.choose_tax_table))
        .setItems(reverseArray(tableIDs), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        }
        catch (IOException e){
            Log.e("Table picker","No files in assets");
        }
        return builder.create();
    }

    private void setTaxPercentage(String taxPercentageText){
        if(taxPercentageText != null && !taxPercentageText.equals("")) {
            taxPercentage = Float.parseFloat(taxPercentageText);
            if (taxPercentage > 100 ){
                taxPercentage = 100;
            }
            // Lagrer skatteprosent i sharedprefs
            saveTaxPercentage(taxPercentage);
        }
    }
}