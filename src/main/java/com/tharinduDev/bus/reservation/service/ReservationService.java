package com.tharinduDev.bus.reservation.service;


import com.tharinduDev.bus.reservation.dto.AvailabilityReport;
import com.tharinduDev.bus.reservation.dto.TicketInquiry;
import com.tharinduDev.bus.reservation.entity.Reservation;
import com.tharinduDev.bus.reservation.entity.Route;
import com.tharinduDev.bus.reservation.entity.Seat;
import com.tharinduDev.bus.reservation.exception.InvalidReservationException;
import com.tharinduDev.bus.reservation.exception.RouteNotFoundException;
import com.tharinduDev.bus.reservation.repository.ReservationRepository;
import com.tharinduDev.bus.reservation.repository.RouteRepository;
import com.tharinduDev.bus.reservation.repository.SeatRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReservationService {

    private final RouteRepository routeRepository;
    private final SeatRepository seatRepository;
    private final ReservationRepository reservationRepository;

    public ReservationService(RouteRepository routeRepository, SeatRepository seatRepository, ReservationRepository reservationRepository) {
        this.routeRepository = routeRepository;
        this.seatRepository = seatRepository;
        this.reservationRepository = reservationRepository;
    }

    public AvailabilityReport checkAvailability(TicketInquiry request) {

        // Validate origin and destination
        if (request.getOrigin().equals(request.getDestination())) {
            throw new InvalidReservationException("Origin and destination cannot be the same");
        }

        // Find the specific route info
        Route route = routeRepository.findByFromLocationAndToLocation( request.getOrigin(), request.getDestination())
                .orElseThrow(() -> new RouteNotFoundException(
                "No route found from " + request.getOrigin() + " to " + request.getDestination()
        ));

        List<Seat> allSeats = seatRepository.findAll();
        List<Reservation> allReservations = reservationRepository.findAll();

        // Get list of reserved seat IDs
        List<Long> reservedSeatIds = allReservations.stream()
                .flatMap(r -> r.getSeats().stream())
                .map(seat -> seat.getId())
                .collect(Collectors.toList());

        // Filter available seats
        List<Seat> availableSeats = allSeats.stream()
                .filter(seat -> !reservedSeatIds.contains(seat.getId()))
                .collect(Collectors.toList());

        // Calculate pricing
        BigDecimal pricePerSeat = route.getPrice();
        BigDecimal totalPrice = pricePerSeat.multiply(BigDecimal.valueOf(request.getPassengerCount()));

        // Get available seat numbers
        List<String> availableSeatNumbers = availableSeats.stream()
                .map(seat -> seat.getSeatNumber())
                .collect(Collectors.toList());

        return new AvailabilityReport(availableSeats.size(), pricePerSeat, totalPrice, availableSeatNumbers);
    }
}
