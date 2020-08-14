package com.example.keysam.Controller;

import com.example.keysam.db_Connection;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;

@RestController
public class KeywordController {

    // DB Connection
    private db_Connection db_connection = new db_Connection();
    private Connection con = db_connection.getCon();

    // API4
    // 키워드 및 웹페이지 데이터 삽입
    @PostMapping("/insertKeywordAndWebpage")
    public String insertKeywordAndWebpage(@RequestParam("cid") int cid,
                              @RequestParam("webpage") String webpage,
                              @RequestParam("keyword") String keyword,
                              @RequestParam("classid") String classid){

        String query = "{call sp_insert_keyword(?, ?, ?, ?)}";

        try {
            CallableStatement stmt = con.prepareCall(query);
            stmt.setInt(1, cid);
            stmt.setString(2, webpage);
            stmt.setString(3, keyword);
            stmt.setString(4, classid);

            stmt.execute();
            stmt.close();

        }catch (SQLException e){
            e.printStackTrace();
        }

        return "keyword insert successfully";
    }

    // API5
    // 키워드 삭제
    // 키워드가 존재하지 않는 웹사이트가 있다면 삭제
    @PostMapping("/deleteKeyword")
    public String deleteKeyword(@RequestParam("sid") int sid,
                              @RequestParam("kid") int kid){

        String query = "{call sp_delete_keyword(?, ?)}";

        try {
            CallableStatement stmt = con.prepareCall(query);
            stmt.setInt(1, sid);
            stmt.setInt(2, kid);

            stmt.execute();
            stmt.close();

        }catch (SQLException e){
            e.printStackTrace();
        }

        return "keyword delete successfully";
    }
}
