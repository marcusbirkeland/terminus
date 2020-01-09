package com.example.jobbkalender;

import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.jobbkalender.dialogFragments.ChooseWorkplaceDialogFragment;
import com.example.jobbkalender.dialogFragments.TimePickerDialogFragment;

import org.w3c.dom.Text;

public class CreateEvent extends AppCompatActivity implements TimePickerDialogFragment.OnInputListener, ChooseWorkplaceDialogFragment.OnInputListener {

    int editTextToChange = 0;
    TimePickerDialogFragment timePickerDialogFragment = new TimePickerDialogFragment();
    ChooseWorkplaceDialogFragment chooseWorkplaceDialogFragment = new ChooseWorkplaceDialogFragment();

    @Override
    public void sendTime(String input) {
        Log.d("Set time: ", input);
        if(editTextToChange == 1){
            TextView timeInput = findViewById(R.id.timeInputFrom);
            timeInput.setText(input);
        } else if(editTextToChange == 2){
            TextView timeInput = findViewById(R.id.timeInputTo);
            timeInput.setText(input);
        }
    }
    @Override
    public void sendWorkplace(String workplace){
        Log.d("Set workplace:", workplace);
        TextView t = findViewById(R.id.textViewSelectedWorkplaceCreateEvent);
        t.setText(workplace);
    }

    void showTimePickerDialog() {
        timePickerDialogFragment.show(getSupportFragmentManager(), "Pick time:");
        Log.d("Dialog: ", "Time picker opened");
    }
    void showChooseWorkplaceDialog(){
        chooseWorkplaceDialogFragment.show(getSupportFragmentManager(), "Choose workplace: ");
        Log.d("Dialog","Choose workplace opened");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_event);
        TextView timeInputFrom = findViewById(R.id.timeInputFrom);
        TextView timeInputTo = findViewById(R.id.timeInputTo);
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

        Button addWorkplace = findViewById(R.id.buttonChooseWorkplaceCreateEvent);
        addWorkplace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showChooseWorkplaceDialog();
            }
        });
    }
}
