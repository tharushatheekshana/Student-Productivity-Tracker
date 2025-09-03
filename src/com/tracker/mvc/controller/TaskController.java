/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.tracker.mvc.controller;

import com.tracker.mvc.dto.TaskDto;
import com.tracker.mvc.model.TaskModel;
import java.time.LocalDate;
import java.util.ArrayList;

/**
 *
 * @author Theekshana
 */
public class TaskController {

    private TaskModel taskModel;

    public TaskController() throws Exception {
        taskModel = new TaskModel();
    }

    public String saveTask(TaskDto taskDto) throws Exception {
        return taskModel.saveTask(taskDto);
    }

    public ArrayList<TaskDto> getAllTasks() throws Exception {
        ArrayList<TaskDto> tasks = taskModel.getAllTasks();
        LocalDate today = LocalDate.now();

        for (TaskDto task : tasks) {
            if (!task.isCompleted() && task.getDeadline().isBefore(today)) {
                task.setOverdue(true);
            } else {
                task.setOverdue(false);
            }
        }
        return tasks;
    }

    public TaskDto searchTask(String id) throws Exception {
        return taskModel.searchTask(id);
    }

    public String updateTask(TaskDto taskDto) throws Exception {
        return taskModel.updateTask(taskDto);
    }

    public String deleteTask(String id) throws Exception {
        return taskModel.deleteTask(id);
    }

}
