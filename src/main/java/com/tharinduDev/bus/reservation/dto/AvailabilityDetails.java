package com.tharinduDev.bus.reservation.dto;

import com.tharinduDev.bus.reservation.entity.Route;
import com.tharinduDev.bus.reservation.entity.Seat;
import java.util.List;

public record AvailabilityDetails(Route route, List<Seat> availableSeats) { }
