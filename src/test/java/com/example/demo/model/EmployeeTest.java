package com.example.demo.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EmployeeTest {

    private Employee employee;
    private LocalDateTime baseTime;

    @BeforeEach
    void setUp() {
        employee = new Employee("John Doe");
        baseTime = LocalDateTime.of(2024, 1, 15, 9, 0); // Monday 9 AM
    }

    @Test
    @DisplayName("Should create employee with name")
    void testConstructor() {
        Employee emp = new Employee("Jane Smith");
        assertEquals("Jane Smith", emp.getName());
        assertNotNull(emp.getMeetings());
        assertTrue(emp.getMeetings().isEmpty());
    }

    @Test
    @DisplayName("Should return correct name")
    void testGetName() {
        assertEquals("John Doe", employee.getName());
    }

    @Test
    @DisplayName("Should return empty meetings list initially")
    void testGetMeetings_InitiallyEmpty() {
        List<Meeting> meetings = employee.getMeetings();
        assertNotNull(meetings);
        assertTrue(meetings.isEmpty());
    }

    @Test
    @DisplayName("Should add meeting successfully")
    void testAddMeeting() {
        Meeting meeting = new Meeting(baseTime, baseTime.plusHours(1), "Team Meeting");
        
        employee.addMeeting(meeting);
        
        assertEquals(1, employee.getMeetings().size());
        assertEquals(meeting, employee.getMeetings().get(0));
    }

    @Test
    @DisplayName("Should add multiple meetings")
    void testAddMultipleMeetings() {
        Meeting meeting1 = new Meeting(baseTime, baseTime.plusHours(1), "Meeting 1");
        Meeting meeting2 = new Meeting(baseTime.plusHours(2), baseTime.plusHours(3), "Meeting 2");
        Meeting meeting3 = new Meeting(baseTime.plusHours(4), baseTime.plusHours(5), "Meeting 3");
        
        employee.addMeeting(meeting1);
        employee.addMeeting(meeting2);
        employee.addMeeting(meeting3);
        
        assertEquals(3, employee.getMeetings().size());
        assertTrue(employee.getMeetings().contains(meeting1));
        assertTrue(employee.getMeetings().contains(meeting2));
        assertTrue(employee.getMeetings().contains(meeting3));
    }

    @Test
    @DisplayName("Should allow adding same meeting multiple times")
    void testAddSameMeetingMultipleTimes() {
        Meeting meeting = new Meeting(baseTime, baseTime.plusHours(1), "Team Meeting");
        
        employee.addMeeting(meeting);
        employee.addMeeting(meeting);
        
        assertEquals(2, employee.getMeetings().size());
        assertEquals(meeting, employee.getMeetings().get(0));
        assertEquals(meeting, employee.getMeetings().get(1));
    }

    @Test
    @DisplayName("Should handle null meeting")
    void testAddNullMeeting() {
        employee.addMeeting(null);
        
        assertEquals(1, employee.getMeetings().size());
        assertNull(employee.getMeetings().get(0));
    }

    @Test
    @DisplayName("Should maintain meeting order")
    void testMeetingOrder() {
        Meeting meeting1 = new Meeting(baseTime, baseTime.plusHours(1), "First Meeting");
        Meeting meeting2 = new Meeting(baseTime.plusHours(2), baseTime.plusHours(3), "Second Meeting");
        Meeting meeting3 = new Meeting(baseTime.plusHours(4), baseTime.plusHours(5), "Third Meeting");
        
        employee.addMeeting(meeting1);
        employee.addMeeting(meeting2);
        employee.addMeeting(meeting3);
        
        List<Meeting> meetings = employee.getMeetings();
        assertEquals(meeting1, meetings.get(0));
        assertEquals(meeting2, meetings.get(1));
        assertEquals(meeting3, meetings.get(2));
    }

    @Test
    @DisplayName("Should return modifiable meetings list")
    void testMeetingsListModifiable() {
        Meeting meeting = new Meeting(baseTime, baseTime.plusHours(1), "Team Meeting");
        employee.addMeeting(meeting);
        
        List<Meeting> meetings = employee.getMeetings();
        
        // Should be able to modify the returned list
        assertDoesNotThrow(() -> meetings.add(new Meeting(baseTime.plusHours(2), baseTime.plusHours(3), "Another Meeting")));
        
        // The modification should affect the employee's meetings
        assertEquals(2, employee.getMeetings().size());
    }

    @Test
    @DisplayName("Should handle meetings with overlapping times")
    void testOverlappingMeetings() {
        Meeting meeting1 = new Meeting(baseTime, baseTime.plusHours(1), "Meeting 1");
        Meeting meeting2 = new Meeting(baseTime.plusMinutes(30), baseTime.plusMinutes(90), "Meeting 2");
        
        employee.addMeeting(meeting1);
        employee.addMeeting(meeting2);
        
        assertEquals(2, employee.getMeetings().size());
        assertTrue(employee.getMeetings().contains(meeting1));
        assertTrue(employee.getMeetings().contains(meeting2));
    }

    @Test
    @DisplayName("Should handle meetings with different durations")
    void testMeetingsWithDifferentDurations() {
        Meeting shortMeeting = new Meeting(baseTime, baseTime.plusMinutes(15), "Short Meeting");
        Meeting longMeeting = new Meeting(baseTime.plusHours(2), baseTime.plusHours(4), "Long Meeting");
        
        employee.addMeeting(shortMeeting);
        employee.addMeeting(longMeeting);
        
        assertEquals(2, employee.getMeetings().size());
        assertEquals(15, shortMeeting.getDurationInMinutes());
        assertEquals(120, longMeeting.getDurationInMinutes());
    }

    @Test
    @DisplayName("Should handle employee with empty name")
    void testEmployeeWithEmptyName() {
        Employee empWithEmptyName = new Employee("");
        assertEquals("", empWithEmptyName.getName());
        assertNotNull(empWithEmptyName.getMeetings());
        assertTrue(empWithEmptyName.getMeetings().isEmpty());
    }

    @Test
    @DisplayName("Should handle employee with null name")
    void testEmployeeWithNullName() {
        Employee empWithNullName = new Employee(null);
        assertNull(empWithNullName.getName());
        assertNotNull(empWithNullName.getMeetings());
        assertTrue(empWithNullName.getMeetings().isEmpty());
    }
} 