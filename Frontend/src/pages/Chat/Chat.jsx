import React, { useState, useEffect, useContext } from "react";
import ServerSidebar from "../../components/layout/ServerSidebar/ServerSidebar";
import ChannelSidebar from "../../components/layout/ChannelSidebar/ChannelSidebar";
import ChatHeader from "../../components/layout/ChatHeader/ChatHeader";
import UserPanel from "../../components/layout/UserPanel/UserPanel";
import MessageList from "../../components/chat/MessageList/MessageList";
import MessageInput from "../../components/chat/MessageInput/MessageInput";
import { serverApi } from "../../api/server.api";
import { channelApi } from "../../api/channel.api";
import { AuthContext } from "../../context/AuthContext";
import { ChatContext } from "../../context/ChatContext";
import "./Chat.css";

function Chat() {
  const { isAuthenticated, user } = useContext(AuthContext);
  const { getMessagesForChannel, addMessage, setTyping, typingUsers, fetchMessagesForChannel, isLoadingMessages, setActiveChannel: setChatActiveChannel, channel: activeChannel } = useContext(ChatContext);

  const [servers, setServers] = useState([]);
  const [activeServer, setActiveServer] = useState(null);
  const [channels, setChannels] = useState([]);
  const [isCreateModalOpen, setIsCreateModalOpen] = useState(false);

  // eslint-disable-next-line react-hooks/exhaustive-deps
  useEffect(() => {
    const fetchServers = async () => {
      if (isAuthenticated) {
        try {
          const fetchedServers = await serverApi.fetchServers();
          setServers(fetchedServers);
          if (fetchedServers.length > 0 && !activeServer) {
            setActiveServer(fetchedServers[0].id);
          }
        } catch (error) {
          console.error("Failed to fetch servers:", error);
        }
      } else {
        setServers([]);
        setActiveServer(null);
        setChatActiveChannel(null);
      }
    };
    fetchServers();
  }, [isAuthenticated, activeServer, setChatActiveChannel]);

  // eslint-disable-next-line react-hooks/exhaustive-deps
  useEffect(() => {
    const fetchChannels = async () => {
      if (activeServer) {
        try {
          const fetchedChannels = await channelApi.fetchChannels(activeServer);
          const transformedChannels = fetchedChannels.map(channel => ({
            id: channel.channelId,
            name: channel.channelName,
            guildId: channel.guildId,
            adminUserId: channel.adminUserId,
            createdAt: channel.createdAt,
            updatedAt: channel.updatedAt,
            members: channel.members,
            category: "TEXT CHANNELS",
            type: "text"
          }));
          setChannels(transformedChannels);
          // Automatically select the first channel if no channel is active
          if (transformedChannels.length > 0 && !activeChannel) {
            setChatActiveChannel(transformedChannels[0]);
            fetchMessagesForChannel(transformedChannels[0].id);
          }
        } catch (error) {
          console.error("Failed to fetch channels:", error);
          setChannels([]);
        }
      } else {
        setChannels([]);
      }
    };
    fetchChannels();
  }, [activeServer]);



  const handleCreateServer = async (serverName) => {
    try {
      const newServer = await serverApi.createServer(serverName);
      setServers((prev) => [...prev, newServer]);
      setActiveServer(newServer.id);
      setChatActiveChannel(null); // Reset active channel when switching servers
    } catch (error) {
      console.error("Failed to create server:", error);
    }
  };

  const handleJoinServer = async (guildId) => {
    try {
      const server = await serverApi.joinServer(guildId, user?.userId);
      setServers((prev) => [...prev, server]);
      setActiveServer(server.id);
      setChatActiveChannel(null); // Reset active channel when switching servers
    } catch (error) {
      console.error("Failed to join server:", error);
    }
  };

  const handleSelectServer = (serverId) => {
    setActiveServer(serverId);
    setChatActiveChannel(null); // Reset active channel when switching servers
    // Automatically select the first channel if available
    const serverChannels = channels.filter(channel => channel.guildId === serverId);
    if (serverChannels.length > 0) {
      setChatActiveChannel(serverChannels[0]);
      fetchMessagesForChannel(serverChannels[0].id);
    }
  };

  const handleSelectChannel = (channelId) => {
    const channel = channels.find(channel => channel.id === channelId);
    setChatActiveChannel(channel);
    if (channel) {
      fetchMessagesForChannel(channel.id);
    }
  };

  useEffect(() => {
    if (activeChannel?.id) {
      fetchMessagesForChannel(activeChannel.id);
    }
  }, [activeChannel?.id, fetchMessagesForChannel]);

  return (
    <div className="app-root">
      <ServerSidebar
        servers={servers}
        activeServerId={activeServer}
        onSelectServer={handleSelectServer}
        onCreateServer={handleCreateServer}
        onJoinServer={handleJoinServer}
        isCreateModalOpen={isCreateModalOpen}
        setIsCreateModalOpen={setIsCreateModalOpen}
      />

      <div className="chat-main">
        <ChannelSidebar
          activeChannelId={activeChannel}
          onSelectChannel={handleSelectChannel}
          isOpen={true}
          activeServerId={activeServer}
          guildName={servers.find(server => server.id === activeServer)?.name}
          guildId={activeServer}
          channels={channels}
          onChannelCreated={(newChannel) => setChannels(prev => [...prev, newChannel])}
        />

        <div className="chat-container">
          <ChatHeader channel={activeChannel} />

          {!activeChannel ? (
            <div className="empty-chat">
              <div className="welcome-icon">💬</div>
              <h2>Welcome to Gtalk!</h2>
              <p>Select a channel to start chatting or create a new guild to get started.</p>
            </div>
          ) : (
            <MessageList
              messages={getMessagesForChannel(activeChannel.id)}
              typingUsers={typingUsers}
              loading={isLoadingMessages(activeChannel.id)}
            />
          )}

          {activeChannel && user && (
            <MessageInput
              onSendMessage={(content, username) => addMessage(activeChannel.id, content, user.userId, user.userName)}
              onTyping={setTyping}
              user={user}
              channelName={activeChannel.name}
            />
          )}
        </div>

        <UserPanel />
      </div>
    </div>
  );
}

export default Chat;
