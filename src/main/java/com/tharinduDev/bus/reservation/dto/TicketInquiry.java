package com.tharinduDev.bus.reservation.dto;

import com.tharinduDev.bus.reservation.enums.Location;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TicketInquiry {

    @NotNull(message = "Number of passengers is required")
    @Min(value = 1, message = "Number of passengers must be at least 1")
    @Max(value = 40, message = "Number of passengers cannot exceed 40")
    private Integer passengerCount;

    @NotNull(message = "Origin location is required")
    private Location origin;

    @NotNull(message = "Destination location is required")
    private Location destination;
}
