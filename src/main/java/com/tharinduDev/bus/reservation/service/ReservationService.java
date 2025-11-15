package com.tharinduDev.bus.reservation.service;


import com.tharinduDev.bus.reservation.dto.*;
import com.tharinduDev.bus.reservation.entity.Reservation;
import com.tharinduDev.bus.reservation.entity.Route;
import com.tharinduDev.bus.reservation.entity.Seat;
import com.tharinduDev.bus.reservation.enums.Location;
import com.tharinduDev.bus.reservation.exception.InvalidReservationException;
import com.tharinduDev.bus.reservation.exception.NoSeatsAvailableException;
import com.tharinduDev.bus.reservation.exception.RouteNotFoundException;
import com.tharinduDev.bus.reservation.repository.ReservationRepository;
import com.tharinduDev.bus.reservation.repository.RouteRepository;
import com.tharinduDev.bus.reservation.repository.SeatRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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

        AvailabilityDetails details = getAvailabilityDetails(request);

        // calculate pricing
        BigDecimal pricePerSeat = details.route().getPrice();
        BigDecimal totalPrice = pricePerSeat.multiply(BigDecimal.valueOf(request.getPassengerCount()));

        // Get available seat numbers
        List<String> availableSeatNumbers = details.availableSeats().stream()
                .map(seat -> seat.getSeatNumber())
                .collect(Collectors.toList());

        return new AvailabilityReport(details.availableSeats().size(), pricePerSeat, totalPrice, availableSeatNumbers);
    }

    @Transactional
    public ReservationDetails reserveTickets(ReservationInquiry request) {

        AvailabilityDetails details = getAvailabilityDetails(request);

        List<Seat> availableSeats = details.availableSeats();
        // Check if enough seats are available
        if (availableSeats.size() < request.getPassengerCount()) {
            throw new NoSeatsAvailableException("Not enough seats available. Requested: " + request.getPassengerCount() +
                            ", Available: " + availableSeats.size()
            );
        }

        // take the first N number of requested seats
        List<Seat> assignedSeats = availableSeats.subList(0, request.getPassengerCount());

        // Create one reservation with multiple seats
        String reservationNumber = generateReservationNumber();
        Reservation reservation = new Reservation();
        reservation.setReservationNumber(reservationNumber);
        reservation.setFromLocation(request.getOrigin());
        reservation.setToLocation(request.getDestination());

        // calculates the finalized total ticket price for the entire trip
        BigDecimal expectedPrice = details.route().getPrice().multiply(BigDecimal.valueOf(request.getPassengerCount()));
        reservation.setTotalPrice(expectedPrice);

        reservation.getSeats().addAll(assignedSeats);

        // Save reservation
        Reservation savedReservation = reservationRepository.save(reservation);

        // Get seat numbers for response
        List<String> seatNumbers = assignedSeats.stream().map(seat -> seat.getSeatNumber()).collect(Collectors.toList());

        return new ReservationDetails(
                savedReservation.getReservationNumber(),
                seatNumbers,
                request.getOrigin(),
                request.getDestination(),
                expectedPrice
        );
    }

    // core validation and calculates the available seats for a given request
    private AvailabilityDetails getAvailabilityDetails(TicketInquiry request) {

        // validate origin and destination
        if (request.getOrigin().equals(request.getDestination())) {
            throw new InvalidReservationException("Origin and destination cannot be the same");
        }

        // Find the specific route info
        Route route = routeRepository.findByFromLocationAndToLocation( request.getOrigin(), request.getDestination())
                .orElseThrow(() -> new RouteNotFoundException(
                        "No route found from " + request.getOrigin() + " to " + request.getDestination()
                ));

        if (request instanceof ReservationInquiry reservationInquiry) {

            // Validate price confirmation
            BigDecimal expectedPrice = route.getPrice().multiply(BigDecimal.valueOf(request.getPassengerCount()));
            if (reservationInquiry.getPriceConfirmation().compareTo(expectedPrice) != 0) {
                throw new InvalidReservationException(
                        "Price confirmation mismatch. Expected: Rs. " + expectedPrice + ", Received: Rs. " + reservationInquiry.getPriceConfirmation()
                );
            }
        }

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
        List<Seat> availableSeats = allSeats.stream().filter(seat -> !reservedSeatIds.contains(seat.getId()))
                .collect(Collectors.toList());

        return new AvailabilityDetails(route, availableSeats);
    }

    private String generateReservationNumber() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        return "RES-" + timestamp + "-" + (int) (Math.random() * 1000);
    }

    // checks if two routes overlap. Overlap only occurs if they travel in the same direction and their segment intersect.
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
