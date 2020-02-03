package com.birkeland.terminus.ui.settings;

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
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.birkeland.terminus.DataClasses.Setting;
import com.birkeland.terminus.R;
import com.birkeland.terminus.Adapters.SettingsAdapter;
import com.birkeland.terminus.ViewAllEventsActivity;
import com.birkeland.terminus.ViewAllJobsActivity;

import java.util.ArrayList;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;

public class SettingsFragment extends Fragment {

    private SettingsViewModel settingsViewModel;
    private List<Setting> settingsList= new ArrayList<>();
    private boolean getIsDarkMode (){
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("DARKMODE",MODE_PRIVATE);
        return sharedPreferences.getBoolean("isDarkMode",false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ListView listView = view.findViewById(R.id.listViewSettings);

        Setting setting = new Setting(getString(R.string.about), android.R.drawable.ic_menu_info_details);
        settingsList.add(setting);
        setting = new Setting(getString(R.string.administer_jobs), android.R.drawable.ic_menu_my_calendar);
        settingsList.add(setting);
        setting = new Setting(getString(R.string.administer_shifts),android.R.drawable.ic_menu_day);
        settingsList.add(setting);
        setting = new Setting(getString(R.string.delete_all), android.R.drawable.ic_menu_delete);
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
        final Switch switchNightmode = view.findViewById(R.id.switchNightMode);
        switchNightmode.setChecked(getIsDarkMode());
        switchNightmode.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                setDarkMode(isChecked);
                restartApp();
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
        builder.setMessage(getString(R.string.delte_dialog))
                .setPositiveButton(getString(R.string.confirm_delete_all), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // Sletter all brukerdata
                        SharedPreferences pref = getActivity().getSharedPreferences("SHARED PREFERENCES", MODE_PRIVATE);
                        SharedPreferences.Editor editor = pref.edit();
                        editor.clear();
                        editor.commit();
                        //TODO slett alle bilder også
                    }
                })
                .setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // Do nothing
                    }
                });
        return builder.create();
    }
    private Dialog createAboutAlert(){
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(getString(R.string.about_info) +  "\n" +
                getString(R.string.version));
        return builder.create();
    }

    private void setDarkMode(boolean state){
        SharedPreferences pref = getActivity().getSharedPreferences("DARKMODE", MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean("isDarkMode",state);
        editor.apply();
    }
    private void restartApp(){
        Intent i = getActivity().getPackageManager().
                getLaunchIntentForPackage(getActivity().getPackageName());
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);
    }
}

