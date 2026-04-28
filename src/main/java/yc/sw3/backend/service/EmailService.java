package yc.sw3.backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    public void sendVerificationCode(String toEmail, String code) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("[Grad-Link] 회원가입 인증 코드입니다.");
        message.setText("인증 코드: " + code + "\n요청하신 인증 코드를 입력하여 가입을 완료해 주세요.");
        message.setFrom("rla005@naver.com"); // application.yaml의 username과 일치해야 함

        mailSender.send(message);
    }
}
