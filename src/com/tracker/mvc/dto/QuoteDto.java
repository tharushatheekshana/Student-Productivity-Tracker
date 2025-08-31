/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.tracker.mvc.dto;

/**
 *
 * @author Theekshana
 */
public class QuoteDto {
    private int id;
    private String message;

    public QuoteDto(int id, String message) {
        this.id = id;
        this.message = message;
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
     * @return the message
     */
    public String getMessage() {
        return message;
    }

    /**
     * @param message the message to set
     */
    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "QuoteDto{" + "id=" + id + ", message=" + message + '}';
    }
    
}
