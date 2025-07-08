package com.example.demo.controller;

import com.example.demo.model.Meeting;
import com.example.demo.service.MeetingService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MeetingController.class)
class MeetingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MeetingService meetingService;

    @Autowired
    private ObjectMapper objectMapper;

    private LocalDateTime baseTime;
    private Meeting testMeeting;

    @BeforeEach
    void setUp() {
        baseTime = LocalDateTime.of(2024, 1, 15, 9, 0); // Monday 9 AM
        testMeeting = new Meeting(baseTime, baseTime.plusHours(1), "Test Meeting");
        testMeeting.setDescription("Test Description");
    }

    @Test
    @DisplayName("Should book meeting successfully")
    void testBookMeeting_Success() throws Exception {
        // Arrange
        String empName = "John Doe";
        String expectedResponse = "Meeting booked for " + empName;
        when(meetingService.bookMeeting(eq(empName), any(Meeting.class))).thenReturn(expectedResponse);

        // Act & Assert
        mockMvc.perform(post("/meetings/book")
                .param("empName", empName)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testMeeting)))
                .andExpect(status().isOk())
                .andExpect(content().string(expectedResponse));

        // Verify service method was called
        verify(meetingService, times(1)).bookMeeting(eq(empName), any(Meeting.class));
    }

    @Test
    @DisplayName("Should handle book meeting with missing employee name")
    void testBookMeeting_MissingEmpName() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/meetings/book")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testMeeting)))
                .andExpect(status().isBadRequest());

        // Verify service method was not called
        verify(meetingService, never()).bookMeeting(anyString(), any(Meeting.class));
    }

    @Test
    @DisplayName("Should handle book meeting with invalid JSON")
    void testBookMeeting_InvalidJson() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/meetings/book")
                .param("empName", "John Doe")
                .contentType(MediaType.APPLICATION_JSON)
                .content("invalid json"))
                .andExpect(status().isBadRequest());

        // Verify service method was not called
        verify(meetingService, never()).bookMeeting(anyString(), any(Meeting.class));
    }

    @Test
    @DisplayName("Should find conflicts successfully")
    void testCheckConflicts_Success() throws Exception {
        // Arrange
        List<String> participants = Arrays.asList("John Doe", "Jane Smith");
        List<String> conflicts = Arrays.asList("John Doe");
        when(meetingService.findConflicts(any(Meeting.class), eq(participants))).thenReturn(conflicts);

        // Act & Assert
        mockMvc.perform(post("/meetings/conflicts")
                .param("participants", "John Doe", "Jane Smith")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testMeeting)))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(conflicts)));

        // Verify service method was called
        verify(meetingService, times(1)).findConflicts(any(Meeting.class), eq(participants));
    }

    @Test
    @DisplayName("Should find no conflicts when none exist")
    void testCheckConflicts_NoConflicts() throws Exception {
        // Arrange
        List<String> participants = Arrays.asList("John Doe", "Jane Smith");
        List<String> conflicts = Collections.emptyList();
        when(meetingService.findConflicts(any(Meeting.class), eq(participants))).thenReturn(conflicts);

        // Act & Assert
        mockMvc.perform(post("/meetings/conflicts")
                .param("participants", "John Doe", "Jane Smith")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testMeeting)))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));

        // Verify service method was called
        verify(meetingService, times(1)).findConflicts(any(Meeting.class), eq(participants));
    }

    @Test
    @DisplayName("Should handle conflicts check with missing participants")
    void testCheckConflicts_MissingParticipants() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/meetings/conflicts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testMeeting)))
                .andExpect(status().isBadRequest());

        // Verify service method was not called
        verify(meetingService, never()).findConflicts(any(Meeting.class), anyList());
    }

    @Test
    @DisplayName("Should get free slots successfully")
    void testGetFreeSlots_Success() throws Exception {
        // Arrange
        String emp1 = "John Doe";
        String emp2 = "Jane Smith";
        int durationMinutes = 30;
        
        List<Meeting> freeSlots = Arrays.asList(
            new Meeting(baseTime, baseTime.plusMinutes(30), "Free Slot"),
            new Meeting(baseTime.plusMinutes(30), baseTime.plusHours(1), "Free Slot")
        );
        
        when(meetingService.getOrCreateEmployee(emp1)).thenReturn(mock(com.example.demo.model.Employee.class));
        when(meetingService.getOrCreateEmployee(emp2)).thenReturn(mock(com.example.demo.model.Employee.class));
        when(meetingService.findFreeSlots(any(), any(), eq(durationMinutes))).thenReturn(freeSlots);

        // Act & Assert
        mockMvc.perform(get("/meetings/free-slots")
                .param("emp1", emp1)
                .param("emp2", emp2)
                .param("durationMinutes", String.valueOf(durationMinutes)))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(freeSlots)));

        // Verify service methods were called
        verify(meetingService, times(1)).getOrCreateEmployee(emp1);
        verify(meetingService, times(1)).getOrCreateEmployee(emp2);
        verify(meetingService, times(1)).findFreeSlots(any(), any(), eq(durationMinutes));
    }

    @Test
    @DisplayName("Should get empty free slots when none available")
    void testGetFreeSlots_NoSlotsAvailable() throws Exception {
        // Arrange
        String emp1 = "John Doe";
        String emp2 = "Jane Smith";
        int durationMinutes = 30;
        
        List<Meeting> freeSlots = Collections.emptyList();
        
        when(meetingService.getOrCreateEmployee(emp1)).thenReturn(mock(com.example.demo.model.Employee.class));
        when(meetingService.getOrCreateEmployee(emp2)).thenReturn(mock(com.example.demo.model.Employee.class));
        when(meetingService.findFreeSlots(any(), any(), eq(durationMinutes))).thenReturn(freeSlots);

        // Act & Assert
        mockMvc.perform(get("/meetings/free-slots")
                .param("emp1", emp1)
                .param("emp2", emp2)
                .param("durationMinutes", String.valueOf(durationMinutes)))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));

        // Verify service methods were called
        verify(meetingService, times(1)).getOrCreateEmployee(emp1);
        verify(meetingService, times(1)).getOrCreateEmployee(emp2);
        verify(meetingService, times(1)).findFreeSlots(any(), any(), eq(durationMinutes));
    }

    @Test
    @DisplayName("Should handle free slots request with missing emp1")
    void testGetFreeSlots_MissingEmp1() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/meetings/free-slots")
                .param("emp2", "Jane Smith")
                .param("durationMinutes", "30"))
                .andExpect(status().isBadRequest());

        // Verify service methods were not called
        verify(meetingService, never()).getOrCreateEmployee(anyString());
        verify(meetingService, never()).findFreeSlots(any(), any(), anyInt());
    }

    @Test
    @DisplayName("Should handle free slots request with missing emp2")
    void testGetFreeSlots_MissingEmp2() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/meetings/free-slots")
                .param("emp1", "John Doe")
                .param("durationMinutes", "30"))
                .andExpect(status().isBadRequest());

        // Verify service methods were not called
        verify(meetingService, never()).getOrCreateEmployee(anyString());
        verify(meetingService, never()).findFreeSlots(any(), any(), anyInt());
    }

    @Test
    @DisplayName("Should handle free slots request with missing duration")
    void testGetFreeSlots_MissingDuration() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/meetings/free-slots")
                .param("emp1", "John Doe")
                .param("emp2", "Jane Smith"))
                .andExpect(status().isBadRequest());

        // Verify service methods were not called
        verify(meetingService, never()).getOrCreateEmployee(anyString());
        verify(meetingService, never()).findFreeSlots(any(), any(), anyInt());
    }

    @Test
    @DisplayName("Should handle free slots request with invalid duration")
    void testGetFreeSlots_InvalidDuration() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/meetings/free-slots")
                .param("emp1", "John Doe")
                .param("emp2", "Jane Smith")
                .param("durationMinutes", "invalid"))
                .andExpect(status().isBadRequest());

        // Verify service methods were not called
        verify(meetingService, never()).getOrCreateEmployee(anyString());
        verify(meetingService, never()).findFreeSlots(any(), any(), anyInt());
    }

    @Test
    @DisplayName("Should handle service exception in book meeting")
    void testBookMeeting_ServiceException() throws Exception {
        // Arrange
        String empName = "John Doe";
        when(meetingService.bookMeeting(eq(empName), any(Meeting.class)))
                .thenThrow(new RuntimeException("Service error"));

        // Act & Assert
        mockMvc.perform(post("/meetings/book")
                .param("empName", empName)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testMeeting)))
                .andExpect(status().isInternalServerError());

        // Verify service method was called
        verify(meetingService, times(1)).bookMeeting(eq(empName), any(Meeting.class));
    }

    @Test
    @DisplayName("Should handle service exception in find conflicts")
    void testCheckConflicts_ServiceException() throws Exception {
        // Arrange
        List<String> participants = Arrays.asList("John Doe", "Jane Smith");
        when(meetingService.findConflicts(any(Meeting.class), eq(participants)))
                .thenThrow(new RuntimeException("Service error"));

        // Act & Assert
        mockMvc.perform(post("/meetings/conflicts")
                .param("participants", "John Doe", "Jane Smith")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testMeeting)))
                .andExpect(status().isInternalServerError());

        // Verify service method was called
        verify(meetingService, times(1)).findConflicts(any(Meeting.class), eq(participants));
    }

    @Test
    @DisplayName("Should handle service exception in get free slots")
    void testGetFreeSlots_ServiceException() throws Exception {
        // Arrange
        String emp1 = "John Doe";
        String emp2 = "Jane Smith";
        int durationMinutes = 30;
        
        when(meetingService.getOrCreateEmployee(emp1)).thenReturn(mock(com.example.demo.model.Employee.class));
        when(meetingService.getOrCreateEmployee(emp2)).thenReturn(mock(com.example.demo.model.Employee.class));
        when(meetingService.findFreeSlots(any(), any(), eq(durationMinutes)))
                .thenThrow(new RuntimeException("Service error"));

        // Act & Assert
        mockMvc.perform(get("/meetings/free-slots")
                .param("emp1", emp1)
                .param("emp2", emp2)
                .param("durationMinutes", String.valueOf(durationMinutes)))
                .andExpect(status().isInternalServerError());

        // Verify service methods were called
        verify(meetingService, times(1)).getOrCreateEmployee(emp1);
        verify(meetingService, times(1)).getOrCreateEmployee(emp2);
        verify(meetingService, times(1)).findFreeSlots(any(), any(), eq(durationMinutes));
    }
} 