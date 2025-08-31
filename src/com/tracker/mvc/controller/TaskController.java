/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.tracker.mvc.controller;

import com.tracker.mvc.dto.TaskDto;
import com.tracker.mvc.model.TaskModel;

/**
 *
 * @author Theekshana
 */
public class TaskController {
    private TaskModel taskModel;
    
    public TaskController() throws Exception{
        taskModel = new TaskModel();
    }
    public String saveTask(TaskDto taskDto) throws Exception{
        return taskModel.saveTask(taskDto);
    }
}
