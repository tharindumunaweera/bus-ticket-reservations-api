package com.tharinduDev.bus.reservation.dto;

import com.tharinduDev.bus.reservation.enums.Location;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TicketInquiry {

    private Integer passengerCount;
    private Location origin;
    private Location destination;
}
