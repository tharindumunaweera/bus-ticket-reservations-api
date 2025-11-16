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
        reservation.setPassengerCount(request.getPassengerCount());

        // mark seats as booked and link to reservation
        for (Seat seat : assignedSeats) {
            if (details.isForwardTrip()) {
                seat.setBookedAD(true);
            } else {
                seat.setBookedDA(true);
            }
            seat.setReservation(reservation);
            reservation.getSeats().add(seat);
        }

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

        boolean isForwardTrip = isForwardDirection(request.getOrigin(), request.getDestination());

        List<Seat> availableSeats = isForwardTrip ? seatRepository.findByIsBookedADFalse() : seatRepository.findByIsBookedDAFalse();

        return new AvailabilityDetails(route, availableSeats, isForwardTrip);
    }

    // check if a route is in the forward direction (A -> D) or return direction
    private boolean isForwardDirection(Location from, Location to) {
        int fromPos = getLocationPosition(from);
        int toPos = getLocationPosition(to);
        return fromPos < toPos;
    }

    private String generateReservationNumber() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        return "RES-" + timestamp + "-" + (int) (Math.random() * 1000);
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
