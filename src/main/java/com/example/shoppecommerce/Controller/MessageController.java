package com.example.shoppecommerce.Controller;

import com.example.shoppecommerce.Entity.Message;
import com.example.shoppecommerce.Repository.MessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/messages")
public class MessageController {

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private SimpMessagingTemplate messagingTemplate; // ✅ Hỗ trợ gửi tin nhắn riêng tư

    // ✅ API gửi tin nhắn công khai (broadcast)
    @MessageMapping("/chat")
    @SendTo("/topic/messages")
    public Message broadcastMessage(Message message) {
        return messageRepository.save(message);
    }

    // ✅ API gửi tin nhắn riêng tư từ Admin đến khách hàng
    @MessageMapping("/private-message")
    public void sendPrivateMessage(Message message) {
        System.out.println("📩 Admin gửi tin nhắn đến: " + message.getReceiver());

        messageRepository.save(message);

        messagingTemplate.convertAndSendToUser(
                message.getReceiver(), "/queue/private", message
        );

        System.out.println("✅ Tin nhắn đã gửi qua WebSocket đến " + message.getReceiver());
    }

    // ✅ Lấy danh sách khách hàng đang chat
    @GetMapping("/customers")
    public List<String> getAllCustomers() {
        return messageRepository.findDistinctSenders();
    }

    // ✅ Lấy lịch sử chat giữa 2 người
    @GetMapping("/{sender}/{receiver}")
    public List<Message> getMessages(@PathVariable String sender, @PathVariable String receiver) {
        return messageRepository.findChatHistory(sender, receiver);
    }


    @GetMapping("/conversations/{user}")
    public List<String> getUserConversations(@PathVariable String user) {
        return messageRepository.findConversations(user);
    }

}
