import React from "react";
import { Link, Outlet, useLocation } from "react-router-dom";
import { useAuth } from "../context/AuthContext";

export default function Layout() {
  const { user, logout } = useAuth();
  const location = useLocation();

  const active = (path) => location.pathname === path ? "active" : "";

  return (
    <div className="app-shell">
      <header className="top-bar">
        <Link to="/" className="brand">TripMate</Link>
        {user ? (
          <button className="text-button" onClick={logout}>로그아웃</button>
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
        <Link className={active("/applications")} to="/applications">신청내역</Link>
        <Link className={active("/profile")} to="/profile">마이</Link>
      </nav>
    </div>
  );
}
