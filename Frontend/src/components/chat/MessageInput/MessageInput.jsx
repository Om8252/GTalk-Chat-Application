import React, { useState } from "react";
import "./MessageInput.css";

function MessageInput({ onSendMessage, onTyping, user, channelName }) {
  const [value, setValue] = useState("");

  const handleChange = (e) => {
    setValue(e.target.value);
    onTyping(true);
  };

  const handleSend = () => {
    if (!value.trim() || !user) return;

    onSendMessage(value, user.userName);

    setValue("");
    onTyping(false);
  };

  const handleKeyDown = (e) => {
    if (e.key === "Enter" && !e.shiftKey) {
      e.preventDefault();
      handleSend();
    }
  };

  return (
    <div className="message-input">
      <button className="icon-btn" aria-label="Emoji">
        😊
      </button>

      <textarea
        className="message-textarea"
        placeholder={`Message #${channelName || 'general'}`}
        value={value}
        onChange={handleChange}
        onKeyDown={handleKeyDown}
        rows={1}
      />

      <button className="icon-btn" aria-label="Upload File">
        📎
      </button>

      <button className="send-btn" onClick={handleSend}>
        Send
      </button>
    </div>
  );
}

export default MessageInput;
