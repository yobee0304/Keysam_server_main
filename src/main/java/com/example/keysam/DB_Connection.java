package com.example.keysam;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DB_Connection {

    public Connection getCon(){

        Connection con = null;
        String DB_URL = "jdbc:mysql://localhost:3306/keysam?characterEncoding=UTF-8&serverTimezone=UTC";
        String user = "root";
        String pwd = "root";

        try{
            con = DriverManager.getConnection(DB_URL, user, pwd);
        }catch (SQLException e) {
            e.printStackTrace();
        }

        return con;
    }
}
