package com.tharinduDev.bus.reservation.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.tharinduDev.bus.reservation.dto.AvailabilityReport;
import com.tharinduDev.bus.reservation.dto.ReservationDetails;
import com.tharinduDev.bus.reservation.dto.ReservationInquiry;
import com.tharinduDev.bus.reservation.dto.TicketInquiry;
import com.tharinduDev.bus.reservation.enums.Location;
import com.tharinduDev.bus.reservation.exception.GlobalExceptionHandler;
import com.tharinduDev.bus.reservation.exception.InvalidReservationException;
import com.tharinduDev.bus.reservation.service.ReservationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ReservationController.class)
@Import(GlobalExceptionHandler.class)
public class ReservationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ReservationService reservationService;

    private static final String API_ENDPOINT_CHECK_AVAILABILITY = "/api/v1/reservations/check-availability";
    private static final String API_ENDPOINT_RESERVE = "/api/v1/reservations/reserve";

    @Test
    void checkAvailability_Success() throws Exception {
        // set up the request body
        TicketInquiry request = new TicketInquiry(2,Location.A, Location.C);

        // set up the expected response from the mocked service
        AvailabilityReport mockResponse = new AvailabilityReport();
        mockResponse.setAvailableSeats(40);
        mockResponse.setPricePerSeat(new BigDecimal("100.00"));
        mockResponse.setTotalPrice(new BigDecimal("200.00"));
        mockResponse.setAvailableSeatNumbers(List.of("1A", "1B"));

        // define Mock behavior of service class
        when(reservationService.checkAvailability(any(TicketInquiry.class)))
                .thenReturn(mockResponse);

        mockMvc.perform(post(API_ENDPOINT_CHECK_AVAILABILITY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                // Assert response body content matches the mocked object
                .andExpect(jsonPath("$.availableSeats").value(40))
                .andExpect(jsonPath("$.totalPrice").value(200.00));
    }

    @Test
    void checkAvailability_ValidationFailure_MaxPassengers() throws Exception {
        TicketInquiry invalidInquiry = new TicketInquiry(41, Location.A, Location.D);

        mockMvc.perform(post(API_ENDPOINT_CHECK_AVAILABILITY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidInquiry)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.passengerCount").value("Number of passengers cannot exceed 40"));
    }

    @Test
    void checkAvailability_Origin_Destination_Exception() throws Exception {
        TicketInquiry invalidInquiry = new TicketInquiry(2, Location.A, Location.A);

        when(reservationService.checkAvailability(any(TicketInquiry.class)))
                .thenThrow(new InvalidReservationException("Origin and destination cannot be the same"));

        mockMvc.perform(post(API_ENDPOINT_CHECK_AVAILABILITY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidInquiry)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Origin and destination cannot be the same"));
    }

    @Test
    void reserveTickets_Success() throws Exception {

        ReservationInquiry reservationInquiry = new ReservationInquiry(2, Location.A, Location.B, new BigDecimal("100.00"));

        ReservationDetails mockResponse = new ReservationDetails(
                "RES-20251117120413-732",
                List.of("1A", "1B"),
                Location.A,
                Location.B,
                new BigDecimal("100.00"));

        when(reservationService.reserveTickets(any(ReservationInquiry.class)))
                .thenReturn(mockResponse);

        mockMvc.perform(post(API_ENDPOINT_RESERVE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reservationInquiry)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.reservationNumber").value("RES-20251117120413-732"))
                .andExpect(jsonPath("$.seatNumbers").isArray())
                .andExpect(jsonPath("$.seatNumbers", hasSize(2)))
                .andExpect(jsonPath("$.departureLocation").value("A"))
                .andExpect(jsonPath("$.arrivalLocation").value("B"))
                .andExpect(jsonPath("$.totalPrice").value(100.00));
    }

    @Test
    void reserveTickets_PriceConfirmationMismatch() throws Exception {
        ReservationInquiry reservationInquiry = new ReservationInquiry(1, Location.A, Location.B, new BigDecimal("100.00"));

        when(reservationService.reserveTickets(any(ReservationInquiry.class)))
                .thenThrow(new InvalidReservationException("Price confirmation mismatch"));

        mockMvc.perform(post(API_ENDPOINT_RESERVE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reservationInquiry)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("Price confirmation mismatch")));

    }



}
