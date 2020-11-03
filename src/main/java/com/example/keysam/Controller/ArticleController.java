package com.example.keysam.Controller;

import com.example.keysam.dbConnection;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

@RestController
public class ArticleController {

    // DB Connection
    @Autowired
    private dbConnection dbconnection;
    private Connection con;

    // API4
    // 사용자가 보유하고 있는 게시물 데이터 반
    @GetMapping("/getArticle")
    public JSONArray getArticle(@RequestParam("cid") int cid){
        con = dbconnection.getCon();

        JSONArray ja = new JSONArray();

        String query = "{call sp_get_article_user(?)}";

        try {
            CallableStatement stmt = con.prepareCall(query);
            stmt.setInt(1, cid);
            boolean hadResults = stmt.execute();

            // 남아있는 결과가 없을 때 까지 반복
            // True = 아직 결과가 남아있음
            // False = 남은 결과가 존재하지 않음
            while(hadResults){
                ResultSet rs = stmt.getResultSet();

                // 결과 데이터를 JSON으로 변환하여 JSONArray에 저장
                while(rs.next()){
                    JSONObject jo = new JSONObject();
                    jo.put("aid", rs.getInt("aid"));
                    jo.put("sid", rs.getString("sid"));
                    jo.put("url", rs.getString("url"));

                    ja.add(jo);
                }

                // 남은 결과가 존재하는지 확인
                hadResults = stmt.getMoreResults();
            }

            stmt.close();

        }catch (SQLException e){
            e.printStackTrace();
        }

        return ja;
    }

}
