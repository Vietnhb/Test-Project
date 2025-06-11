package com.fpt.hivtreatment.controller;

import com.fpt.hivtreatment.dto.ChatMessageDTO;
import com.fpt.hivtreatment.dto.ConversationDTO;
import com.fpt.hivtreatment.security.services.UserDetailsImpl;
import com.fpt.hivtreatment.service.ChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
@Slf4j
public class ChatController {

    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * WebSocket endpoint for sending messages
     * Client sends to: /app/chat.sendMessage
     * Message is broadcast to: /topic/public
     */
    @MessageMapping("/chat.sendMessage")
    @SendTo("/topic/public")
    public ChatMessageDTO sendMessage(@Payload ChatMessageDTO chatMessageDTO) {
        return chatService.saveMessage(chatMessageDTO);
    }

    /**
     * WebSocket endpoint for sending private messages
     * Client sends to: /app/chat.sendPrivateMessage/{conversationId}
     * Message is sent only to users in that conversation
     */
    @MessageMapping("/chat.sendPrivateMessage/{conversationId}")
    public ChatMessageDTO sendPrivateMessage(
            @DestinationVariable String conversationId,
            @Payload ChatMessageDTO chatMessageDTO,
            SimpMessageHeaderAccessor headerAccessor) {

        // Save the message
        ChatMessageDTO savedMessage = chatService.saveMessage(chatMessageDTO);

        // Send the message to the topic for this conversation
        messagingTemplate.convertAndSend("/topic/conversations." + conversationId, savedMessage);

        return savedMessage;
    }

    /**
     * WebSocket endpoint for joining a chat
     * Client sends to: /app/chat.addUser
     * Notification is broadcast to: /topic/public
     */
    @MessageMapping("/chat.addUser")
    @SendTo("/topic/public")
    public ChatMessageDTO addUser(@Payload ChatMessageDTO chatMessageDTO,
            SimpMessageHeaderAccessor headerAccessor) {

        // Add username in web socket session
        headerAccessor.getSessionAttributes().put("username", chatMessageDTO.getSenderName());
        headerAccessor.getSessionAttributes().put("userId", chatMessageDTO.getSenderId());
        return chatMessageDTO;
    }

    /**
     * REST endpoint for getting current user's conversations
     */
    @GetMapping("/conversations")
    public ResponseEntity<?> getConversations() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) auth.getPrincipal();
        Long userId = userDetails.getId();

        List<ConversationDTO> conversations = chatService.findConversationsByUserId(userId);
        return ResponseEntity.ok(conversations);
    }

    /**
     * REST endpoint for getting or creating a conversation with another user
     */
    @GetMapping("/conversations/user/{userId}")
    public ResponseEntity<?> getOrCreateConversation(@PathVariable Long userId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) auth.getPrincipal();
        Long currentUserId = userDetails.getId();

        ConversationDTO conversation = chatService.findOrCreateConversation(currentUserId, userId);
        return ResponseEntity.ok(conversation);
    }

    /**
     * REST endpoint for getting messages from a conversation
     */
    @GetMapping("/conversations/{conversationId}/messages")
    public ResponseEntity<?> getMessages(
            @PathVariable String conversationId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) auth.getPrincipal();
        Long userId = userDetails.getId();

        // Mark messages as read
        chatService.markMessagesAsRead(conversationId, userId);

        // Get paginated messages
        Page<ChatMessageDTO> messages = chatService.findMessagesByConversationIdPaged(
                conversationId, PageRequest.of(page, size));

        Map<String, Object> response = new HashMap<>();
        response.put("messages", messages.getContent());
        response.put("currentPage", messages.getNumber());
        response.put("totalItems", messages.getTotalElements());
        response.put("totalPages", messages.getTotalPages());

        return ResponseEntity.ok(response);
    }

    /**
     * REST endpoint for sending a message
     */
    @PostMapping("/messages")
    public ResponseEntity<?> sendMessageRest(@Valid @RequestBody ChatMessageDTO messageDTO) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) auth.getPrincipal();
        Long userId = userDetails.getId();

        // Ensure sender ID matches authenticated user
        messageDTO.setSenderId(userId);

        ChatMessageDTO savedMessage = chatService.saveMessage(messageDTO);

        // Notify connected users via WebSocket
        messagingTemplate.convertAndSend("/topic/conversations." + savedMessage.getConversationId(), savedMessage);

        return ResponseEntity.ok(savedMessage);
    }

    /**
     * REST endpoint for getting unread message count
     */
    @GetMapping("/messages/unread/count")
    public ResponseEntity<?> getUnreadMessageCount() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) auth.getPrincipal();
        Long userId = userDetails.getId();

        long count = chatService.countUnreadMessages(userId);
        Map<String, Long> response = new HashMap<>();
        response.put("count", count);

        return ResponseEntity.ok(response);
    }

    /**
     * REST endpoint for marking messages as read
     */
    @PutMapping("/conversations/{conversationId}/read")
    public ResponseEntity<?> markMessagesAsRead(@PathVariable String conversationId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) auth.getPrincipal();
        Long userId = userDetails.getId();

        chatService.markMessagesAsRead(conversationId, userId);

        return ResponseEntity.ok().build();
    }
}