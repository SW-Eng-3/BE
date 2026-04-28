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

    public void sendVerificationCode(String toEmail, String code) {
        MimeMessage message = mailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(toEmail);
            helper.setSubject("[Grad-Link] 회원가입 인증 코드입니다.");
            helper.setFrom("rla005@naver.com");

            String htmlContent = "<div style=\"font-family: 'Apple SD Gothic Neo', 'Malgun Gothic', sans-serif; max-width: 500px; margin: 0 auto; padding: 40px 20px; border: 1px solid #eee; border-radius: 10px;\">" +
                    "    <h2 style=\"color: #007bff; margin-bottom: 30px; text-align: center;\">Grad-Link</h2>" +
                    "    <p style=\"font-size: 16px; color: #333; line-height: 1.6;\">안녕하세요! Grad-Link 가입을 진심으로 환영합니다.</p>" +
                    "    <p style=\"font-size: 16px; color: #333; line-height: 1.6;\">아래의 인증 번호를 가입 화면에 입력하여 이메일 인증을 완료해 주세요.</p>" +
                    "    <div style=\"background-color: #f8f9fa; border-radius: 8px; padding: 25px; margin: 30px 0; text-align: center;\">" +
                    "        <span style=\"font-size: 32px; font-weight: bold; color: #007bff; letter-spacing: 5px;\">" + code + "</span>" +
                    "    </div>" +
                    "    <p style=\"font-size: 14px; color: #888; line-height: 1.6;\">본 인증 번호는 발송 후 5분 동안 유효합니다.</p>" +
                    "    <hr style=\"border: 0; border-top: 1px solid #eee; margin: 30px 0;\">" +
                    "    <p style=\"font-size: 12px; color: #aaa; text-align: center;\">본 메일은 발신 전용입니다. 문의 사항은 고객센터를 이용해 주세요.<br>© 2026 Grad-Link. All rights reserved.</p>" +
                    "</div>";

            helper.setText(htmlContent, true);
            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("이메일 발송에 실패했습니다.", e);
        }
    }
}
