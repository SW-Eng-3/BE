package yc.sw3.backend.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    public void sendNotification(String toEmail, String subject, String title, String body) {
        MimeMessage message = mailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(toEmail);
            helper.setSubject("[Grad-Link] " + subject);
            helper.setFrom("Grad-Link <rla005@naver.com>");

            String htmlContent = "<div style=\"font-family: 'Apple SD Gothic Neo', 'Malgun Gothic', sans-serif; max-width: 500px; margin: 0 auto; padding: 40px 20px; border: 1px solid #eee; border-radius: 10px;\">" +
                    "    <h2 style=\"color: #007bff; margin-bottom: 30px; text-align: center;\">Grad-Link</h2>" +
                    "    <h3 style=\"color: #333; margin-bottom: 20px;\">" + title + "</h3>" +
                    "    <p style=\"font-size: 16px; color: #333; line-height: 1.6; white-space: pre-wrap;\">" + body + "</p>" +
                    "    <div style=\"margin: 30px 0; text-align: center;\">" +
                    "        <a href=\"http://localhost:3000\" style=\"background-color: #007bff; color: white; padding: 12px 25px; text-decoration: none; border-radius: 5px; font-weight: bold;\">사이트 바로가기</a>" +
                    "    </div>" +
                    "    <hr style=\"border: 0; border-top: 1px solid #eee; margin: 30px 0;\">" +
                    "    <p style=\"font-size: 12px; color: #aaa; text-align: center;\">본 메일은 발신 전용입니다. 문의 사항은 고객센터를 이용해 주세요.<br>© 2026 Grad-Link. All rights reserved.</p>" +
                    "</div>";

            helper.setText(htmlContent, true);
            mailSender.send(message);
        } catch (MessagingException e) {
            // 알림 메일 발송 실패는 핵심 로직 중단 사유가 아니므로 로그만 남김 (실제 운영 시에는 SLF4J 사용 권장)
            System.err.println("이메일 알림 발송 실패: " + e.getMessage());
        }
    }

    public void sendVerificationCode(String toEmail, String code) {
        String title = "안녕하세요! Grad-Link 가입을 진심으로 환영합니다.";
        String body = "아래의 인증 번호를 가입 화면에 입력하여 이메일 인증을 완료해 주세요.";
        // 기존 스타일 유지를 위해 sendVerificationCode는 별도로 두거나 통합 가능하나, 
        // 여기서는 기존 코드를 유지하면서 위 스타일을 활용하도록 제안합니다.
        sendNotification(toEmail, "회원가입 인증 코드입니다.", title, body + "\n\n인증번호: " + code);
    }
}
