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
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
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

        log.info("Checking availability for route {} -> {} with {} passengers",
                request.getOrigin(), request.getDestination(), request.getPassengerCount());

        AvailabilityDetails details = getAvailabilityDetails(request);

        // calculate pricing
        BigDecimal pricePerSeat = details.route().getPrice();
        BigDecimal totalPrice = pricePerSeat.multiply(BigDecimal.valueOf(request.getPassengerCount()));

        // Get available seat numbers
        List<String> availableSeatNumbers = details.availableSeats().stream()
                .map(seat -> seat.getSeatNumber())
                .collect(Collectors.toList());

        log.info("Availability check complete: {} seats available for {} -> {}, total price: Rs. {}",
                details.availableSeats().size(), request.getOrigin(), request.getDestination(), totalPrice);

        return new AvailabilityReport(details.availableSeats().size(), pricePerSeat, totalPrice, availableSeatNumbers);
    }

    @Transactional
    public ReservationDetails reserveTickets(ReservationInquiry request) {

        log.info("Start reservation for {} passengers from {} to {}",
                request.getPassengerCount(), request.getOrigin(), request.getDestination());

        AvailabilityDetails details = getAvailabilityDetails(request);

        List<Seat> availableSeats = details.availableSeats();
        // Check if enough seats are available
        if (availableSeats.size() < request.getPassengerCount()) {
            log.error("Insufficient seats available. Requested: {}, Available: {}",
                    request.getPassengerCount(), availableSeats.size());
            throw new NoSeatsAvailableException("Not enough seats available. Requested: " + request.getPassengerCount() +
                            ", Available: " + availableSeats.size()
            );
        }

        // Create one reservation with multiple seats
        String reservationNumber = generateReservationNumber();
        log.debug("Reservation number: {}", reservationNumber);

        Reservation reservation = new Reservation();
        reservation.setReservationNumber(reservationNumber);
        reservation.setFromLocation(request.getOrigin());
        reservation.setToLocation(request.getDestination());

        // calculates the finalized total ticket price for the entire trip
        BigDecimal expectedPrice = details.route().getPrice().multiply(BigDecimal.valueOf(request.getPassengerCount()));
        reservation.setTotalPrice(expectedPrice);
        reservation.setPassengerCount(request.getPassengerCount());

        // take the first N number of requested seats
        List<Seat> assignedSeats = availableSeats.subList(0, request.getPassengerCount());

        // link seats to reservation
        for (Seat seat : assignedSeats) {
            seat.setReservation(reservation);
            reservation.getSeats().add(seat);
        }

        // Save reservation
        Reservation savedReservation = reservationRepository.save(reservation);

        // Get seat numbers for response
        List<String> seatNumbers = assignedSeats.stream().map(seat -> seat.getSeatNumber()).collect(Collectors.toList());

        log.info("Reservation successful!. Reservation number: {}, Seats: {}, Total price: Rs. {}",
                reservationNumber, seatNumbers, expectedPrice);

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
            log.warn("Invalid request: origin and destination are the same - {}", request.getOrigin());
            throw new InvalidReservationException("Origin and destination cannot be the same");
        }

        // Find the specific route info
        Route route = routeRepository.findByFromLocationAndToLocation( request.getOrigin(), request.getDestination())
                .orElseThrow(() -> {
                    log.error("Route not found: {} -> {}", request.getOrigin(), request.getDestination());
                    return new RouteNotFoundException("No route found from " + request.getOrigin() + " to " + request.getDestination());
                });

        if (request instanceof ReservationInquiry reservationInquiry) {

            // Validate price confirmation
            BigDecimal expectedPrice = route.getPrice().multiply(BigDecimal.valueOf(request.getPassengerCount()));
            if (reservationInquiry.getPriceConfirmation().compareTo(expectedPrice) != 0) {
                log.warn("Price confirmation mismatch. Expected: Rs. {}, Received: Rs. {}",
                        expectedPrice, reservationInquiry.getPriceConfirmation());
                throw new InvalidReservationException("Price confirmation mismatch. Expected: Rs. " + expectedPrice +
                        ", Received: Rs. " + reservationInquiry.getPriceConfirmation()
                );
            }
        }

        // Get all seats and reservations to check for overlaps
        List<Seat> allSeats = seatRepository.findAll();
        List<Reservation> allReservations = reservationRepository.findAll();

        // Find seats that overlap with the requested route
        List<Long> conflictingSeatIds = allReservations.stream()
                .filter(reservation -> routesOverlap(
                        request.getOrigin(), request.getDestination(),
                        reservation.getFromLocation(), reservation.getToLocation()
                ))
                .flatMap(reservation -> reservation.getSeats().stream())
                .map(Seat::getId)
                .distinct()
                .collect(Collectors.toList());

        log.debug("Found {} seats with conflicting reservations", conflictingSeatIds.size());

        List<Seat> availableSeats = allSeats.stream()
                .filter(seat -> !conflictingSeatIds.contains(seat.getId()))
                .collect(Collectors.toList());

        log.debug("Number of  {} available seats", availableSeats.size());

        return new AvailabilityDetails(route, availableSeats);
    }

    private String generateReservationNumber() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        return "RES-" + timestamp + "-" + (int) (Math.random() * 1000);
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
