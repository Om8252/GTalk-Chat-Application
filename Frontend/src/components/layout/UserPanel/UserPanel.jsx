import React, { useState } from "react";
import useAuth from "../../../hooks/useAuth";
import ProfileModal from "./ProfileModal";
import { attachmentApi } from "../../../api/attachment.api";
import "./UserPanel.css";

function UserPanel({ activeChannel }) {
  const { user, logout, getUserProfilePictureUrl } = useAuth();
  const [status, setStatus] = useState("online");
  const [isModalOpen, setIsModalOpen] = useState(false);

  const handleProfileClick = () => {
    setIsModalOpen(true);
  };

  const closeModal = () => {
    setIsModalOpen(false);
  };

  const userAvatar = user ? getUserProfilePictureUrl(user.userId) : "/default-avatar.png";

  return (
    <div className="user-panel">
      <div className="user-panel-header">
        <div className="profile-section">
          <img
            src={userAvatar}
            alt="Profile"
            className="profile-icon"
          />
          <div className="user-info">
            <span className="username">{user?.userName || "User"}</span>
            <span className="user-description">{user?.description || "No description"}</span>
            <div className="status-info">
              <div className={`status-dot ${status}`} />
              <select
                className="status-select"
                value={status}
                onChange={(e) => setStatus(e.target.value)}
                aria-label="Change status"
              >
                <option value="online">Online</option>
                <option value="idle">Idle</option>
                <option value="dnd">Do Not Disturb</option>
              </select>
            </div>
          </div>
          <button className="edit-profile-btn" onClick={handleProfileClick} aria-label="Edit Profile">
            ✏️
          </button>
        </div>
      </div>

      <div className="user-panel-middle">
        {activeChannel ? (
          <div className="channel-details">
            <h3 className="channel-name">{activeChannel.name}</h3>
            <p className="channel-description">{activeChannel.description || "No description"}</p>
            <div className="channel-members">
              <h4>Members</h4>
              <ul>
                {activeChannel.members?.map((member, index) => (
                  <li key={index} className="member-item">
                    <img src="/default-avatar.png" alt={member.userName} className="member-avatar" />
                    <span>{member.userName}</span>
                  </li>
                )) || <li>No members</li>}
              </ul>
            </div>
          </div>
        ) : (
          <div className="no-channel">Select a channel to view details</div>
        )}
      </div>

      <div className="user-panel-footer">
        <button className="logout-btn" onClick={logout}>
          Logout
        </button>
      </div>

      {isModalOpen && <ProfileModal onClose={closeModal} />}
    </div>
  );
}

export default UserPanel;

