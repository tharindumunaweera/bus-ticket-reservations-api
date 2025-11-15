package com.tharinduDev.bus.reservation.dto;

import java.math.BigDecimal;
import java.util.List;

public class AvailabilityReport {
    private Integer availableSeats;
    private BigDecimal pricePerSeat;
    private BigDecimal totalPrice;
    private List<String> availableSeatNumbers;
}
