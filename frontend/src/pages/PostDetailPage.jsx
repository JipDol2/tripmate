import React, { useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import { api, getErrorMessage } from "../lib/api";
import { useAuth } from "../context/AuthContext";
import { formatDateRange } from "../lib/postDate";
import { getPurposeLabel } from "../lib/purposeOptions";

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
  const metaText = [dateText, post.timeSlot, purposeText].filter(Boolean).join(" · ");

  return (
    <section className="page">
      <article className="detail-card">
        <div className="card-top">
          <span className="badge">{post.city}</span>
          <span className={`status ${post.status === "OPEN" ? "open" : ""}`}>{post.status}</span>
        </div>
        <h1>{post.title}</h1>
        <p className="meta">{metaText}</p>
        <p className="meta">모집 인원: {post.maxParticipants}명 · 성별 조건: {post.genderPreference || "무관"}</p>

        <div className="profile-box">
          <strong>{post.authorNickname}</strong>
          <span>{post.authorAgeRange} · {post.authorGender}</span>
        </div>

        <p className="content">{post.content}</p>

        <div className="tag-row">
          {post.travelStyles?.map((style) => <span key={style}>{style}</span>)}
        </div>
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
