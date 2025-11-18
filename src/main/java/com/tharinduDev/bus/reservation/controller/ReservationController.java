package com.tharinduDev.bus.reservation.controller;

import com.tharinduDev.bus.reservation.dto.AvailabilityReport;
import com.tharinduDev.bus.reservation.dto.ReservationDetails;
import com.tharinduDev.bus.reservation.dto.ReservationInquiry;
import com.tharinduDev.bus.reservation.dto.TicketInquiry;
import com.tharinduDev.bus.reservation.service.ReservationService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/reservations")
@Slf4j
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @PostMapping("/check-availability")
    public ResponseEntity<AvailabilityReport> checkAvailability(@Valid  @RequestBody TicketInquiry request) {
        log.info("Received availability check request: {} passengers from {} to {}",
                request.getPassengerCount(), request.getOrigin(), request.getDestination());
        AvailabilityReport response = reservationService.checkAvailability(request);
        log.info("Availability check completed: {} seats available", response.getAvailableSeats());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/reserve")
    public ResponseEntity<ReservationDetails> reserveTickets(@Valid @RequestBody ReservationInquiry request) {
        log.info("Received reservation request: {} passengers from {} to {} with price Rs. {}",
                request.getPassengerCount(), request.getOrigin(), request.getDestination(),
                request.getPriceConfirmation());
        ReservationDetails response = reservationService.reserveTickets(request);
        log.info("Reservation completed: Reservation number {}, {} seats assigned",
                response.getReservationNumber(), response.getSeatNumbers().size());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
