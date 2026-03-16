import httpClient from "./httpClient";

const BASE_URL = "http://localhost:8080";

export const attachmentApi = {
  uploadAttachment: async (file) => {
    const formData = new FormData();
    formData.append("file", file);

    const token = localStorage.getItem("gtalk_token");
    const headers = {
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
    };

    const response = await fetch(`${BASE_URL}/api/attachments`, {
      method: "POST",
      headers,
      body: formData,
    });

    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }

    const data = await response.json();
    return data.attachmentId;
  },

  getAttachmentUrl: (attachmentId) => {
    if (!attachmentId) return "/default-avatar.png";
    const token = localStorage.getItem("gtalk_token");
    return `${BASE_URL}/api/attachments/${attachmentId}${token ? `?token=${token}` : ''}`;
  },
};
