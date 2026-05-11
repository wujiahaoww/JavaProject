package com.travel.integration.hotel;

import com.travel.domain.hotel.HotelSearchCriteria;
import com.travel.domain.hotel.HotelSummary;
import com.travel.domain.spi.HotelSupplier;

import java.util.Collections;
import java.util.List;

/**
 * 酒店供应商占位实现（后续替换为真实 HTTP 适配器）。
 */
public class StubHotelSupplier implements HotelSupplier {

    @Override
    public List<HotelSummary> search(HotelSearchCriteria criteria) {
        return Collections.emptyList();
    }

    @Override
    public String detail(String hotelId) {
        return "";
    }
}
