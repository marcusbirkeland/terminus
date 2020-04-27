package com.birkeland.terminus.DataClasses;

import android.util.Log;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Job implements Serializable {
    private String image;
    private String name;
    private double salary;
    private boolean hasPaidBreak;
    private int salaryPeriodDate;
    private List<SalaryRule> salaryRules = new ArrayList<>();

    public Job(Job job) {
        this.image = job.image;
        this.name = job.name;
        this.salary = job.salary;
        this.hasPaidBreak = job.hasPaidBreak;
        this.salaryPeriodDate = job.salaryPeriodDate;
        this. salaryRules = job.salaryRules;
    }

    public Job(String nameIn, double salaryIn, int salaryPeriodDateIn, List<SalaryRule> salaryRulesIn, boolean paidBreak){
        name = nameIn;
        salary = salaryIn;
        salaryPeriodDate = salaryPeriodDateIn;
        hasPaidBreak = paidBreak;
        for (SalaryRule s:salaryRulesIn
             ) {
            try {
                salaryRules.add(s);
            }catch (NullPointerException n){
                Log.d("ERROR","SalaryRules are empty");
            }
        }
    }

    public boolean hasPaidBreak() {
        return hasPaidBreak;
    }

    public List<SalaryRule> getSalaryRules() {
        return salaryRules;
    }

    public String getName(){
        return name;
    }

    public String getImage() {
        return image;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setSalary(double salary) {
        this.salary = salary;
    }

    public void setHasPaidBreak(boolean hasPaidBreak) {
        this.hasPaidBreak = hasPaidBreak;
    }

    public void setSalaryPeriodDate(int salaryPeriodDate) {
        this.salaryPeriodDate = salaryPeriodDate;
    }

    public void setSalaryRules(List<SalaryRule> salaryRules) {
        this.salaryRules = salaryRules;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public int getSalaryPeriodDate() {
        return salaryPeriodDate;
    }

    public double getSalary() {
        return salary;
    }

    public String toString(){
        String out = "Name: " + name +
                " Salary: " + salary+
                "SalaryRules: ";
        for (SalaryRule s : salaryRules) {
            out+=s.toString();
        }
        return out;
    }
}