package com.tripmate.location;

import java.util.List;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class LocationSeedData implements ApplicationRunner {
    private final CountryRepository countryRepository;
    private final CityRepository cityRepository;

    public LocationSeedData(CountryRepository countryRepository, CityRepository cityRepository) {
        this.countryRepository = countryRepository;
        this.cityRepository = cityRepository;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        int countryOrder = 0;
        for (CountrySeed countrySeed : seeds()) {
            final int currentCountryOrder = countryOrder;
            Country country = countryRepository.findByCode(countrySeed.code())
                    .map(existing -> {
                        existing.update(countrySeed.name(), currentCountryOrder);
                        return existing;
                    })
                    .orElseGet(() -> new Country(countrySeed.code(), countrySeed.name(), currentCountryOrder));
            country = countryRepository.save(country);
            final Country savedCountry = country;

            int cityOrder = 0;
            for (CitySeed citySeed : countrySeed.cities()) {
                final int currentCityOrder = cityOrder;
                cityRepository.findByCode(citySeed.code())
                        .ifPresentOrElse(
                                existing -> existing.update(citySeed.name(), currentCityOrder, savedCountry),
                                () -> cityRepository.save(new City(citySeed.code(), citySeed.name(), currentCityOrder, savedCountry))
                        );
                cityOrder++;
            }

            countryOrder++;
        }
    }

    private List<CountrySeed> seeds() {
        return List.of(
                new CountrySeed("JP", "Japan", List.of(
                        new CitySeed("JP-TOKYO", "Tokyo"),
                        new CitySeed("JP-OSAKA", "Osaka"),
                        new CitySeed("JP-FUKUOKA", "Fukuoka"),
                        new CitySeed("JP-KYOTO", "Kyoto"),
                        new CitySeed("JP-SAPPORO", "Sapporo")
                )),
                new CountrySeed("KR", "Korea", List.of(
                        new CitySeed("KR-SEOUL", "Seoul"),
                        new CitySeed("KR-BUSAN", "Busan"),
                        new CitySeed("KR-JEJU", "Jeju"),
                        new CitySeed("KR-GANGNEUNG", "Gangneung")
                )),
                new CountrySeed("TH", "Thailand", List.of(
                        new CitySeed("TH-BANGKOK", "Bangkok"),
                        new CitySeed("TH-CHIANGMAI", "Chiang Mai"),
                        new CitySeed("TH-PHUKET", "Phuket")
                )),
                new CountrySeed("VN", "Vietnam", List.of(
                        new CitySeed("VN-HANOI", "Hanoi"),
                        new CitySeed("VN-HCMC", "Ho Chi Minh City"),
                        new CitySeed("VN-DANANG", "Da Nang"),
                        new CitySeed("VN-HOIAN", "Hoi An")
                )),
                new CountrySeed("TW", "Taiwan", List.of(
                        new CitySeed("TW-TAIPEI", "Taipei"),
                        new CitySeed("TW-KAOHSIUNG", "Kaohsiung"),
                        new CitySeed("TW-TAICHUNG", "Taichung")
                )),
                new CountrySeed("HK", "Hong Kong", List.of(
                        new CitySeed("HK-HONGKONG", "Hong Kong")
                )),
                new CountrySeed("SG", "Singapore", List.of(
                        new CitySeed("SG-SINGAPORE", "Singapore")
                ))
        );
    }

    private record CountrySeed(String code, String name, List<CitySeed> cities) {
    }

    private record CitySeed(String code, String name) {
    }
}
