import React, { useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import { api, getErrorMessage } from "../lib/api";
import { useAuth } from "../context/AuthContext";
import { formatDateRange } from "../lib/postDate";
import { getPurposeLabel } from "../lib/purposeOptions";

function formatWrittenDate(value) {
  if (!value) return "";

  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return "";

  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, "0");
  const day = String(date.getDate()).padStart(2, "0");

  return `${year}.${month}.${day}`;
}

export default function PostDetailPage() {
  const { postId } = useParams();
  const { user } = useAuth();
  const [post, setPost] = useState(null);
  const [message, setMessage] = useState("");

  const loadPost = async () => {
    try {
      const res = await api.get(`/posts/${postId}`);
      setPost(res.data);
    } catch (error) {
      alert(getErrorMessage(error));
    }
  };

  useEffect(() => {
    loadPost();
  }, [postId]);

  const apply = async () => {
    if (!message.trim()) {
      alert("요청 메시지를 입력해 주세요.");
      return;
    }

    try {
      await api.post(`/applications/posts/${postId}`, { message });
      alert("동행 요청이 완료되었습니다.");
      setMessage("");
    } catch (error) {
      alert(getErrorMessage(error));
    }
  };

  const report = async () => {
    const reason = prompt("신고 사유를 입력해 주세요.");
    if (!reason) return;

    try {
      await api.post("/reports", {
        targetUserId: post.authorId,
        targetPostId: post.id,
        reason,
        detail: "",
      });
      alert("신고가 접수되었습니다.");
    } catch (error) {
      alert(getErrorMessage(error));
    }
  };

  if (!post) return <section className="page">로딩 중...</section>;

  const isAuthor = user?.id === post.authorId;
  const dateText = formatDateRange(post.startDate || post.companionDate, post.endDate || post.companionDate);
  const purposeText = post.purposes?.length
    ? post.purposes.map(getPurposeLabel).join(", ")
    : getPurposeLabel(post.purpose);
  const authorMeta = [post.authorAgeRange, post.authorGender].filter(Boolean).join(" · ");
  const writtenDate = formatWrittenDate(post.createdAt || post.createdDate);
  const agePreferences = post.agePreferences
    || post.preferredAgeRanges
    || post.allowedAgeRanges
    || post.targetAgeRanges
    || [];
  const conditionChips = [
    ...(agePreferences.length > 0 ? agePreferences : ["나이 무관"]),
    post.genderPreference && post.genderPreference !== "무관" ? post.genderPreference : "성별 무관",
    `${post.maxParticipants}명 모집`,
  ];
  const typeChips = [
    post.timeSlot,
    ...(post.purposes?.map(getPurposeLabel) || (post.purpose ? [getPurposeLabel(post.purpose)] : [])),
  ].filter(Boolean);

  return (
    <section className="page">
      <article className="trip-detail">
        <header className="detail-header">
          <div className="card-top">
            <span className="badge">{post.city}</span>
            <span className={`status ${post.status === "OPEN" ? "open" : ""}`}>{post.status}</span>
          </div>

          <h1 className="detail-title">{post.title}</h1>
          {writtenDate && <p className="detail-submeta">{writtenDate} 작성</p>}
        </header>

        <section className="detail-section">
          <h2>여행 일정</h2>
          <div className="detail-schedule-card">
            <div className="detail-schedule-row">
              <span className="detail-schedule-label">일정</span>
              <strong>{dateText}</strong>
            </div>
            <div className="detail-schedule-row">
              <span className="detail-schedule-label">지역</span>
              <strong>{post.city}</strong>
            </div>
            {post.timeSlot && (
              <div className="detail-schedule-row">
                <span className="detail-schedule-label">시간</span>
                <strong>{post.timeSlot}</strong>
              </div>
            )}
            <div className="detail-schedule-row">
              <span className="detail-schedule-label">목적</span>
              <strong>{purposeText}</strong>
            </div>
          </div>
        </section>

        <section className="detail-section">
          <h2>여행 소개</h2>
          <p className="content detail-content">{post.content}</p>
        </section>

        <section className="detail-section detail-section-compact">
          <h2>동행 조건</h2>
          <div className="detail-chip-row">
            {conditionChips.map((chip) => <span key={chip}>{chip}</span>)}
          </div>
        </section>

        {typeChips.length > 0 && (
          <section className="detail-section detail-section-compact">
            <h2>동행 유형</h2>
            <div className="detail-chip-row">
              {typeChips.map((chip) => <span key={chip}>{chip}</span>)}
            </div>
          </section>
        )}

        {post.travelStyles?.length > 0 && (
          <section className="detail-section detail-section-compact">
            <h2>여행 스타일</h2>
            <div className="detail-chip-row">
              {post.travelStyles.map((style) => <span key={style}>{style}</span>)}
            </div>
          </section>
        )}

        <section className="detail-section detail-section-compact">
          <h2>여행장</h2>
          <div className="detail-host-card">
            <div className="detail-host-avatar">{post.authorNickname?.slice(0, 1) || "T"}</div>
            <div>
              <strong>{post.authorNickname}</strong>
              {authorMeta && <span>{authorMeta}</span>}
            </div>
          </div>
        </section>
      </article>

      {!isAuthor && user && (
        <div className="apply-box">
          <h2>동행 요청</h2>
          <textarea
            placeholder="간단한 자기소개와 함께하고 싶은 이유를 적어 주세요."
            value={message}
            onChange={(e) => setMessage(e.target.value)}
          />
          <button className="primary-button" onClick={apply}>요청하기</button>
          <button className="danger-button" onClick={report}>신고하기</button>
        </div>
      )}

      {!user && <p className="helper">요청하려면 로그인이 필요합니다.</p>}
    </section>
  );
}
