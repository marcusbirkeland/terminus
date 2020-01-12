package com.example.jobbkalender.DataClasses;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;

public class SalaryRule implements Serializable {

    private String ruleName="Lønnstillegg";
    private double changeInPay;
    private  LocalTime startTime;
    private LocalTime endTime;
    private ArrayList<DayOfWeek> daysOfWeek;

    public SalaryRule(String name, double pay, LocalTime sTime, LocalTime eTime, ArrayList<DayOfWeek> days){
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
    public LocalTime getStartTime(){
        return startTime;
    }
    public LocalTime getEndTime(){
        return endTime;
    }
    public ArrayList<DayOfWeek> getDaysOfWeek(){
        return daysOfWeek;
    }
    public String toString(){
        String out;
        out =  ruleName.toUpperCase() +" : " + "+ " + changeInPay + ",-" +
                " fra "+startTime.toString() +
                " til " + endTime.toString();
        return  out;
    }

}
