package com.birkeland.terminus;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
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
import android.widget.LinearLayout;
import android.widget.TextView;

import com.birkeland.terminus.DataClasses.Job;
import com.birkeland.terminus.DataClasses.WorkdayEvent;
import com.birkeland.terminus.dialogFragments.ChooseWorkplaceDialogFragment;
import com.birkeland.terminus.dialogFragments.ColorPickerDialogFragment;
import com.birkeland.terminus.dialogFragments.NoteDialogFragment;
import com.birkeland.terminus.dialogFragments.OvertimeDialogFragment;
import com.birkeland.terminus.dialogFragments.RepeatEventDialogFragment;
import com.birkeland.terminus.dialogFragments.TimePickerDialogFragment;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static com.birkeland.terminus.MainActivity.DELETE_EVENT;

public class CreateEventActivity extends AppCompatActivity implements TimePickerDialogFragment.OnInputListener, ChooseWorkplaceDialogFragment.OnInputListener, RepeatEventDialogFragment.OnInputListener, OvertimeDialogFragment.OnInputListener, ColorPickerDialogFragment.OnInputListener, NoteDialogFragment.OnInputListener {

    public static  final int EDIT_SHIFT = 42069;
    int editTextToChange = 0;
    TimePickerDialogFragment timePickerDialogFragment = new TimePickerDialogFragment();
    ChooseWorkplaceDialogFragment chooseWorkplaceDialogFragment = new ChooseWorkplaceDialogFragment();
    RepeatEventDialogFragment repeatEventDialogFragment = new RepeatEventDialogFragment();
    OvertimeDialogFragment overtimeDialogFragment;
    ColorPickerDialogFragment colorPickerDialogFragment;
    NoteDialogFragment noteDialogFragment;
    List<Job> jobList = new ArrayList<>();
    private List<WorkdayEvent> workdayEvents = new ArrayList<>();
    String jobName = "";
    private String errorMessage = "";
    private boolean cancelSubmit;
    private boolean editMode = false;
    private WorkdayEvent eventToSave;
    private WorkdayEvent eventToEdit;
    private Job selectedJob;
    private Boolean isOvertime = false;

    private int repeatMonths;
    private Boolean repeatEachWeek = false;
    private Boolean repeatEveryOtherWeek = false;

    private int overtimePercentage = 100;

    private int color = 0;

    private String note = "";

