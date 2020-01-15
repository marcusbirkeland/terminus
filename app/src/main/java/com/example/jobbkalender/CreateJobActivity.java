package com.example.jobbkalender;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.example.jobbkalender.DataClasses.Job;
import com.example.jobbkalender.DataClasses.SalaryRule;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class CreateJobActivity extends AppCompatActivity {

    List<Job> jobList = new ArrayList<>();
     List<String> salaryRuleStrings = new ArrayList<>();
     List<SalaryRule> salaryRulesArrayList = new ArrayList<>();

    private void saveJob(){
        SharedPreferences pref = getSharedPreferences("SHAREDPREFERENCES", MODE_PRIVATE);
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
                double salary= Double.parseDouble(editTextEnterSalary.getText().toString());
                if(name!= "" && salary != 0) {
                    Job job = new Job(name, salary, salaryRulesArrayList);
                    jobList.add(job);
                    saveJob();
                    finish();
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 1 && resultCode == RESULT_OK){
            Bundle bundle = data.getExtras();
            SalaryRule salaryRule = (SalaryRule) bundle.getSerializable("SALARYRULE");
            salaryRulesArrayList.add(salaryRule);
            Log.d("SalaryRule:","NEW SALARY RULE: " + salaryRule.toString());
            ListView listViewSalaryRules = findViewById(R.id.listViewEventList);
            salaryRuleStrings.add(salaryRule.toString());
            ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1,salaryRuleStrings);
            listViewSalaryRules.setAdapter(arrayAdapter);
        }
    }

}
