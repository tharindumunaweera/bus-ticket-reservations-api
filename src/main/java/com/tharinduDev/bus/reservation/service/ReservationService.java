package com.tharinduDev.bus.reservation.service;


import com.tharinduDev.bus.reservation.dto.AvailabilityReport;
import com.tharinduDev.bus.reservation.dto.TicketInquiry;
import com.tharinduDev.bus.reservation.entity.Reservation;
import com.tharinduDev.bus.reservation.entity.Route;
import com.tharinduDev.bus.reservation.entity.Seat;
import com.tharinduDev.bus.reservation.enums.Location;
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

        // validate origin and destination
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

        // get list of reserved seat IDs
        List<Long> reservedSeatIds = allReservations.stream()
                .filter(reservation -> routesOverlap(
                        request.getOrigin(), request.getDestination(),
                        reservation.getFromLocation(), reservation.getToLocation()
                ))
                .flatMap(r -> r.getSeats().stream())
                .map(seat -> seat.getId())
                .collect(Collectors.toList());

        // filter available seats
        List<Seat> availableSeats = allSeats.stream()
                .filter(seat -> !reservedSeatIds.contains(seat.getId()))
                .collect(Collectors.toList());

        // calculate pricing
        BigDecimal pricePerSeat = route.getPrice();
        BigDecimal totalPrice = pricePerSeat.multiply(BigDecimal.valueOf(request.getPassengerCount()));

        // Get available seat numbers
        List<String> availableSeatNumbers = availableSeats.stream()
                .map(seat -> seat.getSeatNumber())
                .collect(Collectors.toList());

        return new AvailabilityReport(availableSeats.size(), pricePerSeat, totalPrice, availableSeatNumbers);
    }

    // Checks if two routes overlap. Overlap only occurs if they travel in the same direction and their segment intersect.
    private boolean routesOverlap(Location req1From, Location req1To, Location req2From, Location req2To) {

        // use the difference in positions to determine direction - positive for forward, negative for backward
        int req1Direction = getLocationPosition(req1To) - getLocationPosition(req1From);
        int req2Direction = getLocationPosition(req2To) - getLocationPosition(req2From);

        // routes only overlap if they are traveling in the same direction
        if (Math.signum(req1Direction) != Math.signum(req2Direction)) {
            return false;
        }

        // Need the start and end positions, regardless of the travel direction.
        int req1Start = Math.min(getLocationPosition(req1From), getLocationPosition(req1To));
        int req1End = Math.max(getLocationPosition(req1From), getLocationPosition(req1To));

        int req2Start = Math.min(getLocationPosition(req2From), getLocationPosition(req2To));
        int req2End = Math.max(getLocationPosition(req2From), getLocationPosition(req2To));

        return (req1Start < req2End) && (req2Start < req1End);
    }

    private int getLocationPosition(Location location) {
        return switch (location) {
            case A -> 0;
            case B -> 1;
            case C -> 2;
            case D -> 3;
        };
    }
}
