export const purposeOptions = [
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

export function getPurposeLabel(value) {
  return purposeOptions.find((option) => option.value === value)?.label || value;
}
