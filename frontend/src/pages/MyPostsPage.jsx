import React, { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { api, getErrorMessage } from "../lib/api";
import { useAuth } from "../context/AuthContext";
import { formatDateRange } from "../lib/postDate";
import { getPurposeLabel } from "../lib/purposeOptions";

export default function MyPostsPage() {
  const { user } = useAuth();
  const [posts, setPosts] = useState([]);

  useEffect(() => {
    if (!user?.id) {
      return;
    }

    api.get(`/users/${user.id}/profile`)
      .then((res) => setPosts(res.data.posts || []))
      .catch((error) => alert(getErrorMessage(error)));
  }, [user?.id]);

  return (
    <section className="page">
      <h1>내가 작성한 글</h1>

      <div className="post-list">
        {posts.map((post) => (
          <Link className="post-card" to={`/posts/${post.id}`} key={post.id}>
            <div className="card-top">
              <span className="badge">{post.city}</span>
              <span className={`status ${post.status === "OPEN" ? "open" : ""}`}>{post.status}</span>
            </div>
            <h2>{post.title}</h2>
            <p>{formatDateRange(post.startDate, post.endDate)} · {post.timeSlot}</p>
            <p className="meta">{post.currentParticipants || 1}/{post.maxParticipants}명 참여</p>
            <div className="tag-row">
              {post.purposes?.map((purpose) => <span key={purpose}>{getPurposeLabel(purpose)}</span>)}
              {post.travelStyles?.map((style) => <span key={style}>{style}</span>)}
            </div>
          </Link>
        ))}

        {posts.length === 0 && <p className="empty">아직 작성한 동행글이 없습니다.</p>}
      </div>
    </section>
  );
}
