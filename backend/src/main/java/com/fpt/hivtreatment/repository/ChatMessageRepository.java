package com.fpt.hivtreatment.repository;

import com.fpt.hivtreatment.model.entity.ChatMessage;
import com.fpt.hivtreatment.model.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    List<ChatMessage> findByConversationIdOrderByTimestampAsc(String conversationId);

    Page<ChatMessage> findByConversationIdOrderByTimestampDesc(String conversationId, Pageable pageable);

    List<ChatMessage> findBySenderAndReceiverOrderByTimestampAsc(User sender, User receiver);

    @Query("SELECT COUNT(m) FROM ChatMessage m WHERE m.receiver.id = :userId AND m.isRead = false")
    long countUnreadMessagesForUser(@Param("userId") Long userId);

    @Query("SELECT m FROM ChatMessage m WHERE m.conversationId = :conversationId AND m.receiver.id = :userId AND m.isRead = false")
    List<ChatMessage> findUnreadMessagesInConversation(@Param("conversationId") String conversationId,
            @Param("userId") Long userId);

    @Modifying
    @Query("UPDATE ChatMessage m SET m.isRead = true WHERE m.conversationId = :conversationId AND m.receiver.id = :userId")
    void markMessagesAsRead(@Param("conversationId") String conversationId, @Param("userId") Long userId);
}