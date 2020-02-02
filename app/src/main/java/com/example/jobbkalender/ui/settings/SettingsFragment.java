package com.example.jobbkalender.ui.settings;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.example.jobbkalender.DataClasses.Setting;
import com.example.jobbkalender.R;
import com.example.jobbkalender.Adapters.SettingsAdapter;
import com.example.jobbkalender.ViewAllEventsActivity;
import com.example.jobbkalender.ViewAllJobsActivity;

import java.util.ArrayList;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;

public class SettingsFragment extends Fragment {

    private SettingsViewModel settingsViewModel;
    private List<Setting> settingsList= new ArrayList<>();

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ListView listView = view.findViewById(R.id.listViewSettings);

        Setting setting = new Setting("Om appen", android.R.drawable.ic_menu_info_details);
        settingsList.add(setting);
        setting = new Setting("Administer jobber", android.R.drawable.ic_menu_my_calendar);
        settingsList.add(setting);
        setting = new Setting("Administer vakter",android.R.drawable.ic_menu_day);
        settingsList.add(setting);
        setting = new Setting("Slett all data", android.R.drawable.ic_menu_delete);
        settingsList.add(setting);
        SettingsAdapter settingsAdapter= new SettingsAdapter(getContext(),0,settingsList);
        listView.setAdapter(settingsAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position){
                    case 0:
                        Log.d("Settings", "Showing about");
                        Dialog aboutDialog = createAboutAlert();
                        aboutDialog.show();
                        break;
                    case 1:
                        Log.d("Settings","Showing administer jobs");
                        Intent viewAllJobs = new Intent(getActivity(), ViewAllJobsActivity.class);
                        startActivity(viewAllJobs);
                        break;
                    case 2:
                        Log.d("Settings", "Showing adminster events");
                        Intent viewAllEvents = new Intent(getActivity(), ViewAllEventsActivity.class);
                        startActivity(viewAllEvents);
                        break;
                    case 3:
                        Log.d("Settings", "Showing delete data fragment");
                        Dialog dialog = createDeleteDataAlert();
                        dialog.show();
                        break;

                        default:
                            Log.e("Settings","Clicked item out of range");
                            break;
                }
            }
        });
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        settingsViewModel =
                ViewModelProviders.of(this).get(SettingsViewModel.class);
        View root = inflater.inflate(R.layout.fragment_settings, container, false);
        final TextView textView = root.findViewById(R.id.text_notifications);
        settingsViewModel.getText().observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });
        return root;
    }

    private Dialog createDeleteDataAlert (){
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(" Vil du virkelig slette alle jobber og vakter?")
                .setPositiveButton("Ja, slett alt", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // Sletter all brukerdata
                        SharedPreferences pref = getActivity().getSharedPreferences("SHARED PREFERENCES", MODE_PRIVATE);
                        SharedPreferences.Editor editor = pref.edit();
                        editor.clear();
                        editor.commit();
                        //TODO slett alle bilder også
                    }
                })
                .setNegativeButton("Avbryt", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // Do nothing
                    }
                });
        return builder.create();
    }
    private Dialog createAboutAlert(){
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage("Laget av: Marcus B. Birkeland \n" +
                "v.1.0");
        return builder.create();
    }
}
