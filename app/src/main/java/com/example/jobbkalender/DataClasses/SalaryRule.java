package com.example.jobbkalender.DataClasses;

import java.time.DayOfWeek;
import java.time.LocalTime;

public class SalaryRule {

    private String ruleName="Lønnstillegg";
    private double changeInPay;
    private  LocalTime startTime;
    private LocalTime endTime;
    private DayOfWeek [] daysOfWeek;

    SalaryRule(String name, double pay, LocalTime sTime, LocalTime eTime, DayOfWeek [] days){
        ruleName = name;
        changeInPay = pay;
        startTime = sTime;
        endTime = eTime;
        daysOfWeek = days;
    }
}
