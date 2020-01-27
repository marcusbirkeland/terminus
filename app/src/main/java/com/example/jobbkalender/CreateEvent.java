package com.example.jobbkalender;

import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
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

    List<Job> jobList = new ArrayList<>();
    String jobName = "";

    private void saveEvent(List<WorkdayEvent> eventList){
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
                DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ISO_LOCAL_TIME;
                EditText editTextBreakTime = findViewById(R.id.editTextBreakTime);
                TextView timeInputFrom = findViewById(R.id.timeInputFromCreateEvent);
                TextView timeInputTo = findViewById(R.id.timeInputToCreateEvent);
                CheckBox checkBoxIsNightShift = findViewById(R.id.checkBoxNightshift);
                final ToggleRadioButton radioButtonRepeatEachWeek = findViewById(R.id.radioButtonRepeatEachWeek);
                final ToggleRadioButton radioButtonRepeatEveryOtherWeek = findViewById(R.id.radioButtonRepeatEveryOtherWeek);
                final RadioGroup radioGroup = findViewById(R.id.radioGroupRepeat);
                // For å kunne unchecke radioButton


                LocalTime startTime = LocalTime.parse(timeInputFrom.getText().toString(), dateTimeFormatter.ofPattern("HH:mm"));
                LocalTime endTime = LocalTime.parse(timeInputTo.getText().toString(), dateTimeFormatter.ofPattern("HH:mm"));
                if(startTime.isAfter(endTime) && !checkBoxIsNightShift.isChecked()){
                    Log.d("Error", "Workday cant end before it starts!");
                    return;
                }
                if (jobName.equals("") ){
                    Log.e("Error", "Please fill all fields");
                    return;
                }
                String date = getIntent().getStringExtra("DATE");
                LocalDate eventDate = LocalDate.parse(date,dateTimeFormatter.ofPattern("ddMMyyyy"));
                // Setter default verdi til pause
                int breakTime = 30;
                Job selectedJob = getJobByName(jobName);
                // Lag event
                WorkdayEvent workdayEvent = new WorkdayEvent(eventDate.toString(),startTime.toString(),endTime.toString(),breakTime,selectedJob);
                workdayEvent.setDayOfWeek(eventDate.getDayOfWeek().name());
                workdayEvent.setNightShift(checkBoxIsNightShift.isChecked());

                if(!editTextBreakTime.getText().toString().equals("")){
                    breakTime = Integer.parseInt(editTextBreakTime.getText().toString());
                }
                // Lag event og lagre event
                if(radioButtonRepeatEachWeek.isChecked()){
                    repeatEvent(workdayEvent,eventDate,7);
                }else if(radioButtonRepeatEveryOtherWeek.isChecked()){
                    repeatEvent(workdayEvent,eventDate,14);
                } else{

                List<WorkdayEvent> eventList = new ArrayList<>();
                eventList.add(workdayEvent);
                    saveEvent(eventList);
                }
                setResult(RESULT_OK);
                finish();
            }
        });
    }
    private void repeatEvent(WorkdayEvent event, LocalDate eventDate, int dayInterval){
        LocalDate itterDate = eventDate;
        int currentYear = itterDate.getYear();
        List<WorkdayEvent> eventList = new ArrayList<>();
        while(itterDate.getYear() != (currentYear + 1)){
            WorkdayEvent tempEvent = new WorkdayEvent(event);
            tempEvent.setDate(itterDate.toString());
            eventList.add(eventList.size(),tempEvent);
            itterDate = itterDate.plusDays(dayInterval);
        }
        for(WorkdayEvent tempEvent : eventList){
            Log.d("Adding event on",tempEvent.getDate().toString());
        }
        saveEvent(eventList);
    }
}
