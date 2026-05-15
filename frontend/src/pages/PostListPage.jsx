import React, { useEffect, useState } from "react";
import { Link, useSearchParams } from "react-router-dom";
import { api, getErrorMessage } from "../lib/api";
import { getCountryCityOptions } from "../lib/locationApi";
import {
  createCalendarDays,
  createMonthStart,
  formatDateLabel,
  formatDateRange,
  isDateInRange,
  weekdayLabels,
} from "../lib/postDate";

const purposeOptions = [
  { label: "식사", value: "FOOD" },
  { label: "카페", value: "CAFE" },
  { label: "관광", value: "TOUR" },
  { label: "쇼핑", value: "SHOPPING" },
  { label: "자연", value: "NATURE" },
  { label: "사진", value: "PHOTO" },
  { label: "문화", value: "CULTURE" },
  { label: "액티비티", value: "ACTIVITY" },
  { label: "나이트라이프", value: "NIGHTLIFE" },
  { label: "휴식", value: "RELAXATION" },
];
const agePreferenceOptions = ["20대", "30대", "40대", "50대", "60대"];
const genderPreferenceOptions = ["남성만", "여성만"];
const timeSlotOptions = ["아침", "점심", "오후", "저녁", "종일"];
const FILTER_STORAGE_KEY = "tripmate_post_list_filters";
const emptyFilters = {
  countryCode: "",
  cityCode: "",
  startDate: "",
  endDate: "",
  timeSlot: "",
  purposes: [],
  agePreferences: [],
  genderPreference: "",
};

function normalizeFilters(filters) {
  return {
    ...emptyFilters,
    ...filters,
    purposes: Array.isArray(filters?.purposes) ? filters.purposes : [],
    agePreferences: Array.isArray(filters?.agePreferences) ? filters.agePreferences : [],
  };
}

function getPurposeLabel(value) {
  return purposeOptions.find((option) => option.value === value)?.label || value;
}

function getFiltersFromSearchParams(searchParams) {
  return {
    ...emptyFilters,
    countryCode: searchParams.get("countryCode") || "",
    cityCode: searchParams.get("cityCode") || "",
    startDate: searchParams.get("startDate") || "",
    endDate: searchParams.get("endDate") || "",
    timeSlot: searchParams.get("timeSlot") || "",
    purposes: searchParams.getAll("purposes"),
    agePreferences: searchParams.getAll("agePreferences"),
    genderPreference: searchParams.get("genderPreference") || "",
  };
}

function hasFilterSearchParams(searchParams) {
  return Array.from(searchParams.keys()).some((key) => key in emptyFilters);
}

function getStoredFilters() {
  try {
    const value = sessionStorage.getItem(FILTER_STORAGE_KEY);
    return value ? normalizeFilters(JSON.parse(value)) : null;
  } catch {
    return null;
  }
}

function getInitialFilters(searchParams) {
  if (hasFilterSearchParams(searchParams)) {
    return getFiltersFromSearchParams(searchParams);
  }

  return getStoredFilters() || emptyFilters;
}

function storeFilters(filters) {
  sessionStorage.setItem(FILTER_STORAGE_KEY, JSON.stringify(normalizeFilters(filters)));
}

function createSearchParamsFromFilters(filters) {
  const searchParams = new URLSearchParams();

  Object.entries(filters).forEach(([key, value]) => {
    if (Array.isArray(value)) {
      value.forEach((item) => {
        if (item) {
          searchParams.append(key, item);
        }
      });
      return;
    }

    if (value) {
      searchParams.set(key, value);
    }
  });

  return searchParams;
}

function filterPostsByDateRange(items, startDate, endDate) {
  if (!startDate && !endDate) {
    return items;
  }

  return items.filter((post) => {
    const postStartDate = post.startDate;
    const postEndDate = post.endDate || post.startDate;

    if (startDate && endDate) {
      return postStartDate <= endDate && postEndDate >= startDate;
    }

    if (startDate) {
      return postEndDate >= startDate;
    }

    return postStartDate <= endDate;
  });
}

