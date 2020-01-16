package com.example.jobbkalender.DataClasses;

import android.util.Log;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Job implements Serializable {
    private String image;
    private String name;
    private double salary;
    private List<SalaryRule> salaryRules = new ArrayList<>();

    public Job(String nameIn, double salaryIn, List<SalaryRule> salaryRulesIn){
        name = nameIn;
        salary = salaryIn;
        for (SalaryRule s:salaryRulesIn
             ) {
            try {
                salaryRules.add(s);
            }catch (NullPointerException n){
                Log.d("ERROR","SalaryRules are empty");
            }
        }
    }
    public String getName(){
        return name;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
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