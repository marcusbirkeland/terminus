package com.birkeland.terminus.DataClasses;

import android.content.Context;
import android.content.SharedPreferences;

import com.birkeland.terminus.R;

import java.io.Serializable;
import java.time.DayOfWeek;
import java.util.ArrayList;

import static android.content.Context.MODE_PRIVATE;

public class SalaryRule implements Serializable {

    private String ruleName="Lønnstillegg";
    private double changeInPay;
    private  String startTime;
    private String endTime;
    private ArrayList<DayOfWeek> daysOfWeek;

    public SalaryRule(String name, double pay, String sTime, String eTime, ArrayList<DayOfWeek> days){
        ruleName = name;
        changeInPay = pay;
        startTime = sTime;
        endTime = eTime;
        daysOfWeek = days;
    }
    public String getRuleName(){
        return ruleName;
    }
    public double getChangeInPay(){
        return changeInPay;
    }
    public String getStartTime(){
        return startTime;
    }
    public String getEndTime(){
        return endTime;
    }
    public ArrayList<DayOfWeek> getDaysOfWeek(){
        return daysOfWeek;
    }
    public String toString(Context context){
            SharedPreferences pref = context.getSharedPreferences("LOCALE",MODE_PRIVATE);
            String currency = pref.getString("CURRENCY","");

        String out;
        out =  ruleName.toUpperCase() +":  ";
        if(changeInPay > 0)
            out+="+";
        out += changeInPay + currency + '\n' +
                context.getString(R.string.from)+" "+startTime +
                " " + context.getString(R.string.to).toLowerCase()+ " " + endTime;
        if (daysOfWeek.contains(DayOfWeek.MONDAY)) {
            out+=" (" + context.getString(R.string.weekdays)+")";
        }
        if(daysOfWeek.contains(DayOfWeek.SATURDAY)){
            out+=" (" + context.getString(R.string.saturday)+")";
        }if (daysOfWeek.contains(DayOfWeek.SUNDAY)){
            out+=" (" +context.getString(R.string.sundays) + ")";
        }
        return  out;
    }

}
