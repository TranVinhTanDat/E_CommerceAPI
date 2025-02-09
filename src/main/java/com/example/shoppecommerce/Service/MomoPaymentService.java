package com.example.shoppecommerce.Service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Service
public class MomoPaymentService {

    private static final String HMAC_SHA256_ALGORITHM = "HmacSHA256";

    @Value("${momo.partnerCode}")
    private String partnerCode;

    @Value("${momo.accessKey}")
    private String accessKey;

    @Value("${momo.secretKey}")
    private String secretKey;

    @Value("${momo.returnUrl}")
    private String returnUrl;

    @Value("${momo.notifyUrl}")
    private String notifyUrl;

    private static final String MOMO_API_URL = "https://test-payment.momo.vn/v2/gateway/api/create";

    public String createPayment(String orderId, String amount, String orderInfo) {
        try {
            int amountInt = new java.math.BigDecimal(amount).intValue();
            if (amountInt < 1000 || amountInt > 50000000) {
                throw new IllegalArgumentException("Số tiền phải từ 1,000 đến 50,000,000 VND.");
            }

            String requestId = orderId;
            String requestType = "captureWallet";
            String extraData = "";

            // 🔥 **Tạo đúng format `rawData` theo thứ tự chính xác**
            String rawData = "accessKey=" + accessKey +
                    "&amount=" + amountInt +
                    "&extraData=" + extraData +
                    "&ipnUrl=" + notifyUrl +
                    "&orderId=" + orderId +
                    "&orderInfo=" + orderInfo +
                    "&partnerCode=" + partnerCode +
                    "&redirectUrl=" + returnUrl +
                    "&requestId=" + requestId +
                    "&requestType=" + requestType;

            // ✅ **Tạo chữ ký HMAC SHA256 chính xác**
            String signature = hmacSHA256(secretKey, rawData);

            System.out.println("🔹 MoMo Raw Data: " + rawData);
            System.out.println("🔹 Signature: " + signature);

            // ✅ **Chuẩn bị JSON request**
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("partnerCode", partnerCode);
            requestBody.put("accessKey", accessKey);
            requestBody.put("requestId", requestId);
            requestBody.put("amount", String.valueOf(amountInt));
            requestBody.put("orderId", orderId);
            requestBody.put("orderInfo", orderInfo);
            requestBody.put("redirectUrl", returnUrl);
            requestBody.put("ipnUrl", notifyUrl);
            requestBody.put("extraData", extraData);
            requestBody.put("requestType", requestType);
            requestBody.put("signature", signature);

            // ✅ **Gửi request đến MoMo**
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<String> response = restTemplate.exchange(MOMO_API_URL, HttpMethod.POST, requestEntity, String.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                System.out.println("✅ Thanh toán MoMo thành công: " + response.getBody());
                return response.getBody();
            } else {
                System.out.println("❌ Lỗi từ MoMo API: " + response.getBody());
                return "Error: " + response.getBody();
            }

        } catch (Exception e) {
            System.err.println("❌ Lỗi tạo thanh toán MoMo: " + e.getMessage());
            return "Error: " + e.getMessage();
        }
    }

    // ✅ **Fix lỗi tạo chữ ký bằng HMAC SHA256**
    private String hmacSHA256(String secretKey, String data) {
        try {
            Mac mac = Mac.getInstance(HMAC_SHA256_ALGORITHM);
            SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), HMAC_SHA256_ALGORITHM);
            mac.init(secretKeySpec);
            byte[] hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));

            // 🔥 **Chuyển đổi hash thành dạng HEX thay vì Base64**
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                hexString.append(String.format("%02x", b));
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi tạo chữ ký: " + e.getMessage(), e);
        }
    }
}
