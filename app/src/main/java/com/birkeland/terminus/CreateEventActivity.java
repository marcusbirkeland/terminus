package com.birkeland.terminus;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.birkeland.terminus.DataClasses.Job;
import com.birkeland.terminus.DataClasses.WorkdayEvent;
import com.birkeland.terminus.R;
import com.birkeland.terminus.customViews.ToggleRadioButton;
import com.birkeland.terminus.dialogFragments.ChooseWorkplaceDialogFragment;
import com.birkeland.terminus.dialogFragments.TimePickerDialogFragment;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class CreateEventActivity extends AppCompatActivity implements TimePickerDialogFragment.OnInputListener, ChooseWorkplaceDialogFragment.OnInputListener {

    int editTextToChange = 0;
    TimePickerDialogFragment timePickerDialogFragment = new TimePickerDialogFragment();
    ChooseWorkplaceDialogFragment chooseWorkplaceDialogFragment = new ChooseWorkplaceDialogFragment();
    List<Job> jobList = new ArrayList<>();
    String jobName = "";
    private String errorMessage = "";
    private boolean cancelSubmit;
    private Job selectedJob;

    private void startViewJob(Job job) {
        Intent intent = new Intent(getApplicationContext(),CreateJobActivity.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable("JOB",job);
        bundle.putBoolean("EDITMODE",true);
        intent.putExtra("BUNDLE",bundle);
        startActivityForResult(intent, 1);
    }

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
        LinearLayout linearLayout = findViewById(R.id.linearLayoutJobViewCreateEvent);
        linearLayout.setVisibility(View.VISIBLE);
        jobName = workplace.getName();
        t.setText(jobName);
        selectedJob = new Job(workplace);
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

        SharedPreferences pref = this.getSharedPreferences("DARKMODE",MODE_PRIVATE);
        boolean isDarkMode = pref.getBoolean("isDarkMode",false);
        if(isDarkMode){
            setTheme(R.style.AppThemeDark);
        }else{
            setTheme(R.style.AppTheme);
        }
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

        LinearLayout linearLayoutJobView = findViewById(R.id.linearLayoutJobViewCreateEvent);
        linearLayoutJobView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startViewJob(selectedJob);

            }
        });
        submitWorkday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadJobs();
                DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ISO_LOCAL_TIME;
                EditText editTextBreakTime = findViewById(R.id.editTextBreakTime);
                TextView timeInputFrom = findViewById(R.id.timeInputFromCreateEvent);
                TextView timeInputTo = findViewById(R.id.timeInputToCreateEvent);
                TextView textViewErrorMessage = findViewById(R.id.textViewCreateEventError);
                CheckBox checkBoxIsNightShift = findViewById(R.id.checkBoxNightshift);
                final ToggleRadioButton radioButtonRepeatEachWeek = findViewById(R.id.radioButtonRepeatEachWeek);
                final ToggleRadioButton radioButtonRepeatEveryOtherWeek = findViewById(R.id.radioButtonRepeatEveryOtherWeek);

                textViewErrorMessage.setText("");
                cancelSubmit = false;
                errorMessage = "";

                LocalTime startTime = LocalTime.parse(timeInputFrom.getText().toString(), dateTimeFormatter.ofPattern("HH:mm"));
                LocalTime endTime = LocalTime.parse(timeInputTo.getText().toString(), dateTimeFormatter.ofPattern("HH:mm"));
                if(startTime.equals(endTime)){
                    errorMessage = getString(R.string.error_pick_other_time);
                    Log.d("Create Event: ", "End time cannot be equal to start time");
                    cancelSubmit = true;
                }
                else if(startTime.isAfter(endTime) && !checkBoxIsNightShift.isChecked()){
                    errorMessage = getString(R.string.error_check_nightshift);
                    Log.d("Error", "Invalid time for regular shift");
                    cancelSubmit = true;
                } else if(startTime.isBefore(endTime) && checkBoxIsNightShift.isChecked()){
                    errorMessage = getString(R.string.error_uncheck_nightshift);
                    Log.d("Create Event: ", "Invalid time for night shift");
                    cancelSubmit = true;
                }
                else if (selectedJob == null){
                    errorMessage = getString(R.string.error_pick_job);
                    Log.e("Error", "Please pick job");
                    cancelSubmit = true;
                }
                if(cancelSubmit){
                    textViewErrorMessage.setText(errorMessage);
                    return;
                }
                String date = getIntent().getStringExtra("DATE");
                LocalDate eventDate = LocalDate.parse(date,dateTimeFormatter.ofPattern("ddMMyyyy"));

                // Setter default verdi til pause
                int breakTime = 30;
                Job selectedJob = getJobByName(jobName);
                // Lag event
                if(!editTextBreakTime.getText().toString().equals("")){
                    breakTime = Integer.parseInt(editTextBreakTime.getText().toString());
                }
                WorkdayEvent workdayEvent = new WorkdayEvent(eventDate.toString(),startTime.toString(),endTime.toString(),breakTime,selectedJob);
                workdayEvent.setDayOfWeek(eventDate.getDayOfWeek().name());
                workdayEvent.setNightShift(checkBoxIsNightShift.isChecked());

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
            Log.d("Adding event on",tempEvent.getDate());
        }
        saveEvent(eventList);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK){
            try{
                LinearLayout linearLayout = findViewById(R.id.linearLayoutJobViewCreateEvent);
                linearLayout.setVisibility(View.VISIBLE);
                TextView t = findViewById(R.id.textViewCreateEventJobName);
                ImageView imageView = findViewById(R.id.imageViewCreateEvent);
                Bundle bundle = data.getBundleExtra("BUNDLE");
                Job job = (Job) bundle.getSerializable("EDITED_JOB");
                selectedJob = new Job(job);
                t.setText(job.getName());
                imageView.setImageURI(Uri.parse(job.getImage()));
                loadJobs();
            }catch (NullPointerException n){
                Log.e("Create Event","No result");
            }
        }
        if(resultCode == CreateJobActivity.DELETE_JOB){
            loadJobs();
            LinearLayout layout = findViewById(R.id.linearLayoutJobViewCreateEvent);
            layout.setVisibility(View.INVISIBLE);
        }
    }
}

