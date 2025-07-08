package com.example.demo.model;

import java.time.LocalDateTime;

public class Meeting {
    private LocalDateTime start;
    private LocalDateTime end;
    private String title;
    private String description;

    public Meeting() {
    }

    public Meeting(LocalDateTime start, LocalDateTime end) {
        this.start = start;
        this.end = end;
    }

    public Meeting(LocalDateTime start, LocalDateTime end, String title) {
        this.start = start;
        this.end = end;
        this.title = title;
    }

    public LocalDateTime getStart() {
        return start;
    }

    public void setStart(LocalDateTime start) {
        this.start = start;
    }

    public LocalDateTime getEnd() {
        return end;
    }

    public void setEnd(LocalDateTime end) {
        this.end = end;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    // Helper method to check if this meeting overlaps with another
    public boolean overlapsWith(Meeting other) {
        return this.start.isBefore(other.end) && this.end.isAfter(other.start);
    }

    // Helper method to get duration in minutes
    public long getDurationInMinutes() {
        return java.time.Duration.between(start, end).toMinutes();
    }

    @Override
    public String toString() {
        return "Meeting{" +
                "start=" + start +
                ", end=" + end +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}
