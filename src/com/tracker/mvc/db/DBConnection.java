/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.tracker.mvc.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 *
 * @author Theekshana
 */
public class DBConnection {

    private static DBConnection dbConnection;
    private final Connection connection;

    private DBConnection() throws SQLException, ClassNotFoundException {
        // Load SQLite JDBC driver
        Class.forName("org.sqlite.JDBC");

        // Connect to database (file-based)
        String url = "jdbc:sqlite:tasks.db"; // SQLite database file
        connection = DriverManager.getConnection(url);
    }

    // Singleton instance
    public static DBConnection getInstance() throws SQLException, ClassNotFoundException {
        if (dbConnection == null) {
            dbConnection = new DBConnection();
        }
        return dbConnection;
    }

    public Connection getConnection() {
        return connection;
    }
}