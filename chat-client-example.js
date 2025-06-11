/**
 * Example JavaScript code for interacting with the WebSocket chat system
 * 
 * Prerequisites:
 * - Include SockJS and STOMP client libraries:
 *   <script src="https://cdn.jsdelivr.net/npm/sockjs-client@1/dist/sockjs.min.js"></script>
 *   <script src="https://cdn.jsdelivr.net/npm/stompjs@2.3.3/lib/stomp.min.js"></script>
 */

// Configuration
const API_URL = 'http://localhost:8080';
const SOCKET_URL = API_URL + '/ws';
let stompClient = null;
let selectedConversation = null;
let currentUser = null;

// Connect to WebSocket
function connectToChat(token) {
    // Create SockJS connection to the backend
    const socket = new SockJS(SOCKET_URL);
    stompClient = Stomp.over(socket);

    // Add JWT token to connect headers
    const headers = {
        'Authorization': 'Bearer ' + token
    };

    // Connect to the WebSocket server
    stompClient.connect(headers, onConnected, onError);
}

// Callback when successfully connected to WebSocket
function onConnected() {
    console.log('Connected to WebSocket');

    // Subscribe to the public topic
    stompClient.subscribe('/topic/public', onMessageReceived);

    // Get user information
    fetchCurrentUser().then(user => {
        currentUser = user;

        // Join the public chat
        const joinMessage = {
            type: 'JOIN',
            senderId: currentUser.id,
            senderName: currentUser.fullName,
            content: currentUser.fullName + ' joined the chat'
        };

        // Send the join message
        stompClient.send('/app/chat.addUser', {}, JSON.stringify(joinMessage));

        // Load user's conversations
        loadConversations();
    });
}

// Callback when connection error occurs
function onError(error) {
    console.error('WebSocket connection error:', error);
}

// Handle received messages
function onMessageReceived(payload) {
    const message = JSON.parse(payload.body);
    console.log('Message received:', message);

    // Handle different message types
    switch (message.type) {
        case 'JOIN':
            // Handle user join notification
            displayNotification(message.senderName + ' joined the chat');
            break;
        case 'LEAVE':
            // Handle user leave notification
            displayNotification(message.senderName + ' left the chat');
            break;
        default:
            // Display regular chat message
            displayMessage(message);
    }
}

// Send a message in the current conversation
function sendMessage(content) {
    if (!stompClient || !selectedConversation || !content.trim()) {
        return;
    }

    const chatMessage = {
        senderId: currentUser.id,
        senderName: currentUser.fullName,
        receiverId: getReceiverIdFromConversation(selectedConversation),
        content: content,
        conversationId: selectedConversation.conversationId,
        type: 'CHAT'
    };

    // Send to private conversation topic
    stompClient.send(
        '/app/chat.sendPrivateMessage/' + selectedConversation.conversationId,
        {},
        JSON.stringify(chatMessage)
    );

    // Clear input field
    document.getElementById('message-input').value = '';
}

// Subscribe to a specific conversation's messages
function subscribeToConversation(conversationId) {
    // Unsubscribe from previous conversation if any
    if (window.conversationSubscription) {
        window.conversationSubscription.unsubscribe();
    }

    // Subscribe to the new conversation
    window.conversationSubscription = stompClient.subscribe(
        '/topic/conversations.' + conversationId,
        onConversationMessageReceived
    );

    // Mark messages as read
    markMessagesAsRead(conversationId);
}

// Handle messages in a specific conversation
function onConversationMessageReceived(payload) {
    const message = JSON.parse(payload.body);
    console.log('Conversation message received:', message);

    // Display the message
    displayMessage(message);

    // Mark as read if the current user is the receiver
    if (message.receiverId === currentUser.id) {
        markMessagesAsRead(message.conversationId);
    }
}

// Fetch current user info
async function fetchCurrentUser() {
    try {
        const response = await fetch(API_URL + '/api/user/me', {
            headers: {
                'Authorization': 'Bearer ' + getToken()
            }
        });

        if (!response.ok) {
            throw new Error('Failed to fetch user info');
        }

        return await response.json();
    } catch (error) {
        console.error('Error fetching user info:', error);
        return null;
    }
}

