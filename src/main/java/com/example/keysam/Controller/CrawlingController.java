package com.example.keysam.Controller;

import com.example.keysam.dbConnection;
import com.example.keysam.fcm;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.sql.*;

@RestController
public class CrawlingController {

    // DB Connection
    @Autowired
    private dbConnection dbconnection;
    private Connection con;

    // API1
    // 크롤링할 웹페이지 url 데이터를 크롤러로 전송
    @GetMapping("/spGetWebpage")
    public JSONArray getWebpage(){
        con = dbconnection.getCon();
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
                    jo.put("cid", rs.getInt("cid"));
                    jo.put("sid", rs.getInt("sid"));
                    jo.put("keyword", rs.getString("keyword"));
                    jo.put("webpage", rs.getString("webpage"));
                    jo.put("classid", rs.getString("classid"));

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

        fcm fcm = new fcm();

        // 비어있는 파일인 경우
        if (file.isEmpty()) { ;
            return "file is empty";
        }else{

            String query = "{call sp_insert_article(?, ?, ?)}";
            String query_token = "{call sp_get_device_token(?)}";

            CallableStatement stmt;

            try{
                byte[] bytes = file.getBytes();
                // 받은 CSV 파일을 read
                String completeData = new String(bytes);

                // 열 단위로 분리
                String[] rows = completeData.split("\n");

                // 사용자 정보
                String device_token = null;
                int cid = -1;
                // 해당 사용자에 insert되는 article 개수
                int article_cnt = 0;

                for(int i=1; i<rows.length;i++){
                    // column : cid, sid, url 순서
                    String[] column = rows[i].split(",");

                    int sid = Integer.parseInt(column[1]);
                    String url = column[2];

                    try
                    {
                        // sid, url을 sp 파라미터에 넣고 실행
                        stmt = con.prepareCall(query);
                        stmt.registerOutParameter(3, Types.INTEGER);
                        stmt.setInt(1,sid);
                        stmt.setString(2,url);

                        stmt.execute();

                        article_cnt += stmt.getInt("out_cnt");

                        stmt.close();
                    }catch (SQLException e){
                        e.printStackTrace();
                    }

                    int new_cid = Integer.parseInt(column[0]);

                    // 푸시 메세지를 발송하는 경우
                    // 1. 기존에 cid가 존재하고 다음 cid가 다른 cid인 경우
                    // 2. 마지막 insert인 경우
                    // 3. 새로 추가되는 게시물 개수가 0보다 큰 경우
//                    System.out.println("cid :" + cid + ", new cid :" + new_cid + ", article cnt :" + article_cnt);
                    if(((cid > 0 && cid != new_cid) || i == rows.length-1) && article_cnt > 0){
                        String message = "신규 게시물 " + Integer.toString(article_cnt) + "건 추가되었습니다!";

                        try{
                            stmt = con.prepareCall(query_token);
                            stmt.setInt(1, cid);
                            stmt.execute();
                            ResultSet rs = stmt.getResultSet();
                            rs.next();
                            device_token = rs.getString("device_token");

//                            System.out.println(device_token);
                        }catch (SQLException e){
                            e.printStackTrace();
                        }

                        fcm.setConfig(message, device_token);
                        fcm.pushMessage();

                        article_cnt = 0;
                    }
                    cid = new_cid;
                }

            }catch (IOException e){
                e.printStackTrace();
            }

            System.out.println("Insert Data Successfully");

            return "article upload successfully";
        }

    }
}
