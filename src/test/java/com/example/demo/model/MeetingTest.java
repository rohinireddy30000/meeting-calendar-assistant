package com.example.demo.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class MeetingTest {

    private LocalDateTime baseTime;
    private Meeting meeting1;
    private Meeting meeting2;

    @BeforeEach
    void setUp() {
        baseTime = LocalDateTime.of(2024, 1, 15, 9, 0); // Monday 9 AM
        meeting1 = new Meeting(baseTime, baseTime.plusHours(1), "Meeting 1");
        meeting2 = new Meeting(baseTime.plusHours(2), baseTime.plusHours(3), "Meeting 2");
    }

    @Test
    @DisplayName("Should create meeting with default constructor")
    void testDefaultConstructor() {
        Meeting meeting = new Meeting();
        assertNotNull(meeting);
        assertNull(meeting.getStart());
        assertNull(meeting.getEnd());
        assertNull(meeting.getTitle());
        assertNull(meeting.getDescription());
    }

    @Test
    @DisplayName("Should create meeting with start and end time")
    void testConstructorWithStartAndEnd() {
        Meeting meeting = new Meeting(baseTime, baseTime.plusHours(1));
        assertEquals(baseTime, meeting.getStart());
        assertEquals(baseTime.plusHours(1), meeting.getEnd());
        assertNull(meeting.getTitle());
        assertNull(meeting.getDescription());
    }

    @Test
    @DisplayName("Should create meeting with start, end, and title")
    void testConstructorWithStartEndAndTitle() {
        Meeting meeting = new Meeting(baseTime, baseTime.plusHours(1), "Test Meeting");
        assertEquals(baseTime, meeting.getStart());
        assertEquals(baseTime.plusHours(1), meeting.getEnd());
        assertEquals("Test Meeting", meeting.getTitle());
        assertNull(meeting.getDescription());
    }

    @Test
    @DisplayName("Should set and get start time")
    void testSetAndGetStart() {
        Meeting meeting = new Meeting();
        meeting.setStart(baseTime);
        assertEquals(baseTime, meeting.getStart());
    }

    @Test
    @DisplayName("Should set and get end time")
    void testSetAndGetEnd() {
        Meeting meeting = new Meeting();
        meeting.setEnd(baseTime.plusHours(1));
        assertEquals(baseTime.plusHours(1), meeting.getEnd());
    }

    @Test
    @DisplayName("Should set and get title")
    void testSetAndGetTitle() {
        Meeting meeting = new Meeting();
        meeting.setTitle("Test Meeting");
        assertEquals("Test Meeting", meeting.getTitle());
    }

    @Test
    @DisplayName("Should set and get description")
    void testSetAndGetDescription() {
        Meeting meeting = new Meeting();
        meeting.setDescription("Test Description");
        assertEquals("Test Description", meeting.getDescription());
    }

    @Test
    @DisplayName("Should detect overlap when meetings overlap")
    void testOverlapsWith_Overlapping() {
        // Meeting 1: 9-10 AM
        // Meeting 2: 9:30-10:30 AM (overlaps)
        Meeting overlappingMeeting = new Meeting(baseTime.plusMinutes(30), baseTime.plusMinutes(90));
        
        assertTrue(meeting1.overlapsWith(overlappingMeeting));
        assertTrue(overlappingMeeting.overlapsWith(meeting1));
    }

    @Test
    @DisplayName("Should not detect overlap when meetings don't overlap")
    void testOverlapsWith_NotOverlapping() {
        // Meeting 1: 9-10 AM
        // Meeting 2: 11-12 PM (no overlap)
        assertFalse(meeting1.overlapsWith(meeting2));
        assertFalse(meeting2.overlapsWith(meeting1));
    }

    @Test
    @DisplayName("Should not detect overlap when meetings touch at boundaries")
    void testOverlapsWith_TouchingBoundaries() {
        // Meeting 1: 9-10 AM
        // Meeting 2: 10-11 AM (touching but not overlapping)
        Meeting touchingMeeting = new Meeting(baseTime.plusHours(1), baseTime.plusHours(2));
        
        assertFalse(meeting1.overlapsWith(touchingMeeting));
        assertFalse(touchingMeeting.overlapsWith(meeting1));
    }

    @Test
    @DisplayName("Should detect overlap when one meeting contains another")
    void testOverlapsWith_ContainedMeeting() {
        // Meeting 1: 9-10 AM
        // Meeting 2: 9:15-9:45 AM (contained within meeting 1)
        Meeting containedMeeting = new Meeting(baseTime.plusMinutes(15), baseTime.plusMinutes(45));
        
        assertTrue(meeting1.overlapsWith(containedMeeting));
        assertTrue(containedMeeting.overlapsWith(meeting1));
    }

    @Test
    @DisplayName("Should detect overlap when meeting starts before and ends during")
    void testOverlapsWith_StartsBefore() {
        // Meeting 1: 9-10 AM
        // Meeting 2: 8:30-9:30 AM (starts before, ends during)
        Meeting startingBeforeMeeting = new Meeting(baseTime.minusMinutes(30), baseTime.plusMinutes(30));
        
        assertTrue(meeting1.overlapsWith(startingBeforeMeeting));
        assertTrue(startingBeforeMeeting.overlapsWith(meeting1));
    }

    @Test
    @DisplayName("Should detect overlap when meeting starts during and ends after")
    void testOverlapsWith_EndsAfter() {
        // Meeting 1: 9-10 AM
        // Meeting 2: 9:30-10:30 AM (starts during, ends after)
        Meeting endingAfterMeeting = new Meeting(baseTime.plusMinutes(30), baseTime.plusMinutes(90));
        
        assertTrue(meeting1.overlapsWith(endingAfterMeeting));
        assertTrue(endingAfterMeeting.overlapsWith(meeting1));
    }

    @Test
    @DisplayName("Should calculate duration in minutes correctly")
    void testGetDurationInMinutes() {
        // Meeting 1: 9-10 AM (60 minutes)
        assertEquals(60, meeting1.getDurationInMinutes());
        
        // Meeting 2: 11-12 PM (60 minutes)
        assertEquals(60, meeting2.getDurationInMinutes());
        
        // 30-minute meeting
        Meeting shortMeeting = new Meeting(baseTime, baseTime.plusMinutes(30));
        assertEquals(30, shortMeeting.getDurationInMinutes());
        
        // 2-hour meeting
        Meeting longMeeting = new Meeting(baseTime, baseTime.plusHours(2));
        assertEquals(120, longMeeting.getDurationInMinutes());
    }

    @Test
    @DisplayName("Should return correct string representation")
    void testToString() {
        Meeting meeting = new Meeting(baseTime, baseTime.plusHours(1), "Test Meeting");
        meeting.setDescription("Test Description");
        
        String result = meeting.toString();
        
        assertTrue(result.contains("Meeting{"));
        assertTrue(result.contains("start=" + baseTime));
        assertTrue(result.contains("end=" + baseTime.plusHours(1)));
        assertTrue(result.contains("title='Test Meeting'"));
        assertTrue(result.contains("description='Test Description'"));
    }

    @Test
    @DisplayName("Should handle null values in toString")
    void testToString_WithNullValues() {
        Meeting meeting = new Meeting();
        
        String result = meeting.toString();
        
        assertTrue(result.contains("Meeting{"));
        assertTrue(result.contains("start=null"));
        assertTrue(result.contains("end=null"));
        assertTrue(result.contains("title='null'"));
        assertTrue(result.contains("description='null'"));
    }

    @Test
    @DisplayName("Should handle zero duration meeting")
    void testZeroDurationMeeting() {
        Meeting zeroDurationMeeting = new Meeting(baseTime, baseTime);
        assertEquals(0, zeroDurationMeeting.getDurationInMinutes());
        
        // Zero duration meeting should not overlap with itself
        assertFalse(zeroDurationMeeting.overlapsWith(zeroDurationMeeting));
    }
} 