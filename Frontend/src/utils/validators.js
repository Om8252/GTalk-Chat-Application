export function isValidEmail(email) {
  return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email);
}

export function isStrongPassword(password) {
  if (password.length < 6) return false;
  return /[A-Za-z]/.test(password) && /\d/.test(password);
}

export function isValidUsername(username) {
  return /^[a-zA-Z0-9_]{3,16}$/.test(username);
}
