package com.travel.hotel.api;

import com.travel.common.api.ApiResult;
import com.travel.domain.hotel.HotelSearchCriteria;
import com.travel.domain.hotel.HotelSummary;
import com.travel.hotel.application.HotelApplicationService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 酒店相关 API（占位）。
 */
@RestController
@RequestMapping("/api/v1/hotels")
public class HotelController {

    private final HotelApplicationService hotelApplicationService;

    public HotelController(HotelApplicationService hotelApplicationService) {
        this.hotelApplicationService = hotelApplicationService;
    }

    @GetMapping("/search")
    public ApiResult<List<HotelSummary>> search(
            @RequestParam String destination,
            @RequestParam(required = false) String checkInDate,
            @RequestParam(required = false) String checkOutDate,
            @RequestParam(required = false) Integer guestCount
    ) {
        var criteria = new HotelSearchCriteria(destination, checkInDate, checkOutDate, guestCount);
        return ApiResult.ok(hotelApplicationService.search(criteria));
    }

    @GetMapping("/{hotelId}")
    public ApiResult<String> detail(@PathVariable String hotelId) {
        return ApiResult.ok(hotelApplicationService.detail(hotelId));
    }
}
