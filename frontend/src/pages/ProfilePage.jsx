import React, { useEffect, useMemo, useState } from "react";
import { Link } from "react-router-dom";
import { api, getErrorMessage } from "../lib/api";
import { useAuth } from "../context/AuthContext";
import { formatMannerAltitude } from "../lib/mannerAltitude";

const createProfileForm = (user) => ({
  nickname: user?.nickname || "",
  ageRange: user?.ageRange || "30대",
  gender: user?.gender || "PRIVATE",
  bio: user?.bio || "",
  profileImageUrl: user?.profileImageUrl || "",
  travelStyles: user?.travelStyles || [],
  languages: user?.languages || [],
});

const genderLabel = {
  PRIVATE: "비공개",
  MALE: "남성",
  FEMALE: "여성",
  OTHER: "기타",
};

export default function ProfilePage() {
  const { user, setUser } = useAuth();
  const [mode, setMode] = useState("home");
  const [form, setForm] = useState(() => createProfileForm(user));
  const [reviews, setReviews] = useState([]);

  const profile = useMemo(() => createProfileForm(user), [user]);
  const initial = profile.nickname.trim().charAt(0) || "T";
  const altitudeText = formatMannerAltitude(reviews);

  useEffect(() => {
    if (!user?.id) {
      setReviews([]);
      return;
    }

    api.get(`/users/${user.id}/profile`)
      .then((res) => setReviews(res.data.reviews || []))
      .catch(() => setReviews([]));
  }, [user?.id]);

  const onChange = (e) => setForm({ ...form, [e.target.name]: e.target.value });

  const openDetail = () => {
    setMode("detail");
  };

  const openEdit = () => {
    setForm(createProfileForm(user));
    setMode("edit");
  };

  const save = async (e) => {
    e.preventDefault();
    try {
      const res = await api.put("/users/me", form);
      setUser(res.data);
      alert("프로필이 저장되었습니다.");
      setMode("detail");
    } catch (error) {
      alert(getErrorMessage(error));
    }
  };

  if (mode === "detail") {
    return (
      <section className="page my-page">
        <div className="page-head">
          <button className="back-button" onClick={() => setMode("home")} type="button">‹</button>
          <h1>프로필 정보</h1>
        </div>

        <div className="profile-summary">
          {profile.profileImageUrl ? (
            <img className="profile-avatar" src={profile.profileImageUrl} alt="" />
          ) : (
            <div className="profile-avatar profile-avatar-fallback">{initial}</div>
          )}
          <div>
            <strong>{profile.nickname || "닉네임 없음"}</strong>
            <span>{profile.bio || "등록된 자기소개가 없습니다."}</span>
          </div>
        </div>

        <div className="altitude-card">
          <span>매너 지표</span>
          <strong>{altitudeText}</strong>
        </div>

        <div className="info-list">
          <div className="info-row">
            <span>연령대</span>
            <strong>{profile.ageRange || "비공개"}</strong>
          </div>
          <div className="info-row">
            <span>성별</span>
            <strong>{genderLabel[profile.gender] || profile.gender || "비공개"}</strong>
          </div>
          <div className="info-row">
            <span>프로필 이미지</span>
            <strong>{profile.profileImageUrl ? "등록됨" : "미등록"}</strong>
          </div>
        </div>

        <button className="primary-button wide-button" onClick={openEdit} type="button">프로필 수정</button>
      </section>
    );
  }

  if (mode === "edit") {
    return (
      <section className="page my-page">
        <div className="page-head">
          <button className="back-button" onClick={() => setMode("detail")} type="button">‹</button>
          <h1>프로필 수정</h1>
        </div>

        <form className="form profile-edit-form" onSubmit={save}>
          <label>
            <span>닉네임</span>
            <input name="nickname" placeholder="닉네임" value={form.nickname} onChange={onChange} />
          </label>
          <label>
            <span>연령대</span>
            <select name="ageRange" value={form.ageRange} onChange={onChange}>
              <option>20대</option>
              <option>30대</option>
              <option>40대</option>
              <option>비공개</option>
            </select>
          </label>
          <label>
            <span>성별</span>
            <select name="gender" value={form.gender} onChange={onChange}>
              <option value="PRIVATE">비공개</option>
              <option value="MALE">남성</option>
              <option value="FEMALE">여성</option>
              <option value="OTHER">기타</option>
            </select>
          </label>
          <label>
            <span>자기소개</span>
            <textarea name="bio" placeholder="자기소개" value={form.bio} onChange={onChange} />
          </label>
          <label>
            <span>프로필 이미지 URL</span>
            <input name="profileImageUrl" placeholder="프로필 이미지 URL" value={form.profileImageUrl} onChange={onChange} />
          </label>

          <div className="button-row">
            <button className="ghost-button" onClick={() => setMode("detail")} type="button">취소</button>
            <button className="primary-button" type="submit">저장하기</button>
          </div>
        </form>
      </section>
    );
  }

  return (
    <section className="page my-page">
      <h1>마이</h1>

      <button className="my-profile-card" onClick={openDetail} type="button">
        {profile.profileImageUrl ? (
          <img className="profile-avatar" src={profile.profileImageUrl} alt="" />
        ) : (
          <div className="profile-avatar profile-avatar-fallback">{initial}</div>
        )}
        <div>
          <strong>{profile.nickname || "닉네임 없음"}</strong>
          <span>{altitudeText}</span>
        </div>
        <span className="menu-arrow">›</span>
      </button>

      <div className="my-menu-list">
        <Link className="my-menu-item" to="/my/posts">
          <span>내가 작성한 글</span>
          <strong>›</strong>
        </Link>
        <Link className="my-menu-item" to="/chats">
          <span>채팅</span>
          <strong>›</strong>
        </Link>
      </div>
    </section>
  );
}
