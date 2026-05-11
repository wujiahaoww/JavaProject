package com.travel.domain.hotel;

/**
 * 酒店检索条件（占位）。
 */
public record HotelSearchCriteria(
        String destination,
        String checkInDate,
        String checkOutDate,
        Integer guestCount
) {
}
