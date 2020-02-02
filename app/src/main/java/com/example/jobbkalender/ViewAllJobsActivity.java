package com.example.jobbkalender;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.jobbkalender.Adapters.JobListAdapter;
import com.example.jobbkalender.DataClasses.Job;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class ViewAllJobsActivity extends AppCompatActivity {

    private List<Job> savedJobs;

    private void loadJobs(){
        // Laster listen med lagrede jobber.
        SharedPreferences pref = this.getSharedPreferences("SHARED PREFERENCES", MODE_PRIVATE);
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
        setContentView(R.layout.activity_view_all_jobs);
        loadJobs();
        if(savedJobs == null)
            return;
        ListView listView = findViewById(R.id.listViewAllJobs);
        JobListAdapter jobListAdapter = new JobListAdapter(this,0,savedJobs);
        listView.setAdapter(jobListAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getApplicationContext(),CreateJobActivity.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable("JOB", savedJobs.get(position));
                bundle.putBoolean("EDITMODE",true);
                intent.putExtra("BUNDLE",bundle);
                startActivityForResult(intent, 1);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        loadJobs();
        ListView listView = findViewById(R.id.listViewAllJobs);
        JobListAdapter jobListAdapter = new JobListAdapter(this,0,savedJobs);
        listView.setAdapter(jobListAdapter);
    }
}
