-- Table for storing conversations
CREATE TABLE IF NOT EXISTS conversations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    conversation_id VARCHAR(100) NOT NULL UNIQUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_message_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_message TEXT
);

-- Table for mapping users to conversations (many-to-many relationship)
CREATE TABLE IF NOT EXISTS conversation_participants (
    conversation_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    PRIMARY KEY (conversation_id, user_id),
    CONSTRAINT fk_conversation_participant_conversation
        FOREIGN KEY (conversation_id) REFERENCES conversations (id)
        ON DELETE CASCADE,
    CONSTRAINT fk_conversation_participant_user
        FOREIGN KEY (user_id) REFERENCES users (id)
        ON DELETE CASCADE
);

-- Table for storing chat messages
CREATE TABLE IF NOT EXISTS chat_messages (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    sender_id BIGINT NOT NULL,
    receiver_id BIGINT NOT NULL,
    content TEXT NOT NULL,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_read BOOLEAN DEFAULT FALSE,
    conversation_id VARCHAR(100) NOT NULL,
    CONSTRAINT fk_chat_message_sender
        FOREIGN KEY (sender_id) REFERENCES users (id)
        ON DELETE CASCADE,
    CONSTRAINT fk_chat_message_receiver
        FOREIGN KEY (receiver_id) REFERENCES users (id)
        ON DELETE CASCADE,
    INDEX idx_conversation_id (conversation_id),
    INDEX idx_timestamp (timestamp)
);

-- Add indexes for better performance
CREATE INDEX IF NOT EXISTS idx_conversations_last_message_at ON conversations (last_message_at);
CREATE INDEX IF NOT EXISTS idx_chat_messages_sender_receiver ON chat_messages (sender_id, receiver_id);
CREATE INDEX IF NOT EXISTS idx_chat_messages_receiver_is_read ON chat_messages (receiver_id, is_read); 