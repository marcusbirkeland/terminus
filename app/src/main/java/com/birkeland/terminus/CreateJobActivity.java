package com.birkeland.terminus;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.TextView;

import com.birkeland.terminus.DataClasses.Job;
import com.birkeland.terminus.DataClasses.SalaryRule;
import com.birkeland.terminus.DataClasses.WorkdayEvent;
import com.birkeland.terminus.dialogFragments.SalaryPeriodDatePicker;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class CreateJobActivity extends AppCompatActivity implements NumberPicker.OnValueChangeListener {

    private int salaryPeriodDate=1;
    private boolean editMode;
    private Job jobIn;
    public static final int CREATE_SALARY_RULE = 1;
    public static final int PICK_IMAGE = 2;
    public static final int DELETE_JOB = 2321;
    private String selectedImagePath;
    private List<Job> savedJobs = new ArrayList<>();
    private List<Job> jobList = new ArrayList<>();
    private List<String> salaryRuleStrings = new ArrayList<>();
    private List<SalaryRule> salaryRulesArrayList = new ArrayList<>();
    private List<WorkdayEvent> savedWorkdayEvents = new ArrayList<>();

    private int getJobIndexByName(String name){
        // Finn Job klasse etter navn i en liste. Brukes for å søke i shared preferences.
        int i =0;
        for (Job job: savedJobs
        ) {
            if(job.getName().equals(name)){
               return i;
            }
            i++;
        }
        return -1;
    }

    private void saveEvents(List<WorkdayEvent> eventsToSave){
        SharedPreferences pref = getSharedPreferences("SHARED PREFERENCES", MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        Gson gson = new Gson();
        Type type = new TypeToken<ArrayList<WorkdayEvent>>(){}.getType();
        String eventInfo = gson.toJson(eventsToSave,type);
            editor.putString("EVENTLIST", eventInfo);
        Log.d("Saving to sharedprefs: ", eventInfo);
        editor.apply();
    }
    private void loadEvents(){
        // Laster listen med lagrede jobber.
        SharedPreferences pref =getSharedPreferences("SHARED PREFERENCES", Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String json = pref.getString("EVENTLIST",null);
        Log.d("JSON","Json read: " + json);
        Type type = new TypeToken<ArrayList<WorkdayEvent>>(){}.getType();
        try {
            savedWorkdayEvents = gson.fromJson(json,type);
        } catch (Exception e){
            Log.e("Error","Failed to load events");
        }
    }

    private void editAllEventsWithJob(Job prevJob, Job newJob){
        loadEvents();
        List<WorkdayEvent> newEventList = savedWorkdayEvents;
        if (savedWorkdayEvents == null)
            return;
        for(WorkdayEvent event : newEventList){
            if(event.getJob().getName().equals(prevJob.getName())){
                LocalDate now = LocalDate.now();
                DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ISO_DATE_TIME;
                LocalDate eventDate = LocalDate.parse(event.getDate(), dateTimeFormatter.ofPattern("yyyy-MM-dd"));
                Log.d("Editing event on",eventDate.toString());
                Job setJob = new Job (jobIn);
                setJob.setImage(newJob.getImage());
                setJob.setName(newJob.getName());
                if(eventDate.isAfter(now)){
                    // Dersom event er etter nåværende dato vil lønn endre seg.
                   setJob.setSalary(newJob.getSalary());
                   setJob.setHasPaidBreak(newJob.hasPaidBreak());
                   setJob.setSalaryRules(newJob.getSalaryRules());
                }
                event.setJob(setJob);
            }
        }
        saveEvents(newEventList);
    }
    private void deleteAllEventsWithJob(Job job){
        loadEvents();
        List<WorkdayEvent> newEventList = new ArrayList<>();
        for(WorkdayEvent event : savedWorkdayEvents){
            if(!event.getJob().getName().equals(job.getName())){
                newEventList.add(event);
            }else {
                LocalDate now = LocalDate.now();
                DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ISO_DATE_TIME;
                LocalDate eventDate = LocalDate.parse(event.getDate(), dateTimeFormatter.ofPattern("yyyy-MM-dd"));
                if (!eventDate.isAfter(now)) {
                    newEventList.add(event);
                }
            }
        }
        saveEvents(newEventList);
    }
    private void deleteJob(Job job){
        loadJobs();
        int index = getJobIndexByName(job.getName());
        if(index != -1){
            List<Job> newJobList = savedJobs;
            newJobList.remove(index);
            deleteAllEventsWithJob(job);
            saveJobList(newJobList);
        }
        Intent intent = new Intent();
        setResult(DELETE_JOB,intent);
        finish();
    }

    private int getJobIndex(Job jobIn){
         int i = 0;
         for(Job job : savedJobs){
             if(job.getName().equals(jobIn.getName())){
                 return i;
             }
             i++;
         }
         Log.e("Null","Job out of index");
         return -1;
    }
    private void loadJobs(){
        // Laster listen med lagrede jobber.
        SharedPreferences pref = getSharedPreferences("SHARED PREFERENCES", MODE_PRIVATE);
        Gson gson = new Gson();
        String json = pref.getString("JOBLIST",null);
        Log.d("JSON","Json read: " + json);
        Type type = new TypeToken<ArrayList<Job>>(){}.getType();
        try {
            savedJobs= gson.fromJson(json,type);
        } catch (Exception e){
            Log.e("Error","Failed to load jobs");
        }
    }
    private void saveJobList (List<Job> jobListIn){
        SharedPreferences pref = getSharedPreferences("SHARED PREFERENCES", MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        Gson gson = new Gson();
        Type type = new TypeToken<ArrayList<Job>>(){}.getType();
        String jobInfo = gson.toJson(jobListIn,type);
        editor.putString("JOBLIST",jobInfo);
        editor.apply();
    }

    private void saveJob(boolean isEditMode){
        SharedPreferences pref = getSharedPreferences("SHARED PREFERENCES", MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        Gson gson = new Gson();
        Type type = new TypeToken<ArrayList<Job>>(){}.getType();
        String currentList = pref.getString("JOBLIST",null);
        String jobInfo = gson.toJson(jobList,type);

        if(isEditMode){
            Log.d("Saving job", "SAVING IN EDIT MODE");
            loadJobs();
            List<Job> newJobList = savedJobs;
            try{
                int index = getJobIndex(jobIn);
                newJobList.remove(index);
                // Job list inneholder kun en jobb.
                Job newJob = jobList.get(0);
                newJobList.add(index, newJob);
                jobInfo = gson.toJson(newJobList);
                editor.putString("JOBLIST", jobInfo);
                editor.apply();
                editAllEventsWithJob(jobIn, newJob);
            }catch (ArrayIndexOutOfBoundsException a){
                Log.d("Edit Job","Could not find job");
            }
            return;
        }

        if (currentList != null && !currentList.equals("[]")){
            editor.putString("JOBLIST", currentList.substring(0,currentList.length()-1)+","+jobInfo.substring(1));
        }else{
            editor.putString("JOBLIST",jobInfo);
        }
        editor.apply();
    }

    @Override
    public void onValueChange(NumberPicker numberPicker, int i, int i1) {
        TextView editTextSalaryPeriod = findViewById(R.id.editTextSetSalaryPeriod);
        editTextSalaryPeriod.setText(numberPicker.getValue() +". " + this.getString(R.string.salary_period_desc));
        salaryPeriodDate = numberPicker.getValue();
        Log.d("Selected date", "" +  numberPicker.getValue());
    }
    public void showNumberPicker(){
        SalaryPeriodDatePicker salaryPeriodDatePicker = new SalaryPeriodDatePicker();
        salaryPeriodDatePicker.setValueChangeListener(this);
        salaryPeriodDatePicker.show(getSupportFragmentManager(),"Salary Period Picker");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences pref = this.getSharedPreferences("DARKMODE",MODE_PRIVATE);
        boolean isDarkMode = pref.getBoolean("isDarkMode",false);
        if(isDarkMode){
            setTheme(R.style.AppThemeDark);
        }else{
            setTheme(R.style.AppTheme);
        }
        setContentView(R.layout.activity_create_job);
        //Setter default bilde-path for editMode
        final TextView textViewHourlyRate = findViewById(R.id.textViewHourlyRate);
        final Button buttonDelete = findViewById(R.id.buttonDeleteJob);
        final TextView editTextsetSalaryPeriod = findViewById(R.id.editTextSetSalaryPeriod);
        final EditText editTextJobName = findViewById(R.id.editTextNameJob);
        final EditText editTextEnterSalary = findViewById(R.id.editTextSalaryCreateJob);
        final CheckBox checkBoxPaidBreak = findViewById(R.id.checkBoxPaidBreak);
        final ImageView imageView = findViewById(R.id.imageViewCreateJob);
        final ListView listViewSalaryRules = findViewById(R.id.listViewSalaryrules);
        final Bundle bundle = getIntent().getBundleExtra("BUNDLE");
        buttonDelete.setVisibility(View.INVISIBLE);

        textViewHourlyRate.setText(getCurrency()+ "/" + getString(R.string.hour));
        if(bundle != null && bundle.getBoolean("EDITMODE")){
            TextView textViewTitle = findViewById(R.id.textViewCreateJobTitle);
            textViewTitle.setText(getString(R.string.edit_job_title));
            editMode = true;
            final Button deleteButton = findViewById(R.id.buttonDeleteJob);
            deleteButton.setVisibility(View.VISIBLE);
            jobIn = (Job) bundle.getSerializable("JOB");
            salaryPeriodDate = jobIn.getSalaryPeriodDate();
            editTextJobName.setText(jobIn.getName());
            editTextEnterSalary.setText(jobIn.getSalary()+"");
            editTextsetSalaryPeriod.setText(jobIn.getSalaryPeriodDate()+". " + this.getString(R.string.salary_period_desc));
            checkBoxPaidBreak.setChecked(jobIn.hasPaidBreak());
            if(jobIn.getImage() != null) {
                Uri uri = Uri.parse(jobIn.getImage());
                imageView.setImageURI(uri);
            }
            salaryRulesArrayList = jobIn.getSalaryRules();
            for(SalaryRule salaryRule : salaryRulesArrayList){
                salaryRuleStrings.add(salaryRule.toString(this));
            }
            selectedImagePath= jobIn.getImage();

            deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Dialog deleteDialog = makeDeleteDialog();
                    deleteDialog.show();
                }
            });
        }


        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1,salaryRuleStrings);
        listViewSalaryRules.setAdapter(arrayAdapter);
        listViewSalaryRules.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Lar brukeren redigere tilleggsregler
                Intent editSalaryRule = new Intent(getApplicationContext(), CreateSalaryRuleActivity.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable("SALARYRULE",salaryRulesArrayList.get(position));
                bundle.putBoolean("EDITMODE",true);
                bundle.putInt("INDEX",position);
                editSalaryRule.putExtra("BUNDLE",bundle);
                startActivityForResult(editSalaryRule, CREATE_SALARY_RULE);
            }
        });
        Button buttonCreateSalaryRule = findViewById(R.id.buttonAddSalaryRule);
        buttonCreateSalaryRule.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(),CreateSalaryRuleActivity.class);
                startActivityForResult(intent, 1);
            }
        });

        editTextsetSalaryPeriod.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showNumberPicker();
            }
        });
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public int hashCode() {
                return super.hashCode();
            }

            @Override
            public void onClick(View v) {
                // Sjekker for tillatelse
                if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(CreateJobActivity.this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                }else {
                    pickImage();
                }
            }
        });

        Button submitJob = findViewById(R.id.buttonAddJob);
        submitJob.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = editTextJobName.getText().toString();
                loadJobs();
                if(savedJobs != null) {
                    for (Job job : savedJobs) {
                        if (!editMode && job.getName().equals(name)) {
                            TextView textViewErrorMessage = findViewById(R.id.textViewCreateJobErrorMessage);
                            textViewErrorMessage.setText(getText(R.string.error_job_already_exists));
                            return;
                        }
                    }
                }
                if(name.equals("")){
                    TextView textViewErrorMessage = findViewById(R.id.textViewCreateJobErrorMessage);
                    textViewErrorMessage.setText(getText(R.string.error_enter_job_name));
                }else if(editTextEnterSalary.getText().toString().equals("")){
                    TextView textViewErrorMessage = findViewById(R.id.textViewCreateJobErrorMessage);
                    textViewErrorMessage.setText(getText(R.string.error_enter_salary));
                }
                else{
                    Dialog editDialog = makeEditDialog();
                    if(editMode){
                        editDialog.show();
                        return;
                    }else{
                        editJob();
                    }
                }
            }
        });
    }

    private void pickImage (){
        Intent getIntent = new Intent();
        getIntent.setType("image/*");
        getIntent.setAction(Intent.ACTION_GET_CONTENT);
        Intent pickIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        pickIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        pickIntent.setType("image/*");
        Intent chooserIntent = Intent.createChooser(getIntent, "Select Image");
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{pickIntent});
        startActivityForResult(chooserIntent, PICK_IMAGE);
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        pickImage();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == CREATE_SALARY_RULE && resultCode == RESULT_OK){
           final ListView listViewSalaryRules = findViewById(R.id.listViewSalaryrules);
            assert data != null;
            Bundle bundle = data.getExtras();
            assert bundle != null;
            SalaryRule salaryRule = (SalaryRule) bundle.getSerializable("SALARYRULE");
            if(bundle.getBoolean("WAS_EDITED")){
                int index = bundle.getInt("INDEX");
                salaryRulesArrayList.remove(index);
                salaryRuleStrings.remove(index);

                if(!bundle.getBoolean("DELETE")) {
                    salaryRulesArrayList.add(index, salaryRule);
                    salaryRuleStrings.add(index, salaryRule.toString(this));
                }
            }else{
                salaryRulesArrayList.add(salaryRule);
                salaryRuleStrings.add(salaryRule.toString(this));
                Log.d("SalaryRule:","NEW SALARY RULE: " + salaryRule.toString());
            }
            // Setter ListView.
            ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1,salaryRuleStrings);
            listViewSalaryRules.setAdapter(arrayAdapter);
        }

     if (requestCode == PICK_IMAGE && resultCode == RESULT_OK){
         try {
             Uri selectedImageUri = data.getData();
             Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImageUri);

             // Lagre fil i lokal mappe
             String outputPath = getApplicationContext().getApplicationInfo().dataDir + "/JobImages/";
             File dir = new File(outputPath);
             if(!dir.exists())
                 dir.mkdirs();
             int i = 0;
             String filename="ikon" + i +  ".png";
             File file = new File(dir, filename);
             while(file.exists()){
                 i++;
                 filename = "ikon" + i +  ".png";
                 file = new File(dir, filename);
             }
             selectedImagePath = outputPath + filename;
             FileOutputStream fOut = new FileOutputStream(file);
             bitmap.compress(Bitmap.CompressFormat.PNG, 85, fOut);
             fOut.flush();
             fOut.close();

             ImageView imageView = findViewById(R.id.imageViewCreateJob);
             imageView.setImageURI(selectedImageUri);
         } catch (Exception e) {
             Log.e("FileSelectorActivity", "File select error", e);
         }
     }
    }

    private Dialog makeDeleteDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.dialog_delete_job))
                .setPositiveButton(getString(R.string.delete), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        deleteJob(jobIn);
                    }
                })
                .setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // Do nothing
                    }
                });
        return builder.create();
    }
    private Dialog makeEditDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.dialog_edit_job))
                .setPositiveButton(getString(R.string.edit), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        editJob();
                    }
                })
                .setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // Do nothing
                    }
                });
        return builder.create();
    }
    private void editJob(){
        final EditText editTextEnterSalary = findViewById(R.id.editTextSalaryCreateJob);
        final CheckBox checkBoxPaidBreak = findViewById(R.id.checkBoxPaidBreak);
        final EditText editTextJobName = findViewById(R.id.editTextNameJob);
        String name = editTextJobName.getText().toString();
        double salary= Double.parseDouble(editTextEnterSalary.getText().toString());
        Job job = new Job(name, salary, salaryPeriodDate, salaryRulesArrayList,checkBoxPaidBreak.isChecked());
        job.setImage(selectedImagePath);
        jobList.add(job);
        if(!editMode) {
            saveJob(false);
        }else{
            saveJob(true);
            Bundle bundle = new Bundle();
            bundle.putSerializable("EDITED_JOB",job);
            bundle.putString("OLD_JOB_NAME",jobIn.getName());
            Intent intent = new Intent();
            intent.putExtra("BUNDLE",bundle);
            setResult(Activity.RESULT_OK,intent);
        }
        finish();
    }
    private String getCurrency(){
        SharedPreferences pref = this.getSharedPreferences("LOCALE",MODE_PRIVATE);
        return pref.getString("CURRENCY","");
    }
}
