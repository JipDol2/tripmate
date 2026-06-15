import React, { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { api, getErrorMessage } from "../lib/api";
import { applicationStatusBadge } from "../lib/applicationStatus";
import { formatDateRange } from "../lib/postDate";

const genderLabel = {
  MALE: "남성",
  FEMALE: "여성",
  OTHER: "기타",
  PRIVATE: "성별 비공개",
};

function formatApplicantMeta(item) {
  return [
    item.applicantAgeRange || "나이 비공개",
    genderLabel[item.applicantGender] || item.applicantGender || "성별 비공개",
  ].filter(Boolean);
}

export default function ApplicationsPage() {
  const [myApplications, setMyApplications] = useState([]);
  const [receivedApplications, setReceivedApplications] = useState([]);

  const load = async () => {
    try {
      const [myRes, receivedRes] = await Promise.all([
        api.get("/applications/me"),
        api.get("/applications/received"),
      ]);
      setMyApplications(myRes.data);
      setReceivedApplications(receivedRes.data);
    } catch (error) {
      alert(getErrorMessage(error));
    }
  };

  useEffect(() => {
    load();
  }, []);

  const updateStatus = async (id, action) => {
    try {
      await api.patch(`/applications/${id}/${action}`);
      await load();
    } catch (error) {
      alert(getErrorMessage(error));
    }
  };

  return (
    <section className="page">
      <h1>신청내역</h1>

      <h2>내가 신청한 동행</h2>
      <div className="post-list">
        {myApplications.map((item) => (
          <Link className="post-card application-link-card" to={`/posts/${item.postId}`} key={item.id}>
            <h3>{item.postTitle}</h3>
            <p>{item.city} · {formatDateRange(item.startDate || item.companionDate, item.endDate || item.companionDate)}</p>
            <p className="meta">{item.message}</p>
            <div className="card-top">
              <span className="status">{applicationStatusBadge[item.status] || item.status}</span>
              <span className="menu-arrow">›</span>
            </div>
          </Link>
        ))}
      </div>

      <h2>내 글에 들어온 신청</h2>
      <div className="post-list">
        {receivedApplications.map((item) => (
          <div className="post-card application-received-card" key={item.id}>
            <div className="application-card-head">
              <h3>{item.postTitle}</h3>
              <span className="status">{applicationStatusBadge[item.status] || item.status}</span>
            </div>
            <p className="application-message">{item.message}</p>
            <Link className="application-applicant" to={`/users/${item.applicantId}`}>
              {item.applicantNickname}
              <span className="menu-arrow">›</span>
            </Link>
            <div className="application-tag-row">
              {formatApplicantMeta(item).map((label) => <span key={label}>{label}</span>)}
            </div>
            {item.status === "PENDING" && (
              <div className="application-action-row">
                <button className="primary-button" onClick={() => updateStatus(item.id, "accept")}>수락</button>
                <button className="danger-button" onClick={() => updateStatus(item.id, "reject")}>거절</button>
              </div>
            )}
          </div>
        ))}
      </div>
    </section>
  );
}
