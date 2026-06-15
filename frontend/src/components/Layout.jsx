import React, { useEffect, useState } from "react";
import { Link, Outlet, useLocation, useNavigate } from "react-router-dom";
import { api } from "../lib/api";
import { useAuth } from "../context/AuthContext";

export default function Layout() {
  const { user, logout } = useAuth();
  const location = useLocation();
  const navigate = useNavigate();
  const [unreadCount, setUnreadCount] = useState(0);

  const active = (path) => {
    if (path === "/") {
      return location.pathname === "/" ? "active" : "";
    }

    return location.pathname === path || location.pathname.startsWith(`${path}/`) ? "active" : "";
  };

  const handleLogout = () => {
    logout();
    navigate("/login", { replace: true });
  };

  useEffect(() => {
    if (!user) {
      setUnreadCount(0);
      return;
    }

    api.get("/notifications/unread-count")
      .then((res) => setUnreadCount(res.data || 0))
      .catch(() => setUnreadCount(0));
  }, [user, location.pathname]);

  return (
    <div className="app-shell">
      <header className="top-bar">
        <Link to="/" className="brand">TripMate</Link>
        {user ? (
          <div className="top-actions">
            <Link className="notification-button" to="/notifications" aria-label="알림">
              <svg viewBox="0 0 24 24" aria-hidden="true">
                <path d="M18 9.5C18 6.4 15.9 4 12 4S6 6.4 6 9.5c0 4.4-2 5.3-2 6.7h16c0-1.4-2-2.3-2-6.7Z" />
                <path d="M10 19a2 2 0 0 0 4 0" />
              </svg>
              {unreadCount > 0 && <span className="notification-count">{unreadCount > 9 ? "9+" : unreadCount}</span>}
            </Link>
            <button className="text-button" onClick={handleLogout}>로그아웃</button>
          </div>
        ) : (
          <Link className="text-button" to="/login">로그인</Link>
        )}
      </header>

      <main>
        <Outlet />
      </main>

      <nav className="bottom-nav">
        <Link className={active("/")} to="/">동행찾기</Link>
        <Link className={active("/posts/new")} to="/posts/new">글쓰기</Link>
        <Link className={active("/chats")} to="/chats">채팅</Link>
        <Link className={active("/profile")} to="/profile">마이</Link>
      </nav>
    </div>
  );
}
