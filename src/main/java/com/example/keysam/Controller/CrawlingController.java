package com.example.keysam.Controller;

import com.example.keysam.db_Connection;
import com.example.keysam.fcm;
import org.apache.ibatis.jdbc.SQL;
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

            String query = "{call sp_insert_article(?, ?)}";
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
                    String[] column = rows[i].split(",");

                    // 새로운 유저에게 article을 insert하는 경우
                    // cid 및 device token 변경
                    if (cid != Integer.parseInt(column[0])){

                        // 첫 사용자가 아닌 모든 경우
                        // 푸시 메시지 전송(FCM)
                        // TODO title, message 추후 개선 예정
                        if (cid > 0){
                            String title = "푸시 메세지";
                            String message = "새로운 게시물이 총 " + article_cnt + "건 추가되었습니다.";

                            fcm.setConfig(title, message, device_token);
                            fcm.pushMessage();
                        }

                        cid = Integer.parseInt(column[0]);
                        article_cnt = 0;

                        try{
                            stmt = con.prepareCall(query_token);
                            stmt.setInt(1, cid);

                            stmt.execute();
                            ResultSet rs = stmt.getResultSet();
                            rs.next();
                            device_token = rs.getString("device_token");

                            stmt.close();
                        }catch (SQLException e) {
                            e.printStackTrace();
                        }
                    }

                    int sid = Integer.parseInt(column[1]);
                    String url = column[2];

                    article_cnt++;

                    try
                    {
                        // sid, url을 sp 파라미터에 넣고 실행
                        stmt = con.prepareCall(query);
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
