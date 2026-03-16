export function formatTime(timestamp) {
  return new Date(timestamp).toLocaleTimeString([], {
    hour: "2-digit",
    minute: "2-digit",
  });
}

export function classNames(...classes) {
  return classes.filter(Boolean).join(" ");
}

export function truncate(text, max = 80) {
  if (!text) return "";
  return text.length > max ? `${text.slice(0, max)}…` : text;
}

export function generateId(prefix = "") {
  return `${prefix}${Date.now()}-${Math.random().toString(36).slice(2, 8)}`;
}
