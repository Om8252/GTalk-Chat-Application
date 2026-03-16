import React from "react";
import "./Button.css";

function Button({
  children,
  variant = "primary",
  onClick,
  type = "button",
  disabled = false,
  ariaLabel,
}) {
  return (
    <button
      type={type}
      className={`btn ${variant}`}
      onClick={onClick}
      disabled={disabled}
      aria-label={ariaLabel}
    >
      {children}
    </button>
  );
}

export default Button;
