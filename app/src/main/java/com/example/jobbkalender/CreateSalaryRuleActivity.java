package com.example.jobbkalender;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;

import java.sql.Time;
import java.time.LocalTime;

import com.example.jobbkalender.DataClasses.SalaryRule;
import com.example.jobbkalender.dialogFragments.ChooseWorkplaceDialogFragment;
import com.example.jobbkalender.dialogFragments.TimePickerDialogFragment;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class CreateSalaryRuleActivity extends AppCompatActivity implements TimePickerDialogFragment.OnInputListener {

    private int editTextToChange = 0;
    TimePickerDialogFragment timePickerDialogFragment = new TimePickerDialogFragment();


    ArrayList<DayOfWeek> getCheckedDays (){
        final ArrayList<DayOfWeek> checkedDays= new ArrayList<>();
        CheckBox checkBoxMonday = findViewById(R.id.checkBoxMonday);
        if (checkBoxMonday.isChecked())
            checkedDays.add(DayOfWeek.MONDAY);
        CheckBox checkBoxTuesday = findViewById(R.id.checkBoxTuesday);
        if (checkBoxTuesday.isChecked())
            checkedDays.add(DayOfWeek.TUESDAY);
        CheckBox checkBoxWednesday = findViewById(R.id.checkBoxWednesday);
        if (checkBoxWednesday.isChecked())
            checkedDays.add(DayOfWeek.WEDNESDAY);
        CheckBox checkBoxThursday = findViewById(R.id.checkBoxThursday);
        if (checkBoxThursday.isChecked())
            checkedDays.add(DayOfWeek.THURSDAY);
        CheckBox checkBoxFriday = findViewById(R.id.checkBoxFriday);
        if (checkBoxFriday.isChecked())
            checkedDays.add(DayOfWeek.FRIDAY);
        CheckBox checkBoxSaturday = findViewById(R.id.checkBoxSaturday);
        if (checkBoxSaturday.isChecked())
            checkedDays.add(DayOfWeek.SATURDAY);
        CheckBox checkBoxSunday = findViewById(R.id.checkBoxSunday);
        if (checkBoxSunday.isChecked())
            checkedDays.add(DayOfWeek.SUNDAY);
        return checkedDays;
    }
    @Override
    public void sendTime(String input) {
        Log.d("Set time: ", input);
        if(editTextToChange == 1){
            TextView timeInput = findViewById(R.id.timeInputFromCreateSalaryRule);
            timeInput.setText(input);
        } else if(editTextToChange == 2){
            TextView timeInput = findViewById(R.id.timeInputToCreateSalaryRule);
            timeInput.setText(input);
        }
    }
    void showTimePickerDialog() {

        if(timePickerDialogFragment.isAdded())
            return; // Prevent illegal state
        timePickerDialogFragment.show(getSupportFragmentManager(), "Pick time:");
        Log.d("Dialog: ", "Time picker opened");
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_salary_rule);
        TextView textViewTimeFrom = findViewById(R.id.timeInputFromCreateSalaryRule);
        TextView textViewTimeTo = findViewById(R.id.timeInputToCreateSalaryRule);
        textViewTimeFrom.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                showTimePickerDialog();
                editTextToChange = 1;
            }
        });
        textViewTimeTo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTimePickerDialog();
                editTextToChange = 2;
            }
        });
        Button submitButton = findViewById(R.id.buttonSubmitSalaryRule);
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                ArrayList<DayOfWeek> checkedDays = getCheckedDays();
                EditText editTextName = findViewById(R.id.editTextNameSalaryRule);
                EditText editTextSalaryRuleNum = findViewById(R.id.editTextSetSalaryRule);
                if(checkedDays.size()==0 ){
                    Log.d("Error","Please check off a day");
                    return;
                }
                if(editTextName.getText().toString().matches("") || editTextName.getText().toString() == ""){
                    Log.d("Error","Please fill name!");
                    return;
                }
                if( editTextSalaryRuleNum.getText().toString().matches("") || editTextSalaryRuleNum.getText().toString() == ""){
                    Log.d("Error","Please fill salary rule!");
                    return;
                }

                String name = editTextName.getText().toString();
                int pay = Integer.parseInt(editTextSalaryRuleNum.getText().toString());
                TextView textViewTimeFrom = findViewById(R.id.timeInputFromCreateSalaryRule);
                LocalTime startTime = LocalTime.parse(textViewTimeFrom.getText().toString());
                TextView textViewTimeTo = findViewById(R.id.timeInputToCreateSalaryRule);
                LocalTime endTime = LocalTime.parse(textViewTimeTo.getText().toString());
                for(int i = 0; i < checkedDays.toArray().length;i++){
                    Log.d("Days:", checkedDays.toArray()[i].toString()) ;
                }
                //SalaryRule salaryRule = new SalaryRule(name,);

            }
        });

    }
}
