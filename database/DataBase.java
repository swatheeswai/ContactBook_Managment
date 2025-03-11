package com.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DataBase {

    private static Connection con;
    private DataBase(){}

    public static Connection getInstance() {
        if (con == null) {
            try {
                 String url = "jdbc:postgresql://localhost:5432/phonebook";
                String user = "phonebook_user";
                String password = "newpassword";
                con = DriverManager.getConnection(url,user,password);
                System.out.println("✅ Database Connection Established");
            } catch (SQLException e) {
                e.printStackTrace();
                System.out.println("❌ Database Connection Failed!");
            }
        }
        return con;
    }
}


