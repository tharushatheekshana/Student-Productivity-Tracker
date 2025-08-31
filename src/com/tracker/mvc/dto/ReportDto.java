/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.tracker.mvc.dto;

import java.time.LocalDate;

/**
 *
 * @author Theekshana
 */
public class ReportDto {
    private LocalDate weekStartDate;
    private int totalTasks;
    private int completedTasks;
    private int overdueTasks;

    public ReportDto(LocalDate weekStartDate, int totalTasks, int completedTasks, int overdueTasks) {
        this.weekStartDate = weekStartDate;
        this.totalTasks = totalTasks;
        this.completedTasks = completedTasks;
        this.overdueTasks = overdueTasks;
    }

    /**
     * @return the weekStartDate
     */
    public LocalDate getWeekStartDate() {
        return weekStartDate;
    }

    /**
     * @param weekStartDate the weekStartDate to set
     */
    public void setWeekStartDate(LocalDate weekStartDate) {
        this.weekStartDate = weekStartDate;
    }

    /**
     * @return the totalTasks
     */
    public int getTotalTasks() {
        return totalTasks;
    }

    /**
     * @param totalTasks the totalTasks to set
     */
    public void setTotalTasks(int totalTasks) {
        this.totalTasks = totalTasks;
    }

    /**
     * @return the completedTasks
     */
    public int getCompletedTasks() {
        return completedTasks;
    }

    /**
     * @param completedTasks the completedTasks to set
     */
    public void setCompletedTasks(int completedTasks) {
        this.completedTasks = completedTasks;
    }

    /**
     * @return the overdueTasks
     */
    public int getOverdueTasks() {
        return overdueTasks;
    }

    /**
     * @param overdueTasks the overdueTasks to set
     */
    public void setOverdueTasks(int overdueTasks) {
        this.overdueTasks = overdueTasks;
    }

    @Override
    public String toString() {
        return "ReportDto{" + "weekStartDate=" + weekStartDate + ", totalTasks=" + totalTasks + ", completedTasks=" + completedTasks + ", overdueTasks=" + overdueTasks + '}';
    }
    
}
