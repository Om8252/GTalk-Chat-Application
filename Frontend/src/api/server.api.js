import httpClient from "./httpClient";

export const serverApi = {
  fetchServers: async () => {
    const response = await httpClient.get("/api/guilds");
    return response.map(guild => ({
      id: guild.guildId.toString(),
      name: guild.guildName,
      icon: "💬", // Default icon
      members: guild.members.length,
    }));
  },

  createServer: async (serverName) => {
    const response = await httpClient.post("/api/guilds", { guildName: serverName });
    return {
      id: response.guildId.toString(),
      name: response.guildName,
      icon: "💬",
      members: response.members.length,
    };
  },

  joinServer: async (guildId, userId) => {
    const response = await httpClient.post(`/api/guilds/${guildId}/join`, { userId: userId, role: "member" });
    return {
      id: response.guildId.toString(),
      name: response.guildName,
      icon: "💬",
      members: response.members.length,
    };
  },
};

