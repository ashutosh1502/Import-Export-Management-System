package com.project.application;

import java.sql.SQLException;

public class DatabaseErrorHandler {
    public static void handleDatabaseError(SQLException e) {
        System.err.println("Database Error: " + e.getMessage());
        System.err.println("SQL State: " + e.getSQLState());
        System.err.println("Error Code: " + e.getErrorCode());
    }

}
