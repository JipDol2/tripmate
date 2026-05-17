package com.tripmate.location;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/locations")
@Tag(name = "여행지", description = "동행 모집 글에서 사용할 국가와 도시 옵션을 제공하는 API")
public class LocationController {
    private final LocationService locationService;

    public LocationController(LocationService locationService) {
        this.locationService = locationService;
    }

    @GetMapping
    @Operation(summary = "여행지 옵션 조회", description = "서비스에서 선택 가능한 국가와 도시 목록을 계층 구조로 조회합니다.")
    public List<LocationOptionResponse> list() {
        return locationService.getAll();
    }
}
