package com.travel.hotel.application;

import com.travel.domain.hotel.HotelSearchCriteria;
import com.travel.domain.hotel.HotelSummary;
import com.travel.domain.spi.HotelSupplier;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 酒店应用服务（占位：缓存、筛选、聚合等）。
 */
@Service
public class HotelApplicationService {

    private final HotelSupplier hotelSupplier;

    public HotelApplicationService(HotelSupplier hotelSupplier) {
        this.hotelSupplier = hotelSupplier;
    }

    public List<HotelSummary> search(HotelSearchCriteria criteria) {
        return hotelSupplier.search(criteria);
    }

    public String detail(String hotelId) {
        return hotelSupplier.detail(hotelId);
    }
}
