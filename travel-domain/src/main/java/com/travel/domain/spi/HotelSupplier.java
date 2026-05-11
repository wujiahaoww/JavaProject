package com.travel.domain.spi;

import com.travel.domain.hotel.HotelSearchCriteria;
import com.travel.domain.hotel.HotelSummary;

import java.util.List;

/**
 * 酒店数据源 SPI，由 integration 模块实现。
 */
public interface HotelSupplier {

    List<HotelSummary> search(HotelSearchCriteria criteria);

    String detail(String hotelId);
}