    private void loadEvents() {
        // Laster listen med lagrede jobber.
        SharedPreferences pref = getSharedPreferences("SHARED PREFERENCES", Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String json = pref.getString("EVENTLIST", null);
        Log.d("JSON", "Json read: " + json);
        Type type = new TypeToken<ArrayList<WorkdayEvent>>() {
        }.getType();
        try {
            workdayEvents = gson.fromJson(json, type);
        } catch (Exception e) {
            Log.e("Error", "Failed to load events");
        }
    }
    private void saveEvents(){
        SharedPreferences pref = getSharedPreferences("SHARED PREFERENCES", MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        Gson gson = new Gson();
        Type type = new TypeToken<ArrayList<WorkdayEvent>>(){}.getType();
        String eventInfo = gson.toJson(workdayEvents,type);
        editor.putString("EVENTLIST", eventInfo);
        Log.d("Saving to sharedprefs: ", eventInfo);
        editor.apply();
    }
    private int getEventIndex(WorkdayEvent event){
        for (int i = 0; i<workdayEvents.size();i++){
            WorkdayEvent e = workdayEvents.get(i);
            if(e.getDate().equals(event.getDate()) &&
                    e.getJob().getName().equals(event.getJob().getName()) &&
                    e.getStartTime().equals(event.getStartTime()) &&
                    e.getEndTime().equals(event.getEndTime())
            ){
                return i;
            }
        }
        return -1;
    }
    private int getEqualEventIndex(WorkdayEvent event) {
        LocalDate selectedDate = LocalDate.parse(event.getDate(), DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        LocalDate checkDate;
        for (int i = 0; i<workdayEvents.size();i++){
            WorkdayEvent e = workdayEvents.get(i);
            checkDate = LocalDate.parse(e.getDate(), DateTimeFormatter.ofPattern("yyyy-MM-dd"));

            // Skip event if the event is before selected date, or is unequal
            if((checkDate.isAfter(selectedDate) || checkDate.isEqual(selectedDate))&&
                    e.getDayOfWeek().equals(event.getDayOfWeek()) &&
                    e.getJob().getName().equals(event.getJob().getName()) &&
                    e.getStartTime().equals(event.getStartTime()) &&
                    e.getEndTime().equals(event.getEndTime()) &&
                    e.isOvertime() == event.isOvertime() &&
                    e.getOvertimePercentage() == event.getOvertimePercentage() &&
                    e.getBreakTime() == event.getBreakTime()
            ){
                try{
                    if(
                    e.getColor() == event.getColor()&&
                            e.getNote().equals(event.getNote())){
                        return -1;
                    }
                }catch (NullPointerException n){
                }
                Log.d("waho",""+i);
                return i;
            }
        }
        return -1;
    }

    private void saveSelectedTime(String timeFrom, String timeTo){
        SharedPreferences sharedPreferences = getSharedPreferences("SHARED PREFERENCES",MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("lastSelectedTimeFrom",timeFrom);
        editor.putString("lastSelectedTimeTo",timeTo);
        editor.apply();
    }
    private String formatDate(LocalDate date){
        Instant instant = date.atStartOfDay(ZoneId.systemDefault()).toInstant();
        Date date1 = new Date(instant.toEpochMilli());
        //For ukedag
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE");
        String dayOfWeek = sdf.format(date1);
        dayOfWeek = dayOfWeek.substring(0,1).toUpperCase() + dayOfWeek.substring(1);
        //For dato
        DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT);
        return dayOfWeek + " " + df.format(date1) ;
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
    public void sendRepeat(boolean isEachWeek, boolean isEveryOtherWeek, int numMonths){
        Log.d("Set repeat:", "Each week: "+ isEachWeek + " | Every other week: " + isEveryOtherWeek + " | Repeat for: " + numMonths + " months" );
        TextView textViewRepeat = findViewById(R.id.textViewRepeat);
        String repeatMessage = getString(R.string.repeats) + " ";
        if(isEachWeek)
            repeatMessage+= getString(R.string.each_week).toLowerCase();
        else if (isEveryOtherWeek)
            repeatMessage+= getString(R.string.every_other_week).toLowerCase();
        repeatMessage+= " " + getString(R.string.for_repeat) + " " + numMonths + " " + getString(R.string.months_repeat);
        textViewRepeat.setText(repeatMessage);
        repeatEachWeek = isEachWeek;
        repeatEveryOtherWeek = isEveryOtherWeek;
        repeatMonths = numMonths;
    }
    @Override
    public void sendOvertime(double percentage) {
        Log.d("Set overtime: ", percentage + "%");
        TextView textViewOvertime = findViewById(R.id.textViewOvertime);
        textViewOvertime.setText(getString(R.string.overtime) + ": " + (int)percentage +"%");
        overtimePercentage = (int) percentage;
        if(overtimePercentage > 0)
            isOvertime = true;
    }
    @Override
    public void sendColor(int colorIn) {
        this.color = colorIn;
        ImageView imageViewColor = findViewById(R.id.imageViewColor);
        imageViewColor.setBackgroundColor(color);
    }

    private String shortenString(String input, int maxLength){
        if(input.length() < maxLength && !input.contains("\n")){
            return input;
        }else{
            if(input.contains("\n"))
                return (input.substring(0,input.indexOf("\n")));
            else
                return input.substring(0,maxLength-1) + "...";
        }
    }
    @Override
    public void sendNote(String noteOut) {
        this.note = noteOut;
        final TextView textViewNote = findViewById(R.id.textViewViewEventNote);
        textViewNote.setText(shortenString(noteOut,35));
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
            imageView.setImageDrawable(getDrawable(R.drawable.default_job_icon));
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
        chooseWorkplaceDialogFragment.show(getSupportFragmentManager(), "Choose workplace opened");
        Log.d("Dialog","Choose workplace opened");
    }
    void showRepeatEventDialog(){
        if(repeatEventDialogFragment.isAdded())
            return;
        repeatEventDialogFragment.show(getSupportFragmentManager(), "Repeat opened");
        Log.d("Dialog", "Repeat event");
    }
    void showOvertimeDialog(int percentage){
        overtimeDialogFragment = new OvertimeDialogFragment(percentage);
        if(overtimeDialogFragment.isAdded())
            return;
        overtimeDialogFragment.show(getSupportFragmentManager(), "Overtime  opened");
        Log.d("Dialog:", "Overtime");
    }
    void showColorPickerDialog(int color){
        colorPickerDialogFragment = new ColorPickerDialogFragment(color);
        if(colorPickerDialogFragment.isAdded())
            return;
        colorPickerDialogFragment.show(getSupportFragmentManager(), "Color Picker opened");
        Log.d("Dialog: ", "Color picker");
    }
    void showNoteDialog(String note){
        noteDialogFragment = new NoteDialogFragment(note);
        if(noteDialogFragment.isAdded())
            return;
        noteDialogFragment.show(getSupportFragmentManager(), "Note dialog opened");
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {



        color = getColor(R.color.colorAccent);
        editMode =  getIntent().getBooleanExtra("editMode",false);
        SharedPreferences pref = this.getSharedPreferences("DARKMODE",MODE_PRIVATE);
        boolean isDarkMode = pref.getBoolean("isDarkMode",false);
        if(isDarkMode){
            setTheme(R.style.AppThemeDark);
        }else{
            setTheme(R.style.AppTheme);
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_event);
        final EditText editTextBreakTime = findViewById(R.id.editTextBreakTime);
        pref = this.getSharedPreferences("SHARED PREFERENCES" ,MODE_PRIVATE);
        editTextBreakTime.setHint("" + pref.getInt("defaultBreakTime",30));
        final TextView dateView = findViewById(R.id.textViewCreateEventDate);
        // Viser ukedag og dato
        String date = getIntent().getStringExtra("DATE");
        LocalDate eventDate;
        try {
            eventDate = LocalDate.parse(date, DateTimeFormatter.ofPattern("ddMMyyyy"));
        }catch (DateTimeParseException d){
            eventDate = LocalDate.parse(date,DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        }
        dateView.setText(formatDate(eventDate));
        final TextView textViewWeek = findViewById(R.id.textViewWeek);
        WeekFields weekFields = WeekFields.of(Locale.getDefault());
        int weekNum =eventDate.get(weekFields.weekOfWeekBasedYear());
        textViewWeek.setText(getString(R.string.week )+ " " + String.valueOf(weekNum));
        final TextView timeInputFrom = findViewById(R.id.timeInputFromCreateEvent);
        final TextView timeInputTo = findViewById(R.id.timeInputToCreateEvent);
        // Finner forige valgte tidspunkt
        SharedPreferences preferences = getSharedPreferences("SHARED PREFERENCES",MODE_PRIVATE);
        timeInputFrom.setText(preferences.getString("lastSelectedTimeFrom","16:00"));
        timeInputTo.setText(preferences.getString("lastSelectedTimeTo","20:00"));
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

        final ConstraintLayout constraintLayoutRepeatEvent = findViewById(R.id.constraintLayoutRepeat);
        constraintLayoutRepeatEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showRepeatEventDialog();
            }
        });
        final ConstraintLayout constraintLayoutOvertime = findViewById(R.id.constraintLayoutOvertime);
        constraintLayoutOvertime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                  showOvertimeDialog(overtimePercentage);
            }
        });

        final ConstraintLayout constraintLayoutColor = findViewById(R.id.constraintLayoutColor);
        constraintLayoutColor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showColorPickerDialog(color);
            }
        });
        final  ConstraintLayout constraintLayoutNote = findViewById(R.id.constraintLayoutNote);
        constraintLayoutNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showNoteDialog(note);
            }
        });

        final LinearLayout jobConatiner = findViewById(R.id.linearLayoutJobViewCreateEvent);
        jobConatiner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadJobs();
                showChooseWorkplaceDialog();
            }
        });



        Button submitWorkday = findViewById(R.id.buttonSubmitWorkday);
        if(editMode){
            eventToEdit = (WorkdayEvent) getIntent().getSerializableExtra("eventToEdit");
            if(eventToEdit!= null){
                eventToSave = new WorkdayEvent(eventToEdit);
                timeInputFrom.setText(eventToEdit.getStartTime());
                timeInputTo.setText(eventToEdit.getEndTime());
                editTextBreakTime.setText("" + eventToEdit.getBreakTime());
                selectedJob = new Job(eventToEdit.getJob());
                Log.d("SELECTED JOB", selectedJob.toString());
                try {
                    note = eventToEdit.getNote();
                } catch (NullPointerException n){}
                final Button buttonSubmitt = findViewById(R.id.buttonSubmitWorkday);
                buttonSubmitt.setText(R.string.edit);
                jobConatiner.setVisibility(View.VISIBLE);
                final ConstraintLayout constraintLayoutRepeat = findViewById(R.id.constraintLayoutRepeat);
                constraintLayoutRepeat.setVisibility(View.INVISIBLE);
                constraintLayoutRepeat.setMaxHeight(1);
                final TextView textViewOvertime = findViewById(R.id.textViewOvertime);
                textViewOvertime.setText(getString(R.string.overtime) +": " + (int)eventToEdit.getOvertimePercentage() + "%");
                final TextView textViewNote = findViewById(R.id.textViewViewEventNote);
                try {
                    textViewNote.setText(getString(R.string.note )+ ": " + shortenString(eventToEdit.getNote(),35));
                } catch (NullPointerException n){}

                final ImageView imageViewColor = findViewById(R.id.imageViewColor);
                try {
                    imageViewColor.setBackgroundColor(eventToEdit.getColor());
                } catch (NullPointerException n){}
                final TextView jobName = findViewById(R.id.textViewCreateEventJobName);
                final ImageView imageView = findViewById(R.id.imageViewCreateEvent);
                jobName.setText(eventToEdit.getJob().getName());
                try {
                    imageView.setImageURI(Uri.parse(eventToEdit.getJob().getImage()));
                }catch (Exception e){
                    Log.e("Create event", "Failed to load image. Setting default" + e);
                    imageView.setImageResource(R.drawable.default_job_icon);
                }
                eventToSave.setJob(new Job(eventToEdit.getJob()));
                final Button buttonDelete = findViewById(R.id.buttonDeleteEvent);
                buttonDelete.setVisibility(View.VISIBLE);
                buttonDelete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                      Dialog dialog =  makeDeleteEventDialog(eventToEdit);
                      dialog.show();
                    }
                });
            }
        }

        submitWorkday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadJobs();
                DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ISO_LOCAL_TIME;
                final EditText editTextBreakTime = findViewById(R.id.editTextBreakTime);
                final TextView timeInputFrom = findViewById(R.id.timeInputFromCreateEvent);
                final TextView timeInputTo = findViewById(R.id.timeInputToCreateEvent);
                final TextView textViewErrorMessage = findViewById(R.id.textViewCreateEventError);
                textViewErrorMessage.setText("");
                Boolean nightShift = false;
                cancelSubmit = false;
                errorMessage = "";

                LocalTime startTime = LocalTime.parse(timeInputFrom.getText().toString(), dateTimeFormatter.ofPattern("HH:mm"));
                LocalTime endTime = LocalTime.parse(timeInputTo.getText().toString(), dateTimeFormatter.ofPattern("HH:mm"));

                if (selectedJob == null){
                    errorMessage = getString(R.string.error_pick_job);
                    Log.e("Error", "Please pick job");
                    cancelSubmit = true;
                }

                if(startTime.equals(endTime)){
                    errorMessage = getString(R.string.error_pick_other_time);
                    Log.d("Create Event: ", "End time cannot be equal to start time");
                    cancelSubmit = true;
                }
                if(startTime.isAfter(endTime)){
                    nightShift = true;
                }

                if(cancelSubmit){
                    textViewErrorMessage.setText(errorMessage);
                    return;
                }
                saveSelectedTime(timeInputFrom.getText().toString(),timeInputTo.getText().toString());
                String date = getIntent().getStringExtra("DATE");
                LocalDate eventDate = null;
                try {
                     eventDate = LocalDate.parse(date, dateTimeFormatter.ofPattern("ddMMyyyy"));
                }catch(DateTimeParseException d) {
                     eventDate = LocalDate.parse(date, dateTimeFormatter.ofPattern("yyyy-MM-dd"));

                }

                // Setter default verdi for pause
                int breakTime = Integer.parseInt(editTextBreakTime.getHint().toString());
                Job selectedJob = getJobByName(jobName);
                // Lag event
                if(!editTextBreakTime.getText().toString().equals("")){
                    SharedPreferences sharedPreferences = getSharedPreferences("SHARED PREFERENCES",MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    breakTime = Integer.parseInt(editTextBreakTime.getText().toString());
                    // Setter fremtidig default verdi
                    editor.putInt("defaultBreakTime",breakTime);
                    editor.apply();
                }
                eventToSave = new WorkdayEvent(eventDate.toString(),startTime.toString(),endTime.toString(),breakTime,selectedJob);
                eventToSave.setDayOfWeek(eventDate.getDayOfWeek().name());
                eventToSave.setNightShift(nightShift);
                eventToSave.setOvertime(isOvertime);
                eventToSave.setColor(color);
                eventToSave.setNote(note);
                eventToSave.setOvertimePercentage(overtimePercentage);

                int repeatPeriod = 0;
                if(repeatEachWeek || repeatEveryOtherWeek){
                    repeatPeriod = repeatMonths;
                }
                // Lag event og lagre event
                if(repeatEachWeek){
                    repeatEvent(eventToSave,eventDate,7, repeatPeriod);
                }else if(repeatEveryOtherWeek){
                    repeatEvent(eventToSave,eventDate,14,repeatPeriod);
                } else{
                    List<WorkdayEvent> eventList = new ArrayList<>();
                    if(!editMode) {
                        eventList.add(eventToSave);
                        saveEvent(eventList);
                    }else{
                        makeEditDialog().show();
                        return;
                    }
                }
                setResult(RESULT_OK);
                finish();
            }
        });
    }
    private void repeatEvent(WorkdayEvent event, LocalDate eventDate, int dayInterval, int period){
        LocalDate itterDate = eventDate;
        LocalDate endDate = eventDate.plusMonths(period);
        List<WorkdayEvent> eventList = new ArrayList<>();
        while(itterDate.isBefore(endDate)){
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

    private Dialog makeEditDialog (){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.dialog_edit_shift))
                .setMessage(R.string.edit_shift_description)
                .setNegativeButton(getString(R.string.edit), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        editEvent(eventToEdit,eventToSave);
                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                    }
                })
                .setPositiveButton(getString(R.string.edit_equal_shifts), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        editEqualEvents(eventToEdit,eventToSave);
                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                    }
                });
        return builder.create();
    }

    private boolean editEvent(WorkdayEvent eventToEdit, WorkdayEvent eventToSave){
        loadEvents();
        int index = getEventIndex(eventToEdit);
        if(index > -1) {
            eventToSave.setJob(selectedJob);
           Log.d("JOBBB§!!!", eventToSave.getJob().toString());
            workdayEvents.remove(index);
            workdayEvents.add(index,eventToSave);
            saveEvents();
        }else{
            return  false;
        }
        return true;
    }

    private void editEqualEvents (WorkdayEvent eventToEdit, WorkdayEvent eventToSave){
        loadEvents();
        int index = getEqualEventIndex(eventToEdit);
        int prevIndex = -1;
        Log.d("Equal event index", index + "");
        while(index > -1){
            loadEvents();
            // Avslutt hvis ingenting er endret.
            if(index == prevIndex)
                break;
            WorkdayEvent tempEvent = workdayEvents.get(index);
            String date = tempEvent.getDate();
            eventToSave.setDate(date);
            eventToSave.setJob(selectedJob);
            workdayEvents.remove(index);
            workdayEvents.add(index,eventToSave);
            saveEvents();
            prevIndex = index;
            index = getEqualEventIndex(eventToEdit);
            Log.d("Equal event index", index + "");
        }
    }
    private void deleteEvent(WorkdayEvent event){
        loadEvents();
        int index = getEventIndex(event);
        if(index > -1) {
            workdayEvents.remove(index);
        }
        saveEvents();
        setResult(DELETE_EVENT);
        finish();
    }

    private void deleteEqualEvents (WorkdayEvent event){
        loadEvents();
        int index = getEqualEventIndex(event);
        while (index != -1){
            workdayEvents.remove(index);
            index = getEqualEventIndex(event);
        }
        saveEvents();
        setResult(DELETE_EVENT);
        finish();
    }

    private Dialog makeDeleteEventDialog (final WorkdayEvent event){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.delete_shift) + "?")
                .setNegativeButton(getString(R.string.delete_shift), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        deleteEvent(event);
                        //Intent i=new Intent(getApplicationContext(), MainActivity.class);
                        //i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        //startActivity(i);
                    }
                })
                .setPositiveButton(getString(R.string.delete_equal_shifts), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteEqualEvents(event);
                        //Intent i=new Intent(getApplicationContext(), MainActivity.class);
                        //i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        //startActivity(i);
                    }
                });
        return builder.create();
    }
}


