import httpClient from "./httpClient.js";

export const channelApi = {
  fetchChannels: async (serverId) => {
    try {
      const response = await httpClient.get(`/api/channels/guild/${serverId}`);
      return response;
    } catch (error) {
      console.error("Failed to fetch channels:", error);
      return [];
    }
  },

  createChannel: async (guildId, channelName) => {
    const response = await httpClient.post(`/api/channels`, {
      guildId: guildId,
      name: channelName
    });
    return response;
  },

  joinChannel: async (channelId, role = "member") => {
    const response = await httpClient.post(`/api/channels/${channelId}/join`, {
      role: role
    });
    return response;
  },
};
