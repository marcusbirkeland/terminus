package com.example.jobbkalender.dialogFragments;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.widget.TimePicker;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import java.util.Calendar;


public class TimePickerDialogFragment extends DialogFragment implements TimePickerDialog.OnTimeSetListener {
    public TimePickerDialogFragment() {
        // Required empty public constructor
    }
    public interface OnInputListener{
        void sendTime(String string);
    }
    public OnInputListener mOnInputListener;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the current time as the default values for the picker

        final Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        // Create a new instance of TimePickerDialog and return it
        return new TimePickerDialog(getActivity(), this, hour, minute,
                DateFormat.is24HourFormat(getActivity()));
    }

    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        String hourStr = "" + hourOfDay;
        String minuteStr = "" + minute;
        if (hourOfDay == 0)
            hourStr = "00";
        if (minute == 0)
            minuteStr = "00";
        String time = hourStr + ":" + minuteStr;
        mOnInputListener.sendTime(time);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try{
            mOnInputListener = (OnInputListener) getActivity();
        }catch (ClassCastException e){
            Log.e("onAttach", "On attach exception!");
        }
    }
}