export default function PostListPage() {
  const [searchParams, setSearchParams] = useSearchParams();
  const [posts, setPosts] = useState([]);
  const [filters, setFilters] = useState(() => getInitialFilters(searchParams));
  const [locationOptions, setLocationOptions] = useState([]);
  const [loadingLocations, setLoadingLocations] = useState(true);
  const [locationError, setLocationError] = useState("");
  const [isFilterSheetOpen, setIsFilterSheetOpen] = useState(false);
  const [activeFilterTab, setActiveFilterTab] = useState("location");
  const [displayedMonth, setDisplayedMonth] = useState(() => createMonthStart(""));

  const loadPosts = async (nextFilters = filters) => {
    try {
      const res = await api.get("/posts", {
        params: {
          countryCode: nextFilters.countryCode || undefined,
          cityCode: nextFilters.cityCode || undefined,
          startDate: nextFilters.startDate || undefined,
          endDate: nextFilters.endDate || undefined,
          timeSlot: nextFilters.timeSlot || undefined,
          purposes: nextFilters.purposes,
          agePreferences: nextFilters.agePreferences,
          genderPreference: nextFilters.genderPreference || undefined,
        },
        paramsSerializer: (params) => {
          const searchParams = new URLSearchParams();

          Object.entries(params).forEach(([key, value]) => {
            if (value == null || value === "") {
              return;
            }

            if (Array.isArray(value)) {
              value.forEach((item) => {
                if (item) {
                  searchParams.append(key, item);
                }
              });
              return;
            }

            searchParams.append(key, value);
          });

          return searchParams.toString();
        },
      });
      setPosts(filterPostsByDateRange(res.data, nextFilters.startDate, nextFilters.endDate));
    } catch (error) {
      alert(getErrorMessage(error));
    }
  };

  useEffect(() => {
    const nextFilters = getInitialFilters(searchParams);
    setFilters(nextFilters);
    loadPosts(nextFilters);
  }, [searchParams]);

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

  const onChange = (e) => setFilters((current) => ({ ...current, [e.target.name]: e.target.value }));
  const onCountryChange = (e) => {
    const nextCountryCode = e.target.value;
    setFilters((current) => ({
      ...current,
      countryCode: nextCountryCode,
      cityCode: "",
    }));
  };
  const selectedCountryOption = locationOptions.find((option) => option.countryCode === filters.countryCode);
  const cityOptions = selectedCountryOption?.cities || [];
  const selectedCityOption = cityOptions.find((city) => city.code === filters.cityCode);

  useEffect(() => {
    if (!isFilterSheetOpen) {
      return undefined;
    }

    const originalOverflow = document.body.style.overflow;
    document.body.style.overflow = "hidden";

    return () => {
      document.body.style.overflow = originalOverflow;
    };
  }, [isFilterSheetOpen]);

  const openFilterSheet = (tab) => {
    setActiveFilterTab(tab);
    if (tab === "date") {
      setDisplayedMonth(createMonthStart(filters.startDate || filters.endDate));
    }
    setIsFilterSheetOpen(true);
  };

  const closeFilterSheet = () => {
    setIsFilterSheetOpen(false);
  };

  const resetFilters = () => {
    setFilters(emptyFilters);
    sessionStorage.removeItem(FILTER_STORAGE_KEY);
    setSearchParams({});
  };

  const applyFilters = async () => {
    if ((filters.startDate && !filters.endDate) || (!filters.startDate && filters.endDate)) {
      alert("시작날짜와 끝날짜를 모두 선택해주세요.");
      return;
    }

    await loadPosts(filters);
    storeFilters(filters);
    setSearchParams(createSearchParamsFromFilters(filters));
    closeFilterSheet();
  };

  const countryLabel = selectedCountryOption?.countryName || "지역";
  const cityLabel = selectedCityOption?.name;
  const locationLabel = cityLabel || countryLabel;
  const dateLabel = filters.startDate && filters.endDate
    ? `${formatDateLabel(filters.startDate)} - ${formatDateLabel(filters.endDate)}`
    : "날짜";
  const typeCount = filters.purposes.length + (filters.timeSlot ? 1 : 0);
  const purposeLabel = typeCount > 0
    ? `동행 유형 ${typeCount}`
    : "동행 유형";
  const conditionCount = filters.agePreferences.length + (filters.genderPreference ? 1 : 0);
  const conditionLabel = conditionCount > 0
    ? `동행 조건 ${conditionCount}`
    : "동행 조건";
  const calendarDays = createCalendarDays(displayedMonth);
  const monthTitle = `${displayedMonth.getFullYear()}년 ${displayedMonth.getMonth() + 1}월`;
  const selectDate = (dateValue) => {
    setFilters((current) => {
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

  return (
    <section className="page">
      <div className="hero">
        <p>혼자 떠나는 여행, 필요한 순간만 함께하세요.</p>
        <h1>여행 동행 찾기</h1>
      </div>

      <div className="filter-strip">
        <div className="filter-pill-row">
          <button type="button" className="filter-pill" onClick={() => openFilterSheet("location")}>
            <span>{locationLabel}</span>
            <span className="filter-pill-arrow">v</span>
          </button>
          <button type="button" className="filter-pill" onClick={() => openFilterSheet("date")}>
            <span>{dateLabel}</span>
            <span className="filter-pill-arrow">v</span>
          </button>
          <button type="button" className="filter-pill" onClick={() => openFilterSheet("purpose")}>
            <span>{purposeLabel}</span>
            <span className="filter-pill-arrow">v</span>
          </button>
          <button type="button" className="filter-pill" onClick={() => openFilterSheet("condition")}>
            <span>{conditionLabel}</span>
            <span className="filter-pill-arrow">v</span>
          </button>
        </div>
        <button type="button" className="filter-open-button" onClick={() => openFilterSheet("location")}>
          필터 열기
        </button>
      </div>

      <div className="post-list">
        {posts.map((post) => (
          <Link className="post-card" to={`/posts/${post.id}`} key={post.id}>
            <div className="card-top">
              <span className="badge">{post.city}</span>
              <span className={`status ${post.status === "OPEN" ? "open" : ""}`}>{post.status}</span>
            </div>
            <h2>{post.title}</h2>
            <p>{formatDateRange(post.startDate, post.endDate)} · {post.timeSlot}</p>
            <p className="meta">{post.authorNickname} · {post.authorAgeRange} · {post.authorGender}</p>
            <div className="tag-row">
              {post.purposes?.map((purpose) => <span key={purpose}>{getPurposeLabel(purpose)}</span>)}
              {post.travelStyles?.map((style) => <span key={style}>{style}</span>)}
            </div>
          </Link>
        ))}

        {posts.length === 0 && <p className="empty">아직 등록된 동행 글이 없습니다.</p>}
      </div>

      {isFilterSheetOpen && (
        <div className="filter-sheet-overlay" onClick={closeFilterSheet}>
          <div className="filter-sheet" onClick={(e) => e.stopPropagation()}>
            <div className="filter-sheet-handle" />
            <div className="filter-sheet-header">
              <div>
                <p className="filter-sheet-label">필터</p>
                <strong>원하는 동행만 빠르게 추려보세요</strong>
              </div>
              <button type="button" className="filter-sheet-close" onClick={closeFilterSheet}>×</button>
            </div>

            <div className="filter-sheet-tabs">
              <button
                type="button"
                className={activeFilterTab === "location" ? "filter-sheet-tab active" : "filter-sheet-tab"}
                onClick={() => setActiveFilterTab("location")}
              >
                지역
              </button>
              <button
                type="button"
                className={activeFilterTab === "date" ? "filter-sheet-tab active" : "filter-sheet-tab"}
                onClick={() => setActiveFilterTab("date")}
              >
                날짜
              </button>
              <button
                type="button"
                className={activeFilterTab === "purpose" ? "filter-sheet-tab active" : "filter-sheet-tab"}
                onClick={() => setActiveFilterTab("purpose")}
              >
                동행 유형
              </button>
              <button
                type="button"
                className={activeFilterTab === "condition" ? "filter-sheet-tab active" : "filter-sheet-tab"}
                onClick={() => setActiveFilterTab("condition")}
              >
                동행 조건
              </button>
            </div>

            <div className="filter-sheet-body">
              {activeFilterTab === "location" && (
                <div className="filter-panel">
                  <select value={filters.countryCode} onChange={onCountryChange} disabled={loadingLocations}>
                    <option value="">{loadingLocations ? "나라 불러오는 중..." : "나라 선택"}</option>
                    {locationOptions.map((option) => (
                      <option key={option.countryCode} value={option.countryCode}>{option.countryName}</option>
                    ))}
                  </select>
                  <select
                    name="cityCode"
                    value={filters.cityCode}
                    onChange={onChange}
                    disabled={loadingLocations || !filters.countryCode || cityOptions.length === 0}
                  >
                    <option value="">
                      {!filters.countryCode
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
                </div>
              )}

              {activeFilterTab === "date" && (
                <div className="filter-panel">
                  <div className="date-range-summary">
                    <div className="date-range-box">
                      <span className="date-range-label">시작날짜</span>
                      <strong>{filters.startDate || "선택하세요"}</strong>
                    </div>
                    <div className="date-range-divider" />
                    <div className="date-range-box">
                      <span className="date-range-label">끝날짜</span>
                      <strong>{filters.endDate || "선택하세요"}</strong>
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
                            filters.startDate === day.value ? "selected range-start" : "",
                            filters.endDate === day.value ? "selected range-end" : "",
                            isDateInRange(day.value, filters.startDate, filters.endDate) ? "in-range" : "",
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
                </div>
              )}

              {activeFilterTab === "purpose" && (
                <div className="filter-panel">
                  <div className="filter-section-title">
                    <strong>동행 유형</strong>
                    <span>시간대와 함께할 활동을 고르세요.</span>
                  </div>

                  <div className="condition-group">
                    <strong>시간대</strong>
                    <div className="purpose-chip-grid">
                      {timeSlotOptions.map((timeSlot) => (
                        <button
                          key={timeSlot}
                          type="button"
                          className={filters.timeSlot === timeSlot ? "purpose-chip active" : "purpose-chip"}
                          onClick={() => setFilters((current) => ({
                            ...current,
                            timeSlot: current.timeSlot === timeSlot ? "" : timeSlot,
                          }))}
                        >
                          {timeSlot}
                        </button>
                      ))}
                    </div>
                  </div>

                  <div className="condition-group">
                    <strong>활동</strong>
                    <div className="purpose-chip-grid">
                      {purposeOptions.map((purpose) => (
                        <button
                          key={purpose.value}
                          type="button"
                          className={filters.purposes.includes(purpose.value) ? "purpose-chip active" : "purpose-chip"}
                          onClick={() => setFilters((current) => ({
                            ...current,
                            purposes: current.purposes.includes(purpose.value)
                              ? current.purposes.filter((item) => item !== purpose.value)
                              : [...current.purposes, purpose.value],
                          }))}
                        >
                          {purpose.label}
                        </button>
                      ))}
                    </div>
                  </div>
                </div>
              )}

              {activeFilterTab === "condition" && (
                <div className="filter-panel">
                  <div className="filter-section-title">
                    <strong>동행 조건</strong>
                    <span>작성자가 원하는 나이대와 성별 조건으로 찾습니다.</span>
                  </div>

                  <div className="condition-group">
                    <strong>나이대</strong>
                    <div className="purpose-chip-grid">
                      {agePreferenceOptions.map((ageRange) => (
                        <button
                          key={ageRange}
                          type="button"
                          className={filters.agePreferences.includes(ageRange) ? "purpose-chip active" : "purpose-chip"}
                          onClick={() => setFilters((current) => ({
                            ...current,
                            agePreferences: current.agePreferences.includes(ageRange)
                              ? current.agePreferences.filter((item) => item !== ageRange)
                              : [...current.agePreferences, ageRange],
                          }))}
                        >
                          {ageRange}
                        </button>
                      ))}
                    </div>
                  </div>

                  <div className="condition-group">
                    <strong>성별 조건</strong>
                    <div className="purpose-chip-grid">
                      {genderPreferenceOptions.map((genderPreference) => (
                        <button
                          key={genderPreference}
                          type="button"
                          className={filters.genderPreference === genderPreference ? "purpose-chip active" : "purpose-chip"}
                          onClick={() => setFilters((current) => ({
                            ...current,
                            genderPreference: current.genderPreference === genderPreference ? "" : genderPreference,
                          }))}
                        >
                          {genderPreference}
                        </button>
                      ))}
                    </div>
                  </div>
                </div>
              )}
            </div>

            <div className="filter-sheet-actions">
              <button type="button" className="ghost-button" onClick={resetFilters}>초기화</button>
              <button type="button" className="primary-button filter-apply-button" onClick={applyFilters}>적용</button>
            </div>
          </div>
        </div>
      )}
    </section>
  );
}
