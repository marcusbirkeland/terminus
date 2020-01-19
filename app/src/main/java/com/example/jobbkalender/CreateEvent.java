package com.example.jobbkalender;

import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.jobbkalender.DataClasses.Job;
import com.example.jobbkalender.DataClasses.WorkdayEvent;
import com.example.jobbkalender.dialogFragments.ChooseWorkplaceDialogFragment;
import com.example.jobbkalender.dialogFragments.TimePickerDialogFragment;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.w3c.dom.Text;

import java.io.IOException;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class CreateEvent extends AppCompatActivity implements TimePickerDialogFragment.OnInputListener, ChooseWorkplaceDialogFragment.OnInputListener {

    int editTextToChange = 0;
    TimePickerDialogFragment timePickerDialogFragment = new TimePickerDialogFragment();
    ChooseWorkplaceDialogFragment chooseWorkplaceDialogFragment = new ChooseWorkplaceDialogFragment();

    List<WorkdayEvent> eventList = new ArrayList<>();
    List<Job> jobList = new ArrayList<>();
    String jobName = "";

    private void saveEvent(){
        SharedPreferences pref = getSharedPreferences("SHARED PREFERENCES", MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        Gson gson = new Gson();
        Type type = new TypeToken<ArrayList<WorkdayEvent>>(){}.getType();
        String currentList = pref.getString("EVENTLIST",null);
        String eventInfo = gson.toJson(eventList,type);
        if (currentList != null && !currentList.equals("[]")){
            editor.putString("EVENTLIST", currentList.substring(0,currentList.length()-1)+","+eventInfo.substring(1));
        }else{
            editor.putString("EVENTLIST",eventInfo);
        }
        Log.d("Saving to sharedprefs: ", eventInfo);
        editor.apply();
    }

    private void loadJobs(){
        // Laster listen med lagrede jobber.
        SharedPreferences pref = this.getSharedPreferences("SHARED PREFERENCES", MODE_PRIVATE);
        Gson gson = new Gson();
        String json = pref.getString("JOBLIST",null);
        Log.d("JSON","Json read: " + json);
        Type type = new TypeToken<ArrayList<Job>>(){}.getType();
        try {
            jobList= gson.fromJson(json,type);
        } catch (Exception e){
            Log.e("Error","Failed to load jobs");
        }
    }

    private Job getJobByName(String name){
        // Finn Job klasse etter navn i en liste. Brukes for å søke i shared preferences.
        for (Job job: jobList
             ) {
            if(job.getName().equals(name)){
                return job;
            }
        }
        return null;
    }

    @Override
    public void sendTime(String input) {
        // Denne metoden blir kalt på etter dialogvinduet til timepicker blir lukket.
        Log.d("Set time: ", input);
        if(editTextToChange == 1){
            TextView timeInput = findViewById(R.id.timeInputFromCreateEvent);
            timeInput.setText(input);
        } else if(editTextToChange == 2){
            TextView timeInput = findViewById(R.id.timeInputToCreateEvent);
            timeInput.setText(input);
        }
    }
    @Override
    public void sendWorkplace( Job workplace ){
        // Denne metoden blir kalt på etter dialogvinduet til jobpicker blir lukket.
        Log.d("Set workplace:", workplace.toString());
        TextView t = findViewById(R.id.textViewCreateEventJobName);
        ImageView imageView = findViewById(R.id.imageViewCreateEvent);
        jobName = workplace.getName();
        t.setText(jobName);
        try{
            Uri uri = Uri.parse(workplace.getImage());
            imageView.setImageURI(uri);
        }catch (Exception e){
            imageView.setImageDrawable(getDrawable(R.drawable.contacts));
            Log.e("Invalid URI","Invalid or no image Uri");
        }

    }

    void showTimePickerDialog() {

        if(timePickerDialogFragment.isAdded())
            return; // Prevent illegal state
        timePickerDialogFragment.show(getSupportFragmentManager(), "Pick time:");
        Log.d("Dialog: ", "Time picker opened");
    }
    void showChooseWorkplaceDialog(){

        if(chooseWorkplaceDialogFragment.isAdded())
            return; // Prevent illegal state
        chooseWorkplaceDialogFragment.show(getSupportFragmentManager(), "Choose workplace: ");
        Log.d("Dialog","Choose workplace opened");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_event);
        TextView timeInputFrom = findViewById(R.id.timeInputFromCreateEvent);
        TextView timeInputTo = findViewById(R.id.timeInputToCreateEvent);

        // Input for å sette start og slutt-klokkeslett for arbeidsdagen
        timeInputFrom.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                showTimePickerDialog();
                editTextToChange = 1;
            }
        });
        timeInputTo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTimePickerDialog();
                editTextToChange = 2;
            }
        });

        Button addWorkplace = findViewById(R.id.buttonChooseWorkplaceCreateEvent);
        addWorkplace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadJobs();
                showChooseWorkplaceDialog();
            }
        });
        Button submitWorkday = findViewById(R.id.buttonSubmitWorkday);
        submitWorkday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadJobs();
                EditText editTextBreakTime = findViewById(R.id.editTextBreakTime);
                TextView timeInputFrom = findViewById(R.id.timeInputFromCreateEvent);
                TextView timeInputTo = findViewById(R.id.timeInputToCreateEvent);

                if (editTextBreakTime.getText().toString().equals("") ||
                        jobName.equals("")){
                    Log.e("Error", "Please fill all fields");

                    return;
                }
                DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ISO_LOCAL_TIME;

                LocalTime startTime = LocalTime.parse(timeInputFrom.getText().toString(), dateTimeFormatter.ofPattern("HH:mm"));
                LocalTime endTime = LocalTime.parse(timeInputTo.getText().toString(), dateTimeFormatter.ofPattern("HH:mm"));
                if(startTime.isAfter(endTime)){
                    Log.d("Error", "Workday cant end before it starts!");
                    return;
                }
                String date = getIntent().getStringExtra("DATE");
                LocalDate eventDate = LocalDate.parse(date,dateTimeFormatter.ofPattern("ddMMyyyy"));
                Log.e("Event date:", eventDate.toString());
                Job selectedJob = getJobByName(jobName);
                int breakTime = Integer.parseInt(editTextBreakTime.getText().toString());
                WorkdayEvent workdayEvent = new WorkdayEvent(eventDate.toString(),startTime.toString(),endTime.toString(),breakTime,selectedJob);
                workdayEvent.setDayOfWeek(eventDate.getDayOfWeek().name());
                eventList.add(workdayEvent);
                saveEvent();
                setResult(RESULT_OK);
                finish();
            }
        });
    }
}
