import React, { createContext, useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { socket } from "../api/socket";
import httpClient from "../api/httpClient";
import { attachmentApi } from "../api/attachment.api";

export const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const navigate = useNavigate();
  const [user, setUser] = useState(null);
  const [token, setToken] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const storedToken = localStorage.getItem("gtalk_token");
    const storedUser = localStorage.getItem("gtalk_user");

    if (storedToken && storedUser && storedUser !== "undefined" && storedUser !== "null") {
      setToken(storedToken);
      try {
        const parsedUser = JSON.parse(storedUser);
        if (parsedUser && parsedUser.userId) {
          setUser(parsedUser);
          // Connect WebSocket when restoring session
          socket.connect(storedToken);

          // Fetch latest user data from backend to ensure it's up to date
          httpClient.get(`/api/users/${parsedUser.userId}`)
            .then(response => {
              const updatedUser = response.data;
              if (updatedUser) {
                setUser({ ...updatedUser });
                localStorage.setItem("gtalk_user", JSON.stringify(updatedUser));
              }
            })
            .catch(error => {
              console.error("Failed to fetch updated user data:", error);
              // Keep the stored user data if fetch fails
            });
        }
      } catch (error) {
        console.error("Failed to parse stored user data:", error);
        // Clear invalid data
        localStorage.removeItem("gtalk_user");
      }
    }

    setLoading(false);
  }, []);

  const login = async (userData, jwtToken) => {
    if (userData && jwtToken) {
      localStorage.setItem("gtalk_token", jwtToken);
      localStorage.setItem("gtalk_user", JSON.stringify(userData));
      setUser(userData);
      setToken(jwtToken);
      // Connect WebSocket on login
      socket.connect(jwtToken);

      // Fetch latest user data from backend to ensure description and profile pic are up to date
      try {
        const response = await httpClient.get(`/api/users/${userData.userId}`);
        const updatedUser = response.data;
        if (updatedUser) {
          setUser({ ...updatedUser });
          localStorage.setItem("gtalk_user", JSON.stringify(updatedUser));
        }
      } catch (error) {
        console.error("Failed to fetch updated user data after login:", error);
        // Keep the login user data if fetch fails
      }
    }
  };

  const logout = () => {
    localStorage.removeItem("gtalk_token");
    localStorage.removeItem("gtalk_user");
    setUser(null);
    setToken(null);
    // Disconnect WebSocket on logout
    socket.disconnect();
    // Redirect to login page
    navigate("/login");
    // Force page reload to ensure clean state
    window.location.reload();
  };

  const refreshUser = async () => {
    if (!user || !user.userId) {
      return;
    }

    try {
      const response = await httpClient.get(`/api/users/${user.userId}`);
      const updatedUser = response.data;
      setUser({ ...updatedUser });
      localStorage.setItem("gtalk_user", JSON.stringify(updatedUser));
    } catch (error) {
      console.error("Failed to refresh user data:", error);
    }
  };

  const getUserProfilePictureUrl = (userId) => {
    // First try to get from current user if it's the same user
    if (user && user.userId === userId && user.profileAttachmentId) {
      return `${attachmentApi.getAttachmentUrl(user.profileAttachmentId)}?t=${Date.now()}`;
    }
    // For other users, we would need to fetch their data, but for now return default
    return "/default-avatar.png";
  };

  const updateProfile = async (profileData) => {
    if (!user || !user.userId) {
      throw new Error("User not authenticated");
    }

    // Update description if provided
    if (profileData.description !== undefined) {
      await httpClient.post(`/api/users/${user.userId}/description`, {
        description: profileData.description,
      });
    }

    // Update profile picture if attachmentId provided
    if (profileData.attachmentId) {
      await httpClient.post(`/api/users/${user.userId}/profile-picture`, {
        attachmentId: profileData.attachmentId,
      });
    }

  // Fetch updated user data from backend
  const response = await httpClient.get(`/api/users/${user.userId}`);
  const updatedUser = response.data;

  // Update local user state and localStorage
  setUser(prev => ({ ...prev, ...updatedUser }));
  localStorage.setItem("gtalk_user", JSON.stringify(updatedUser));
  };

  return (
    <AuthContext.Provider
      value={{
        user,
        token,
        isAuthenticated: !!token,
        login,
        logout,
        updateProfile,
        refreshUser,
        getUserProfilePictureUrl,
        loading,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
}
