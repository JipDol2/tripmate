package com.tripmate.post;

import java.time.LocalDate;
import java.util.List;

public interface CompanionPostRepositoryCustom {
    List<CompanionPost> search(String countryCode, String cityCode, LocalDate startDate, LocalDate endDate, String timeSlot,
                               List<CompanionPurpose> purposes, List<String> agePreferences,
                               String genderPreference);
}
