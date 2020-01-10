package com.example.jobbkalender;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;

import java.sql.Time;
import java.time.LocalTime;

import com.example.jobbkalender.DataClasses.SalaryRule;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class CreateSalaryRuleActivity extends AppCompatActivity {

    ArrayList<Integer> setDays (CheckBox [] checkBoxes){
        ArrayList<Integer> days = new ArrayList<>();
        for (int i=0; i<checkBoxes.length;i++){
            if (checkBoxes[i].isChecked()){
                days.add(i+1);
            }
        }
        return days;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_salary_rule);
        RadioButton radioButtonPositive = findViewById(R.id.radioButtonPositiveSalaryRule);
        final RadioButton radioButtonNegative = findViewById(R.id.radioButtonNegativeSalaryRule);
        radioButtonPositive.toggle();
        CheckBox checkBoxMonday = findViewById(R.id.checkBoxMonday);
        CheckBox checkBoxTuesday = findViewById(R.id.checkBoxTuesday);
        CheckBox checkBoxWednesday = findViewById(R.id.checkBoxWednesday);
        CheckBox checkBoxThursday = findViewById(R.id.checkBoxThursday);
        CheckBox checkBoxFriday = findViewById(R.id.checkBoxFriday);
        CheckBox checkBoxSaturday = findViewById(R.id.checkBoxSaturday);
        CheckBox checkBoxSunday = findViewById(R.id.checkBoxSunday);
        CheckBox [] checkBoxes = {checkBoxMonday,checkBoxTuesday,checkBoxWednesday,checkBoxThursday,
                checkBoxFriday,checkBoxSaturday,checkBoxSunday};
        // Set active days
        ArrayList<Integer> activeDays = setDays(checkBoxes);
        Button submitButton = findViewById(R.id.buttonSubmitSalaryRule);
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText name = findViewById(R.id.editTextNameSalaryRule);
                EditText salaryRuleNum = findViewById(R.id.editTextSetSalaryRule);
                int sign = 1;
                if(radioButtonNegative.isChecked())
                    sign = -1;
                int pay = Integer.parseInt(salaryRuleNum.getText().toString()) * sign;
                LocalTime startTime = new LocalTime();
                SalaryRule salaryRule = new SalaryRule(name,);

            }
        });

    }
}
