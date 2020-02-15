package com.birkeland.terminus.dialogFragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import androidx.fragment.app.DialogFragment;

import com.birkeland.terminus.CreateJobActivity;
import com.birkeland.terminus.DataClasses.Job;
import com.birkeland.terminus.R;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;

public class ChooseWorkplaceDialogFragment extends DialogFragment {

    List<Job> jobList =new ArrayList<>();

    private void loadJobs(){
        SharedPreferences pref = getContext().getSharedPreferences("SHARED PREFERENCES", MODE_PRIVATE);
        Gson gson = new Gson();
        String json = pref.getString("JOBLIST",null);
        Log.d("JSON","Json read: " + json);
        Type type = new TypeToken<ArrayList<Job>>(){}.getType();
        try {
            jobList= gson.fromJson(json,type);
        } catch (Exception e){
            Log.e("Error","Failed to load jobs");
        }
    }

    public ChooseWorkplaceDialogFragment () {
        // Need empty cosntructor
    }
    public interface OnInputListener{
        void sendWorkplace(Job job);
    }
    private ChooseWorkplaceDialogFragment.OnInputListener mOnInputListener;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState){
        loadJobs();
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setPositiveButton(getString(R.string.new_job), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(getContext(), CreateJobActivity.class);
                startActivity(intent);
                dialog.cancel();
            }
        });
        final List<String> jobNames= new ArrayList<>();
        if( jobList!= null) {
        for (Job j: jobList
             ) {
            jobNames.add(j.getName());
        }
        String[] jobNamesArray = jobNames.toArray(new String[0]);

            builder.setTitle(getString(R.string.pick_job))
                    .setItems(jobNamesArray, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            final Job selectedItem = jobList.get(which);
                            mOnInputListener.sendWorkplace(selectedItem);
                        }
                    });
        } else {
            String [] emptyMessage = {getString(R.string.add_new_job)};
            builder.setTitle(getString(R.string.pick_job))
                    .setItems(emptyMessage, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent(getContext(),CreateJobActivity.class);
                            startActivity(intent);
                            dialog.cancel();
                        }
                    });
        }
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
