package com.example.shoppecommerce.Controller;

import com.example.shoppecommerce.Service.MomoPaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/payments")
public class PaymentController {

    @Autowired
    private MomoPaymentService momoPaymentService;

    @PostMapping("/process")
    public ResponseEntity<?> processPayment(@RequestBody Map<String, Object> payload) {
        try {
            // 🔥 Kiểm tra dữ liệu đầu vào
            if (!payload.containsKey("orderId") || !payload.containsKey("amount")) {
                return ResponseEntity.badRequest().body("Missing required fields: orderId or amount");
            }

            String orderId = payload.get("orderId").toString();
            String amount = payload.get("amount").toString();
            String orderInfo = "Thanh toán đơn hàng " + orderId;

            // ✅ Gọi dịch vụ MoMo để tạo thanh toán
            String paymentUrl = momoPaymentService.createPayment(orderId, amount, orderInfo);

            if (paymentUrl.startsWith("Error")) {
                return ResponseEntity.status(500).body("Lỗi tạo thanh toán: " + paymentUrl);
            }

            return ResponseEntity.ok(paymentUrl);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Lỗi trong quá trình xử lý: " + e.getMessage());
        }
    }



    @PostMapping("/momo-notify")
    public ResponseEntity<String> handleMoMoNotification(@RequestBody Map<String, Object> payload) {
        System.out.println("🔹 Nhận thông báo từ MoMo: " + payload);
        return ResponseEntity.ok("OK");
    }

    @GetMapping("/success")
    public ResponseEntity<String> paymentSuccess(@RequestParam Map<String, String> params) {
        return ResponseEntity.ok("✅ Thanh toán thành công! " + params);
    }
}
