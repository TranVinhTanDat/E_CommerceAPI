package com.example.shoppecommerce.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendOtpEmail(String toEmail, String otpCode) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        helper.setTo(toEmail);
        helper.setSubject("Your OTP Code");
        helper.setText("<h3>Your OTP Code is: <b>" + otpCode + "</b></h3>", true);
        helper.setFrom("tandat27012002.td@gmail.com");

        mailSender.send(message);
    }


    // Gửi email thông báo khi đơn hàng được đặt thành công
    public void sendOrderConfirmationEmail(String toEmail, String orderId, BigDecimal totalAmount) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        // Thiết lập email
        helper.setTo(toEmail);
        helper.setSubject("Order Confirmation - Order ID: " + orderId);

        // Nội dung email với thông tin đơn hàng
        String emailContent = "<h3>Your order has been successfully placed!</h3>" +
                "<p>Order ID: <b>" + orderId + "</b></p>" +
                "<p>Total Amount: <b>" + totalAmount + " VND</b></p>" +
                "<p>We will notify you once your order is processed and shipped. Thank you for shopping with us!</p>";

        helper.setText(emailContent, true);
        helper.setFrom("your-email@example.com");  // Địa chỉ email của bạn

        // Gửi email
        mailSender.send(message);
    }
}
