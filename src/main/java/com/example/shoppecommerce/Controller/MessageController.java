package com.example.shoppecommerce.Controller;

import com.example.shoppecommerce.Entity.Message;
import com.example.shoppecommerce.Repository.MessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/messages")
public class MessageController {

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/chat")
    @SendTo("/topic/messages")
    public Message broadcastMessage(Message message) {
        message.setRead(false);
        return messageRepository.save(message);
    }

    @MessageMapping("/private-message")
    public void sendPrivateMessage(Message message) {
        System.out.println("📩 Admin gửi tin nhắn đến: " + message.getReceiver());
        message.setRead(false); // Tin nhắn từ Admin gửi đến user chưa được đọc
        messageRepository.save(message);
        messagingTemplate.convertAndSendToUser(
                message.getReceiver(), "/queue/private", message
        );
        System.out.println("✅ Tin nhắn đã gửi qua WebSocket đến " + message.getReceiver());
    }

    @GetMapping("/customers")
    public List<String> getAllCustomers() {
        return messageRepository.findDistinctSenders();
    }

    @GetMapping("/{sender}/{receiver}")
    public List<Message> getMessages(@PathVariable String sender, @PathVariable String receiver) {
        return messageRepository.findChatHistory(sender, receiver);
    }

    @GetMapping("/conversations/{user}")
    public List<String> getUserConversations(@PathVariable String user) {
        return messageRepository.findConversations(user);
    }

    @GetMapping("/unread-count")
    public Map<String, Long> getUnreadMessagesCount() {
        List<String> customers = messageRepository.findDistinctSenders();
        Map<String, Long> unreadCountMap = new HashMap<>();
        for (String customer : customers) {
            Long unreadCount = messageRepository.countUnreadMessagesBySender(customer);
            unreadCountMap.put(customer, unreadCount);
        }
        return unreadCountMap;
    }

    @PostMapping("/mark-read/{sender}")
    @Transactional
    public void markMessagesAsRead(@PathVariable String sender) {
        System.out.println("Đánh dấu tin nhắn từ " + sender + " là đã đọc");
        messageRepository.markMessagesAsRead(sender);
    }

    // Lấy số tin nhắn chưa đọc từ Admin gửi đến user hiện tại
    @GetMapping("/unread-count-from-admin/{receiver}")
    public Long getUnreadMessagesFromAdminToUser(@PathVariable String receiver) {
        return messageRepository.countUnreadMessagesFromAdminToUser(receiver);
    }

    // Đánh dấu tất cả tin nhắn từ Admin gửi đến user hiện tại là đã đọc
    @PostMapping("/mark-read-from-admin/{receiver}")
    @Transactional
    public void markMessagesFromAdminAsRead(@PathVariable String receiver) {
        System.out.println("Đánh dấu tin nhắn từ Admin gửi đến " + receiver + " là đã đọc");
        messageRepository.markMessagesFromAdminAsRead(receiver);
    }
}