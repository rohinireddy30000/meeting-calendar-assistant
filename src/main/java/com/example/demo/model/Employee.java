package com.example.demo.model;
import java.util.ArrayList;
import java.util.List;

public class Employee {
    private String name;
    private List<Meeting> meetings = new ArrayList<>();

    public Employee(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public List<Meeting> getMeetings() {
        return meetings;
    }

    public void addMeeting(Meeting meeting) {
        meetings.add(meeting);
    }
}
