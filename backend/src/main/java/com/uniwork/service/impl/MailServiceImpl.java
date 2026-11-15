package com.uniwork.service.impl;

import com.uniwork.constant.ApiEndpoints;
import com.uniwork.service.MailService;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

/**
 * Triển khai MailService:
 * Gửi email bất đồng bộ qua Gmail SMTP (@Async với thread pool mailTaskExecutor).
 * Nếu xảy ra lỗi kết nối SMTP, ghi log cảnh báo và không làm gián đoạn transaction đăng ký.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MailServiceImpl implements MailService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from:UniWork Marketplace <no-reply@uniwork.vn>}")
    private String mailFrom;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    @Override
    @Async("mailTaskExecutor")
    public void sendVerificationEmail(String toEmail, String fullName, String rawToken) {
        try {
            String verifyUrl = baseUrl + ApiEndpoints.V1_AUTH + "/verify-email?token=" + rawToken;

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false, StandardCharsets.UTF_8.name());

            helper.setFrom(mailFrom);
            helper.setTo(toEmail);
            helper.setSubject("[UniWork Marketplace] Xác thực địa chỉ email tài khoản");

            String htmlContent = buildVerificationEmailHtml(fullName, verifyUrl);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Đã gửi email xác thực thành công tới: {}", toEmail);

        } catch (Exception ex) {
            log.error("Gặp sự cố khi gửi email xác thực tới {}: {}", toEmail, ex.getMessage());
            // Theo yêu cầu: Lỗi gửi mail không được làm hỏng quá trình đăng ký tài khoản
        }
    }

    private String buildVerificationEmailHtml(String fullName, String verifyUrl) {
        return "<!DOCTYPE html>"
                + "<html lang=\"vi\">"
                + "<head><meta charset=\"UTF-8\"><title>Xác thực tài khoản</title></head>"
                + "<body style=\"font-family: Arial, sans-serif; line-height: 1.6; color: #1f2a37; max-width: 600px; margin: 0 auto; padding: 20px;\">"
                + "  <div style=\"border: 1px solid #d8dee6; border-top: 3px solid #ee7a17; border-radius: 4px; padding: 24px;\">"
                + "    <div style=\"font-size: 20px; font-weight: bold; color: #ee7a17; margin-bottom: 16px;\">UniWork</div>"
                + "    <h2 style=\"color: #0f3a63; margin-top: 0;\">Chào mừng bạn đến với UniWork</h2>"
                + "    <p>Xin chào <strong>" + escapeHtml(fullName) + "</strong>,</p>"
                + "    <p>Cảm ơn bạn đã đăng ký tài khoản trên nền tảng UniWork Marketplace. Để hoàn tất đăng ký và kích hoạt tài khoản, vui lòng nhấn vào nút bên dưới:</p>"
                + "    <div style=\"text-align: center; margin: 30px 0;\">"
                + "      <a href=\"" + verifyUrl + "\" style=\"background-color: #ee7a17; color: #ffffff; text-decoration: none; padding: 12px 24px; border-radius: 4px; font-weight: bold; display: inline-block;\">"
                + "        Xác thực tài khoản của tôi"
                + "      </a>"
                + "    </div>"
                + "    <p style=\"font-size: 14px; color: #5d6b7a;\">Hoặc bạn có thể sao chép liên kết sau vào trình duyệt:<br>"
                + "      <a href=\"" + verifyUrl + "\" style=\"color: #0d5aa7; word-break: break-all;\">" + verifyUrl + "</a>"
                + "    </p>"
                + "    <p style=\"font-size: 14px; color: #5d6b7a;\">Liên kết này có hiệu lực trong vòng <strong>24 giờ</strong>. Nếu bạn không thực hiện yêu cầu này, vui lòng bỏ qua email.</p>"
                + "    <hr style=\"border: none; border-top: 1px solid #e4e9ef; margin: 20px 0;\">"
                + "    <p style=\"font-size: 12px; color: #7a8794; text-align: center;\">UniWork Marketplace - Nền tảng việc làm bán thời gian cho sinh viên</p>"
                + "  </div>"
                + "</body>"
                + "</html>";
    }

    private String escapeHtml(String input) {
        if (input == null) return "";
        return input.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }
}

