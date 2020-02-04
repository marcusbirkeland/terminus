package com.birkeland.terminus.ui.settings;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputFilter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.birkeland.terminus.DataClasses.Setting;
import com.birkeland.terminus.MainActivity;
import com.birkeland.terminus.R;
import com.birkeland.terminus.Adapters.SettingsAdapter;
import com.birkeland.terminus.ViewAllEventsActivity;
import com.birkeland.terminus.ViewAllJobsActivity;

import java.util.ArrayList;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;

public class SettingsFragment extends Fragment {

    private SettingsViewModel settingsViewModel;
    private List<Setting> settingsListAdminister = new ArrayList<>();
    private List<Setting> settingsListPrimary = new ArrayList<>();
    private boolean getIsDarkMode (){
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("DARKMODE",MODE_PRIVATE);
        return sharedPreferences.getBoolean("isDarkMode",false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ListView listViewSettings = view.findViewById(R.id.listViewSettings);
        ListView listViewSettingsAdminister = view.findViewById(R.id.listViewAdminister);

        Setting setting = new Setting(getString(R.string.about), android.R.drawable.ic_menu_info_details);
        settingsListPrimary.add(setting);
        setting = new Setting(getString(R.string.choose_language),android.R.drawable.ic_menu_manage);
        settingsListPrimary.add(setting);
        setting = new Setting(getString(R.string.choose_currency),android.R.drawable.ic_menu_manage);
        settingsListPrimary.add(setting);

        SettingsAdapter settingsAdapterPrimary = new SettingsAdapter(getContext(),0,settingsListPrimary);
        listViewSettings.setAdapter(settingsAdapterPrimary);
        listViewSettings.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position){
                    case 0 :
                        Log.d("Settings","Showing about");
                        Dialog aboutDialog = createAboutAlert();
                        aboutDialog.show();
                        break;
                    case 1 :
                        Log.d("Settings","Showing language picker");
                        break;
                    case 2:
                        Log.d("Settings","Showing currency picker");
                        Dialog currencyDialog = createCurrencyDialog();
                        currencyDialog.show();
                        break;
                        default:
                            Log.e("Settings","Listview item out of range!");
                            break;
                }
            }
        });

        setting = new Setting(getString(R.string.administer_jobs), android.R.drawable.ic_menu_my_calendar);
        settingsListAdminister.add(setting);
        setting = new Setting(getString(R.string.administer_shifts),android.R.drawable.ic_menu_day);
        settingsListAdminister.add(setting);
        setting = new Setting(getString(R.string.delete_all), android.R.drawable.ic_menu_delete);
        settingsListAdminister.add(setting);

        SettingsAdapter settingsAdapter= new SettingsAdapter(getContext(),0, settingsListAdminister);
        listViewSettingsAdminister.setAdapter(settingsAdapter);
        listViewSettingsAdminister.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position){
                    case 0:
                        Log.d("Settings","Showing administer jobs");
                        Intent viewAllJobs = new Intent(getActivity(), ViewAllJobsActivity.class);
                        startActivity(viewAllJobs);
                        break;
                    case 1:
                        Log.d("Settings", "Showing adminster events");
                        Intent viewAllEvents = new Intent(getActivity(), ViewAllEventsActivity.class);
                        startActivity(viewAllEvents);
                        break;
                    case 2:
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

    private Dialog createCurrencyDialog(){
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        final View dialogView = View.inflate(getContext(),R.layout.currency_dialog,null);
        builder.setMessage(getString(R.string.currency_dialog_text))
                .setView(dialogView)
                .setPositiveButton(getString(R.string.save),new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        EditText editText = dialogView.findViewById(R.id.editTextCurrencyDialog);
                        String currency = editText.getText().toString();
                        Log.d("Currency Dialog", currency);
                        if(!currency.equals("")){
                            saveCurrency(editText.getText().toString());
                        }else {
                            Log.d("Currency Dialog","Please enter currency!");
                        }
                    }
                }).setNegativeButton("", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Avbryt
            }
        });
        return builder.create();
    }

    private void saveCurrency(String currency){
        SharedPreferences pref = getActivity().getSharedPreferences("LOCALE", MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString("CURRENCY",currency);
        editor.apply();
    }
    private String loadCurrency(){
        SharedPreferences pref = getActivity().getSharedPreferences("LOCALE", MODE_PRIVATE);
        return pref.getString("CURRENCY","");
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

