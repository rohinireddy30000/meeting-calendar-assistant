package com.example.demo.controller;

import com.example.demo.model.Meeting;
import com.example.demo.service.MeetingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/meetings")
public class MeetingController {

    @Autowired
    private MeetingService meetingService;

    @PostMapping("/book")
    public String bookMeeting(@RequestParam String empName, @RequestBody Meeting meeting) {
        return meetingService.bookMeeting(empName, meeting);
    }

    @PostMapping("/conflicts")
    public List<String> checkConflicts(@RequestBody Meeting request,
            @RequestParam List<String> participants) {
        return meetingService.findConflicts(request, participants);
    }

    @GetMapping("/free-slots")
    public List<Meeting> getFreeSlots(
            @RequestParam String emp1,
            @RequestParam String emp2,
            @RequestParam int durationMinutes) {
        return meetingService.findFreeSlots(
                meetingService.getOrCreateEmployee(emp1),
                meetingService.getOrCreateEmployee(emp2),
                durationMinutes);
    }
}
