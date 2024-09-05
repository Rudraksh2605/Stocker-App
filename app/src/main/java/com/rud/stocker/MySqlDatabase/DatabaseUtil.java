package com.rud.stocker.MySqlDatabase;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseUtil {
    private static final String URL = "jdbc:mysql://10.0.2.2:3306/Stocker"; // Replace with your database URL
    private static final String USER = "root"; // Replace with your MySQL username
    private static final String PASSWORD = "rudraksh@123"; // Replace with your MySQL password

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}

