import React from "react";
import "./ChatHeader.css";

function ChatHeader({ channel }) {
  if (!channel) {
    return (
      <header className="chat-header">
        <span className="chat-title">Select a channel</span>
      </header>
    );
  }

  return (
    <header className="chat-header">
      <div className="chat-title">
        <span className="chat-icon">
          {channel.type === "text" ? "#" : "🔊"}
        </span>
        <span className="chat-name">{channel.name}</span>
      </div>

      <div className="chat-actions">
        <button aria-label="Threads">🧵</button>
        <button aria-label="Pinned Messages">📌</button>
        <button aria-label="Members">👥</button>
        <button aria-label="Search">🔍</button>
      </div>
    </header>
  );
}

export default ChatHeader;
