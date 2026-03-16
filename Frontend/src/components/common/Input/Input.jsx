import React from "react";
import "./Input.css";

function Input({
  type = "text",
  value,
  onChange,
  placeholder,
  name,
  required = false,
  ariaLabel,
}) {
  return (
    <input
      className="input"
      type={type}
      name={name}
      value={value}
      onChange={onChange}
      placeholder={placeholder}
      required={required}
      aria-label={ariaLabel}
    />
  );
}

export default Input;
