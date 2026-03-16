// src/context/ChatContext.jsx
import React, { createContext, useState, useEffect, useRef } from "react";
import { socket, WS_EVENTS } from "../api/socket";

export const ChatContext = createContext(null);

const initialState = {
  serverId: null,
  channel: null,
  messagesByChannel: {}, // Store messages per channel
  typingUsers: [],
  loadingMessages: {}, // Loading state per channel
  wsConnected: false,
};

export function ChatProvider({ children }) {
  const [state, setState] = useState(initialState);
  const loadingRef = useRef(new Set()); // Use ref to track loading channels
  const currentChannelRef = useRef(null); // Ref to track current channel for reconnect

  // WebSocket connection management
  useEffect(() => {
    const handleWsConnect = () => {
      console.log("[ChatContext] WebSocket connected");
      setState(prev => ({ ...prev, wsConnected: true }));

      // Rejoin current channel on reconnect
      if (currentChannelRef.current) {
        socket.joinChannel(currentChannelRef.current.id);
      }
    };

    const handleWsDisconnect = () => {
      console.log("[ChatContext] WebSocket disconnected");
      setState(prev => ({ ...prev, wsConnected: false }));
    };

    const handleWsError = (error) => {
      console.error("[ChatContext] WebSocket error:", error);
    };

    const handleChannelMessage = (message) => {
      console.log("[ChatContext] Received channel message:", message);

      // Add or update received message to state
      setState(prev => {
        const newMessage = {
          id: message.id || Date.now(), // Use message.id if available from backend
          user: message.username || (message.userId ? `User ${message.userId}` : 'Unknown'),
          userId: message.userId,
          content: message.payload?.content || '',
          timestamp: message.timestamp || Date.now(),
          flagged: false,
          isRealtime: true, // Mark as real-time message
        };

        const channelMessages = prev.messagesByChannel[message.channelId] || [];

        // Check if message already exists (prevent duplicates) using userId and content
        const existingIndex = channelMessages.findIndex(msg =>
          msg.content === newMessage.content &&
          Number(msg.userId) === Number(newMessage.userId) &&
          Math.abs(msg.timestamp - newMessage.timestamp) < 5000 // Within 5 seconds
        );

        if (existingIndex !== -1) {
          // Message already exists, skip adding
          console.log("[ChatContext] Skipping duplicate message");
          return prev;
        }

        // Find optimistic message to replace (to prevent duplication)
        const optimisticIndex = channelMessages.findIndex(msg => msg.isOptimistic && msg.content === newMessage.content && msg.userId === newMessage.userId);

        if (optimisticIndex !== -1) {
          // Replace optimistic with real message
          const updatedMessages = [...channelMessages];
          updatedMessages[optimisticIndex] = { ...newMessage, isOptimistic: false };
          return {
            ...prev,
            messagesByChannel: {
              ...prev.messagesByChannel,
              [message.channelId]: updatedMessages,
            },
          };
        } else {
          // Add as new message
          return {
            ...prev,
            messagesByChannel: {
              ...prev.messagesByChannel,
              [message.channelId]: [...channelMessages, newMessage],
            },
          };
        }
      });
    };

    // Set up WebSocket event listeners
    socket.on(WS_EVENTS.CONNECT, handleWsConnect);
    socket.on(WS_EVENTS.DISCONNECT, handleWsDisconnect);
    socket.on(WS_EVENTS.ERROR, handleWsError);
    socket.on(WS_EVENTS.CHANNEL_MESSAGE, handleChannelMessage);

    return () => {
      // Clean up event listeners
      socket.off(WS_EVENTS.CONNECT);
      socket.off(WS_EVENTS.DISCONNECT);
      socket.off(WS_EVENTS.ERROR);
      socket.off(WS_EVENTS.CHANNEL_MESSAGE);
    };
  }, []);



  const setActiveServer = (serverId) => {
    loadingRef.current.clear(); // Clear loading state when switching servers
    setState((prev) => ({
      ...prev,
      serverId,
      channel: null,
      messagesByChannel: {}, // Clear messages when switching servers
      loadingMessages: {}, // Clear loading state
    }));
  };

  const setActiveChannel = (channel) => {
    const previousChannel = state.channel;

    setState((prev) => ({
      ...prev,
      channel,
    }));

    // Leave previous channel and join new one via WebSocket
    if (previousChannel && previousChannel.id !== channel?.id) {
      socket.leaveChannel();
    }

    if (channel && state.wsConnected) {
      socket.joinChannel(channel.id);
      // Fetch messages for the new channel
      fetchMessagesForChannel(channel.id);
    }
  };

  const addMessage = async (channelId, content, senderUserId, username) => {
    // Use WebSocket for sending messages
    if (state.wsConnected && socket.getCurrentChannel() === channelId) {
      // Add message optimistically to UI
      setState((prev) => {
        const message = {
          id: Date.now(), // Temporary ID for optimistic update
          user: username,
          userId: senderUserId,
          content: content,
          timestamp: Date.now(),
          flagged: false,
          isOptimistic: true, // Mark as optimistic update
        };
        const updatedMessagesByChannel = {
          ...prev.messagesByChannel,
          [channelId]: [...(prev.messagesByChannel[channelId] || []), message],
        };

        return {
          ...prev,
          messagesByChannel: updatedMessagesByChannel,
        };
      });

      socket.sendMessage(content);
    } else {
      console.warn("WebSocket not connected or not in channel, cannot send message");
    }
  };

  const getMessagesForChannel = (channelId) => {
    return state.messagesByChannel[channelId] || [];
  };

  const setTyping = (users) => {
    setState((prev) => ({
      ...prev,
      typingUsers: users,
    }));
  };

  const isLoadingMessages = (channelId) => {
    return state.loadingMessages[channelId] || false;
  };

  const fetchMessagesForChannel = async (channelId) => {
    // Check if messages are already loaded or currently loading
    if (state.messagesByChannel[channelId] || loadingRef.current.has(channelId)) {
      return; // Skip if already loaded or loading
    }

    loadingRef.current.add(channelId);

    setState((prev) => ({
      ...prev,
      loadingMessages: {
        ...prev.loadingMessages,
        [channelId]: true,
      },
    }));

    try {
      console.log("[ChatContext] Fetching messages for channel:", channelId, "WebSocket connected:", state.wsConnected);

      // Use WebSocket to fetch messages (includes both DB and Redis messages)
      const messages = await socket.fetchMessages(channelId);
      console.log("[ChatContext] Received messages:", messages);

      setState((prev) => ({
        ...prev,
        messagesByChannel: {
          ...prev.messagesByChannel,
          [channelId]: messages,
        },
        loadingMessages: {
          ...prev.loadingMessages,
          [channelId]: false,
        },
      }));
    } catch (error) {
      console.error("Failed to fetch messages via WebSocket:", error);
      setState((prev) => ({
        ...prev,
        loadingMessages: {
          ...prev.loadingMessages,
          [channelId]: false,
        },
      }));
    } finally {
      loadingRef.current.delete(channelId);
    }
  };

  return (
    <ChatContext.Provider
      value={{
        ...state,
        setActiveServer,
        setActiveChannel,
        addMessage,
        getMessagesForChannel,
        fetchMessagesForChannel,
        setTyping,
        isLoadingMessages,
      }}
    >
      {children}
    </ChatContext.Provider>
  );
}
