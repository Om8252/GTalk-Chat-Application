import React from "react";
import "./Avatar.css";

function Avatar({ username, size = 36 }) {
  const initial = username ? username.charAt(0).toUpperCase() : "?";

  return (
    <div
      className="avatar"
      style={{ width: size, height: size }}
      aria-label={`Avatar of ${username}`}
    >
      {initial}
    </div>
  );
}

export default Avatar;
