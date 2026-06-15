import React, { useEffect, useMemo, useRef, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { api, getErrorMessage } from "../lib/api";
import { useAuth } from "../context/AuthContext";

function createWebSocketUrl(roomId) {
  const token = localStorage.getItem("tripmate_token");
  const apiUrl = new URL(api.defaults.baseURL);
  const protocol = apiUrl.protocol === "https:" ? "wss:" : "ws:";
  const query = new URLSearchParams({ roomId, token });

  return `${protocol}//${apiUrl.host}/ws/chats?${query.toString()}`;
}

function formatTime(value) {
  if (!value) return "";

  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return "";

  return `${String(date.getHours()).padStart(2, "0")}:${String(date.getMinutes()).padStart(2, "0")}`;
}

export default function ChatRoomPage() {
  const { roomId } = useParams();
  const navigate = useNavigate();
  const { user } = useAuth();
  const [room, setRoom] = useState(null);
  const [messages, setMessages] = useState([]);
  const [content, setContent] = useState("");
  const [connected, setConnected] = useState(false);
  const [reviewTargets, setReviewTargets] = useState([]);
  const [reviewForms, setReviewForms] = useState({});
  const [menuOpen, setMenuOpen] = useState(false);
  const [soloLeavePromptOpen, setSoloLeavePromptOpen] = useState(false);
  const socketRef = useRef(null);
  const endRef = useRef(null);

  const isHost = room?.hostId === user?.id;
  const canJoin = room && !room.tripEnded && !room.myCompanionJoined && room.currentParticipants < room.maxParticipants;
  const title = useMemo(() => room?.postTitle || "채팅방", [room?.postTitle]);

  const loadRoom = async () => {
    try {
      const res = await api.get(`/chats/rooms/${roomId}`);
      setRoom(res.data);
    } catch (error) {
      alert(getErrorMessage(error));
    }
  };

  const loadReviewTargets = async () => {
    try {
      const res = await api.get(`/reviews/rooms/${roomId}/targets`);
      setReviewTargets(res.data);
    } catch {
      setReviewTargets([]);
    }
  };

  const markRoomAsRead = async () => {
    try {
      await api.patch(`/chats/rooms/${roomId}/read`);
    } catch {
      // 읽음 처리는 화면 흐름을 막지 않습니다.
    }
  };

  useEffect(() => {
    loadRoom();
    api.get(`/chats/rooms/${roomId}/messages`)
      .then((res) => {
        setMessages(res.data);
        markRoomAsRead();
      })
      .catch((error) => alert(getErrorMessage(error)));
  }, [roomId, user?.id]);

  useEffect(() => {
    const timer = window.setInterval(loadRoom, 5000);
    return () => window.clearInterval(timer);
  }, [roomId]);

  useEffect(() => {
    if (room?.tripEnded && room.myCompanionJoined) {
      loadReviewTargets();
      return;
    }

    setReviewTargets([]);
  }, [room?.tripEnded, room?.myCompanionJoined, roomId]);

  useEffect(() => {
    const socket = new WebSocket(createWebSocketUrl(roomId));
    socketRef.current = socket;

    socket.onopen = () => setConnected(true);
    socket.onclose = () => setConnected(false);
    socket.onerror = () => setConnected(false);
    socket.onmessage = (event) => {
      const message = JSON.parse(event.data);

      if (message.type === "LEAVE") {
        if (message.userId !== user?.id) {
          setMessages((current) => [
            ...current,
            {
              id: `leave-${message.userId}-${Date.now()}`,
              type: "SYSTEM",
              content: `${message.nickname || "상대방"}님이 채팅방을 나갔습니다.`,
              createdAt: new Date().toISOString(),
            },
          ]);

          if (message.remainingParticipants <= 1) {
            setSoloLeavePromptOpen(true);
          }
        }

        loadRoom();
        return;
      }

      setMessages((current) => [...current, message]);
      markRoomAsRead();
    };

    return () => {
      socket.close();
    };
  }, [roomId]);

  useEffect(() => {
    endRef.current?.scrollIntoView({ block: "end" });
  }, [messages]);

  const sendMessage = (e) => {
    e.preventDefault();

    if (!content.trim() || socketRef.current?.readyState !== WebSocket.OPEN) {
      return;
    }

    socketRef.current.send(JSON.stringify({ content }));
    setContent("");
  };

  const joinCompanion = async () => {
    try {
      const res = await api.post(`/chats/rooms/${roomId}/join`);
      setRoom(res.data);
      alert("동행에 참가했습니다.");
    } catch (error) {
      alert(getErrorMessage(error));
    }
  };

  const leaveRoom = async () => {
    try {
      await api.patch(`/chats/rooms/${roomId}/leave`);
      setMenuOpen(false);
      setSoloLeavePromptOpen(false);
      navigate("/chats", { replace: true });
    } catch (error) {
      alert(getErrorMessage(error));
    }
  };

  const updateReviewForm = (userId, field, value) => {
    setReviewForms((current) => ({
      ...current,
      [userId]: {
        rating: 5,
        content: "",
        ...current[userId],
        [field]: value,
      },
    }));
  };

  const submitReview = async (target) => {
    const form = {
      rating: 5,
      content: "",
      ...reviewForms[target.userId],
    };

    try {
      await api.post("/reviews", {
        roomId: Number(roomId),
        revieweeId: target.userId,
        rating: Number(form.rating),
        content: form.content,
      });
      setReviewForms((current) => ({
        ...current,
        [target.userId]: { rating: 5, content: "" },
      }));
      await loadReviewTargets();
      alert("평가가 등록되었습니다.");
    } catch (error) {
      alert(getErrorMessage(error));
    }
  };

  return (
    <section className="page chat-room-page">
      <div className="chat-room-head">
        <button className="back-button" onClick={() => navigate("/chats")} type="button">‹</button>
        <h1>{title}</h1>
        {room && (
          <button
            className="chat-menu-button"
            onClick={() => setMenuOpen(true)}
            type="button"
            aria-label="채팅방 메뉴"
          >
            <span />
            <span />
            <span />
          </button>
        )}
      </div>

      {room && !isHost && !room.tripEnded && !room.myCompanionJoined && (
        <div className="chat-action-panel">
          <span>동행에 참가하면 여행 종료 후 서로 평가할 수 있어요.</span>
          <div>
            <button className="primary-button" onClick={joinCompanion} disabled={!canJoin} type="button">
              {room.currentParticipants >= room.maxParticipants ? "모집 완료" : "참가하기"}
            </button>
          </div>
        </div>
      )}

      {room?.tripEnded && room?.myCompanionJoined && (
        <div className="review-panel">
          <div className="review-panel-head">
            <strong>참가자 평가</strong>
            <span>함께 여행한 참가자를 각각 평가해 주세요.</span>
          </div>

          {reviewTargets.map((target) => {
            const form = {
              rating: 5,
              content: "",
              ...reviewForms[target.userId],
            };

            return (
              <div className="review-target-row" key={target.userId}>
                <div className="review-target-head">
                  <strong>{target.nickname}</strong>
                  <span>{target.reviewed ? "평가 완료" : "평가 대기"}</span>
                </div>
                {!target.reviewed && (
                  <div className="review-form">
                    <select
                      value={form.rating}
                      onChange={(e) => updateReviewForm(target.userId, "rating", e.target.value)}
                    >
                      <option value="5">5점</option>
                      <option value="4">4점</option>
                      <option value="3">3점</option>
                      <option value="2">2점</option>
                      <option value="1">1점</option>
                    </select>
                    <textarea
                      placeholder="함께한 경험을 남겨주세요"
                      value={form.content}
                      onChange={(e) => updateReviewForm(target.userId, "content", e.target.value)}
                    />
                    <button
                      className="primary-button"
                      onClick={() => submitReview(target)}
                      type="button"
                      disabled={!form.content.trim()}
                    >
                      평가 등록
                    </button>
                  </div>
                )}
              </div>
            );
          })}

          {reviewTargets.length === 0 && <p className="empty">평가할 참가자가 없습니다.</p>}
        </div>
      )}

      <div className="chat-message-list">
        {messages.map((message) => {
          if (message.type === "SYSTEM") {
            return (
              <div className="chat-system-message" key={message.id}>
                <span>{message.content}</span>
              </div>
            );
          }

          const mine = message.senderId === user?.id;

          return (
            <div className={mine ? "chat-message mine" : "chat-message"} key={message.id}>
              {!mine && <span className="chat-sender">{message.senderNickname}</span>}
              <p>{message.content}</p>
              <time>{formatTime(message.createdAt)}</time>
            </div>
          );
        })}
        <div ref={endRef} />
      </div>

      <form className="chat-input-bar" onSubmit={sendMessage}>
        <input
          placeholder="메시지를 입력하세요"
          value={content}
          onChange={(e) => setContent(e.target.value)}
        />
        <button className="primary-button" type="submit" disabled={!connected}>전송</button>
      </form>

      {menuOpen && (
        <div className="chat-action-sheet-overlay" onClick={() => setMenuOpen(false)}>
          <div className="chat-action-sheet" onClick={(e) => e.stopPropagation()}>
            <div className="filter-sheet-handle" />
            <button className="chat-action-sheet-danger" onClick={leaveRoom} type="button">
              채팅방 나가기
            </button>
            <button className="chat-action-sheet-cancel" onClick={() => setMenuOpen(false)} type="button">
              취소
            </button>
          </div>
        </div>
      )}

      {soloLeavePromptOpen && (
        <div className="chat-action-sheet-overlay" onClick={() => setSoloLeavePromptOpen(false)}>
          <div className="chat-action-sheet" onClick={(e) => e.stopPropagation()}>
            <div className="filter-sheet-handle" />
            <div className="chat-action-sheet-head">
              <strong>채팅방에 혼자 남았습니다</strong>
              <span>상대방이 채팅방을 나갔습니다. 이 채팅방을 나가시겠어요?</span>
            </div>
            <button className="chat-action-sheet-danger" onClick={leaveRoom} type="button">
              채팅방 나가기
            </button>
            <button className="chat-action-sheet-cancel" onClick={() => setSoloLeavePromptOpen(false)} type="button">
              계속 보기
            </button>
          </div>
        </div>
      )}
    </section>
  );
}
