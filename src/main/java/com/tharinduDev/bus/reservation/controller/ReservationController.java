package com.tharinduDev.bus.reservation.controller;

import com.tharinduDev.bus.reservation.dto.AvailabilityReport;
import com.tharinduDev.bus.reservation.dto.TicketInquiry;
import com.tharinduDev.bus.reservation.service.ReservationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/reservations")
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @PostMapping("/check-availability")
    public ResponseEntity<AvailabilityReport> checkAvailability(@RequestBody TicketInquiry request) {
        AvailabilityReport response = reservationService.checkAvailability(request);
        return ResponseEntity.ok(response);
    }
}
