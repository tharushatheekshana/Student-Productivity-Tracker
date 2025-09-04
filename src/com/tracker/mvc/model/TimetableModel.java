/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.tracker.mvc.model;

import com.tracker.mvc.db.DBConnection;
import com.tracker.mvc.dto.TimetableDto;
import java.util.ArrayList;

import java.sql.Statement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 *
 * @author Theekshana
 */
public class TimetableModel {

    private final Connection CONNECTION;

    public TimetableModel() throws Exception {
        this.CONNECTION = DBConnection.getInstance().getConnection();
        createTableIfNotExists(); // Ensure table exists
    }

    // Create table if it doesn't exist
    private void createTableIfNotExists() throws Exception {
        String sql = "CREATE TABLE IF NOT EXISTS timetable ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "name TEXT NOT NULL, "
                + "priority TEXT NOT NULL"
                + ");";
        Statement stmt = CONNECTION.createStatement();
        stmt.execute(sql);
    }

    // Save a new subject
    public String saveSubject(TimetableDto dto) throws Exception {
        String sql = "INSERT INTO timetable(name, priority) VALUES (?, ?)";
        PreparedStatement statement = CONNECTION.prepareStatement(sql);
        statement.setString(1, dto.getName());
        statement.setString(2, dto.getPriority());

        if (statement.executeUpdate() > 0) {
            return "Success";
        } else {
            return "Failed";
        }
    }

    // Search subject by ID
    public TimetableDto searchSubject(int id) throws Exception {
        String sql = "SELECT * FROM timetable WHERE id = ?";
        PreparedStatement statement = CONNECTION.prepareStatement(sql);
        statement.setInt(1, id);
        ResultSet rst = statement.executeQuery();

        if (rst.next()) {
            return new TimetableDto(
                    rst.getInt("id"),
                    rst.getString("name"),
                    rst.getString("priority")
            );
        }
        return null;
    }

    // Update existing subject
    public String updateSubject(TimetableDto dto) throws Exception {
        String sql = "UPDATE timetable SET name=?, priority=? WHERE id=?";
        PreparedStatement statement = CONNECTION.prepareStatement(sql);
        statement.setString(1, dto.getName());
        statement.setString(2, dto.getPriority());
        statement.setInt(3, dto.getId());

        if (statement.executeUpdate() > 0) {
            return "Success";
        } else {
            return "Failed";
        }
    }

    // Delete a subject
    public String deleteSubject(int id) throws Exception {
        String sql = "DELETE FROM timetable WHERE id=?";
        PreparedStatement statement = CONNECTION.prepareStatement(sql);
        statement.setInt(1, id);

        if (statement.executeUpdate() > 0) {
            return "Success";
        } else {
            return "Failed";
        }
    }

    // Get all subjects
    public ArrayList<TimetableDto> getAllSubjects() throws Exception {
        ArrayList<TimetableDto> subjects = new ArrayList<>();
        String sql = "SELECT * FROM timetable";
        PreparedStatement statement = CONNECTION.prepareStatement(sql);
        ResultSet rst = statement.executeQuery();

        while (rst.next()) {
            subjects.add(new TimetableDto(
                    rst.getInt("id"),
                    rst.getString("name"),
                    rst.getString("priority")
            ));
        }

        return subjects;
    }
}
