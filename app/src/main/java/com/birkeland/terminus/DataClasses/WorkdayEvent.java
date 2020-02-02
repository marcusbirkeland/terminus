package com.birkeland.terminus.DataClasses;

import java.io.Serializable;

public class WorkdayEvent implements Serializable {
    private boolean isNightShift;
    private String date;
    private String dayOfWeek;
    private String startTime;
    private String endTime;
    private int breakTime;
    private int salary;
    private Job job;

    public WorkdayEvent (String dateIn, String startTimeIn, String endTimeIn, int breakTimeIn, Job jobIn){
        date = dateIn;
        startTime=startTimeIn;
        endTime=endTimeIn;
        breakTime = breakTimeIn;
        job = jobIn;
    }

    public WorkdayEvent (WorkdayEvent copyEvent){
        this.isNightShift = copyEvent.isNightShift;
        this.date = copyEvent.date;
        this.dayOfWeek = copyEvent.dayOfWeek;
        this.startTime = copyEvent.startTime;
        this.endTime = copyEvent.endTime;
        this.breakTime = copyEvent.breakTime;
        this.salary = copyEvent.salary;
        this.job = copyEvent.job;
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




}
