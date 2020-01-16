package com.example.jobbkalender;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;

import com.example.jobbkalender.DataClasses.Job;
import com.example.jobbkalender.DataClasses.SalaryRule;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.lang.reflect.Type;
import java.nio.channels.FileChannel;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class CreateJobActivity extends AppCompatActivity {

    public static final int CREATE_SALARY_RULE = 1;
    public static final int PICK_IMAGE = 2;
    private String selectedImagePath;
    List<Job> jobList = new ArrayList<>();
     List<String> salaryRuleStrings = new ArrayList<>();
     List<SalaryRule> salaryRulesArrayList = new ArrayList<>();


    private void saveJob(){
        SharedPreferences pref = getSharedPreferences("SHARED PREFERENCES", MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        Gson gson = new Gson();
        Type type = new TypeToken<ArrayList<Job>>(){}.getType();
        String currentList = pref.getString("JOBLIST",null);
        String jobInfo = gson.toJson(jobList,type);
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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_job);
        ListView listViewSalaryRules = findViewById(R.id.listViewSalaryrules);
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

        Button submitJob = findViewById(R.id.buttonAddJob);
        submitJob.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText editTextJobName = findViewById(R.id.editTextNameJob);
                EditText editTextEnterSalary = findViewById(R.id.editTextSalaryCreateJob);
                String name = editTextJobName.getText().toString();
                if(!name.equals("") && !editTextEnterSalary.getText().toString().equals("")) {
                    double salary= Double.parseDouble(editTextEnterSalary.getText().toString());
                    Job job = new Job(name, salary, salaryRulesArrayList);
                    job.setImage(selectedImagePath);
                    jobList.add(job);
                    saveJob();
                    finish();
                }


            }
        });
        ImageView imageView = findViewById(R.id.imageViewCreateJob);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public int hashCode() {
                return super.hashCode();
            }

            @Override
            public void onClick(View v) {
                Intent getIntentnt = new Intent();
                getIntentnt.setType("image/*");
                getIntentnt.setAction(Intent.ACTION_GET_CONTENT);
                Intent pickIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                pickIntent.setType("image/*");
                Intent chooserIntent = Intent.createChooser(getIntentnt, "Select Image");
                chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[] {pickIntent});
                startActivityForResult(chooserIntent, PICK_IMAGE);

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
