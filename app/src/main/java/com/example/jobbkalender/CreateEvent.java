package com.example.jobbkalender;

import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.example.jobbkalender.dialogFragments.TimePickerDialogFragment;

public class CreateEvent extends AppCompatActivity implements TimePickerDialogFragment.OnInputListener {

    int editTextToChange = 0;
    TimePickerDialogFragment tp = new TimePickerDialogFragment();
    @Override
    public void sendInput(String input) {
        Log.d("Set time: ", input);
        if(editTextToChange == 1){
            TextView timeInput = findViewById(R.id.timeInputFrom);
            timeInput.setText(input);
        } else if(editTextToChange == 2){
            TextView timeInput = findViewById(R.id.timeInputTo);
            timeInput.setText(input);
        }
    }
    void showDialog() {
        tp.show(getSupportFragmentManager(), "Velg tidspunkt:");
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
                showDialog();
                editTextToChange = 1;
            }
        });
        timeInputTo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog();
                editTextToChange = 2;
            }
        });
    }
}
