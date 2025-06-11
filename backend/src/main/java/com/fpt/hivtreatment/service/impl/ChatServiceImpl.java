package com.fpt.hivtreatment.service.impl;

import com.fpt.hivtreatment.dto.ChatMessageDTO;
import com.fpt.hivtreatment.dto.ConversationDTO;
import com.fpt.hivtreatment.dto.UserDTO;
import com.fpt.hivtreatment.model.entity.ChatMessage;
import com.fpt.hivtreatment.model.entity.Conversation;
import com.fpt.hivtreatment.model.entity.User;
import com.fpt.hivtreatment.repository.ChatMessageRepository;
import com.fpt.hivtreatment.repository.ConversationRepository;
import com.fpt.hivtreatment.repository.UserRepository;
import com.fpt.hivtreatment.service.ChatService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatServiceImpl implements ChatService {

    private final ChatMessageRepository chatMessageRepository;
    private final ConversationRepository conversationRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public ChatMessageDTO saveMessage(ChatMessageDTO messageDTO) {
        User sender = userRepository.findById(messageDTO.getSenderId())
                .orElseThrow(() -> new RuntimeException("Sender not found"));

        User receiver = userRepository.findById(messageDTO.getReceiverId())
                .orElseThrow(() -> new RuntimeException("Receiver not found"));

        // Find or create conversation
        final String conversationIdInput = messageDTO.getConversationId();
        final String conversationId;

        if (conversationIdInput == null || conversationIdInput.isEmpty()) {
            // Create a new conversation ID from user IDs (sorted to ensure consistency)
            Long[] ids = new Long[] { sender.getId(), receiver.getId() };
            Arrays.sort(ids);
            conversationId = String.format("conv_%d_%d", ids[0], ids[1]);
            messageDTO.setConversationId(conversationId);
        } else {
            conversationId = conversationIdInput;
        }

        // Find or create the conversation entity
        Conversation conversation = conversationRepository.findByConversationId(conversationId)
                .orElseGet(() -> {
                    Conversation newConversation = new Conversation();
                    newConversation.setConversationId(conversationId);
                    newConversation.getParticipants().add(sender);
                    newConversation.getParticipants().add(receiver);
                    return conversationRepository.save(newConversation);
                });

        // Update the conversation's last message details
        conversation.setLastMessage(messageDTO.getContent());
        conversation.setLastMessageAt(LocalDateTime.now());
        conversationRepository.save(conversation);

        // Create and save the chat message
        ChatMessage chatMessage = ChatMessage.builder()
                .sender(sender)
                .receiver(receiver)
                .content(messageDTO.getContent())
                .conversationId(conversationId)
                .isRead(false)
                .timestamp(LocalDateTime.now())
                .build();

        ChatMessage savedMessage = chatMessageRepository.save(chatMessage);
        return mapToDTO(savedMessage);
    }

    @Override
    public List<ChatMessageDTO> findMessagesByConversationId(String conversationId) {
        List<ChatMessage> messages = chatMessageRepository.findByConversationIdOrderByTimestampAsc(conversationId);
        return messages.stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    @Override
    public Page<ChatMessageDTO> findMessagesByConversationIdPaged(String conversationId, Pageable pageable) {
        Page<ChatMessage> messagePage = chatMessageRepository.findByConversationIdOrderByTimestampDesc(conversationId,
                pageable);
        List<ChatMessageDTO> messageDTOs = messagePage.getContent().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());

        return new PageImpl<>(messageDTOs, pageable, messagePage.getTotalElements());
    }

    @Override
    @Transactional
    public ConversationDTO findOrCreateConversation(Long userId1, Long userId2) {
        User user1 = userRepository.findById(userId1)
                .orElseThrow(() -> new RuntimeException("User 1 not found"));

        User user2 = userRepository.findById(userId2)
                .orElseThrow(() -> new RuntimeException("User 2 not found"));

        // Sort user IDs to ensure consistent conversation ID
        Long[] ids = new Long[] { userId1, userId2 };
        Arrays.sort(ids);
        String conversationId = String.format("conv_%d_%d", ids[0], ids[1]);

        // Find or create conversation
        Conversation conversation = conversationRepository.findByConversationId(conversationId)
                .orElseGet(() -> {
                    Conversation newConversation = new Conversation();
                    newConversation.setConversationId(conversationId);
                    newConversation.getParticipants().add(user1);
                    newConversation.getParticipants().add(user2);
                    newConversation.setCreatedAt(LocalDateTime.now());
                    newConversation.setLastMessageAt(LocalDateTime.now());
                    return conversationRepository.save(newConversation);
                });

        return mapToConversationDTO(conversation, userId1);
    }

    @Override
    public List<ConversationDTO> findConversationsByUserId(Long userId) {
        List<Conversation> conversations = conversationRepository.findConversationsByParticipantId(userId);
        return conversations.stream()
                .map(conversation -> mapToConversationDTO(conversation, userId))
                .collect(Collectors.toList());
    }

    @Override
    public long countUnreadMessages(Long userId) {
        return chatMessageRepository.countUnreadMessagesForUser(userId);
    }

    @Override
    @Transactional
    public void markMessagesAsRead(String conversationId, Long userId) {
        chatMessageRepository.markMessagesAsRead(conversationId, userId);
    }

    private ChatMessageDTO mapToDTO(ChatMessage message) {
        return ChatMessageDTO.builder()
                .id(message.getId())
                .senderId(message.getSender().getId())
                .senderName(message.getSender().getFullName())
                .receiverId(message.getReceiver().getId())
                .receiverName(message.getReceiver().getFullName())
                .content(message.getContent())
                .timestamp(message.getTimestamp())
                .isRead(message.getIsRead())
                .conversationId(message.getConversationId())
                .build();
    }

    private ConversationDTO mapToConversationDTO(Conversation conversation, Long currentUserId) {
        List<UserDTO> participants = conversation.getParticipants().stream()
                .map(this::mapToUserDTO)
                .collect(Collectors.toList());

        // Count unread messages
        long unreadCount = conversation.getParticipants().stream()
                .filter(user -> user.getId().equals(currentUserId))
                .findFirst()
                .map(user -> chatMessageRepository
                        .findUnreadMessagesInConversation(conversation.getConversationId(), user.getId()).size())
                .orElse(0);

        return ConversationDTO.builder()
                .id(conversation.getId())
                .conversationId(conversation.getConversationId())
                .participants(participants)
                .createdAt(conversation.getCreatedAt())
                .lastMessageAt(conversation.getLastMessageAt())
                .lastMessage(conversation.getLastMessage())
                .unreadCount(unreadCount)
                .build();
    }

    private UserDTO mapToUserDTO(User user) {
        return UserDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .profileImage(user.getProfileImage())
                .roleId(user.getRole() != null ? user.getRole().getId() : null)
                .roleName(user.getRole() != null ? user.getRole().getName() : null)
                .build();
    }
}