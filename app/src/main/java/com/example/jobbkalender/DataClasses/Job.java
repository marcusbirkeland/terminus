package com.example.jobbkalender.DataClasses;

public class Job {
    private int id;
    private String name;
    private double salary;
    private SalaryRule[] salaryRules;

    Job (String nameIn, double salaryIn){
        name = nameIn;
        salary = salaryIn;
    }

    Job (String nameIn, double salaryIn, SalaryRule[] salaryRulesIn){
        name = nameIn;
        salary = salaryIn;
        salaryRules = salaryRulesIn;
    }

    public void getSalaryRules() {
        //TODO
    }
    public void setSalaryRule(){
        //TODO
    }
}