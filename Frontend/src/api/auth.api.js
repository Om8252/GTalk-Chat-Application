import httpClient from "./httpClient";

export const authApi = {
  login: async (credentials) => {
    try {
      const response = await httpClient.post("/api/users/login", credentials);
      return {
        token: response.token,
        user: {
          userId: response.user.userId,
          userName: response.user.userName,
          description: response.user.description,
          profileAttachmentId: response.user.profileAttachmentId,
          status: "online",
          createdAt: response.user.createdAt,
          updatedAt: response.user.updatedAt,
        },
      };
    } catch (error) {
      throw new Error("Login failed");
    }
  },

  register: async (payload) => {
    const response = await httpClient.post("/api/users/register", payload);
    return {
      token: null, // Register doesn't return token, user needs to login after
      user: {
        userId: response.userId,
        userName: response.userName,
        status: "online",
        createdAt: response.createdAt,
        updatedAt: response.updatedAt,
      },
    };
  },

  logout: async () => {
    // For now, just return success, as backend might not have logout endpoint
    return { success: true };
  },
};
