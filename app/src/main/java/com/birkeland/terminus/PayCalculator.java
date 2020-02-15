package com.birkeland.terminus;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.birkeland.terminus.DataClasses.Job;
import com.birkeland.terminus.DataClasses.SalaryRule;
import com.birkeland.terminus.DataClasses.WorkdayEvent;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static android.content.Context.MODE_PRIVATE;
import static com.birkeland.terminus.MainActivity.NORWEGIAN;

public class PayCalculator {

    private String startDateStr;
    private String endDateStr;
    private Context mContext;
    private double feriepengFaktor = 0.12;
    public PayCalculator(List<WorkdayEvent> workdayEvents , Context context) {
        mContext = context;
    }

    public String getStartDateStr() {
        return startDateStr;
    }

    public String getEndDateStr() {
        return endDateStr;
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
                int breakMinutes = 0;
                // Hopp over pause dersom den ikke er betalt
                if (!event.getJob().hasPaidBreak()) {
                    breakMinutes = event.getBreakTime();
                }
                double salary = event.getJob().getSalary();
                // Itererer gjennom hvert minutt av arbeidsdagen og finner ut lønn
                while (!startTime.equals(endTime)) {
                    if(breakMinutes > 0){
                        breakMinutes--;
                        startTime = startTime.plusMinutes(1);
                        continue;
                    }
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
        Instant instant = date.atStartOfDay(ZoneId.systemDefault()).toInstant();
        Date date1 = new Date(instant.toEpochMilli());
        // Formaterer etter system default språk
        DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT);
        return df.format(date1);
    }

    public int getYearlyEarnings(List<WorkdayEvent> workdayEvents){
        LocalDate now = LocalDate.now();
        List<WorkdayEvent> eventsToCalculate = new ArrayList<>();
        try {
            for (WorkdayEvent event : workdayEvents) {
                LocalDate eventDate = LocalDate.parse(event.getDate(), DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                // Hopp over event dersom den ikke er i nåværende år.
                if(eventDate.getYear()!= now.getYear() || eventDate.isAfter(now))
                    continue;
                eventsToCalculate.add(event);
            }
        }catch (NullPointerException e){
            Log.e("Null","No workday events in list");
        }
        return getEarnings(eventsToCalculate);
    }

    public int getPaycheckEarnings(List<WorkdayEvent> workdayEvents, Job selectedJob) {
        if(selectedJob == null){
            return 0;
        }
        List<WorkdayEvent> eventsToCalculate = new ArrayList<>();
        LocalDate now = LocalDate.now();
        LocalDate checkDate = LocalDate.of(now.getYear(), now.getMonthValue(), selectedJob.getSalaryPeriodDate());
        try {

            for (WorkdayEvent event : workdayEvents) {
                LocalDate eventDate = LocalDate.parse(event.getDate(), DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                LocalDate startDate;
                LocalDate endDate;
                // Setter hvilke månder neste lønningsperiode gjelder for.
                if(now.isBefore(checkDate)){
                    startDate = checkDate.minusMonths(1);
                    endDate = checkDate;
                }else{
                    startDate= checkDate;
                    endDate = checkDate.plusMonths(1);
                }
                // Driter i events som ikke er for gjeldende jobb
                if(!event.getJob().getName().equals(selectedJob.getName()) ||
                        eventDate.isBefore(startDate) || eventDate.isAfter(endDate)){
                    continue;
                }

                startDateStr = formatDate(startDate);
                endDateStr = formatDate(endDate);
                Log.d("Calculating on date","From " + startDate.toString() + " to " + endDate.toString());
                eventsToCalculate.add(event);
            }
        }catch (NullPointerException e){
            Log.e("Null","No events");
        }
        return getEarnings(eventsToCalculate);
    }

    public int getNetEarningsWithPercentage(int earnings, float percentage){
        float netEarnings = earnings*(1-(percentage/100));
        return (int) netEarnings;
    }

    public int getYearlyNetEarningsWithTable(List<WorkdayEvent> eventList, String tableID) {
        LocalDate startDate = LocalDate.of(LocalDate.now().getYear(), 1, 1);
        LocalDate endDate = startDate.plusMonths(1);

        int netEarnings = 0;

            // Henter alle events for gjeldende månde
        for (int i = 0; i< 12;i++) {
            List<WorkdayEvent> eventsInMonth = new ArrayList<>();
            for (WorkdayEvent event : eventList) {
                LocalDate tempDate = LocalDate.parse(event.getDate(), DateTimeFormatter.ofPattern("yyyy-MM-dd"));

                if (tempDate.isBefore(LocalDate.now()) && tempDate.getYear() == LocalDate.now().getYear()
                        && tempDate.isBefore(endDate) && tempDate.isAfter(endDate.minusMonths(1))) {
                    Log.d("ADD EVENT: ", "DATE: " + tempDate.toString());
                    eventsInMonth.add(event);
                }
            }
            // Regner ut nettolønn for gjeldende måned
            netEarnings += getMonthlyNetEarningsWithTable(getEarnings(eventsInMonth), tableID);
            Log.d("NET EARNINGS", "" + netEarnings);
            // Hopper til neste måned
            startDate = startDate.plusMonths(1);
            endDate = endDate.plusMonths(1);
        }
        return netEarnings;
    }

    public int getMonthlyNetEarningsWithTable(int earningsIn, String taxTableID){
        int basis = 0;
        int tax = 0;
        double earnings = earningsIn* (1-feriepengFaktor);
        BufferedReader reader = null;
        try{
            // Last inn csv fil
            reader = new BufferedReader( new InputStreamReader(mContext.getAssets().
                    open("tabellene2020/" + taxTableID +".csv"), "UTF-8"));
            String s;
            //Les fil linje etter linje
            while ((s = reader.readLine()) != null) {
                // CSV FORMAT:
                // 00000;00000
                // Grunnlag (5-siffer), ';' , Skatt (5-siffer)
                basis = Integer.parseInt(s.substring(0,5));
                if(basis > earnings)
                    break;
                tax = Integer.parseInt(s.substring(6,12));
            }
        }catch(IOException e){
            Log.e("Pay calculator","Failed to open .csv file");
        }finally {
            if(reader != null){
                try {
                    reader.close();
                }catch(IOException e){
                    Log.e("Pay calculator","Failed to close .csv file");
                }
            }
        }
        return (int)earnings-tax;
    }
}
