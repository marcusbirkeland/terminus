package com.example.jobbkalender;

import android.icu.util.LocaleData;
import android.util.Log;

import com.example.jobbkalender.DataClasses.Job;
import com.example.jobbkalender.DataClasses.SalaryRule;
import com.example.jobbkalender.DataClasses.WorkdayEvent;
import com.nostra13.universalimageloader.utils.L;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class PayCalculator {

    private String startDateStr;
    private String endDateStr;
    public PayCalculator(List<WorkdayEvent> workdayEvents) {}

    public String getStartDateStr() {
        return startDateStr;
    }

    public String getEndDateStr() {
        return endDateStr;
    }

    public int getYearlyEarnings(List<WorkdayEvent> workdayEvents){
        double sum = 0;
        LocalDate now = LocalDate.now();
        try {
            for (WorkdayEvent event : workdayEvents) {
                LocalDate eventDate = LocalDate.parse(event.getDate(), DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                // Hopp over event dersom den ikke er i nåværende år.
                if(eventDate.getYear()!= now.getYear() || eventDate.isAfter(now))
                    continue;
                LocalTime startTime = LocalTime.parse(event.getStartTime(), DateTimeFormatter.ofPattern("HH:mm"));
                LocalTime endTime = LocalTime.parse(event.getEndTime(), DateTimeFormatter.ofPattern("HH:mm"));

                List<SalaryRule> salaryRulesList = new ArrayList<>();
                for (SalaryRule rule : event.getJob().getSalaryRules()) {
                    for (DayOfWeek dayOfWeek : rule.getDaysOfWeek()) {
                        if (dayOfWeek.toString().equals(event.getDayOfWeek())) {
                            salaryRulesList.add(rule);
                            Log.d("ADD", "Salary Rule added");
                        }
                    }
                }
                // Hopp over pause dersom den ikke er betalt
                if (!event.getJob().hasPaidBreak()) {
                    startTime = startTime.plusMinutes(event.getBreakTime());
                }
                double salary = event.getJob().getSalary();
                // Itererer gjennom hvert minutt av arbeidsdagen og finner ut lønn
                while (startTime.isBefore(endTime)) {
                    // Sjekk lønnsregler for gjeldende minutt. Finn kr/min og legg til i gjeldende minuttlønn.
                    double currentSalary = salary;
                    for (SalaryRule salaryRule : salaryRulesList) {
                        LocalTime ruleStartTime = LocalTime.parse(salaryRule.getStartTime(), DateTimeFormatter.ofPattern("HH:mm"));
                        LocalTime ruleEndTime = LocalTime.parse(salaryRule.getEndTime(), DateTimeFormatter.ofPattern("HH:mm"));
                        if (startTime.isAfter(ruleStartTime) && startTime.isBefore(ruleEndTime)) {
                            currentSalary += salaryRule.getChangeInPay();
                        }
                    }
                    // Legg til minutlønn i sum
                    sum += currentSalary / 60;
                    startTime = startTime.plusMinutes(1);
                }
            }
        }catch (NullPointerException e){
            Log.e("Null","No workday events in list");
        }
        return (int) sum;
    }

    public int getEarnings(List<WorkdayEvent> workdayEvents){
        double sum=0;
        try {
            for (WorkdayEvent event : workdayEvents) {
                LocalTime startTime = LocalTime.parse(event.getStartTime(), DateTimeFormatter.ofPattern("HH:mm"));
                LocalTime endTime = LocalTime.parse(event.getEndTime(), DateTimeFormatter.ofPattern("HH:mm"));

                List<SalaryRule> salaryRulesList = new ArrayList<>();
                for (SalaryRule rule : event.getJob().getSalaryRules()) {
                    for (DayOfWeek dayOfWeek : rule.getDaysOfWeek()) {
                        if (dayOfWeek.toString().equals(event.getDayOfWeek())) {
                            salaryRulesList.add(rule);
                            Log.d("ADD", "Salary Rule added");
                        }
                    }
                }
                // Hopp over pause dersom den ikke er betalt
                if (!event.getJob().hasPaidBreak()) {
                    startTime = startTime.plusMinutes(event.getBreakTime());
                }
                double salary = event.getJob().getSalary();
                // Itererer gjennom hvert minutt av arbeidsdagen og finner ut lønn
                while (startTime.isBefore(endTime)) {
                    // Sjekk lønnsregler for gjeldende minutt. Finn kr/min og legg til i gjeldende minuttlønn.
                    double currentSalary = salary;
                    for (SalaryRule salaryRule : salaryRulesList) {
                        LocalTime ruleStartTime = LocalTime.parse(salaryRule.getStartTime(), DateTimeFormatter.ofPattern("HH:mm"));
                        LocalTime ruleEndTime = LocalTime.parse(salaryRule.getEndTime(), DateTimeFormatter.ofPattern("HH:mm"));
                        if (startTime.isAfter(ruleStartTime) && startTime.isBefore(ruleEndTime)) {
                            currentSalary += salaryRule.getChangeInPay();
                        }
                    }
                    // Legg til minutlønn i sum
                    sum += currentSalary / 60;
                    startTime = startTime.plusMinutes(1);
                }
            }
        }catch (NullPointerException e){
            Log.e("Null","No workday events in list");
        }
        return (int) sum;
    }

    private String formatDate(LocalDate date){
        return  date.getDayOfMonth() + "." + date.getMonthValue();
    }

    public int getMonthlyEarnings(List<WorkdayEvent> workdayEvents, Job selectedJob) {
        if(selectedJob == null){
            return 0;
        }
        double monthlyPay = 0;
        LocalDate now = LocalDate.now();
        LocalDate checkDate = LocalDate.of(now.getYear(), now.getMonthValue(), selectedJob.getSalaryPeriodDate());
        try {
            for (WorkdayEvent event : workdayEvents) {
                LocalDate eventDate = LocalDate.parse(event.getDate(), DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                LocalDate startDate;
                LocalDate endDate;
                if(now.isBefore(checkDate)){
                    startDate = checkDate.minusMonths(1);
                    endDate = checkDate;
                }else{
                    startDate= checkDate;
                    endDate = checkDate.plusMonths(1);
                }
                startDateStr = formatDate(startDate);
                endDateStr = formatDate(endDate);
                Log.d("Calculating on date","From " + startDate.toString() + " to " + endDate.toString());
                // Hopp over utregning for andre jobber enn valgt jobb, og for andre månder
                if(!event.getJob().getName().equals(selectedJob.getName()) ||
                eventDate.isBefore(startDate) || eventDate.isAfter(endDate)){
                    continue;
                }
                List<SalaryRule> salaryRulesList = new ArrayList<>();
                for (SalaryRule rule : event.getJob().getSalaryRules()) {
                    for (DayOfWeek dayOfWeek : rule.getDaysOfWeek()) {
                        if (dayOfWeek.toString().equals(event.getDayOfWeek())) {
                            salaryRulesList.add(rule);
                            Log.d("ADD", "Salary Rule added");
                        }
                    }
                }
                double salary = event.getJob().getSalary();
                LocalTime startTime = LocalTime.parse(event.getStartTime(), DateTimeFormatter.ofPattern("HH:mm"));
                LocalTime endTime = LocalTime.parse(event.getEndTime(), DateTimeFormatter.ofPattern("HH:mm"));
                // Hopp over pause dersom den ikke er betalt
                if (!event.getJob().hasPaidBreak()) {
                    startTime = startTime.plusMinutes(event.getBreakTime());
                }
                // Itererer gjennom hvert minutt av arbeidsdagen og finner ut lønn
                while (startTime.isBefore(endTime)) {
                    // Sjekk lønnsregler for gjeldende minutt. Finn kr/min og legg til i gjeldende minuttlønn.
                    double currentSalary = salary;
                    for (SalaryRule salaryRule : salaryRulesList) {
                        LocalTime ruleStartTime = LocalTime.parse(salaryRule.getStartTime(), DateTimeFormatter.ofPattern("HH:mm"));
                        LocalTime ruleEndTime = LocalTime.parse(salaryRule.getEndTime(), DateTimeFormatter.ofPattern("HH:mm"));
                        if (startTime.isAfter(ruleStartTime) && startTime.isBefore(ruleEndTime)) {
                            currentSalary += salaryRule.getChangeInPay();
                        }
                    }
                    monthlyPay += currentSalary / 60;
                    startTime = startTime.plusMinutes(1);
                }
            }
        }catch (NullPointerException e){
            Log.e("Null","No events");
        }
        return (int) monthlyPay;
    }

    public int getTotalEarningsNet (double grossEarnings, double taxPercentage){
        return (int) (grossEarnings*(1-taxPercentage));
    }
}
