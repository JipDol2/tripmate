import React, { useEffect, useState } from "react";
import { api, getErrorMessage } from "../lib/api";
import { formatDateRange } from "../lib/postDate";

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
          <div className="post-card" key={item.id}>
            <h3>{item.postTitle}</h3>
            <p>{item.city} · {formatDateRange(item.startDate || item.companionDate, item.endDate || item.companionDate)}</p>
            <p className="meta">{item.message}</p>
            <span className="status">{item.status}</span>
          </div>
        ))}
      </div>

      <h2>내 글에 들어온 신청</h2>
      <div className="post-list">
        {receivedApplications.map((item) => (
          <div className="post-card" key={item.id}>
            <h3>{item.postTitle}</h3>
            <p>신청자: {item.applicantNickname}</p>
            <p className="meta">{item.message}</p>
            <span className="status">{item.status}</span>
            {item.status === "PENDING" && (
              <div className="button-row">
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
