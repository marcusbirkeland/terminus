package com.birkeland.terminus;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    public static final int CREATE_ALARM = 45456;
    public static final int DELETE_EVENT = 2321;
    public static final int NORWEGIAN = 1814;
    public static final int ENGLISH = 1776;
    public boolean isDarkMode;

    private int loadLanguage(){
        SharedPreferences sharedPreferences = getSharedPreferences("LOCALE",MODE_PRIVATE);
        return sharedPreferences.getInt("LANGUAGE", 0);
    }
    private void setLanguage(String countryCode){
        Resources resources = getResources();
        DisplayMetrics displayMetrics = resources.getDisplayMetrics();
        Configuration configuration = resources.getConfiguration();
        configuration.setLocale(new Locale(countryCode.toLowerCase()));
        resources.updateConfiguration(configuration, displayMetrics);
        configuration.locale = new Locale(countryCode.toLowerCase());
        resources.updateConfiguration(configuration, displayMetrics);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent i = new Intent(this, NotificationService.class);
        startService(i);
        Log.d("LANGUAGE",Locale.getDefault().getDisplayCountry().toString());
        int language = loadLanguage();
        if(language == NORWEGIAN){
            setLanguage("nb");
        }else if (language == ENGLISH){
            setLanguage("en-rUS");
        }
        isDarkMode = getIsDarkMode();
        if(isDarkMode){
            setTheme(R.style.AppThemeDark);
        }else{
            setTheme(R.style.AppTheme);
        }
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        if(getCurrency().equals("")){
            createCurrencyDialog().show();
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        }
        if(ContextCompat.checkSelfPermission(this,Manifest.permission.RECEIVE_BOOT_COMPLETED)
                != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.RECEIVE_BOOT_COMPLETED},1);
        }
        setContentView(R.layout.activity_main);
        if(isDarkMode){
            ImageView logo = findViewById(R.id.imageViewLogo);
            logo.setImageResource(R.drawable.terminus_name_white);
        }

        BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_calendar, R.id.navigation_earnings, R.id.navigation_notifications)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);
    }
    private boolean getIsDarkMode(){
        SharedPreferences pref = this.getSharedPreferences("DARKMODE",MODE_PRIVATE);
        return  pref.getBoolean("isDarkMode",false);
    }
    private Dialog createCurrencyDialog(){
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final View dialogView = View.inflate(this,R.layout.currency_dialog,null);
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

    private String getCurrency(){
        SharedPreferences pref = getSharedPreferences("LOCALE",MODE_PRIVATE);
        return pref.getString("CURRENCY","");
    }
    private void saveCurrency(String currency){
        SharedPreferences pref = getSharedPreferences("LOCALE", MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString("CURRENCY",currency);
        editor.apply();
    }
}
