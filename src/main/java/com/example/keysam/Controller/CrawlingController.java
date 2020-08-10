package com.example.keysam.Controller;

import com.example.keysam.db_Connection;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.sql.*;

@RestController
public class CrawlingController {

    // DB Connection
    private db_Connection db_connection = new db_Connection();
    private Connection con = db_connection.getCon();

    // API1
    // 크롤링할 웹페이지 url 데이터를 크롤러로 전송
    @GetMapping("/spGetWebpage")
    public JSONArray getWebpage(){

        JSONArray ja = new JSONArray();

        String query = "{call sp_get_webpage()}";

        try {
            CallableStatement stmt = con.prepareCall(query);
            boolean hadResults = stmt.execute();

            // 남아있는 결과가 없을 때 까지 반복
            // True = 아직 결과가 남아있음
            // False = 남은 결과가 존재하지 않음
            while(hadResults){
                ResultSet rs = stmt.getResultSet();

                // 결과 데이터를 JSON으로 변환하여 JSONArray에 저장
                while(rs.next()){
                    JSONObject jo = new JSONObject();
                    jo.put("sid", rs.getInt("sid"));
                    jo.put("keyword", rs.getString("keyword"));
                    jo.put("webpage", rs.getString("webpage"));

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

    // API2
    // 크롤링한 결과 데이터를 DB에 INSERT
    @PostMapping("/spInsertArticle")
    public String insertArticle(@RequestParam("csv_file") MultipartFile file){

        // 비어있는 파일인 경우
        if (file.isEmpty()) { ;
            return "file is empty";
        }else{

            String query = "{call sp_insert_article(?, ?)}";

            try{
                byte[] bytes = file.getBytes();
                // 받은 CSV 파일을 read
                String completeData = new String(bytes);

                // 열 단위로 분리
                String[] rows = completeData.split("\n");

                for(int i=1; i<rows.length;i++){
                    String[] column = rows[i].split(",");
                    int sid = Integer.parseInt(column[0]);
                    String url = column[1];

                    try
                    {
                        // sid, url을 sp 파라미터에 넣고 실행
                        CallableStatement stmt = con.prepareCall(query);
                        stmt.setInt(1,sid);
                        stmt.setString(2,url);

                        stmt.execute();
                        stmt.close();
                    }catch (SQLException e){
                        e.printStackTrace();
                    }
                }

            }catch (IOException e){
                e.printStackTrace();
            }

            System.out.println("Insert Data Successfully");

            return "article upload successfully";
        }

    }
}
