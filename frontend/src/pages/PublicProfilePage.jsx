import React, { useEffect, useState } from "react";
import { Link, useParams } from "react-router-dom";
import { api, getErrorMessage } from "../lib/api";
import { formatMannerAltitude } from "../lib/mannerAltitude";
import { formatDateRange } from "../lib/postDate";
import { getPurposeLabel } from "../lib/purposeOptions";

const genderLabel = {
  MALE: "남성",
  FEMALE: "여성",
  OTHER: "기타",
  PRIVATE: "성별 비공개",
};

function formatDate(value) {
  if (!value) return "";

  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return "";

  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, "0");
  const day = String(date.getDate()).padStart(2, "0");

  return `${year}.${month}.${day}`;
}

export default function PublicProfilePage() {
  const { userId } = useParams();
  const [profile, setProfile] = useState(null);

  useEffect(() => {
    api.get(`/users/${userId}/profile`)
      .then((res) => setProfile(res.data))
      .catch((error) => alert(getErrorMessage(error)));
  }, [userId]);

  if (!profile) return <section className="page">로딩 중...</section>;

  const initial = profile.nickname?.trim().charAt(0) || "T";
  const meta = [
    profile.ageRange || "나이 비공개",
    genderLabel[profile.gender] || profile.gender || "성별 비공개",
    profile.joinedAt ? `${formatDate(profile.joinedAt)} 가입` : "",
  ].filter(Boolean);
  const altitudeText = formatMannerAltitude(profile.reviews);

  return (
    <section className="page public-profile-page">
      <div className="profile-summary public-profile-summary">
        {profile.profileImageUrl ? (
          <img className="profile-avatar" src={profile.profileImageUrl} alt="" />
        ) : (
          <div className="profile-avatar profile-avatar-fallback">{initial}</div>
        )}
        <div>
          <strong>{profile.nickname}</strong>
          <span>{meta.join(" · ")}</span>
        </div>
      </div>

      <div className="altitude-card">
        <span>매너 지표</span>
        <strong>{altitudeText}</strong>
      </div>

      {profile.bio && (
        <section className="public-profile-section">
          <h2>소개</h2>
          <p>{profile.bio}</p>
        </section>
      )}

      {(profile.travelStyles?.length > 0 || profile.languages?.length > 0) && (
        <section className="public-profile-section">
          <h2>프로필 태그</h2>
          <div className="detail-chip-row">
            {profile.travelStyles?.map((style) => <span key={`style-${style}`}>{style}</span>)}
            {profile.languages?.map((language) => <span key={`language-${language}`}>{language}</span>)}
          </div>
        </section>
      )}

      <section className="public-profile-section">
        <h2>작성한 동행글</h2>
        <div className="post-list">
          {profile.posts.map((post) => (
            <Link className="post-card" to={`/posts/${post.id}`} key={post.id}>
              <div className="card-top">
                <span className="badge">{post.city}</span>
                <span className={`status ${post.status === "OPEN" ? "open" : ""}`}>{post.status}</span>
              </div>
              <h3>{post.title}</h3>
              <p>{formatDateRange(post.startDate, post.endDate)} · {post.timeSlot}</p>
              <p className="meta">{post.currentParticipants || 1}/{post.maxParticipants}명 참여</p>
              <div className="tag-row">
                {post.purposes?.map((purpose) => <span key={purpose}>{getPurposeLabel(purpose)}</span>)}
              </div>
            </Link>
          ))}

          {profile.posts.length === 0 && <p className="empty">아직 작성한 동행글이 없습니다.</p>}
        </div>
      </section>

      <section className="public-profile-section">
        <h2>동행 후기</h2>
        <div className="post-list">
          {profile.reviews.map((review) => (
            <div className="post-card" key={review.id}>
              <div className="card-top">
                <strong>{review.reviewerNickname}</strong>
                <span className="status">{review.rating}점</span>
              </div>
              <p className="meta">{review.postTitle}</p>
              <p>{review.content}</p>
            </div>
          ))}

          {profile.reviews.length === 0 && <p className="empty">아직 받은 동행 후기가 없습니다.</p>}
        </div>
      </section>
    </section>
  );
}
