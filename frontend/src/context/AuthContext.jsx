import React, { createContext, useContext, useEffect, useState } from "react";
import { api } from "../lib/api";

const AuthContext = createContext(null);
const TOKEN_KEY = "tripmate_token";
const USER_KEY = "tripmate_user";

function readStoredUser() {
  const raw = localStorage.getItem(USER_KEY);

  if (!raw) {
    return null;
  }

  try {
    return JSON.parse(raw);
  } catch {
    localStorage.removeItem(USER_KEY);
    return null;
  }
}

function persistSession(token, user) {
  localStorage.setItem(TOKEN_KEY, token);
  localStorage.setItem(USER_KEY, JSON.stringify(user));
}

function clearSession() {
  localStorage.removeItem(TOKEN_KEY);
  localStorage.removeItem(USER_KEY);
}

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null);
  const [booting, setBooting] = useState(true);

  useEffect(() => {
    const token = localStorage.getItem(TOKEN_KEY);
    const storedUser = readStoredUser();

    if (!token) {
      setBooting(false);
      return;
    }

    if (storedUser) {
      setUser(storedUser);
    }

    api.get("/users/me")
      .then((res) => {
        setUser(res.data);
        localStorage.setItem(USER_KEY, JSON.stringify(res.data));
      })
      .catch((error) => {
        const status = error?.response?.status;

        if (status === 401 || status === 403) {
          clearSession();
          setUser(null);
        }
      })
      .finally(() => setBooting(false));
  }, []);

  const loginWithToken = (token, user) => {
    persistSession(token, user);
    setUser(user);
  };

  const logout = () => {
    clearSession();
    setUser(null);
  };

  const updateUser = (nextUser) => {
    setUser(nextUser);

    if (nextUser) {
      localStorage.setItem(USER_KEY, JSON.stringify(nextUser));
    } else {
      localStorage.removeItem(USER_KEY);
    }
  };

  return (
    <AuthContext.Provider value={{ user, setUser: updateUser, booting, loginWithToken, logout }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  return useContext(AuthContext);
}
