import React, { useState } from "react";
import { api, getErrorMessage } from "../lib/api";
import { useAuth } from "../context/AuthContext";

export default function ProfilePage() {
  const { user, setUser } = useAuth();
  const [form, setForm] = useState({
    nickname: user?.nickname || "",
    ageRange: user?.ageRange || "30대",
    gender: user?.gender || "PRIVATE",
    bio: user?.bio || "",
    profileImageUrl: user?.profileImageUrl || "",
    travelStyles: user?.travelStyles || [],
    languages: user?.languages || [],
  });

  const onChange = (e) => setForm({ ...form, [e.target.name]: e.target.value });

  const save = async (e) => {
    e.preventDefault();
    try {
      const res = await api.put("/users/me", form);
      setUser(res.data);
      alert("프로필이 저장되었습니다.");
    } catch (error) {
      alert(getErrorMessage(error));
    }
  };

  return (
    <section className="page">
      <h1>마이페이지</h1>
      <form className="form" onSubmit={save}>
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
        <textarea name="bio" placeholder="자기소개" value={form.bio} onChange={onChange} />
        <input name="profileImageUrl" placeholder="프로필 이미지 URL" value={form.profileImageUrl} onChange={onChange} />
        <button className="primary-button">저장하기</button>
      </form>
    </section>
  );
}
