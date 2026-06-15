import React, { useEffect, useState } from "react";
import { Link, useNavigate, useParams } from "react-router-dom";
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
  const navigate = useNavigate();
  const { user } = useAuth();
  const [post, setPost] = useState(null);

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
  }, [postId, user?.id]);

  const startChat = async () => {
    try {
      const res = await api.post(`/chats/posts/${postId}/start`);
      navigate(`/chats/${res.data.id}`);
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

  const endTrip = async () => {
    if (!confirm("여행을 종료하고 참가자 평가를 열까요?")) {
      return;
    }

    try {
      const res = await api.patch(`/posts/${postId}/end-trip`);
      setPost(res.data);
      alert("여행이 종료되었습니다. 참가자들이 서로 평가할 수 있습니다.");
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
    `${post.currentParticipants || 1}/${post.maxParticipants}명 참여`,
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
            {post.tripEnded && <span className="status applied">여행 종료</span>}
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
          <Link className="detail-host-card" to={`/users/${post.authorId}`}>
            <div className="detail-host-avatar">{post.authorNickname?.slice(0, 1) || "T"}</div>
            <div>
              <strong>{post.authorNickname}</strong>
              {authorMeta && <span>{authorMeta}</span>}
            </div>
            <span className="menu-arrow">›</span>
          </Link>
        </section>
      </article>

      {user && (
        <div className="apply-box">
          <h2>{isAuthor ? "동행 관리" : "동행 대화"}</h2>
          <p className="meta">
            {isAuthor
              ? post.tripEnded ? "종료된 여행입니다." : "여행이 끝났다면 참가자 평가를 열 수 있어요."
              : post.tripEnded ? "종료된 여행입니다. 참가자는 채팅방에서 서로를 평가할 수 있어요." : "먼저 대화해 보고, 채팅방 안에서 참가 여부를 결정할 수 있어요."}
          </p>
          {!isAuthor && <button className="primary-button" onClick={startChat}>대화하기</button>}
          {isAuthor && !post.tripEnded && (
            <button className="danger-button" onClick={endTrip} type="button">여행 종료</button>
          )}
          {!isAuthor && <button className="danger-button" onClick={report}>신고하기</button>}
        </div>
      )}

      {!user && <p className="helper">요청하려면 로그인이 필요합니다.</p>}
    </section>
  );
}
