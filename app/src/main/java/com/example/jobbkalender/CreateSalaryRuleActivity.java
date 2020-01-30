package com.example.jobbkalender;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import java.time.LocalTime;

import com.example.jobbkalender.DataClasses.SalaryRule;
import com.example.jobbkalender.dialogFragments.TimePickerDialogFragment;

import java.time.DayOfWeek;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class CreateSalaryRuleActivity extends AppCompatActivity implements TimePickerDialogFragment.OnInputListener {

    private int editTextToChange = 0;
    private boolean editMode;
    private SalaryRule salaryRuleIn;
    TimePickerDialogFragment timePickerDialogFragment = new TimePickerDialogFragment();


    ArrayList<DayOfWeek> getCheckedDays (){
        final ArrayList<DayOfWeek> checkedDays= new ArrayList<>();
        CheckBox checkBoxWeekdays = findViewById(R.id.checkBoxWeekdays);
        if (checkBoxWeekdays.isChecked()) {
            checkedDays.add(DayOfWeek.MONDAY);
            checkedDays.add(DayOfWeek.TUESDAY);
            checkedDays.add(DayOfWeek.WEDNESDAY);
            checkedDays.add(DayOfWeek.THURSDAY);
            checkedDays.add(DayOfWeek.FRIDAY);
        }
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
        final TextView textViewTimeFrom = findViewById(R.id.timeInputFromCreateSalaryRule);
        final TextView textViewTimeTo = findViewById(R.id.timeInputToCreateSalaryRule);
        final EditText editTextName = findViewById(R.id.editTextNameSalaryRule);
        final EditText editTextSalaryRuleNum = findViewById(R.id.editTextSetSalaryRule);
        final CheckBox checkBoxWeekdays = findViewById(R.id.checkBoxWeekdays);
        final CheckBox checkBoxSaturday = findViewById(R.id.checkBoxSaturday);
        final CheckBox checkBoxSunday = findViewById(R.id.checkBoxSunday);

        Bundle bundle = getIntent().getBundleExtra("BUNDLE");
        try{
            Log.e("FORTNITE","!!!!");
            if(bundle.getBoolean("EDITMODE")){
                editMode = true;
                salaryRuleIn = (SalaryRule) bundle.getSerializable("SALARYRULE");
                editTextName.setText(salaryRuleIn.getRuleName());
                editTextSalaryRuleNum.setText(salaryRuleIn.getChangeInPay()+ "");
                textViewTimeFrom.setText(salaryRuleIn.getStartTime());
                textViewTimeTo.setText(salaryRuleIn.getEndTime());
                if(salaryRuleIn.getDaysOfWeek().contains(DayOfWeek.MONDAY)){
                    checkBoxWeekdays.setChecked(true);
                }
                if(salaryRuleIn.getDaysOfWeek().contains(DayOfWeek.SATURDAY)){
                    checkBoxSaturday.setChecked(true);
                }
                if(salaryRuleIn.getDaysOfWeek().contains(DayOfWeek.SUNDAY)){
                    checkBoxSunday.setChecked(true);
                }
            }
        } catch (NullPointerException e){
            Log.e("NULL", "No bundle in createSalaryRuleActivity!");
        }

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

                if(checkedDays.size()==0 ){
                    Log.d("Error","Please check off a day");
                    return;
                }
                if(editTextName.getText().toString().matches("") || editTextName.getText().toString().equals( "")){
                    Log.d("Error","Please fill name!");
                    return;
                }
                if( editTextSalaryRuleNum.getText().toString().matches("") || editTextSalaryRuleNum.getText().toString().equals("")){
                    Log.d("Error","Please fill salary rule!");
                    return;
                }
                DateTimeFormatter dateTimeFormatter =DateTimeFormatter.ISO_LOCAL_TIME;
                String name = editTextName.getText().toString();
                double pay = Double.parseDouble(editTextSalaryRuleNum.getText().toString());

                LocalTime startTime = LocalTime.parse(textViewTimeFrom.getText().toString(), dateTimeFormatter.ofPattern("HH:mm"));
                LocalTime endTime = LocalTime.parse(textViewTimeTo.getText().toString() , dateTimeFormatter.ofPattern("HH:mm"));
                if(startTime.isAfter(endTime)){
                    Log.d("Error", "Rule cant end before it starts!");
                    return;
                }
                SalaryRule salaryRule = new SalaryRule(name,pay,startTime.format(dateTimeFormatter.ofPattern("HH:mm")),endTime.format(dateTimeFormatter.ofPattern("HH:mm")),checkedDays);
                Intent intent = new Intent();
                Bundle extras = new Bundle();
                extras.putSerializable("SALARYRULE", salaryRule);
                if(editMode){
                    extras.putSerializable("OLDRULE",salaryRuleIn);
                    extras.putBoolean("WAS_EDITED",true);
                }
                intent.putExtras(extras);
                setResult(RESULT_OK,intent);
                finish();
            }
        });
    }
}
