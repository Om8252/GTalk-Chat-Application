import React, { useState, useEffect, useRef } from "react";
import useAuth from "../../../hooks/useAuth";
import Modal from "../../Modal/Modal";
import { attachmentApi } from "../../../api/attachment.api";
import "./ProfileModal.css";

const MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
const MAX_DESCRIPTION_LENGTH = 500;
const ALLOWED_IMAGE_TYPES = ['image/jpeg', 'image/png', 'image/gif', 'image/webp'];

function ProfileModal({ onClose }) {
  const { user, refreshUser, updateProfile } = useAuth();
  const httpClient = require("../../../api/httpClient").default;
  const fileInputRef = useRef(null);
  const [formData, setFormData] = useState({
    description: user?.description || "",
    avatarPreview: user?.profileAttachmentId ? attachmentApi.getAttachmentUrl(user.profileAttachmentId) : "",
    attachmentId: null,
  });
  const [uploading, setUploading] = useState(false);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");
  const [isDirty, setIsDirty] = useState(false);

  // Update formData when user prop changes
  useEffect(() => {
    setFormData((prev) => ({
      ...prev,
      description: user?.description || "",
      avatarPreview: user?.profileAttachmentId ? attachmentApi.getAttachmentUrl(user.profileAttachmentId) : "",
    }));
  }, [user]);

  // Clean up Blob URLs to prevent memory leaks
  useEffect(() => {
    return () => {
      if (formData.avatarPreview && formData.avatarPreview.startsWith("blob:")) {
        URL.revokeObjectURL(formData.avatarPreview);
      }
    };
  }, [formData.avatarPreview]);

  // Warn user about unsaved changes
  useEffect(() => {
    const handleBeforeUnload = (e) => {
      if (isDirty) {
        e.preventDefault();
        e.returnValue = '';
      }
    };

    window.addEventListener('beforeunload', handleBeforeUnload);
    return () => window.removeEventListener('beforeunload', handleBeforeUnload);
  }, [isDirty]);

  const validateFile = (file) => {
    if (!file) return "Please select a file";

    if (file.size > MAX_FILE_SIZE) {
      return "File size must be less than 10MB";
    }

    if (!ALLOWED_IMAGE_TYPES.includes(file.type)) {
      return "Please select a valid image file (JPEG, PNG, GIF, or WebP)";
    }

    return null;
  };

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
    setIsDirty(true);
    setError("");
    setSuccess("");
  };

  const handleFileChange = async (e) => {
    const file = e.target.files[0];
    if (!file) return;

    const validationError = validateFile(file);
    if (validationError) {
      setError(validationError);
      // Reset file input
      e.target.value = '';
      return;
    }

    setError("");
    setUploading(true);

    try {
      // Create preview immediately for better UX
      const previewUrl = URL.createObjectURL(file);

      // Upload to server
      const attachmentId = await attachmentApi.uploadAttachment(file);

      setFormData((prev) => ({
        ...prev,
        avatarPreview: previewUrl,
        attachmentId: attachmentId,
      }));
      setIsDirty(true);
      setSuccess("Image uploaded successfully!");
    } catch (err) {
      console.error("Upload error:", err);
      setError(err.message || "Failed to upload image. Please try again.");
      // Reset file input on error
      e.target.value = '';
    } finally {
      setUploading(false);
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    // Validate description length
    if (formData.description.length > MAX_DESCRIPTION_LENGTH) {
      setError(`Description must be less than ${MAX_DESCRIPTION_LENGTH} characters`);
      return;
    }

    setUploading(true);
    setError("");

    try {
      await updateProfile({
        description: formData.description.trim(),
        // Only send attachmentId if a new one was uploaded
        ...(formData.attachmentId && { attachmentId: formData.attachmentId }),
      });
      setSuccess("Profile updated successfully!");
      setIsDirty(false);
      setTimeout(() => onClose(), 1500);
    } catch (err) {
      console.error("Update error:", err);
      setError(err.message || "Failed to update profile. Please try again.");
    } finally {
      setUploading(false);
    }
  };

  const handleCancel = () => {
    if (isDirty && !window.confirm("You have unsaved changes. Are you sure you want to cancel?")) {
      return;
    }
    onClose();
  };

  const removeAvatar = () => {
    if (formData.avatarPreview && formData.avatarPreview.startsWith("blob:")) {
      URL.revokeObjectURL(formData.avatarPreview);
    }
    setFormData((prev) => ({
      ...prev,
      avatarPreview: user?.profileAttachmentId ? attachmentApi.getAttachmentUrl(user.profileAttachmentId) : "/default-avatar.png",
      attachmentId: null,
    }));
    setIsDirty(true);
    if (fileInputRef.current) {
      fileInputRef.current.value = '';
    }
  };

  return (
    <Modal isOpen={true} title="Edit Profile" onClose={handleCancel}>
      <div className="profile-modal">
        {error && (
          <div className="error-message" role="alert" aria-live="polite">
            {error}
          </div>
        )}
        {success && (
          <div className="success-message" role="status" aria-live="polite">
            {success}
          </div>
        )}

        <form onSubmit={handleSubmit} noValidate>
          <div className="form-group">
            <label htmlFor="avatar">
              Profile Picture
              <span className="field-hint">
                (max 10MB, JPEG/PNG/GIF/WebP only)
              </span>
            </label>
            <input
              ref={fileInputRef}
              type="file"
              id="avatar"
              name="avatar"
              accept="image/*"
              onChange={handleFileChange}
              disabled={uploading}
              aria-describedby="avatar-hint"
            />
            <div id="avatar-hint" className="sr-only">
              Select an image file to upload as your profile picture
            </div>

            {formData.avatarPreview && (
              <div className="avatar-wrapper">
                <img
                  src={formData.avatarPreview}
                  alt="Profile preview"
                  className="avatar-preview"
                />
                <button
                  type="button"
                  className="remove-avatar-btn"
                  onClick={removeAvatar}
                  disabled={uploading}
                  aria-label="Remove profile picture"
                  title="Remove profile picture"
                >
                  ×
                </button>
              </div>
            )}
          </div>

          <div className="form-group">
            <label htmlFor="description">
              Description
              <span className="field-hint">
                ({formData.description.length}/{MAX_DESCRIPTION_LENGTH} characters)
              </span>
            </label>
            <textarea
              id="description"
              name="description"
              rows="4"
              value={formData.description}
              onChange={handleChange}
              disabled={uploading}
              placeholder="Tell us about yourself..."
              maxLength={MAX_DESCRIPTION_LENGTH}
              aria-describedby="description-hint"
            />
            <div id="description-hint" className="sr-only">
              Enter a description for your profile (maximum {MAX_DESCRIPTION_LENGTH} characters)
            </div>
          </div>

          <div className="modal-actions">
            <button
              type="button"
              className="btn-secondary"
              onClick={handleCancel}
              disabled={uploading}
            >
              Cancel
            </button>
            <button
              type="submit"
              className="btn-primary"
              disabled={uploading || !isDirty}
            >
              {uploading ? "Saving..." : "Save Changes"}
            </button>
          </div>
        </form>
      </div>
    </Modal>
  );
}

export default ProfileModal;
