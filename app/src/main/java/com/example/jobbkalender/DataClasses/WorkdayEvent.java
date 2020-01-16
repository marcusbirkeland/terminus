package com.example.jobbkalender.DataClasses;

import java.io.Serializable;

public class WorkdayEvent implements Serializable {
    private String date;
    private String startTime;
    private String endTime;
    private int breakTime;
    private Job job;

    public String getDate() {
        return date;
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

    public Job getJob() {
        return job;
    }

    public WorkdayEvent (String dateIn, String startTimeIn, String endTimeIn, int breakTimeIn, Job jobIn){
        date = dateIn;
        startTime=startTimeIn;
        endTime=endTimeIn;
        breakTime = breakTimeIn;
        job = jobIn;
    }
}
