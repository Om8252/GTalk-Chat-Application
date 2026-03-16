import React, { useState, useContext } from "react";
import Modal from "../../Modal/Modal";
import Input from "../../common/Input/Input";
import Button from "../../common/Button/Button";
import { channelApi } from "../../../api/channel.api";
import { AuthContext } from "../../../context/AuthContext";
import "./ChannelSidebar.css";

function ChannelSidebar({ activeChannelId, onSelectChannel, isOpen, activeServerId, guildName, channels, onChannelCreated }) {
  const { user } = useContext(AuthContext);
  const [collapsed, setCollapsed] = useState({});
  const [isCreateChannelModalOpen, setIsCreateChannelModalOpen] = useState(false);
  const [newChannelName, setNewChannelName] = useState("");
  const [newChannelType, setNewChannelType] = useState("text");

  const toggleCategory = (id) => {
    setCollapsed((prev) => ({ ...prev, [id]: !prev[id] }));
  };

  const handleCreateChannel = async () => {
    if (newChannelName.trim() && activeServerId && user) {
      try {
        console.log("Creating channel:", { name: newChannelName.trim(), type: newChannelType, serverId: activeServerId });
        const newChannel = await channelApi.createChannel(activeServerId, newChannelName.trim());
        console.log("Channel created:", newChannel);
        // Note: channels are now passed as props, so we can't modify them here. The parent should handle this.
        onChannelCreated({
          id: newChannel.channelId,
          name: newChannel.channelName,
          guildId: newChannel.guildId,
          adminUserId: newChannel.adminUserId,
          createdAt: newChannel.createdAt,
          updatedAt: newChannel.updatedAt,
          members: newChannel.members,
          category: "TEXT CHANNELS", // Backend doesn't distinguish text/voice yet, default to text
          type: "text"
        });
        setNewChannelName("");
        setNewChannelType("text");
        setIsCreateChannelModalOpen(false);
        console.log("Channel added to list");
      } catch (error) {
        console.error("Failed to create channel:", error);
        alert(`Failed to create channel: ${error.message}`);
      }
    }
  };

  // Group channels by category
  const groupedChannels = channels.reduce((acc, channel) => {
    const category = channel.category;
    if (!acc[category]) {
      acc[category] = [];
    }
    acc[category].push(channel);
    return acc;
  }, {});

  return (
    <aside className={`channel-sidebar ${isOpen ? "open" : ""}`}>
      <div className="channel-header">
        <h3>{guildName ? `${guildName} (ID: ${activeServerId})` : "Select a Guild"}</h3>
        <button
          className="create-channel-btn"
          onClick={() => setIsCreateChannelModalOpen(true)}
          title="Create Channel"
          disabled={!activeServerId}
        >
          +
        </button>
      </div>

      <div className="channel-list">
        {Object.entries(groupedChannels).map(([categoryName, categoryChannels]) => (
          <div key={categoryName} className="channel-category">
            <div
              className="category-header"
              onClick={() => toggleCategory(categoryName)}
              role="button"
              tabIndex={0}
            >
              <span>
                {collapsed[categoryName] ? "▶" : "▼"} {categoryName}
              </span>
            </div>

            {!collapsed[categoryName] &&
              categoryChannels.map((channel) => (
                <div
                  key={channel.id}
                  className={`channel-item ${
                    activeChannelId === channel.id ? "active" : ""
                  }`}
                  onClick={() => onSelectChannel(channel.id)}
                  role="button"
                  tabIndex={0}
                >
                  <span className="channel-prefix">
                    {channel.type === "text" ? "#" : "🔊"}
                  </span>
                  <span className="channel-name">{channel.name}</span>
                </div>
              ))}
          </div>
        ))}
      </div>

      <Modal
        isOpen={isCreateChannelModalOpen}
        title="Create Channel"
        onClose={() => setIsCreateChannelModalOpen(false)}
      >
        <div className="create-channel-form">
          <Input
            type="text"
            placeholder="Channel name"
            value={newChannelName}
            onChange={(e) => setNewChannelName(e.target.value)}
          />
          <div className="channel-type-selector">
            <label>Channel Type:</label>
            <select
              value={newChannelType}
              onChange={(e) => setNewChannelType(e.target.value)}
              className="channel-type-select"
            >
              <option value="text">📝 Text Channel</option>
              <option value="voice">🔊 Voice Channel</option>
            </select>
          </div>
          <div className="modal-actions">
            <Button onClick={() => setIsCreateChannelModalOpen(false)} variant="secondary">
              Cancel
            </Button>
            <Button onClick={handleCreateChannel} disabled={!newChannelName.trim()}>
              Create Channel
            </Button>
          </div>
        </div>
      </Modal>
    </aside>
  );
}

export default ChannelSidebar;
