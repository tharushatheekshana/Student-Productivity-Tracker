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
public class TaskDto {
    private String taskId;
    private String subject;
    private String description;
    private LocalDate deadline;
    private boolean completed;
    private boolean overdue;
    private String status; // "TODO", "IN_PROGRESS", "DONE"

    public TaskDto() {
        this.status = "TODO"; // default status
    }

    public TaskDto(String taskId, String subject, String description, LocalDate deadline, boolean completed,
            boolean overdue) {
        this.taskId = taskId;
        this.subject = subject;
        this.description = description;
        this.deadline = deadline;
        this.completed = completed;
        this.overdue = overdue;
        this.status = "TODO"; // default status
    }

    /**
     * @return the taskId
     */
    public String getTaskId() {
        return taskId;
    }

    /**
     * @param taskId the taskId to set
     */
    public void setTaskId(String taskId) {
        this.taskId = taskId;
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
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @return the deadline
     */
    public LocalDate getDeadline() {
        return deadline;
    }

    /**
     * @param deadline the deadline to set
     */
    public void setDeadline(LocalDate deadline) {
        this.deadline = deadline;
    }

    /**
     * @return the completed
     */
    public boolean isCompleted() {
        return completed;
    }

    /**
     * @param completed the completed to set
     */
    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    /**
     * @return the overdue
     */
    public boolean isOverdue() {
        return overdue;
    }

    /**
     * @param overdue the overdue to set
     */
    public void setOverdue(boolean overdue) {
        this.overdue = overdue;
    }

    /**
     * @return the status
     */
    public String getStatus() {
        return status;
    }

    /**
     * @param status the status to set (TODO, IN_PROGRESS, DONE)
     */
    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "TaskDto{" + "taskId=" + taskId + ", subject=" + subject + ", description=" + description + ", deadline="
                + deadline + ", completed=" + completed + ", overdue=" + overdue + ", status=" + status + '}';
    }

}
