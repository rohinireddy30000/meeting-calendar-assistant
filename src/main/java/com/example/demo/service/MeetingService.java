package com.example.demo.service;

import com.example.demo.model.Employee;
import com.example.demo.model.Meeting;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MeetingService {
    private List<Employee> employees = new ArrayList<>();

    public Employee getOrCreateEmployee(String name) {
        for (Employee e : employees) {
            if (e.getName().equalsIgnoreCase(name)) {
                return e;
            }
        }
        Employee newEmp = new Employee(name);
        employees.add(newEmp);
        return newEmp;
    }

    public String bookMeeting(String empName, Meeting meeting) {
        Employee e = getOrCreateEmployee(empName);
        e.addMeeting(meeting);
        return "Meeting booked for " + empName;
    }

    public List<Meeting> findFreeSlots(Employee e1, Employee e2, int durationMinutes) {
        return findFreeSlots(e1, e2, durationMinutes, LocalDateTime.now(), LocalDateTime.now().plusDays(7));
    }

    public List<Meeting> findFreeSlots(Employee e1, Employee e2, int durationMinutes, 
                                       LocalDateTime searchStart, LocalDateTime searchEnd) {
        List<Meeting> freeSlots = new ArrayList<>();
        
        // Get all meetings for both employees in the search period
        List<Meeting> allMeetings = new ArrayList<>();
        allMeetings.addAll(e1.getMeetings().stream()
                .filter(m -> m.getStart().isBefore(searchEnd) && m.getEnd().isAfter(searchStart))
                .collect(Collectors.toList()));
        allMeetings.addAll(e2.getMeetings().stream()
                .filter(m -> m.getStart().isBefore(searchEnd) && m.getEnd().isAfter(searchStart))
                .collect(Collectors.toList()));
        
        // Sort meetings by start time
        allMeetings.sort((m1, m2) -> m1.getStart().compareTo(m2.getStart()));
        
        // Define working hours (9 AM to 6 PM)
        LocalTime workStart = LocalTime.of(9, 0);
        LocalTime workEnd = LocalTime.of(18, 0);
        
        // Check each day in the search period
        LocalDateTime currentDay = searchStart.toLocalDate().atTime(workStart);
        while (currentDay.isBefore(searchEnd)) {
            LocalDateTime dayEnd = currentDay.toLocalDate().atTime(workEnd);
            
            // Skip weekends (optional - can be removed if weekend meetings are allowed)
            if (currentDay.getDayOfWeek().getValue() >= 6) {
                currentDay = currentDay.plusDays(1).toLocalDate().atTime(workStart);
                continue;
            }
            
            // Get meetings for this day
            final LocalDateTime finalCurrentDay = currentDay;
            List<Meeting> dayMeetings = allMeetings.stream()
                    .filter(m -> m.getStart().toLocalDate().equals(finalCurrentDay.toLocalDate()))
                    .collect(Collectors.toList());
            
            // Find free slots in this day
            LocalDateTime slotStart = currentDay;
            
            for (Meeting meeting : dayMeetings) {
                // Check if there's a free slot before this meeting
                if (slotStart.plusMinutes(durationMinutes).isBefore(meeting.getStart()) ||
                    slotStart.plusMinutes(durationMinutes).isEqual(meeting.getStart())) {
                    
                    LocalDateTime slotEnd = meeting.getStart();
                    while (slotStart.plusMinutes(durationMinutes).isBefore(slotEnd) ||
                           slotStart.plusMinutes(durationMinutes).isEqual(slotEnd)) {
                        freeSlots.add(new Meeting(slotStart, slotStart.plusMinutes(durationMinutes), "Free Slot"));
                        slotStart = slotStart.plusMinutes(30); // 30-minute increments
                    }
                }
                slotStart = meeting.getEnd();
            }
            
            // Check for free slot after the last meeting of the day
            if (slotStart.plusMinutes(durationMinutes).isBefore(dayEnd) ||
                slotStart.plusMinutes(durationMinutes).isEqual(dayEnd)) {
                
                while (slotStart.plusMinutes(durationMinutes).isBefore(dayEnd) ||
                       slotStart.plusMinutes(durationMinutes).isEqual(dayEnd)) {
                    freeSlots.add(new Meeting(slotStart, slotStart.plusMinutes(durationMinutes), "Free Slot"));
                    slotStart = slotStart.plusMinutes(30); // 30-minute increments
                }
            }
            
            currentDay = currentDay.plusDays(1).toLocalDate().atTime(workStart);
        }
        
        return freeSlots;
    }

    public List<String> findConflicts(Meeting request, List<String> participantNames) {
        List<String> conflicts = new ArrayList<>();
        for (String name : participantNames) {
            Employee e = getOrCreateEmployee(name);
            for (Meeting m : e.getMeetings()) {
                if (request.overlapsWith(m)) {
                    conflicts.add(name);
                    break;
                }
            }
        }
        return conflicts;
    }

    // Helper method to get all employees (useful for testing)
    public List<Employee> getAllEmployees() {
        return new ArrayList<>(employees);
    }

    // Helper method to clear all data (useful for testing)
    public void clearAllData() {
        employees.clear();
    }
}
