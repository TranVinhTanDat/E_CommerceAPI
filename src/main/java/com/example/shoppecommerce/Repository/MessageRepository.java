package com.example.shoppecommerce.Repository;

import com.example.shoppecommerce.Entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    @Query("SELECT DISTINCT m.sender FROM Message m WHERE m.sender != 'admin'")
    List<String> findDistinctSenders();

    @Query("SELECT m FROM Message m WHERE (m.sender = :sender AND m.receiver = :receiver) OR (m.sender = :receiver AND m.receiver = :sender) ORDER BY m.timestamp ASC")
    List<Message> findBySenderAndReceiver(@Param("sender") String sender, @Param("receiver") String receiver);

    @Query("SELECT DISTINCT CASE WHEN m.sender = :user THEN m.receiver ELSE m.sender END FROM Message m WHERE m.sender = :user OR m.receiver = :user")
    List<String> findConversations(@Param("user") String user);

    @Query("SELECT m FROM Message m WHERE (m.sender = :user1 AND m.receiver = :user2) OR (m.sender = :user2 AND m.receiver = :user1) ORDER BY m.timestamp ASC")
    List<Message> findChatHistory(@Param("user1") String user1, @Param("user2") String user2);

    @Query("SELECT COUNT(m) FROM Message m WHERE m.sender = :sender AND m.receiver = 'admin' AND m.isRead = false")
    Long countUnreadMessagesBySender(@Param("sender") String sender);

    @Modifying
    @Query("UPDATE Message m SET m.isRead = true WHERE m.sender = :sender AND m.receiver = 'admin'")
    void markMessagesAsRead(@Param("sender") String sender);

    // Đếm số tin nhắn chưa đọc từ Admin gửi đến user hiện tại
    @Query("SELECT COUNT(m) FROM Message m WHERE m.sender = 'admin' AND m.receiver = :receiver AND m.isRead = false")
    Long countUnreadMessagesFromAdminToUser(@Param("receiver") String receiver);

    // Đánh dấu tất cả tin nhắn từ Admin gửi đến user hiện tại là đã đọc
    @Modifying
    @Query("UPDATE Message m SET m.isRead = true WHERE m.sender = 'admin' AND m.receiver = :receiver")
    void markMessagesFromAdminAsRead(@Param("receiver") String receiver);
}