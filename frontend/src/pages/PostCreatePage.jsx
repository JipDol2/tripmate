import React, { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { api, getErrorMessage } from "../lib/api";
import { getCountryCityOptions } from "../lib/locationApi";
import { purposeOptions } from "../lib/purposeOptions";
import {
  createCalendarDays,
  createMonthStart,
  formatDateLabel,
  formatDateRange,
  isDateInRange,
  weekdayLabels,
} from "../lib/postDate";

const styleOptions = ["계획형", "즉흥형", "맛집 선호", "사진 선호", "여유로운 일정", "빡센 일정", "야경 선호", "술 가능"];

export default function PostCreatePage() {
  const navigate = useNavigate();
  const [form, setForm] = useState({
    cityCode: "",
    startDate: "",
    endDate: "",
    timeSlot: "저녁",
    purpose: "식사",
    purposes: ["FOOD"],
    maxParticipants: 2,
    genderPreference: "무관",
    title: "",
    content: "",
    travelStyles: [],
  });
  const [selectedCountryCode, setSelectedCountryCode] = useState("");
  const [locationOptions, setLocationOptions] = useState([]);
  const [loadingLocations, setLoadingLocations] = useState(true);
  const [locationError, setLocationError] = useState("");
  const [isDateSheetOpen, setIsDateSheetOpen] = useState(false);
  const [displayedMonth, setDisplayedMonth] = useState(() => createMonthStart(""));

  useEffect(() => {
    let active = true;

    getCountryCityOptions()
      .then((options) => {
        if (!active) return;
        setLocationOptions(options);
        setLocationError("");
      })
      .catch(() => {
        if (!active) return;
        setLocationError("나라/도시 목록을 불러오지 못했습니다.");
      })
      .finally(() => {
        if (!active) return;
        setLoadingLocations(false);
      });

    return () => {
      active = false;
    };
  }, []);

  useEffect(() => {
    if (!isDateSheetOpen) {
      return undefined;
    }

    const originalOverflow = document.body.style.overflow;
    document.body.style.overflow = "hidden";

    return () => {
      document.body.style.overflow = originalOverflow;
    };
  }, [isDateSheetOpen]);

  const onChange = (e) => setForm((current) => ({ ...current, [e.target.name]: e.target.value }));
  const onCountryChange = (e) => {
    const nextCountryCode = e.target.value;
    setSelectedCountryCode(nextCountryCode);
    setForm((current) => ({ ...current, cityCode: "" }));
  };

  const toggleStyle = (style) => {
    setForm((current) => {
      const exists = current.travelStyles.includes(style);

      return {
        ...current,
        travelStyles: exists
          ? current.travelStyles.filter((item) => item !== style)
          : [...current.travelStyles, style],
      };
    });
  };

  const togglePurpose = (purpose) => {
    setForm((current) => {
      const exists = current.purposes.includes(purpose);

      return {
        ...current,
        purposes: exists
          ? current.purposes.filter((item) => item !== purpose)
          : [...current.purposes, purpose],
      };
    });
  };

  const openDateSheet = () => {
    setDisplayedMonth(createMonthStart(form.startDate || form.endDate));
    setIsDateSheetOpen(true);
  };

  const closeDateSheet = () => {
    setIsDateSheetOpen(false);
  };

  const resetDateRange = () => {
    setForm((current) => ({
      ...current,
      startDate: "",
      endDate: "",
    }));
  };

  const selectDate = (dateValue) => {
    setForm((current) => {
      if (!current.startDate || (current.startDate && current.endDate)) {
        return {
          ...current,
          startDate: dateValue,
          endDate: "",
        };
      }

      if (dateValue < current.startDate) {
        return {
          ...current,
          startDate: dateValue,
          endDate: current.startDate,
        };
      }

      if (dateValue === current.startDate) {
        return {
          ...current,
          startDate: dateValue,
          endDate: dateValue,
        };
      }

      return {
        ...current,
        endDate: dateValue,
      };
    });
  };

  const applyDateRange = () => {
    if ((form.startDate && !form.endDate) || (!form.startDate && form.endDate)) {
      alert("시작날짜와 끝날짜를 모두 선택해주세요.");
      return;
    }

    closeDateSheet();
  };

  const onSubmit = async (e) => {
    e.preventDefault();

    if (!selectedCountryCode || !form.cityCode) {
      alert("나라와 도시를 선택해주세요.");
      return;
    }

    if (!form.startDate || !form.endDate) {
      alert("여행 기간을 선택해주세요.");
      return;
    }

    if (form.purposes.length === 0) {
      alert("Select at least one companion type.");
      return;
    }

    try {
      const { purpose, ...payload } = form;
      const res = await api.post("/posts", {
        ...payload,
        companionDate: form.startDate,
        endDate: form.endDate,
        maxParticipants: Number(form.maxParticipants),
      });
      navigate(`/posts/${res.data.id}`);
    } catch (error) {
      alert(getErrorMessage(error));
    }
  };

  const selectedCountryOption = locationOptions.find((option) => option.countryCode === selectedCountryCode);
  const cityOptions = selectedCountryOption?.cities || [];
  const calendarDays = createCalendarDays(displayedMonth);
  const monthTitle = `${displayedMonth.getFullYear()}년 ${displayedMonth.getMonth() + 1}월`;
  const hasDateRange = form.startDate && form.endDate;
  const dateSummaryLabel = hasDateRange
    ? `${formatDateLabel(form.startDate)} - ${formatDateLabel(form.endDate)}`
    : "여행 기간을 선택하세요";
  const dateRangeLabel = hasDateRange
    ? formatDateRange(form.startDate, form.endDate)
    : "선택하세요";

  return (
    <section className="page create-page">
      <div className="create-hero">
        <p>어디서, 언제, 어떤 분위기로 함께할지 한 번에 정리해보세요.</p>
        <h1>동행 글쓰기</h1>
      </div>

      <form className="form create-form" onSubmit={onSubmit}>
        <div className="create-section">
          <div className="section-heading">
            <p className="section-eyebrow">Location & Date</p>
            <h2>지역과 일정을 정해주세요</h2>
          </div>

          <select value={selectedCountryCode} onChange={onCountryChange} disabled={loadingLocations} required>
            <option value="">{loadingLocations ? "나라 불러오는 중..." : "나라 선택"}</option>
            {locationOptions.map((option) => (
              <option key={option.countryCode} value={option.countryCode}>{option.countryName}</option>
            ))}
          </select>
          <select
            name="cityCode"
            value={form.cityCode}
            onChange={onChange}
            disabled={loadingLocations || !selectedCountryCode || cityOptions.length === 0}
            required
          >
            <option value="">
              {!selectedCountryCode
                ? "먼저 나라를 선택하세요"
                : cityOptions.length === 0
                  ? "도시 정보 없음"
                  : "도시 선택"}
            </option>
            {cityOptions.map((city) => (
              <option key={city.code} value={city.code}>{city.name}</option>
            ))}
          </select>
          {locationError && <p className="helper">{locationError}</p>}

          <button type="button" className="date-trigger" onClick={openDateSheet}>
            <div className="date-trigger-top">
              <span className="date-trigger-label">여행 기간</span>
              <span className="date-trigger-arrow">›</span>
            </div>
            <strong>{dateSummaryLabel}</strong>
            <span className="date-trigger-caption">
              {hasDateRange ? dateRangeLabel : "동행찾기 필터처럼 기간을 선택할 수 있어요."}
            </span>
          </button>
          <p className="helper">같은 날짜를 두 번 선택하면 당일 일정으로 등록됩니다.</p>
        </div>

        <div className="create-section">
          <div className="section-heading">
            <p className="section-eyebrow">Companion</p>
            <h2>모집 조건을 설정하세요</h2>
          </div>

          <select name="timeSlot" value={form.timeSlot} onChange={onChange}>
            <option>아침</option>
            <option>점심</option>
            <option>오후</option>
            <option>저녁</option>
            <option>종일</option>
          </select>
          <select name="purpose" value={form.purpose} onChange={onChange} hidden aria-hidden="true" tabIndex={-1}>
            <option>식사</option>
            <option>카페</option>
            <option>관광</option>
            <option>사진</option>
            <option>야경</option>
            <option>술/펍</option>
            <option>근교 투어</option>
          </select>
          <div className="purpose-chip-grid">
            {purposeOptions.map((purpose) => (
              <button
                key={purpose.value}
                type="button"
                className={form.purposes.includes(purpose.value) ? "purpose-chip active" : "purpose-chip"}
                onClick={() => togglePurpose(purpose.value)}
              >
                {purpose.label}
              </button>
            ))}
          </div>
          <div className="create-split-grid">
            <input name="maxParticipants" type="number" min="1" max="10" value={form.maxParticipants} onChange={onChange} />
            <select name="genderPreference" value={form.genderPreference} onChange={onChange}>
              <option>무관</option>
              <option>남성만</option>
              <option>여성만</option>
            </select>
          </div>
        </div>

        <div className="create-section">
          <div className="section-heading">
            <p className="section-eyebrow">Post</p>
            <h2>동행 글을 소개해주세요</h2>
          </div>

          <input name="title" placeholder="제목" value={form.title} onChange={onChange} />
          <textarea name="content" placeholder="상세 내용" value={form.content} onChange={onChange} />

          <div className="chip-box">
            {styleOptions.map((style) => (
              <button
                key={style}
                type="button"
                className={form.travelStyles.includes(style) ? "chip selected" : "chip"}
                onClick={() => toggleStyle(style)}
              >
                {style}
              </button>
            ))}
          </div>
        </div>

        <button className="primary-button create-submit-button">등록하기</button>
      </form>

      {isDateSheetOpen && (
        <div className="filter-sheet-overlay" onClick={closeDateSheet}>
          <div className="filter-sheet create-date-sheet" onClick={(e) => e.stopPropagation()}>
            <div className="filter-sheet-handle" />
            <div className="filter-sheet-header">
              <div>
                <p className="filter-sheet-label">여행 기간</p>
                <strong>함께할 날짜를 범위로 선택하세요</strong>
              </div>
              <button type="button" className="filter-sheet-close" onClick={closeDateSheet}>×</button>
            </div>

            <div className="filter-sheet-body">
              <div className="filter-panel">
                <div className="date-range-summary">
                  <div className="date-range-box">
                    <span className="date-range-label">시작날짜</span>
                    <strong>{form.startDate || "선택하세요"}</strong>
                  </div>
                  <div className="date-range-divider" />
                  <div className="date-range-box">
                    <span className="date-range-label">끝날짜</span>
                    <strong>{form.endDate || "선택하세요"}</strong>
                  </div>
                </div>
                <div className="calendar-card">
                  <div className="calendar-header">
                    <button
                      type="button"
                      className="calendar-nav"
                      onClick={() => setDisplayedMonth((current) => new Date(current.getFullYear(), current.getMonth() - 1, 1))}
                    >
                      ‹
                    </button>
                    <strong>{monthTitle}</strong>
                    <button
                      type="button"
                      className="calendar-nav"
                      onClick={() => setDisplayedMonth((current) => new Date(current.getFullYear(), current.getMonth() + 1, 1))}
                    >
                      ›
                    </button>
                  </div>

                  <div className="calendar-weekdays">
                    {weekdayLabels.map((label) => (
                      <span key={label}>{label}</span>
                    ))}
                  </div>

                  <div className="calendar-grid">
                    {calendarDays.map((day) => (
                      <button
                        key={day.key}
                        type="button"
                        className={[
                          "calendar-day",
                          day.isCurrentMonth ? "" : "muted",
                          day.isToday ? "today" : "",
                          form.startDate === day.value ? "selected range-start" : "",
                          form.endDate === day.value ? "selected range-end" : "",
                          isDateInRange(day.value, form.startDate, form.endDate) ? "in-range" : "",
                        ].join(" ").trim()}
                        onClick={() => {
                          selectDate(day.value);
                          if (!day.isCurrentMonth) {
                            setDisplayedMonth(createMonthStart(day.value));
                          }
                        }}
                      >
                        {day.label}
                      </button>
                    ))}
                  </div>
                </div>
                <p className="sheet-helper">시작일을 고른 뒤 끝나는 날짜를 선택하면 기간이 완성됩니다.</p>
              </div>
            </div>

            <div className="filter-sheet-actions">
              <button type="button" className="ghost-button" onClick={resetDateRange}>초기화</button>
              <button type="button" className="primary-button filter-apply-button" onClick={applyDateRange}>완료</button>
            </div>
          </div>
        </div>
      )}
    </section>
  );
}
