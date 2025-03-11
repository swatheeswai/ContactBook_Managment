package com.controllerClass;

import com.modelClass.User;
import com.database.DataBase;

import java.sql.*;

public class UserController {
    private static Connection con;
    private int loggedInUserId = -1;

    public UserController() {
        con = DataBase.getInstance();
        if (con == null) {
            System.out.println("❌ Database connection failed! 'con' is null.");
        } else {
            System.out.println("✅ Database connection established.");
        }
        createTable();
    }

    public void setLoggedInUserId(int loggedInUserId) {
        this.loggedInUserId = loggedInUserId;
    }

    public int getLoggedInUserId() {
        return loggedInUserId;
    }

    // Create phoneusers table if not exists
    public void createTable() {
        String userTable = "CREATE TABLE IF NOT EXISTS phoneusers ("
                + "phoneuser_id SERIAL PRIMARY KEY,"
                + "name VARCHAR(255) NOT NULL,"
                + "email VARCHAR(255) UNIQUE NOT NULL,"
                + "phone BIGINT UNIQUE NOT NULL,"
                + "password VARCHAR(255) NOT NULL"
                + ")";
        ensureConnectionOpen(); // Ensure connection is open before running SQL
        try (Statement stmt = con.createStatement()) {
            stmt.execute(userTable);
            System.out.println("✅ Users table is ready.");
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("❌ Failed to create users table.");
        }
    }

    // Ensure database connection is open
    private static void ensureConnectionOpen() {
        try {
            if (con == null || con.isClosed()) {
                System.out.println("🔄 Reconnecting to the database...");
                con = DataBase.getInstance();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Register user
    public static boolean registerUser(User user) {
        ensureConnectionOpen();

        String sql = "INSERT INTO phoneusers (name, email, phone, password) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, user.getName());
            pst.setString(2, user.getEmail());
            pst.setLong(3, user.getPhoneNo());
            pst.setString(4, user.getPassword());

            int affectedRows = pst.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("❌ Failed to register user.");
            return false;
        }
    }

    // Login user
    public int loginUser(String email, String password) {
        ensureConnectionOpen();

        String sql = "SELECT phoneuser_id, password FROM phoneusers WHERE email = ?";
        try (PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setString(1, email);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                int userId = rs.getInt("phoneuser_id");
                String storedPassword = rs.getString("password");

                if (password.equals(storedPassword)) {
                    loggedInUserId = userId; // ✅ Store logged-in user
                    return userId;
                } else {
                    System.out.println("❌ Incorrect password.");
                    return -1;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    // ✅ Logout method
    public void logoutUser() {
        if (loggedInUserId == -1) {
            System.out.println("⚠️ No user is currently logged in.");
        } else {
            loggedInUserId = -1; // Reset logged-in user
            System.out.println("✅ Successfully logged out.");
        }
    }

    public boolean isUserLoggedIn() {
        return loggedInUserId != -1;
    }
}

