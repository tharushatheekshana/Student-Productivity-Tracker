/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.tracker.mvc.dto;

import java.time.LocalDateTime;

/**
 *
 * @author Theekshana
 */
public class ScheduleDto {
    private int ScheduleId;
    private String subject;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    public ScheduleDto(int ScheduleId, String subject, LocalDateTime startTime, LocalDateTime endTime) {
        this.ScheduleId = ScheduleId;
        this.subject = subject;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    /**
     * @return the ScheduleId
     */
    public int getScheduleId() {
        return ScheduleId;
    }

    /**
     * @param ScheduleId the ScheduleId to set
     */
    public void setScheduleId(int ScheduleId) {
        this.ScheduleId = ScheduleId;
    }

    /**
     * @return the subject
     */
    public String getSubject() {
        return subject;
    }

    /**
     * @param subject the subject to set
     */
    public void setSubject(String subject) {
        this.subject = subject;
    }

    /**
     * @return the startTime
     */
    public LocalDateTime getStartTime() {
        return startTime;
    }

    /**
     * @param startTime the startTime to set
     */
    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    /**
     * @return the endTime
     */
    public LocalDateTime getEndTime() {
        return endTime;
    }

    /**
     * @param endTime the endTime to set
     */
    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    @Override
    public String toString() {
        return "ScheduleDto{" + "ScheduleId=" + ScheduleId + ", subject=" + subject + ", startTime=" + startTime + ", endTime=" + endTime + '}';
    }
    
}
