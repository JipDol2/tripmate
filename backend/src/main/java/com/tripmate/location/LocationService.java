package com.tripmate.location;

import com.tripmate.common.ApiException;
import java.util.List;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class LocationService {
    private final CountryRepository countryRepository;
    private final CityRepository cityRepository;

    public LocationService(CountryRepository countryRepository, CityRepository cityRepository) {
        this.countryRepository = countryRepository;
        this.cityRepository = cityRepository;
    }

    public List<LocationOptionResponse> getAll() {
        return countryRepository.findAllWithCities().stream()
                .map(country -> new LocationOptionResponse(
                        country.getCode(),
                        country.getName(),
                        country.getCities().stream()
                                .map(city -> new LocationCityResponse(city.getCode(), city.getName()))
                                .toList()
                ))
                .toList();
    }

    public City requireCity(String cityCode) {
        return cityRepository.findWithCountryByCode(cityCode)
                .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "Invalid city code."));
    }

    public ResolvedLocation resolve(String storedCityCode) {
        if (storedCityCode == null || storedCityCode.isBlank()) {
            return new ResolvedLocation(null, null, null, null, false);
        }

        Optional<City> city = cityRepository.findWithCountryByCode(storedCityCode);
        if (city.isEmpty()) {
            return new ResolvedLocation(null, null, storedCityCode, storedCityCode, false);
        }

        City resolved = city.get();
        return new ResolvedLocation(
                resolved.getCountry().getCode(),
                resolved.getCountry().getName(),
                resolved.getCode(),
                resolved.getName(),
                true
        );
    }

    public record ResolvedLocation(
            String countryCode,
            String countryName,
            String cityCode,
            String cityName,
            boolean managed
    ) {
    }
}
