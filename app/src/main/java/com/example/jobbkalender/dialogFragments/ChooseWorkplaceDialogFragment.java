package com.example.jobbkalender.dialogFragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.RecyclerView;

import com.example.jobbkalender.CreateEvent;
import com.example.jobbkalender.CreateJobActivity;
import com.example.jobbkalender.MainActivity;
import com.example.jobbkalender.R;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class ChooseWorkplaceDialogFragment extends DialogFragment {

    public ChooseWorkplaceDialogFragment () {
        // Need empty cosntructor
    }
    public interface OnInputListener{
        void sendWorkplace(String string);
    }
    public ChooseWorkplaceDialogFragment.OnInputListener mOnInputListener;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState){
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setPositiveButton("Legg til jobb", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(getContext(),CreateJobActivity.class);
                startActivity(intent);
                dialog.cancel();
            }
        });
        builder.setNegativeButton("Lukk", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        final String [] arr = {"Meny","Coop","Dassen"};
        builder.setTitle("Velg arbeidsplass")
                .setItems(arr, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        final String selectedItem = arr[which];
                       Toast toast = Toast.makeText(getContext(), selectedItem,Toast.LENGTH_SHORT);
                       toast.show();
                        mOnInputListener.sendWorkplace(selectedItem);
                    }
                });

        return builder.create();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try{
            mOnInputListener = (ChooseWorkplaceDialogFragment.OnInputListener) getActivity();
        }catch (ClassCastException e){
            Log.e("onAttach", "On attach exception!");
        }
    }
}
