package com.example.jobbkalender.DataClasses;

import java.time.DayOfWeek;
import java.time.LocalTime;

public class SalaryRule {

    private String ruleName="Lønnstillegg";
    private  LocalTime startTime;
    private LocalTime endTime;
    private int [] daysOfWeek;
    private double changeInPay;

    SalaryRule(String name, LocalTime sTime, LocalTime eTime, int[] days, double pay){
        ruleName = name;
        startTime = sTime;
        endTime = eTime;
        daysOfWeek = days;
        changeInPay = pay;
    }
}
