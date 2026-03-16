// WebSocket event types matching backend
export const WS_EVENTS = {
  CONNECT: "connect",
  DISCONNECT: "disconnect",
  ERROR: "error",

  JOIN_CHANNEL: "JOIN_CHANNEL",
  CHANNEL_MESSAGE: "CHANNEL_MESSAGE",
  LEAVE_CHANNEL: "LEAVE_CHANNEL",
  FETCH_MESSAGES: "FETCH_MESSAGES",
  FETCH_MESSAGES_RESPONSE: "FETCH_MESSAGES_RESPONSE",
};

// Real WebSocket client for chat
class WebSocketClient {
  constructor() {
    this.ws = null;
    this.listeners = {};
    this.connected = false;
    this.currentChannel = null;
    this.reconnectAttempts = 0;
    this.maxReconnectAttempts = 5;
    this.reconnectInterval = 3000;
  }

  connect(token) {
    if (this.ws && this.ws.readyState === WebSocket.OPEN) {
      return;
    }

    const wsUrl = `ws://localhost:8080/ws?token=${token}`;
    console.log("[WebSocket] Connecting to:", wsUrl);

    this.ws = new WebSocket(wsUrl);

    this.ws.onopen = () => {
      console.log("[WebSocket] Connected");
      this.connected = true;
      this.reconnectAttempts = 0;
      this.listeners[WS_EVENTS.CONNECT]?.();
    };

    this.ws.onmessage = (event) => {
      try {
        if (typeof event.data !== 'string') {
          console.warn("[WebSocket] Received non-string data:", event.data);
          return;
        }
        const message = JSON.parse(event.data);
        console.log("[WebSocket] Received:", message);

        if (message.type === "ERROR") {
          console.error("[WebSocket] Error:", message.message);
          this.listeners[WS_EVENTS.ERROR]?.(message.message);
        } else {
          // Handle different message types
          this.listeners[message.type]?.(message);
        }
      } catch (error) {
        console.error("[WebSocket] Failed to parse message:", event.data, error);
      }
    };

    this.ws.onclose = (event) => {
      console.log("[WebSocket] Disconnected:", event.code, event.reason);
      this.connected = false;
      this.listeners[WS_EVENTS.DISCONNECT]?.();

      // Auto-reconnect if not a normal closure
      if (event.code !== 1000 && this.reconnectAttempts < this.maxReconnectAttempts) {
        setTimeout(() => {
          this.reconnectAttempts++;
          console.log(`[WebSocket] Reconnecting... (${this.reconnectAttempts}/${this.maxReconnectAttempts})`);
          this.connect(token);
        }, this.reconnectInterval);
      } else if (event.code === 1000) {
        // Normal closure, reset current channel
        this.currentChannel = null;
      }
    };

    this.ws.onerror = (error) => {
      console.error("[WebSocket] Error:", error);
      this.listeners[WS_EVENTS.ERROR]?.(error);
    };
  }

  joinChannel(channelId) {
    if (!this.connected) {
      console.warn("[WebSocket] Not connected, cannot join channel");
      return;
    }

    const message = {
      type: WS_EVENTS.JOIN_CHANNEL,
      channelId: channelId
    };

    this.send(message);
    this.currentChannel = channelId;
    console.log("[WebSocket] Joining channel:", channelId);
  }

  leaveChannel() {
    if (!this.connected || !this.currentChannel) {
      return;
    }

    const message = {
      type: WS_EVENTS.LEAVE_CHANNEL,
      channelId: this.currentChannel
    };

    this.send(message);
    this.currentChannel = null;
    console.log("[WebSocket] Leaving channel");
  }

  sendMessage(content, attachmentIds = []) {
    if (!this.connected || !this.currentChannel) {
      console.warn("[WebSocket] Not connected or no channel joined");
      return;
    }

    const message = {
      type: WS_EVENTS.CHANNEL_MESSAGE,
      channelId: this.currentChannel,
      payload: {
        content: content,
        attachmentIds: attachmentIds
      }
    };

    this.send(message);
    console.log("[WebSocket] Sending message:", content);
  }

  fetchMessages(channelId) {
    return new Promise((resolve, reject) => {
      if (!this.connected) {
        reject(new Error("[WebSocket] Not connected"));
        return;
      }

      const message = {
        type: WS_EVENTS.FETCH_MESSAGES,
        channelId: channelId
      };

      const handleResponse = (response) => {
        console.log("[WebSocket] Received FETCH_MESSAGES_RESPONSE:", response);
        if (response.channelId === channelId) {
          this.off(WS_EVENTS.FETCH_MESSAGES_RESPONSE, handleResponse);
          // Map RedisMessage to expected format
          const mappedMessages = (response.messages || []).map((msg, index) => ({
            id: `msg-${channelId}-${index}`, // Generate ID since RedisMessage doesn't have id
            user: msg.username || 'Unknown',
            content: msg.content,
            timestamp: new Date(msg.timestamp).getTime(),
            formattedTime: new Date(msg.timestamp).toLocaleString(),
            flagged: false,
          })).reverse(); // Reverse to show oldest first
          console.log("[WebSocket] Mapped messages:", mappedMessages);
          resolve(mappedMessages);
        }
      };

      this.on(WS_EVENTS.FETCH_MESSAGES_RESPONSE, handleResponse);
      this.send(message);
      console.log("[WebSocket] Fetching messages for channel:", channelId);

      // Timeout after 5 seconds
      setTimeout(() => {
        this.off(WS_EVENTS.FETCH_MESSAGES_RESPONSE, handleResponse);
        reject(new Error("Fetch messages timeout"));
      }, 5000);
    });
  }

  send(data) {
    if (this.ws && this.ws.readyState === WebSocket.OPEN) {
      this.ws.send(JSON.stringify(data));
    } else {
      console.warn("[WebSocket] Cannot send, not connected");
    }
  }

  on(event, callback) {
    this.listeners[event] = callback;
  }

  off(event) {
    delete this.listeners[event];
  }

  disconnect() {
    if (this.ws) {
      this.ws.close(1000, "Client disconnect");
      this.ws = null;
    }
    this.connected = false;
    this.currentChannel = null;
    console.log("[WebSocket] Disconnected");
  }

  isConnected() {
    return this.connected;
  }

  getCurrentChannel() {
    return this.currentChannel;
  }
}

export const socket = new WebSocketClient();
