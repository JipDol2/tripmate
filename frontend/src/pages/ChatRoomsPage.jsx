import React, { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { api, getErrorMessage } from "../lib/api";

function formatTime(value) {
  if (!value) return "";

  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return "";

  const month = String(date.getMonth() + 1).padStart(2, "0");
  const day = String(date.getDate()).padStart(2, "0");
  const hours = String(date.getHours()).padStart(2, "0");
  const minutes = String(date.getMinutes()).padStart(2, "0");

  return `${month}.${day} ${hours}:${minutes}`;
}

export default function ChatRoomsPage() {
  const [rooms, setRooms] = useState([]);

  const loadRooms = () => {
    api.get("/chats/rooms")
      .then((res) => setRooms(res.data))
      .catch((error) => alert(getErrorMessage(error)));
  };

  useEffect(() => {
    loadRooms();
    const timer = window.setInterval(loadRooms, 5000);
    return () => window.clearInterval(timer);
  }, []);

  return (
    <section className="page chat-page">
      <h1>채팅</h1>

      <div className="chat-room-list">
        {rooms.map((room) => (
          <Link className="chat-room-card" to={`/chats/${room.id}`} key={room.id}>
            <div>
              <strong>{room.postTitle}</strong>
              <p>{room.lastMessage || "아직 메시지가 없습니다."}</p>
              <span>{room.tripEnded ? "여행 종료 · " : ""}{room.participantNicknames.join(", ")}</span>
            </div>
            <div className="chat-room-card-side">
              <time>{formatTime(room.lastMessageAt)}</time>
              {room.unreadCount > 0 && (
                <span className="chat-unread-count">{room.unreadCount > 99 ? "99+" : room.unreadCount}</span>
              )}
            </div>
          </Link>
        ))}

        {rooms.length === 0 && <p className="empty">아직 열린 채팅방이 없습니다.</p>}
      </div>
    </section>
  );
}
