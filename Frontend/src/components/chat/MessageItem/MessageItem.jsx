// src/components/chat/MessageItem/MessageItem.jsx
import React from "react";
import Avatar from "../../common/Avatar/Avatar";
import "./MessageItem.css";

function MessageItem({ message }) {
  const formatTime = (timestamp) => {
    const date = new Date(timestamp);
    let hours = date.getHours();
    const minutes = date.getMinutes().toString().padStart(2, '0');
    const seconds = date.getSeconds().toString().padStart(2, '0');
    const ampm = hours >= 12 ? 'PM' : 'AM';
    hours = hours % 12;
    hours = hours ? hours : 12; // the hour '0' should be '12'
    return `${hours}:${minutes}:${seconds} ${ampm}`;
  };

  return (
    <div className="message-item">
      <Avatar username={message.user} />

      <div className="message-content">
        <div className="message-header">
          <span className="message-username">{message.user}</span>
          <span className="message-timestamp">
            {formatTime(message.timestamp)}
          </span>
        </div>

        <div className="message-body">{message.content}</div>

        {message.flagged && (
          <div className="message-warning">
            ⚠️ This message was flagged by AI moderation
          </div>
        )}
      </div>
    </div>
  );
}

export default MessageItem;
