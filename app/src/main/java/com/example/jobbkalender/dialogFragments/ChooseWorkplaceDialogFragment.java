package com.example.jobbkalender.dialogFragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.RecyclerView;

import com.example.jobbkalender.CreateEvent;
import com.example.jobbkalender.CreateJobActivity;
import com.example.jobbkalender.DataClasses.Job;
import com.example.jobbkalender.DataClasses.SalaryRule;
import com.example.jobbkalender.MainActivity;
import com.example.jobbkalender.R;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;

public class ChooseWorkplaceDialogFragment extends DialogFragment {

    List<Job> jobList =new ArrayList<>();

    private void loadJobs(){
        SharedPreferences pref = getContext().getSharedPreferences("Shared pref", MODE_PRIVATE);
        Gson gson = new Gson();
        String json = pref.getString("JOBLIST",null);
        Log.d("JSON","Json read: " + json);
        Type type = new TypeToken<ArrayList<Job>>(){}.getType();
        try {
            jobList= gson.fromJson(json,type);
        } catch (Exception e){
            Log.e("Eroor","Failed to load jobs");
        }
        Log.d("List:", jobList.get(0).toString());
    }

    public ChooseWorkplaceDialogFragment () {
        // Need empty cosntructor
    }
    public interface OnInputListener{
        void sendWorkplace(Job job);
    }
    public ChooseWorkplaceDialogFragment.OnInputListener mOnInputListener;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState){
        loadJobs();
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setPositiveButton("Legg til jobb", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(getContext(),CreateJobActivity.class);
                startActivity(intent);
                dialog.cancel();
            }
        });
        builder.setNegativeButton("Lukk", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        final List<String> jobNames= new ArrayList<>();
        for (Job j: jobList
             ) {
            jobNames.add(j.getName());
        }
        String[] jobNamesArray = jobNames.toArray(new String[0]);
        builder.setTitle("Velg arbeidsplass")
                .setItems(jobNamesArray, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        final Job selectedItem = jobList.get(which);
                         mOnInputListener.sendWorkplace(selectedItem);
                    }
                });

        return builder.create();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try{
            mOnInputListener = (ChooseWorkplaceDialogFragment.OnInputListener) getActivity();
        }catch (ClassCastException e){
            Log.e("onAttach", "On attach exception!");
        }
    }
}
