package yc.sw3.backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${resend.api-key}")
    private String apiKey;

    @Value("${resend.from-email:onboarding@resend.dev}")
    private String fromEmail;

    @Value("${custom.site-url:http://localhost:3000}")
    private String siteUrl;

    @Async
    public void sendNotification(String toEmail, String subject, String title, String body) {
        String url = "https://api.resend.com/emails";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + apiKey);

        String htmlContent = "<div style=\"font-family: 'Apple SD Gothic Neo', 'Malgun Gothic', sans-serif; max-width: 500px; margin: 0 auto; padding: 40px 20px; border: 1px solid #eee; border-radius: 10px;\">" +
                "    <h2 style=\"color: #007bff; margin-bottom: 30px; text-align: center;\">Grad-Link</h2>" +
                "    <h3 style=\"color: #333; margin-bottom: 20px;\">" + title + "</h3>" +
                "    <p style=\"font-size: 16px; color: #333; line-height: 1.6; white-space: pre-wrap;\">" + body + "</p>" +
                "    <div style=\"margin: 30px 0; text-align: center;\">" +
                "        <a href=\"" + siteUrl + "\" style=\"background-color: #007bff; color: white; padding: 12px 25px; text-decoration: none; border-radius: 5px; font-weight: bold;\">사이트 바로가기</a>" +
                "    </div>" +
                "    <hr style=\"border: 0; border-top: 1px solid #eee; margin: 30px 0;\">" +
                "    <p style=\"font-size: 12px; color: #aaa; text-align: center;\">본 메일은 발신 전용입니다. 문의 사항은 고객센터를 이용해 주세요.<br>© 2026 Grad-Link. All rights reserved.</p>" +
                "</div>";

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("from", fromEmail);
        requestBody.put("to", toEmail);
        requestBody.put("subject", "[Grad-Link] " + subject);
        requestBody.put("html", htmlContent);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
            if (response.getStatusCode().is2xxSuccessful()) {
                System.out.println("Resend 이메일 발송 성공 (" + toEmail + ")");
            } else {
                System.err.println("Resend 이메일 발송 실패 (" + toEmail + "): " + response.getBody());
            }
        } catch (Exception e) {
            System.err.println("Resend 이메일 발송 중 예외 발생 (" + toEmail + "): " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Async
    public void sendVerificationCode(String toEmail, String code) {
        String title = "안녕하세요! Grad-Link 가입을 진심으로 환영합니다.";
        String body = "아래의 인증 번호를 가입 화면에 입력하여 이메일 인증을 완료해 주세요.";
        sendNotification(toEmail, "회원가입 인증 코드입니다.", title, body + "\n\n인증번호: " + code);
    }
}
