package com.example.jobbkalender.DataClasses;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;

public class WorkdayEvent implements Serializable {
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;
    private int breakTime;
    private Job job;

    public LocalDate getDate() {
        return date;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public int getBreakTime() {
        return breakTime;
    }

    public Job getJob() {
        return job;
    }

    public WorkdayEvent (LocalDate dateIn, LocalTime startTimeIn, LocalTime endTimeIn, int breakTimeIn, Job jobIn){
        date = dateIn;
        startTime=startTimeIn;
        endTime=endTimeIn;
        breakTime = breakTimeIn;
        job = jobIn;
    }
}
