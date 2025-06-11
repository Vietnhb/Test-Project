package com.fpt.hivtreatment.repository;

import com.fpt.hivtreatment.model.entity.Conversation;
import com.fpt.hivtreatment.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, Long> {

    Optional<Conversation> findByConversationId(String conversationId);

    @Query("SELECT c FROM Conversation c JOIN c.participants p WHERE p.id = :userId ORDER BY c.lastMessageAt DESC")
    List<Conversation> findConversationsByParticipantId(@Param("userId") Long userId);

    @Query("SELECT c FROM Conversation c WHERE :user1 MEMBER OF c.participants AND :user2 MEMBER OF c.participants AND SIZE(c.participants) = 2")
    Optional<Conversation> findPrivateConversation(@Param("user1") User user1, @Param("user2") User user2);

    @Query("SELECT c FROM Conversation c WHERE EXISTS (SELECT p FROM c.participants p WHERE p.id = :user1Id) AND EXISTS (SELECT p FROM c.participants p WHERE p.id = :user2Id) AND SIZE(c.participants) = 2")
    Optional<Conversation> findPrivateConversationByUserIds(@Param("user1Id") Long user1Id,
            @Param("user2Id") Long user2Id);
}