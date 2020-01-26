package com.example.jobbkalender;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import com.example.jobbkalender.DataClasses.Job;
import com.example.jobbkalender.DataClasses.SalaryRule;
import com.example.jobbkalender.DataClasses.WorkdayEvent;
import com.example.jobbkalender.dialogFragments.SalaryPeriodDatePicker;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class CreateJobActivity extends AppCompatActivity implements NumberPicker.OnValueChangeListener {

    private int salaryPeriodDate=1;
    private boolean editMode;
    private Job jobIn;
    public static final int CREATE_SALARY_RULE = 1;
    public static final int PICK_IMAGE = 2;
    private String selectedImagePath;
    private List<Job> savedJobs = new ArrayList<>();
    private List<Job> jobList = new ArrayList<>();
    private List<String> salaryRuleStrings = new ArrayList<>();
    private List<SalaryRule> salaryRulesArrayList = new ArrayList<>();
    private List<WorkdayEvent> savedWorkdayEvents = new ArrayList<>();


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
        for(WorkdayEvent event : newEventList){
            if(event.getJob().getName().equals(prevJob.getName())){
                event.setJob(newJob);
            }
        }
        saveEvents(newEventList);
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
            int index = getJobIndex(jobIn);
            newJobList.remove(index);
            // Job list inneholder kun en jobb.
            Job newJob = jobList.get(0);
            newJobList.add(index, newJob);
            jobInfo = gson.toJson(newJobList);
            editor.putString("JOBLIST", jobInfo);
            editor.apply();
            editAllEventsWithJob(jobIn, newJob);
            return;
        }

        if (currentList != null){
            editor.putString("JOBLIST", currentList.substring(0,currentList.length()-1)+","+jobInfo.substring(1));
        }else{
            editor.putString("JOBLIST",jobInfo);
        }
        for (Job j: jobList) {
            Log.d("JOBS",j.toString());
        }
        editor.apply();
    }


    @Override
    public void onValueChange(NumberPicker numberPicker, int i, int i1) {
        TextView editTextSalaryPeriod = findViewById(R.id.editTextSetSalaryPeriod);
        editTextSalaryPeriod.setText("Hver "+ numberPicker.getValue() +"." );
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
        setContentView(R.layout.activity_create_job);
        //Setter default bilde-path for editMode

        final Button buttonDelete = findViewById(R.id.buttonDeleteJob);
        final TextView editTextsetSalaryPeriod = findViewById(R.id.editTextSetSalaryPeriod);
        final EditText editTextJobName = findViewById(R.id.editTextNameJob);
        final EditText editTextEnterSalary = findViewById(R.id.editTextSalaryCreateJob);
        final CheckBox checkBoxPaidBreak = findViewById(R.id.checkBoxPaidBreak);
        final ImageView imageView = findViewById(R.id.imageViewCreateJob);
        final ListView listViewSalaryRules = findViewById(R.id.listViewSalaryrules);
        final Bundle bundle = getIntent().getBundleExtra("BUNDLE");

        buttonDelete.setVisibility(View.INVISIBLE);

        if(bundle != null && bundle.getBoolean("EDITMODE")){
            editMode = true;
            Button deleteButton = findViewById(R.id.buttonDeleteJob);
            deleteButton.setVisibility(View.VISIBLE);
            jobIn = (Job) bundle.getSerializable("JOB");
            editTextJobName.setText(jobIn.getName());
            editTextEnterSalary.setText(jobIn.getSalary()+"");
            editTextsetSalaryPeriod.setText("Hver " +  jobIn.getSalaryPeriodDate()+".");
            checkBoxPaidBreak.setChecked(jobIn.hasPaidBreak());
            if(jobIn.getImage() != null) {
                Uri uri = Uri.parse(jobIn.getImage());
                imageView.setImageURI(uri);
            }
            salaryRulesArrayList = jobIn.getSalaryRules();
            for(SalaryRule salaryRule : salaryRulesArrayList){
                salaryRuleStrings.add(salaryRule.toString());
            }
            selectedImagePath= jobIn.getImage();
        }

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1,salaryRuleStrings);
        listViewSalaryRules.setAdapter(arrayAdapter);
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
                Intent getIntent = new Intent();
                getIntent.setType("image/*");
                getIntent.setAction(Intent.ACTION_GET_CONTENT);
                Intent pickIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                pickIntent.setType("image/*");
                Intent chooserIntent = Intent.createChooser(getIntent, "Select Image");
                chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[] {pickIntent});
                startActivityForResult(chooserIntent, PICK_IMAGE);

            }
        });

        Button submitJob = findViewById(R.id.buttonAddJob);
        submitJob.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = editTextJobName.getText().toString();
                if(!name.equals("") && !editTextEnterSalary.getText().toString().equals("")) {
                    double salary= Double.parseDouble(editTextEnterSalary.getText().toString());
                    Job job = new Job(name, salary, salaryPeriodDate, salaryRulesArrayList,checkBoxPaidBreak.isChecked());
                    job.setImage(selectedImagePath);
                    jobList.add(job);
                    if(!editMode) {
                        saveJob(false);
                    }else{
                        saveJob(true);
                    }
                    finish();
                }
            }
        });

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == CREATE_SALARY_RULE && resultCode == RESULT_OK){
            Bundle bundle = data.getExtras();
            SalaryRule salaryRule = (SalaryRule) bundle.getSerializable("SALARYRULE");
            salaryRulesArrayList.add(salaryRule);
            Log.d("SalaryRule:","NEW SALARY RULE: " + salaryRule.toString());
            ListView listViewSalaryRules = findViewById(R.id.listViewSalaryrules);
            salaryRuleStrings.add(salaryRule.toString());
            ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1,salaryRuleStrings);
            listViewSalaryRules.setAdapter(arrayAdapter);
        }
     if (requestCode == PICK_IMAGE && resultCode == RESULT_OK){
         try {
             Uri selectedImageUri = data.getData();
             final String path = getPathFromURI(selectedImageUri);
             String fileName = "";
             File f = new File(path);
             if (path != null) {
                 selectedImageUri = Uri.fromFile(f);
             }
             ImageView imageView = findViewById(R.id.imageViewCreateJob);
             imageView.setImageURI(selectedImageUri);
             // Lag en kopi av bildet og lagre path
             selectedImagePath = copyFile(f);
             Log.d("COPY IMAGE TO PATH",selectedImagePath);
         } catch (Exception e) {
             Log.e("FileSelectorActivity", "File select error", e);
         }
     }
    }

    private String copyFile(File file) {
    // Denne metoden lagrer en kopi av valgt bilde i jobimages mappen
        InputStream in = null;
        OutputStream out = null;
        String outputPath = getApplicationContext().getApplicationInfo().dataDir + "/JobImages/";
        try {
            //create output directory if it doesn't exist
            File dir = new File (outputPath);
            if (!dir.exists())
            {
                dir.mkdirs();
            }
            in = new FileInputStream(file);
            out = new FileOutputStream(outputPath+ file.getName());

            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            in.close();
            in = null;

            // write the output file (You have now copied the file)
            out.flush();
            out.close();
            out = null;
            Log.d("Image saved to:",outputPath+ file.getName());
            return outputPath+ file.getName();

        }  catch (FileNotFoundException fnfe1) {
            Log.e("File not found", fnfe1.getMessage());
        }
        catch (Exception e) {
            Log.e("Error", e.getMessage());
        }
        return null;
    }

    public String getPathFromURI(Uri contentUri) {
        // Kode hentet fra https://mobikul.com/pick-image-gallery-android/
        String res = null;
        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(contentUri, proj, null, null, null);
        if (cursor.moveToFirst()) {
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            res = cursor.getString(column_index);
        }
        cursor.close();
        return res;
    }

}
