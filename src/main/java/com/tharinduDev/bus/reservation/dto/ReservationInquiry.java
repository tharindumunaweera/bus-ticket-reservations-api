package com.tharinduDev.bus.reservation.dto;

import com.tharinduDev.bus.reservation.enums.Location;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReservationInquiry extends TicketInquiry {

    private BigDecimal priceConfirmation;
}
