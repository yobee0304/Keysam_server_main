package com.example.keysam;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import org.json.simple.JSONObject;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.io.FileInputStream;
import java.util.Arrays;

public class fcm {

    private String title;
    private String body;
    private String device_token;

//    public fcm(String title, String message, String device_token){
//        this.title = title;
//        this.message = message;
//        this.device_token = device_token;
//    }

    public void setConfig(String body, String device_token){
        this.title = "Keysam";
        this.body = body;
        this.device_token = device_token;
    }

    public void pushMessage(){
        try{
            // 비공개키 경로
            String path = "/Users/cy/Computer/IdeaProjects/keysam/src/main/resources/fcm-private-key.json";
            String MESSAGING_SCOPE = "https://www.googleapis.com/auth/firebase.messaging";
            String[] SCOPES = { MESSAGING_SCOPE };

            GoogleCredential googleCredential = GoogleCredential
                    .fromStream(new FileInputStream(path))
                    .createScoped(Arrays.asList(SCOPES));
            googleCredential.refreshToken();

            HttpHeaders headers = new HttpHeaders();
            headers.add("content-type" , MediaType.APPLICATION_JSON_VALUE);
            headers.add("Authorization", "Bearer " + googleCredential.getAccessToken());

            // 푸시할 메세지 적기
            //TODO title이 변하지 않음! 왜???
            JSONObject notification = new JSONObject();
            notification.put("title", title);
            notification.put("body", body);

            JSONObject message = new JSONObject();
            message.put("token", device_token);
            message.put("notification", notification);

            JSONObject jsonParams = new JSONObject();
            jsonParams.put("message", message);

            HttpEntity<JSONObject> httpEntity = new HttpEntity<JSONObject>(jsonParams, headers);
            RestTemplate rt = new RestTemplate();

            ResponseEntity<String> res = rt.exchange("https://fcm.googleapis.com/v1/projects/keysam-4a826/messages:send"
                    , HttpMethod.POST
                    , httpEntity
                    , String.class);

//            if (res.getStatusCode() != HttpStatus.OK) {
//                log.debug("FCM-Exception");
//                log.debug(res.getStatusCode().toString());
//                log.debug(res.getHeaders().toString());
//                log.debug(res.getBody().toString());
//
//            } else {
//                log.debug(res.getStatusCode().toString());
//                log.debug(res.getHeaders().toString());
//                log.debug(res.getBody().toLowerCase());
//            }
            System.out.println("fcm complete");
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
