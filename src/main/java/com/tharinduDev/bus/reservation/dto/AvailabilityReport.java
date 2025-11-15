package com.tharinduDev.bus.reservation.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AvailabilityReport {
    private Integer availableSeats;
    private BigDecimal pricePerSeat;
    private BigDecimal totalPrice;
    private List<String> availableSeatNumbers;
}
