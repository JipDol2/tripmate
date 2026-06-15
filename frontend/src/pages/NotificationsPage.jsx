import React, { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { api, getErrorMessage } from "../lib/api";

function formatNotificationTime(value) {
  if (!value) return "";

  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return "";

  const month = String(date.getMonth() + 1).padStart(2, "0");
  const day = String(date.getDate()).padStart(2, "0");
  const hours = String(date.getHours()).padStart(2, "0");
  const minutes = String(date.getMinutes()).padStart(2, "0");

  return `${month}.${day} ${hours}:${minutes}`;
}

export default function NotificationsPage() {
  const navigate = useNavigate();
  const [notifications, setNotifications] = useState([]);

  const loadNotifications = async () => {
    try {
      const res = await api.get("/notifications");
      setNotifications(res.data);
    } catch (error) {
      alert(getErrorMessage(error));
    }
  };

  useEffect(() => {
    loadNotifications();
  }, []);

  const openNotification = async (notification) => {
    try {
      if (!notification.read) {
        await api.patch(`/notifications/${notification.id}/read`);
      }

      if (notification.linkUrl) {
        navigate(notification.linkUrl);
      } else {
        await loadNotifications();
      }
    } catch (error) {
      alert(getErrorMessage(error));
    }
  };

  const markAllAsRead = async () => {
    try {
      await api.patch("/notifications/read-all");
      await loadNotifications();
    } catch (error) {
      alert(getErrorMessage(error));
    }
  };

  const hasUnread = notifications.some((notification) => !notification.read);

  return (
    <section className="page notifications-page">
      <div className="notification-head">
        <h1>알림</h1>
        <button className="text-button" onClick={markAllAsRead} disabled={!hasUnread} type="button">
          모두 읽음
        </button>
      </div>

      <div className="notification-list">
        {notifications.map((notification) => (
          <button
            className={`notification-card ${notification.read ? "" : "unread"}`}
            key={notification.id}
            onClick={() => openNotification(notification)}
            type="button"
          >
            <span className="notification-unread-dot" />
            <div>
              <strong>{notification.title}</strong>
              <p>{notification.message}</p>
              <span>{formatNotificationTime(notification.createdAt)}</span>
            </div>
          </button>
        ))}

        {notifications.length === 0 && <p className="empty">아직 도착한 알림이 없습니다.</p>}
      </div>
    </section>
  );
}
