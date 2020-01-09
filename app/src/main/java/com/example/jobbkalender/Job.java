package com.example.jobbkalender;

public class Job {
    private int Id;
    private String Name;
    private double Salary;
    private SalaryRule[] SalaryRules;

    Job (String name, double salary){
        Name = name;
        Salary = salary;
    }

    Job (String name, double salary, SalaryRule[] salaryRules){
        Name = name;
        Salary = salary;
        SalaryRules = salaryRules;
    }

    public void GetSalaryRules() {
        //TODO
    }
    public void SetSalaryRule(){
        //TODO
    }
}