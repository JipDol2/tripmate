import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import { api, getErrorMessage } from "../lib/api";

export default function RegisterPage() {
  const navigate = useNavigate();
  const [form, setForm] = useState({
    email: "",
    password: "",
    nickname: "",
    ageRange: "30대",
    gender: "PRIVATE",
  });

  const onChange = (e) => setForm({ ...form, [e.target.name]: e.target.value });

  const onSubmit = async (e) => {
    e.preventDefault();
    try {
      await api.post("/auth/register", form);
      navigate("/login", {
        replace: true,
        state: { registered: true, email: form.email },
      });
    } catch (error) {
      alert(getErrorMessage(error));
    }
  };

  return (
    <section className="page">
      <h1>회원가입</h1>
      <form className="form" onSubmit={onSubmit}>
        <input name="email" placeholder="이메일" value={form.email} onChange={onChange} />
        <input name="password" type="password" placeholder="비밀번호" value={form.password} onChange={onChange} />
        <input name="nickname" placeholder="닉네임" value={form.nickname} onChange={onChange} />
        <select name="ageRange" value={form.ageRange} onChange={onChange}>
          <option>20대</option>
          <option>30대</option>
          <option>40대</option>
          <option>비공개</option>
        </select>
        <select name="gender" value={form.gender} onChange={onChange}>
          <option value="PRIVATE">비공개</option>
          <option value="MALE">남성</option>
          <option value="FEMALE">여성</option>
          <option value="OTHER">기타</option>
        </select>
        <button className="primary-button">가입하기</button>
      </form>
    </section>
  );
}
