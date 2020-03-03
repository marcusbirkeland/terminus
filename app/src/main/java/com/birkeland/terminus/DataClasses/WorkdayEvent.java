package com.birkeland.terminus.DataClasses;

import android.util.Log;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class WorkdayEvent implements Serializable {
    private boolean isNightShift;
    private boolean isOvertime;
    private String date;
    private String dayOfWeek;
    private String startTime;
    private String endTime;
    private int breakTime;
    private int salary;
    private float overtimePercentage;
    private Job job;

    public WorkdayEvent (String dateIn, String startTimeIn, String endTimeIn, int breakTimeIn, Job jobIn){
        date = dateIn;
        startTime=startTimeIn;
        endTime=endTimeIn;
        breakTime = breakTimeIn;
        job = jobIn;
    }

    public WorkdayEvent(boolean isNightShift, boolean isOvertime, String date, String dayOfWeek, String startTime, String endTime, int breakTime, int salary, int overtimePercentage, Job job) {
        this.isNightShift = isNightShift;
        this.isOvertime = isOvertime;
        this.date = date;
        this.dayOfWeek = dayOfWeek;
        this.startTime = startTime;
        this.endTime = endTime;
        this.breakTime = breakTime;
        this.salary = salary;
        this.overtimePercentage = overtimePercentage;
        this.job = job;
    }

    public WorkdayEvent (WorkdayEvent copyEvent){
        this.isNightShift = copyEvent.isNightShift;
        this.isOvertime = copyEvent.isOvertime;
        this.overtimePercentage = copyEvent.overtimePercentage;
        this.date = copyEvent.date;
        this.dayOfWeek = copyEvent.dayOfWeek;
        this.startTime = copyEvent.startTime;
        this.endTime = copyEvent.endTime;
        this.breakTime = copyEvent.breakTime;
        this.salary = copyEvent.salary;
        this.job = copyEvent.job;
    }

    public boolean isOvertime() {
        return isOvertime;
    }

    public void setOvertime(boolean overtime) {
        isOvertime = overtime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public void setBreakTime(int breakTime) {
        this.breakTime = breakTime;
    }

    public float getOvertimePercentage() {
        return overtimePercentage;
    }

    public void setOvertimePercentage(int overtimePercentage) {
        this.overtimePercentage = overtimePercentage;
    }

    public int getSalary() {
        return salary;
    }

    public void setSalary(int salary) {
        this.salary = salary;
    }

    public String getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(String dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getStartTime() {
        return startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public int getBreakTime() {
        return breakTime;
    }

    public boolean isNightShift() {
        return isNightShift;
    }

    public void setNightShift(boolean nightShift) {
        isNightShift = nightShift;
    }

    public void setJob(Job jobIn){this.job = jobIn;}

    public Job getJob() {
        return job;
    }

    public double getLength(){

        LocalDateTime startTime = LocalDateTime.parse(this.startTime + this.date,DateTimeFormatter.ofPattern("HH:mmyyyy-MM-dd"));
        LocalDateTime endTime;
        if (this.isNightShift){
            endTime = LocalDateTime.parse(this.endTime + this.date, DateTimeFormatter.ofPattern("HH:mmyyyy-MM-dd")).plusDays(1);
        }else{
            endTime = LocalDateTime.parse(this.endTime + this.date, DateTimeFormatter.ofPattern("HH:mmyyyy-MM-dd"));
        }
        Log.d("Shift length; ", startTime.until(endTime, ChronoUnit.HOURS) + "");
        return  startTime.until(endTime, ChronoUnit.HOURS);
    }



}
