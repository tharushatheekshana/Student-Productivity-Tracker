/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.tracker.mvc.model;

import com.tracker.mvc.db.DBConnection;
import com.tracker.mvc.dto.TaskDto;

import java.sql.Statement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.ArrayList;

/**
 *
 * @author Theekshana
 */
public class TaskModel {

    private final Connection CONNECTION;

    public TaskModel() throws Exception {
        this.CONNECTION = DBConnection.getInstance().getConnection();
        createTableIfNotExists(); // ✅ Ensure table exists
    }

    // Create table if it doesn't exist
    private void createTableIfNotExists() throws Exception {
        String sql = "CREATE TABLE IF NOT EXISTS tasks ("
                + "taskId INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "subject TEXT NOT NULL, "
                + "description TEXT, "
                + "deadline TEXT, "
                + "completed INTEGER, "
                + "overdue INTEGER, "
                + "status TEXT DEFAULT 'TODO'"
                + ");";
        Statement stmt = CONNECTION.createStatement();
        stmt.execute(sql);

        // Add status column if it doesn't exist (for existing databases)
        try {
            String alterSql = "ALTER TABLE tasks ADD COLUMN status TEXT DEFAULT 'TODO';";
            Statement alterStmt = CONNECTION.createStatement();
            alterStmt.execute(alterSql);
        } catch (Exception e) {
            // Column already exists, ignore
        }
    }

    // Save a new task
    public String saveTask(TaskDto taskDto) throws Exception {
        String sql = "INSERT INTO tasks(subject, description, deadline, completed, overdue, status) VALUES (?,?,?,?,?,?)";
        PreparedStatement statement = CONNECTION.prepareStatement(sql);
        statement.setString(1, taskDto.getSubject());
        statement.setString(2, taskDto.getDescription());
        statement.setString(3, taskDto.getDeadline().toString());
        statement.setInt(4, taskDto.isCompleted() ? 1 : 0);
        statement.setInt(5, taskDto.isOverdue() ? 1 : 0);
        statement.setString(6, taskDto.getStatus() != null ? taskDto.getStatus() : "TODO");

        if (statement.executeUpdate() > 0) {
            return "Success";
        } else {
            return "Failed";
        }
    }

    // Search task by ID
    public TaskDto searchTask(String taskId) throws Exception {
        String sql = "SELECT * FROM tasks WHERE taskId = ?";
        PreparedStatement statement = CONNECTION.prepareStatement(sql);
        statement.setString(1, taskId);
        ResultSet rst = statement.executeQuery();

        while (rst.next()) {
            TaskDto dto = new TaskDto(
                    rst.getString("taskId"),
                    rst.getString("subject"),
                    rst.getString("description"),
                    LocalDate.parse(rst.getString("deadline")),
                    rst.getInt("completed") == 1,
                    rst.getInt("overdue") == 1);
            dto.setStatus(rst.getString("status") != null ? rst.getString("status") : "TODO");
            return dto;
        }

        return null;
    }

    // Update existing task
    public String updateTask(TaskDto taskDto) throws Exception {
        String sql = "UPDATE tasks SET subject=?, description=?, deadline=?, completed=?, overdue=?, status=? WHERE taskId=?";
        PreparedStatement statement = CONNECTION.prepareStatement(sql);
        statement.setString(1, taskDto.getSubject());
        statement.setString(2, taskDto.getDescription());
        statement.setString(3, taskDto.getDeadline().toString());
        statement.setInt(4, taskDto.isCompleted() ? 1 : 0);
        statement.setInt(5, taskDto.isOverdue() ? 1 : 0);
        statement.setString(6, taskDto.getStatus() != null ? taskDto.getStatus() : "TODO");
        statement.setString(7, taskDto.getTaskId());

        if (statement.executeUpdate() > 0) {
            return "Success";
        } else {
            return "Failed";
        }
    }

    // Delete a task
    public String deleteTask(String taskId) throws Exception {
        String sql = "DELETE FROM tasks WHERE taskId=?";
        PreparedStatement statement = CONNECTION.prepareStatement(sql);
        statement.setString(1, taskId);

        if (statement.executeUpdate() > 0) {
            return "Success";
        } else {
            return "Failed";
        }
    }

    // Get all tasks
    public ArrayList<TaskDto> getAllTasks() throws Exception {
        ArrayList<TaskDto> tasks = new ArrayList<>();

        String sql = "SELECT * FROM tasks";
        PreparedStatement statement = CONNECTION.prepareStatement(sql);
        ResultSet rst = statement.executeQuery();

        while (rst.next()) {
            TaskDto dto = new TaskDto(
                    rst.getString("taskId"),
                    rst.getString("subject"),
                    rst.getString("description"),
                    LocalDate.parse(rst.getString("deadline")),
                    rst.getInt("completed") == 1,
                    rst.getInt("overdue") == 1);
            dto.setStatus(rst.getString("status") != null ? rst.getString("status") : "TODO");
            tasks.add(dto);
        }

        return tasks;
    }
}
