// src/api/message.api.js
import httpClient from "./httpClient";
import { socket } from "./socket";

// const dummyMessages = {
//   t1: [
//     {
//       id: "m1",
//       user: "alice",
//       content: "Welcome to #general 👋",
//       timestamp: Date.now() - 120000,
//       flagged: false,
//     },
//     {
//       id: "m2",
//       user: "bob",
//       content: "Gtalk UI looks clean!",
//       timestamp: Date.now() - 60000,
//       flagged: false,
//     },
//   ],
// };

export const messageApi = {
  fetchMessages: async (channelId) => {
    // Use WebSocket to fetch messages if connected
    if (socket.isConnected()) {
      try {
        console.log("[messageApi] Fetching messages via WebSocket for channel:", channelId);
        const messages = await socket.fetchMessages(channelId);
        console.log("[messageApi] WebSocket returned messages:", messages);
        return messages;
      } catch (error) {
        console.error("Failed to fetch messages via WebSocket, falling back to HTTP:", error);
      }
    } else {
      console.log("[messageApi] WebSocket not connected, using HTTP fallback");
    }

    // Fallback to HTTP API
    try {
      const response = await httpClient.get(`/api/messages/${channelId}`);
      console.log("[messageApi] HTTP response:", response);
      // Map Message entities to expected format
      const mappedMessages = (response || []).map(msg => ({
        id: msg.id,
        user: msg.senderUserName || msg.username || `User ${msg.senderUserId}`, // Use senderUserId if username not available
        content: msg.content,
        timestamp: new Date(msg.createdAt).getTime(),
        formattedTime: new Date(msg.createdAt).toLocaleString(),
        flagged: false,
      })).reverse(); // Reverse to show oldest first
      console.log("[messageApi] Mapped HTTP messages:", mappedMessages);
      return mappedMessages;
    } catch (httpError) {
      console.error("HTTP fallback also failed:", httpError);
      return [];
    }
  },

  sendMessage: async (channelId, payload) => {
    const response = await httpClient.post(`/api/channels/${channelId}/messages`, payload);
    return response;
  },

  deleteMessage: async (messageId) => {
    await httpClient.delete(`/api/messages/${messageId}`);
    return { success: true };
  },
};
