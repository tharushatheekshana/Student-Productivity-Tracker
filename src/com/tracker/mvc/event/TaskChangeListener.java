/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.tracker.mvc.event;

/**
 * Interface for listening to task changes (add, update, delete)
 * 
 * @author Theekshana
 */
public interface TaskChangeListener {

    /**
     * Called when a task is added, updated, or deleted
     */
    void onTaskChanged();
}
