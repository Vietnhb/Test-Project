package com.fpt.hivtreatment.service;

import com.fpt.hivtreatment.dto.ChatMessageDTO;
import com.fpt.hivtreatment.dto.ConversationDTO;
import com.fpt.hivtreatment.model.entity.ChatMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ChatService {

    /**
     * Save a chat message
     * 
     * @param messageDTO the message to save
     * @return the saved message
     */
    ChatMessageDTO saveMessage(ChatMessageDTO messageDTO);

    /**
     * Find messages in a conversation
     * 
     * @param conversationId the conversation ID
     * @return list of messages
     */
    List<ChatMessageDTO> findMessagesByConversationId(String conversationId);

    /**
     * Find messages in a conversation with pagination
     * 
     * @param conversationId the conversation ID
     * @param pageable       pagination parameters
     * @return paged messages
     */
    Page<ChatMessageDTO> findMessagesByConversationIdPaged(String conversationId, Pageable pageable);

    /**
     * Find or create a conversation between two users
     * 
     * @param userId1 first user ID
     * @param userId2 second user ID
     * @return the conversation
     */
    ConversationDTO findOrCreateConversation(Long userId1, Long userId2);

    /**
     * Get all conversations for a user
     * 
     * @param userId the user ID
     * @return list of conversations
     */
    List<ConversationDTO> findConversationsByUserId(Long userId);

    /**
     * Get unread message count for a user
     * 
     * @param userId the user ID
     * @return the count of unread messages
     */
    long countUnreadMessages(Long userId);

    /**
     * Mark messages as read in a conversation
     * 
     * @param conversationId the conversation ID
     * @param userId         the user ID who is reading the messages
     */
    void markMessagesAsRead(String conversationId, Long userId);
}