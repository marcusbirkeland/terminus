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
import android.widget.ListView;

import com.example.jobbkalender.DataClasses.SalaryRule;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.ArrayList;

public class CreateJobActivity extends AppCompatActivity {

    public ArrayList<String> salaryRuleStrings = new ArrayList<>();
    public ArrayList<SalaryRule> salaryRulesArrayList = new ArrayList<>();

    private void saveData(){
        SharedPreferences pref = getSharedPreferences("Shared pref", MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        Gson gson = new Gson();
        String json = gson.toJson(salaryRulesArrayList);
        editor.putString("salary rules",json);
        editor.apply();
    }
    private void loadData(){
        SharedPreferences pref = getSharedPreferences("Shared pref", MODE_PRIVATE);
        Gson gson = new Gson();
        String json = pref.getString("salary rules",null);
        Type type = new TypeToken<ArrayList<SalaryRule>>(){}.getType();
        salaryRulesArrayList = gson.fromJson(json,type);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_job);

        loadData();
        try {
            for (SalaryRule s : salaryRulesArrayList) {
                salaryRuleStrings.add(s.toString());
            }
        } catch (NullPointerException n){
            Log.d("ERROR", "NULLPOINTER EXCEPTION");
        }
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
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        ArrayList<SalaryRule> salaryRules = new ArrayList<>();
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 1 && resultCode == RESULT_OK){
            Bundle bundle = data.getExtras();
            SalaryRule salaryRule = (SalaryRule) bundle.getSerializable("SALARYRULE");
            salaryRules.add(salaryRule);
            salaryRulesArrayList= salaryRules;
            Log.d("SalaryRule:","NEW SALARY RULE: " + salaryRule.toString());
            ListView listViewSalaryRules = findViewById(R.id.listViewSalaryrules);
            salaryRuleStrings.add(salaryRule.toString());
            ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1,salaryRuleStrings);
            listViewSalaryRules.setAdapter(arrayAdapter);
            saveData();

        }
    }
}
