package com.tharinduDev.bus.reservation.dto;

import com.tharinduDev.bus.reservation.enums.Location;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReservationInquiry extends TicketInquiry {

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    private BigDecimal priceConfirmation;

    // this is for cover unit test scenario
    public ReservationInquiry(Integer passengerCount, Location origin, Location destination, BigDecimal priceConfirmation) {
        super(passengerCount, origin, destination);
        this.priceConfirmation = priceConfirmation;
    }
}
