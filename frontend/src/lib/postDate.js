export const weekdayLabels = ["일", "월", "화", "수", "목", "금", "토"];

export function createMonthStart(dateValue) {
  const baseDate = dateValue ? new Date(`${dateValue}T00:00:00`) : new Date();
  return new Date(baseDate.getFullYear(), baseDate.getMonth(), 1);
}

export function formatDateValue(date) {
  const year = date.getFullYear();
  const month = `${date.getMonth() + 1}`.padStart(2, "0");
  const day = `${date.getDate()}`.padStart(2, "0");
  return `${year}-${month}-${day}`;
}

export function createCalendarDays(monthDate) {
  const year = monthDate.getFullYear();
  const month = monthDate.getMonth();
  const firstDay = new Date(year, month, 1);
  const firstWeekday = firstDay.getDay();
  const gridStart = new Date(year, month, 1 - firstWeekday);

  return Array.from({ length: 42 }, (_, index) => {
    const date = new Date(gridStart);
    date.setDate(gridStart.getDate() + index);

    return {
      key: formatDateValue(date),
      label: date.getDate(),
      value: formatDateValue(date),
      isCurrentMonth: date.getMonth() === month,
      isToday: formatDateValue(date) === formatDateValue(new Date()),
    };
  });
}

export function formatDateLabel(dateValue) {
  if (!dateValue) {
    return "";
  }

  const [, month, day] = dateValue.split("-");
  return `${Number(month)}/${Number(day)}`;
}

export function formatDateRange(startDate, endDate) {
  if (!startDate) {
    return "";
  }

  if (!endDate || startDate === endDate) {
    return startDate;
  }

  return `${startDate} - ${endDate}`;
}

export function isDateInRange(dateValue, startDate, endDate) {
  if (!startDate || !endDate) {
    return false;
  }

  return dateValue > startDate && dateValue < endDate;
}
