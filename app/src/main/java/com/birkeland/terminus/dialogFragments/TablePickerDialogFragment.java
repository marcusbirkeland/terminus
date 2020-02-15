package com.birkeland.terminus.dialogFragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.fragment.app.DialogFragment;

import com.birkeland.terminus.DataClasses.Job;
import com.birkeland.terminus.R;

import java.io.IOException;

public class TablePickerDialogFragment extends DialogFragment {

    public TablePickerDialogFragment() {
    }

    private String[] reverseArray(String[] arrIn){
        String[] reversedArray = arrIn.clone();
        for(int i = 0;i < reversedArray.length;i++){
            int j = reversedArray.length -(1+ i);
            reversedArray[i] = arrIn[j];
        }
        return reversedArray;
    }
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        AssetManager assetManager = getContext().getAssets();
        String[] tableIDs;
        try {
            tableIDs = assetManager.list("tabellene2020");
            int i = 0;
            for (String ID : tableIDs){
                tableIDs[i]= ID.substring(0,ID.length()-4);
                i++;
            }
            final String [] tableArray = reverseArray(tableIDs);
            builder.setTitle(getString(R.string.choose_tax_table))
                    .setItems(tableArray, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Log.d("Table picker","Table " + tableArray[which] + " selected");
                            Intent intent = new Intent();
                            intent.putExtra("Table",tableArray[which]);
                            getTargetFragment().onActivityResult(getTargetRequestCode(),0,intent);
                        }
                    })
                    .setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // Cancel
                        }
                    });
        }
        catch (IOException e){
            Log.e("Table picker","No files in assets");
        }
        return builder.create();
    }
}
