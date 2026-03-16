import React, { useEffect, useRef } from "react";
import MessageItem from "../MessageItem/MessageItem";
import "./MessageList.css";

function MessageList({ messages, typingUsers, loading }) {
  const bottomRef = useRef(null);

  useEffect(() => {
    bottomRef.current?.scrollIntoView({ behavior: "smooth" });
  }, [messages, typingUsers]);

  return (
    <div className="message-list">
      {loading && (
        <div className="loading-indicator">
          Loading messages...
        </div>
      )}

      {messages.map((msg) => (
        <MessageItem key={msg.id} message={msg} />
      ))}

      {typingUsers.length > 0 && (
        <div className="typing-indicator">
          {typingUsers.join(", ")} typing...
        </div>
      )}

      <div ref={bottomRef} />
    </div>
  );
}

export default MessageList;
