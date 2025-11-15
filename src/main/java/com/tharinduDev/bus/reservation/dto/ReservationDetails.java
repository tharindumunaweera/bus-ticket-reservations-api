package com.tharinduDev.bus.reservation.dto;

import com.tharinduDev.bus.reservation.enums.Location;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReservationDetails {

    private String reservationNumber;
    private List<String> seatNumbers;
    private Location departureLocation;
    private Location arrivalLocation;
    private BigDecimal totalPrice;
}
