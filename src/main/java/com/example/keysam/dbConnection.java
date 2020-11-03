package com.example.keysam;

import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

@Component
public class dbConnection {

    private String DB_URL = "jdbc:mysql://localhost:3306/keysam?characterEncoding=UTF-8&serverTimezone=UTC";
    private String user = "root";
    private String pwd = "root";

    public Connection getCon(){

        Connection con = null;

        try{
            con = DriverManager.getConnection(DB_URL, user, pwd);
        }catch (SQLException e) {
            e.printStackTrace();
        }

        return con;
    }
}
