import { api } from "./api";

let cachedCountryCityOptions = null;
let pendingCountryCityRequest = null;

function normalizeCountryCityOptions(payload) {
  const entries = Array.isArray(payload) ? payload : [];

  return entries
    .map((entry) => {
      const countryCode = typeof entry?.countryCode === "string" ? entry.countryCode.trim() : "";
      const countryName = typeof entry?.countryName === "string" ? entry.countryName.trim() : "";
      const cities = Array.isArray(entry?.cities)
        ? entry.cities
            .map((city) => ({
              code: typeof city?.code === "string" ? city.code.trim() : "",
              name: typeof city?.name === "string" ? city.name.trim() : "",
            }))
            .filter((city) => city.code && city.name)
            .sort((left, right) => left.name.localeCompare(right.name))
        : [];

      if (!countryCode || !countryName) {
        return null;
      }

      return { countryCode, countryName, cities };
    })
    .filter(Boolean)
    .sort((left, right) => left.countryName.localeCompare(right.countryName));
}

export async function getCountryCityOptions() {
  if (cachedCountryCityOptions) {
    return cachedCountryCityOptions;
  }

  if (!pendingCountryCityRequest) {
    pendingCountryCityRequest = api.get("/locations")
      .then((response) => {
        const normalized = normalizeCountryCityOptions(response.data);
        cachedCountryCityOptions = normalized;
        return normalized;
      })
      .finally(() => {
        pendingCountryCityRequest = null;
      });
  }

  return pendingCountryCityRequest;
}
