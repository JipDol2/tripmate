import React, { useState } from "react";
import { Link, useLocation, useNavigate } from "react-router-dom";
import { api, getErrorMessage } from "../lib/api";
import { useAuth } from "../context/AuthContext";

export default function LoginPage() {
  const navigate = useNavigate();
  const location = useLocation();
  const { loginWithToken } = useAuth();
  const [form, setForm] = useState({
    email: location.state?.email ?? "",
    password: "",
  });

  const onChange = (e) => setForm({ ...form, [e.target.name]: e.target.value });

  const onSubmit = async (e) => {
    e.preventDefault();
    try {
      const res = await api.post("/auth/login", form);
      loginWithToken(res.data.token, res.data.user);
      navigate("/");
    } catch (error) {
      alert(getErrorMessage(error));
    }
  };

  return (
    <section className="page">
      <h1>로그인</h1>
      {location.state?.registered && (
        <p className="helper">회원가입이 완료되었습니다. 가입한 계정으로 로그인해 주세요.</p>
      )}
      <form className="form" onSubmit={onSubmit}>
        <input name="email" placeholder="이메일" value={form.email} onChange={onChange} />
        <input name="password" type="password" placeholder="비밀번호" value={form.password} onChange={onChange} />
        <button className="primary-button">로그인</button>
      </form>
      <p className="helper">아직 계정이 없다면 <Link to="/register">회원가입</Link></p>
    </section>
  );
}
