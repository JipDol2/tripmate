const DEFAULT_ALTITUDE = 100;

export function calculateMannerAltitude(reviews = []) {
  if (!Array.isArray(reviews) || reviews.length === 0) {
    return DEFAULT_ALTITUDE;
  }

  const validReviews = reviews.filter((review) => Number.isFinite(Number(review.rating)));

  if (validReviews.length === 0) {
    return DEFAULT_ALTITUDE;
  }

  const average = validReviews.reduce((sum, review) => sum + Number(review.rating), 0) / validReviews.length;
  const reviewBonus = Math.min(validReviews.length * 5, 50);
  const altitude = DEFAULT_ALTITUDE + Math.round((average - 3) * 30) + reviewBonus;

  return Math.max(0, altitude);
}

export function formatMannerAltitude(reviews = []) {
  return `동행고도 ${calculateMannerAltitude(reviews)}m`;
}
