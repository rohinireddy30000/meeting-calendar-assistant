package com.example.demo.service;

import com.example.demo.model.Employee;
import com.example.demo.model.Meeting;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class MeetingServiceTest {

    private MeetingService meetingService;
    private Employee employee1;
    private Employee employee2;
    private LocalDateTime baseTime;

    @BeforeEach
    void setUp() {
        meetingService = new MeetingService();
        employee1 = meetingService.getOrCreateEmployee("John Doe");
        employee2 = meetingService.getOrCreateEmployee("Jane Smith");
        baseTime = LocalDateTime.of(2024, 1, 15, 9, 0); // Monday 9 AM
    }

    @Test
    @DisplayName("Should create or retrieve existing employee")
    void testGetOrCreateEmployee() {
        // Test creating new employee
        Employee newEmployee = meetingService.getOrCreateEmployee("Alice Johnson");
        assertNotNull(newEmployee);
        assertEquals("Alice Johnson", newEmployee.getName());

        // Test retrieving existing employee (case insensitive)
        Employee existingEmployee = meetingService.getOrCreateEmployee("john doe");
        assertEquals(employee1, existingEmployee);
        assertEquals("John Doe", existingEmployee.getName());

        // Verify total employee count
        assertEquals(3, meetingService.getAllEmployees().size());
    }

    @Test
    @DisplayName("Should book meeting successfully")
    void testBookMeeting() {
        // Arrange
        Meeting meeting = new Meeting(baseTime, baseTime.plusHours(1), "Team Meeting");

        // Act
        String result = meetingService.bookMeeting("John Doe", meeting);

        // Assert
        assertEquals("Meeting booked for John Doe", result);
        assertEquals(1, employee1.getMeetings().size());
        assertEquals(meeting, employee1.getMeetings().get(0));
    }

    @Test
    @DisplayName("Should book multiple meetings for same employee")
    void testBookMultipleMeetings() {
        // Arrange
        Meeting meeting1 = new Meeting(baseTime, baseTime.plusHours(1), "Meeting 1");
        Meeting meeting2 = new Meeting(baseTime.plusHours(2), baseTime.plusHours(3), "Meeting 2");

        // Act
        meetingService.bookMeeting("John Doe", meeting1);
        meetingService.bookMeeting("John Doe", meeting2);

        // Assert
        assertEquals(2, employee1.getMeetings().size());
        assertTrue(employee1.getMeetings().contains(meeting1));
        assertTrue(employee1.getMeetings().contains(meeting2));
    }

    @Test
    @DisplayName("Should find conflicts when meetings overlap")
    void testFindConflicts_WithOverlap() {
        // Arrange
        Meeting existingMeeting = new Meeting(baseTime, baseTime.plusHours(1), "Existing Meeting");
        employee1.addMeeting(existingMeeting);

        Meeting requestedMeeting = new Meeting(baseTime.plusMinutes(30), baseTime.plusMinutes(90), "Requested Meeting");
        List<String> participants = Arrays.asList("John Doe", "Jane Smith");

        // Act
        List<String> conflicts = meetingService.findConflicts(requestedMeeting, participants);

        // Assert
        assertEquals(1, conflicts.size());
        assertTrue(conflicts.contains("John Doe"));
        assertFalse(conflicts.contains("Jane Smith"));
    }

    @Test
    @DisplayName("Should find no conflicts when meetings don't overlap")
    void testFindConflicts_NoOverlap() {
        // Arrange
        Meeting existingMeeting = new Meeting(baseTime, baseTime.plusHours(1), "Existing Meeting");
        employee1.addMeeting(existingMeeting);

        Meeting requestedMeeting = new Meeting(baseTime.plusHours(2), baseTime.plusHours(3), "Requested Meeting");
        List<String> participants = Arrays.asList("John Doe", "Jane Smith");

        // Act
        List<String> conflicts = meetingService.findConflicts(requestedMeeting, participants);

        // Assert
        assertTrue(conflicts.isEmpty());
    }

    @Test
    @DisplayName("Should find conflicts for multiple participants")
    void testFindConflicts_MultipleParticipants() {
        // Arrange
        Meeting meeting1 = new Meeting(baseTime, baseTime.plusHours(1), "Meeting 1");
        Meeting meeting2 = new Meeting(baseTime.plusMinutes(30), baseTime.plusMinutes(90), "Meeting 2");
        employee1.addMeeting(meeting1);
        employee2.addMeeting(meeting2);

        Meeting requestedMeeting = new Meeting(baseTime.plusMinutes(15), baseTime.plusMinutes(45), "Requested Meeting");
        List<String> participants = Arrays.asList("John Doe", "Jane Smith", "Bob Wilson");

        // Act
        List<String> conflicts = meetingService.findConflicts(requestedMeeting, participants);

        // Assert
        assertEquals(2, conflicts.size());
        assertTrue(conflicts.contains("John Doe"));
        assertTrue(conflicts.contains("Jane Smith"));
        assertFalse(conflicts.contains("Bob Wilson"));
    }

    @Test
    @DisplayName("Should find free slots when no meetings exist")
    void testFindFreeSlots_NoMeetings() {
        // Arrange
        LocalDateTime searchStart = baseTime; // Monday 9 AM
        LocalDateTime searchEnd = baseTime.plusDays(1); // Tuesday 9 AM

        // Act
        List<Meeting> freeSlots = meetingService.findFreeSlots(employee1, employee2, 30, searchStart, searchEnd);

        // Assert
        assertFalse(freeSlots.isEmpty());
        
        // Should have multiple 30-minute slots during working hours (9 AM - 6 PM)
        // Working hours = 9 hours = 540 minutes
        // 30-minute slots in 30-minute increments = 18 slots per day
        assertEquals(18, freeSlots.size());
        
        // Verify first slot starts at 9 AM
        assertEquals(baseTime, freeSlots.get(0).getStart());
        assertEquals(baseTime.plusMinutes(30), freeSlots.get(0).getEnd());
    }

    @Test
    @DisplayName("Should find free slots around existing meetings")
    void testFindFreeSlots_WithExistingMeetings() {
        // Arrange
        Meeting meeting1 = new Meeting(baseTime.plusHours(1), baseTime.plusHours(2), "Meeting 1"); // 10-11 AM
        Meeting meeting2 = new Meeting(baseTime.plusHours(3), baseTime.plusHours(4), "Meeting 2"); // 12-1 PM
        employee1.addMeeting(meeting1);
        employee2.addMeeting(meeting2);

        LocalDateTime searchStart = baseTime; // Monday 9 AM
        LocalDateTime searchEnd = baseTime.plusDays(1); // Tuesday 9 AM

        // Act
        List<Meeting> freeSlots = meetingService.findFreeSlots(employee1, employee2, 30, searchStart, searchEnd);

        // Assert
        assertFalse(freeSlots.isEmpty());
        
        // Should have slots before first meeting, between meetings, and after last meeting
        // But not during the meeting times
        boolean hasSlotAt9AM = freeSlots.stream().anyMatch(slot -> slot.getStart().equals(baseTime));
        boolean hasSlotAt930AM = freeSlots.stream().anyMatch(slot -> slot.getStart().equals(baseTime.plusMinutes(30)));
        boolean hasSlotAt2PM = freeSlots.stream().anyMatch(slot -> slot.getStart().equals(baseTime.plusHours(5)));
        
        assertTrue(hasSlotAt9AM);
        assertTrue(hasSlotAt930AM);
        assertTrue(hasSlotAt2PM);
        
        // Should not have slots during meeting times
        boolean hasSlotDuringMeeting1 = freeSlots.stream().anyMatch(slot -> 
            slot.getStart().equals(baseTime.plusHours(1)) || slot.getStart().equals(baseTime.plusHours(1).plusMinutes(30)));
        boolean hasSlotDuringMeeting2 = freeSlots.stream().anyMatch(slot -> 
            slot.getStart().equals(baseTime.plusHours(3)) || slot.getStart().equals(baseTime.plusHours(3).plusMinutes(30)));
        
        assertFalse(hasSlotDuringMeeting1);
        assertFalse(hasSlotDuringMeeting2);
    }

    @Test
    @DisplayName("Should find free slots for longer duration")
    void testFindFreeSlots_LongerDuration() {
        // Arrange
        LocalDateTime searchStart = baseTime; // Monday 9 AM
        LocalDateTime searchEnd = baseTime.plusDays(1); // Tuesday 9 AM

        // Act - looking for 60-minute slots
        List<Meeting> freeSlots = meetingService.findFreeSlots(employee1, employee2, 60, searchStart, searchEnd);

        // Assert
        assertFalse(freeSlots.isEmpty());
        
        // Should have fewer slots for longer duration
        // Working hours = 9 hours, 60-minute slots in 30-minute increments = 16 slots
        assertEquals(16, freeSlots.size());
        
        // Verify slots are 60 minutes long
        for (Meeting slot : freeSlots) {
            assertEquals(60, slot.getDurationInMinutes());
        }
    }

    @Test
    @DisplayName("Should skip weekends when finding free slots")
    void testFindFreeSlots_SkipWeekends() {
        // Arrange - Start on Friday
        LocalDateTime fridayStart = LocalDateTime.of(2024, 1, 19, 9, 0); // Friday 9 AM
        LocalDateTime mondayEnd = LocalDateTime.of(2024, 1, 22, 9, 0); // Monday 9 AM

        // Act
        List<Meeting> freeSlots = meetingService.findFreeSlots(employee1, employee2, 30, fridayStart, mondayEnd);

        // Assert
        assertFalse(freeSlots.isEmpty());
        
        // Should only have slots for Friday (no weekend slots)
        for (Meeting slot : freeSlots) {
            int dayOfWeek = slot.getStart().getDayOfWeek().getValue();
            assertTrue(dayOfWeek < 6, "Should not have weekend slots"); // 1-5 are weekdays
        }
    }

    @Test
    @DisplayName("Should handle edge case with no free slots")
    void testFindFreeSlots_NoFreeSlots() {
        // Arrange - Fill entire day with meetings
        for (int hour = 9; hour < 18; hour++) {
            Meeting meeting = new Meeting(baseTime.plusHours(hour - 9), baseTime.plusHours(hour - 8), "Meeting " + hour);
            employee1.addMeeting(meeting);
        }

        LocalDateTime searchStart = baseTime;
        LocalDateTime searchEnd = baseTime.plusDays(1);

        // Act
        List<Meeting> freeSlots = meetingService.findFreeSlots(employee1, employee2, 30, searchStart, searchEnd);

        // Assert
        assertTrue(freeSlots.isEmpty());
    }

    @Test
    @DisplayName("Should clear all data")
    void testClearAllData() {
        // Arrange
        meetingService.bookMeeting("John Doe", new Meeting(baseTime, baseTime.plusHours(1), "Test Meeting"));
        assertEquals(2, meetingService.getAllEmployees().size());

        // Act
        meetingService.clearAllData();

        // Assert
        assertTrue(meetingService.getAllEmployees().isEmpty());
    }

    @Test
    @DisplayName("Should handle edge case with exact meeting boundaries")
    void testFindConflicts_ExactBoundaries() {
        // Arrange - Meeting ends exactly when new meeting starts
        Meeting existingMeeting = new Meeting(baseTime, baseTime.plusHours(1), "Existing Meeting");
        employee1.addMeeting(existingMeeting);

        Meeting requestedMeeting = new Meeting(baseTime.plusHours(1), baseTime.plusHours(2), "Requested Meeting");
        List<String> participants = Arrays.asList("John Doe");

        // Act
        List<String> conflicts = meetingService.findConflicts(requestedMeeting, participants);

        // Assert
        assertTrue(conflicts.isEmpty(), "Meetings that touch at boundaries should not conflict");
    }
} 