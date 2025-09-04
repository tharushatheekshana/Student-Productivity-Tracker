/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.tracker.mvc.dto;

/**
 *
 * @author Theekshana
 */
public class TimetableDto {

    private int id;
    private String name;
    private String priority;

    public TimetableDto() {
    }

    public TimetableDto(int id, String name, String priority) {
        this.id = id;
        this.name = name;
        this.priority = priority;
    }

    /**
     * @return the id
     */
    public int getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the priority
     */
    public String getPriority() {
        return priority;
    }

    /**
     * @param priority the priority to set
     */
    public void setPriority(String priority) {
        this.priority = priority;
    }

    @Override
    public String toString() {
        return "TimetableDto{" + "id=" + id + ", name=" + name + ", priority=" + priority + '}';
    }
    
    
}
