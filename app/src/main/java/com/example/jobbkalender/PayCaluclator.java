package com.example.jobbkalender;

import android.util.Log;

import com.example.jobbkalender.DataClasses.SalaryRule;
import com.example.jobbkalender.DataClasses.WorkdayEvent;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class PayCaluclator {

    public PayCaluclator() {
        // NEED EMPTY CONSTRUCTOR
    }

    public int getTotalEarningsGross (List<WorkdayEvent> workdayEvents){
        double sum=0;
        for(WorkdayEvent event : workdayEvents){
            double salary = event.getJob().getSalary();
            LocalTime startTime = LocalTime.parse(event.getStartTime(), DateTimeFormatter.ofPattern("HH:mm"));
            LocalTime endTime = LocalTime.parse(event.getEndTime(), DateTimeFormatter.ofPattern("HH:mm"));

            List<SalaryRule> salaryRulesList = new ArrayList<>();
            for (SalaryRule rule : event.getJob().getSalaryRules()){
                for (DayOfWeek dayOfWeek : rule.getDaysOfWeek()){
                    if(dayOfWeek.toString().equals(event.getDayOfWeek())){
                        salaryRulesList.add(rule);
                        Log.d("ADD", "Salary Rule added");
                    }
                }
            }
            // Hopp over pause dersom den ikke er betalt
            if(!event.getJob().hasPaidBreak()){
                startTime = startTime.plusMinutes(event.getBreakTime());
            }
            // Itererer gjennom hvert minutt av arbeidsdagen og finner ut lønn
            while(startTime.isBefore(endTime)){
                // Sjekk lønnsregler for gjeldende minutt. Finn kr/min og legg til i gjeldende minuttlønn.
                double currentSalary = salary;
                for(SalaryRule salaryRule : salaryRulesList){
                    LocalTime ruleStartTime = LocalTime.parse(salaryRule.getStartTime(), DateTimeFormatter.ofPattern("HH:mm"));
                    LocalTime ruleEndTime = LocalTime.parse(salaryRule.getEndTime(), DateTimeFormatter.ofPattern("HH:mm"));
                    if(startTime.isAfter(ruleStartTime) && startTime.isBefore(ruleEndTime)){
                        currentSalary+= salaryRule.getChangeInPay();
                    }
                }
                // Legg til minuttlønn
                sum+= currentSalary/60;
                startTime = startTime.plusMinutes(1);
            }
        }
        return (int)sum;
    }
}