// Load user's conversations
async function loadConversations() {
    try {
        const response = await fetch(API_URL + '/api/chat/conversations', {
            headers: {
                'Authorization': 'Bearer ' + getToken()
            }
        });

        if (!response.ok) {
            throw new Error('Failed to fetch conversations');
        }

        const conversations = await response.json();
        displayConversations(conversations);
    } catch (error) {
        console.error('Error loading conversations:', error);
    }
}

// Get or create a conversation with another user
async function getOrCreateConversation(userId) {
    try {
        const response = await fetch(API_URL + '/api/chat/conversations/user/' + userId, {
            headers: {
                'Authorization': 'Bearer ' + getToken()
            }
        });

        if (!response.ok) {
            throw new Error('Failed to get/create conversation');
        }

        const conversation = await response.json();
        selectConversation(conversation);
        return conversation;
    } catch (error) {
        console.error('Error getting/creating conversation:', error);
        return null;
    }
}

// Load conversation messages
async function loadMessages(conversationId, page = 0, size = 20) {
    try {
        const response = await fetch(
            `${API_URL}/api/chat/conversations/${conversationId}/messages?page=${page}&size=${size}`,
            {
                headers: {
                    'Authorization': 'Bearer ' + getToken()
                }
            }
        );

        if (!response.ok) {
            throw new Error('Failed to load messages');
        }

        const data = await response.json();
        displayMessages(data.messages);
        return data;
    } catch (error) {
        console.error('Error loading messages:', error);
        return null;
    }
}

// Mark messages as read
async function markMessagesAsRead(conversationId) {
    try {
        await fetch(`${API_URL}/api/chat/conversations/${conversationId}/read`, {
            method: 'PUT',
            headers: {
                'Authorization': 'Bearer ' + getToken()
            }
        });
    } catch (error) {
        console.error('Error marking messages as read:', error);
    }
}

// Select a conversation
function selectConversation(conversation) {
    selectedConversation = conversation;

    // Subscribe to the conversation's messages
    subscribeToConversation(conversation.conversationId);

    // Load messages
    loadMessages(conversation.conversationId);

    // Update UI to show selected conversation
    highlightSelectedConversation(conversation.conversationId);
}

// Disconnect WebSocket
function disconnect() {
    if (stompClient) {
        // Send a leave message
        if (currentUser) {
            const leaveMessage = {
                type: 'LEAVE',
                senderId: currentUser.id,
                senderName: currentUser.fullName
            };
            stompClient.send('/app/chat.addUser', {}, JSON.stringify(leaveMessage));
        }

        // Disconnect
        stompClient.disconnect();
        console.log('Disconnected from WebSocket');
    }
}

// Helper function to get the token
function getToken() {
    // Replace with your token retrieval logic
    return localStorage.getItem('jwt_token');
}

// Helper function to get the receiver ID from a conversation
function getReceiverIdFromConversation(conversation) {
    // Find the participant who is not the current user
    const otherParticipant = conversation.participants.find(p => p.id !== currentUser.id);
    return otherParticipant ? otherParticipant.id : null;
}

// UI helper functions (implement these according to your UI framework)
function displayNotification(message) {
    // Display notification in UI
    console.log('Notification:', message);
}

function displayMessage(message) {
    // Display message in UI
    console.log('Message to display:', message);
}

function displayConversations(conversations) {
    // Display conversations list in UI
    console.log('Conversations to display:', conversations);
}

function displayMessages(messages) {
    // Display messages in UI
    console.log('Messages to display:', messages);
}

function highlightSelectedConversation(conversationId) {
    // Highlight the selected conversation in UI
    console.log('Highlighting conversation:', conversationId);
}

// Event listeners
document.addEventListener('DOMContentLoaded', function () {
    // Connect button
    document.getElementById('connect-button').addEventListener('click', function () {
        connectToChat(getToken());
    });

    // Send message button
    document.getElementById('send-button').addEventListener('click', function () {
        const messageInput = document.getElementById('message-input');
        sendMessage(messageInput.value);
    });

    // Message input - send on Enter key
    document.getElementById('message-input').addEventListener('keypress', function (e) {
        if (e.key === 'Enter') {
            sendMessage(this.value);
        }
    });

    // Disconnect when leaving the page
    window.addEventListener('beforeunload', disconnect);
}); 