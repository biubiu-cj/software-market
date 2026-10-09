package software_market.service.impl;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import software_market.service.MailService;

@Service
@RequiredArgsConstructor
public class MailServiceImpl implements MailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String from;

    @Override
    public void sendCode(String to, String code) {
        try {
            MimeMessage message = mailSender.createMimeMessage();

            MimeMessageHelper helper = new MimeMessageHelper(
                    message,
                    true,
                    "UTF-8"
            );

            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject("软工集市验证码");

            // HTML邮件内容
            String html = buildHtml(code);

            helper.setText(html, true);

            mailSender.send(message);

        } catch (MessagingException e) {
            throw new RuntimeException("邮件发送失败: " + e.getMessage());
        }
    }

    private String buildHtml(String code) {
        return """
                <div style="font-family: Arial; padding: 20px;">
                    <h2>软工集市验证码</h2>
                    <p>你的验证码是：</p>
                    <h1 style="color:#4CAF50;">%s</h1>
                    <p>有效时间：5分钟</p>
                    <p>请勿泄露给他人</p>
                </div>
                """.formatted(code);
    }
}