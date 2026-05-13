package com.tripmate.location;

import java.util.List;

public record LocationOptionResponse(
        String countryCode,
        String countryName,
        List<LocationCityResponse> cities
) {
}
