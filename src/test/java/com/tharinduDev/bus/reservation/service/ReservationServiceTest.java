package com.tharinduDev.bus.reservation.service;

import com.tharinduDev.bus.reservation.dto.AvailabilityReport;
import com.tharinduDev.bus.reservation.dto.ReservationDetails;
import com.tharinduDev.bus.reservation.dto.ReservationInquiry;
import com.tharinduDev.bus.reservation.dto.TicketInquiry;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ReservationServiceTest {

    @Mock
    private RouteRepository routeRepository;

    @Mock
    private SeatRepository seatRepository;

    @Mock
    private ReservationRepository reservationRepository;

    @InjectMocks
    private ReservationService reservationService;

    private Route forwardRoute;
    private Route returnRoute;
    private List<Seat> testSeats;

    @BeforeEach
    void setUp() {
        // Setup test routes
        forwardRoute = new Route(1L, Location.A, Location.B, new BigDecimal("50.00"));
        returnRoute = new Route(2L, Location.B, Location.A, new BigDecimal("50.00"));

        // setup test seats - 10 seats
        testSeats = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            testSeats.add(new Seat((long) i, i + "A"));
        }
    }

    // -- checkAvailability Tests --

    @Test
    void checkAvailability_ForwardTrip_Success() {

        TicketInquiry request = new TicketInquiry(2, Location.A, Location.B);
        when(routeRepository.findByFromLocationAndToLocation(Location.A, Location.B))
                .thenReturn(Optional.of(forwardRoute));
        when(seatRepository.findByIsBookedADFalse()).thenReturn(testSeats);

        AvailabilityReport response = reservationService.checkAvailability(request);

        assertNotNull(response);
        assertEquals(10, response.getAvailableSeats());
        assertEquals(new BigDecimal("50.00"), response.getPricePerSeat());
        assertEquals(new BigDecimal("100.00"), response.getTotalPrice());
        assertEquals(10, response.getAvailableSeatNumbers().size());
        assertTrue(response.getAvailableSeatNumbers().contains("1A"));
        assertTrue(response.getAvailableSeatNumbers().contains("10A"));

        verify(routeRepository).findByFromLocationAndToLocation(Location.A, Location.B);
        verify(seatRepository).findByIsBookedADFalse();
    }

    // partial availability when some seats are booked in forward direction
    @Test
    void checkAvailability_ForwardTrip_PartialAvailability() {

        // only 6 out of 10 seats available
        TicketInquiry request = new TicketInquiry(2, Location.A, Location.B);
        List<Seat> availableSeats = testSeats.subList(0, 6);

        when(routeRepository.findByFromLocationAndToLocation(Location.A, Location.B))
                .thenReturn(Optional.of(forwardRoute));
        when(seatRepository.findByIsBookedADFalse()).thenReturn(availableSeats);

        AvailabilityReport response = reservationService.checkAvailability(request);

        assertEquals(6, response.getAvailableSeats());
        assertEquals(6, response.getAvailableSeatNumbers().size());
    }

    // No seats are available in forward direction
    @Test
    void checkAvailability_ForwardTrip_NoSeatsAvailable() {

        TicketInquiry request = new TicketInquiry(2, Location.A, Location.B);
        when(routeRepository.findByFromLocationAndToLocation(Location.A, Location.B))
                .thenReturn(Optional.of(forwardRoute));
        when(seatRepository.findByIsBookedADFalse()).thenReturn(Collections.emptyList());

        AvailabilityReport response = reservationService.checkAvailability(request);

        assertEquals(0, response.getAvailableSeats());
        assertEquals(0, response.getAvailableSeatNumbers().size());
    }

    @Test
    void checkAvailability_ReturnTrip_Success() {

        TicketInquiry request = new TicketInquiry(2, Location.B, Location.A);
        when(routeRepository.findByFromLocationAndToLocation(Location.B, Location.A))
                .thenReturn(Optional.of(returnRoute));
        when(seatRepository.findByIsBookedDAFalse()).thenReturn(testSeats);

        AvailabilityReport response = reservationService.checkAvailability(request);

        assertEquals(10, response.getAvailableSeats());
        assertEquals(new BigDecimal("50.00"), response.getPricePerSeat());
        assertEquals(new BigDecimal("100.00"), response.getTotalPrice());
        assertEquals(10, response.getAvailableSeatNumbers().size());
        assertTrue(response.getAvailableSeatNumbers().contains("1A"));
        assertTrue(response.getAvailableSeatNumbers().contains("10A"));

        verify(routeRepository).findByFromLocationAndToLocation(Location.B, Location.A);
        verify(seatRepository).findByIsBookedDAFalse();
    }

    // partial availability when some seats are booked in return direction
    @Test
    void testCheckAvailability_ReturnTrip_PartialAvailability() {
        // only 6 out of 10 seats available
        TicketInquiry request = new TicketInquiry(2, Location.B, Location.A);
        List<Seat> availableSeats = testSeats.subList(0, 6);

        when(routeRepository.findByFromLocationAndToLocation(Location.B, Location.A))
                .thenReturn(Optional.of(returnRoute));
        when(seatRepository.findByIsBookedDAFalse()).thenReturn(availableSeats);

        AvailabilityReport response = reservationService.checkAvailability(request);

        assertEquals(6, response.getAvailableSeats());
        assertEquals(6, response.getAvailableSeatNumbers().size());
    }

    // No seats are available in return direction
    @Test
    void checkAvailability_ReturnTrip_NoSeatsAvailable() {

        TicketInquiry request = new TicketInquiry(2, Location.B, Location.A);
        when(routeRepository.findByFromLocationAndToLocation(Location.B, Location.A))
                .thenReturn(Optional.of(returnRoute));
        when(seatRepository.findByIsBookedDAFalse()).thenReturn(Collections.emptyList());

        AvailabilityReport response = reservationService.checkAvailability(request);

        assertEquals(0, response.getAvailableSeats());
        assertEquals(0, response.getAvailableSeatNumbers().size());
    }

    @Test
    void check_InvalidReservationException_SameOriginDestinationException() {

        TicketInquiry invalidRequest = new TicketInquiry(1, Location.A, Location.A);

        InvalidReservationException exception = assertThrows(InvalidReservationException.class, () ->
                reservationService.checkAvailability(invalidRequest)
        );

        assertEquals("Origin and destination cannot be the same", exception.getMessage());
        // only hit the initial validation, no repository interactions
        verifyNoInteractions(routeRepository, seatRepository, reservationRepository);
    }

    @Test
    void check_RouteNotFoundException() {

        TicketInquiry request = new TicketInquiry(1, Location.A, Location.B);
        when(routeRepository.findByFromLocationAndToLocation(Location.A, Location.B)).thenReturn(Optional.empty());

        RouteNotFoundException exception = assertThrows(RouteNotFoundException.class, () ->
                reservationService.checkAvailability(request)
        );

        assertEquals("No route found from " + request.getOrigin() + " to " + request.getDestination(), exception.getMessage());
        // Only route repository should be called
        verify(routeRepository).findByFromLocationAndToLocation(Location.A, Location.B);
        verifyNoInteractions(seatRepository, reservationRepository);
    }


    // -- Reserve Tickets TESTS --

    @Test
    void reserveTickets_ForwardTrip_Success() {

        ReservationInquiry request = new ReservationInquiry(2, Location.A, Location.B, new BigDecimal("100.00"));

        when(routeRepository.findByFromLocationAndToLocation(Location.A, Location.B))
                .thenReturn(Optional.of(forwardRoute));
        when(seatRepository.findByIsBookedADFalse()).thenReturn(testSeats);
        when(reservationRepository.save(any(Reservation.class)))
                .thenAnswer(response -> response.getArgument(0));

        ReservationDetails response = reservationService.reserveTickets(request);

        assertNotNull(response);
        assertNotNull(response.getReservationNumber());
        assertTrue(response.getReservationNumber().startsWith("RES-"));
        assertEquals(2, response.getSeatNumbers().size());
        assertEquals(Location.A, response.getDepartureLocation());
        assertEquals(Location.B, response.getArrivalLocation());
        assertEquals(new BigDecimal("100.00"), response.getTotalPrice());

        // verify seats are marked as booked in forward direction
        ArgumentCaptor<Reservation> reservationCaptor = ArgumentCaptor.forClass(Reservation.class);
        verify(reservationRepository).save(reservationCaptor.capture());
        Reservation savedReservation = reservationCaptor.getValue();
        assertEquals(2, savedReservation.getSeats().size());
        savedReservation.getSeats().forEach(seat -> assertTrue(seat.isBookedAD()));

    }

    @Test
    void reserveTickets_ForwardTrip_AllSeats() {

        ReservationInquiry request = new ReservationInquiry(10, Location.A, Location.B, new BigDecimal("500.00"));

        when(routeRepository.findByFromLocationAndToLocation(Location.A, Location.B))
                .thenReturn(Optional.of(forwardRoute));
        when(seatRepository.findByIsBookedADFalse()).thenReturn(testSeats);
        when(reservationRepository.save(any(Reservation.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ReservationDetails response = reservationService.reserveTickets(request);

        assertEquals(10, response.getSeatNumbers().size());
        assertEquals(new BigDecimal("500.00"), response.getTotalPrice());
    }

    @Test
    void reserveTickets_ReturnTrip_Success() {

        ReservationInquiry request = new ReservationInquiry(2, Location.B, Location.A, new BigDecimal("100.00"));

        when(routeRepository.findByFromLocationAndToLocation(Location.B, Location.A))
                .thenReturn(Optional.of(returnRoute));
        when(seatRepository.findByIsBookedDAFalse()).thenReturn(testSeats);
        when(reservationRepository.save(any(Reservation.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));


        ReservationDetails response = reservationService.reserveTickets(request);

        assertNotNull(response);
        assertEquals(2, response.getSeatNumbers().size());
        assertEquals(Location.B, response.getDepartureLocation());
        assertEquals(Location.A, response.getArrivalLocation());
        assertEquals(new BigDecimal("100.00"), response.getTotalPrice());

        // verify seats are marked as booked in return direction
        ArgumentCaptor<Reservation> reservationCaptor = ArgumentCaptor.forClass(Reservation.class);
        verify(reservationRepository).save(reservationCaptor.capture());
        Reservation savedReservation = reservationCaptor.getValue();
        savedReservation.getSeats().forEach(seat -> assertTrue(seat.isBookedDA()));
    }

    @Test
    void reserveTickets_PriceConfirmationMismatchException() {
        // adding incorrect price
        ReservationInquiry request = new ReservationInquiry(2, Location.A, Location.B, new BigDecimal("150.00"));

        when(routeRepository.findByFromLocationAndToLocation(Location.A, Location.B))
                .thenReturn(Optional.of(forwardRoute));

        InvalidReservationException exception = assertThrows(InvalidReservationException.class,
                () -> reservationService.reserveTickets(request));
        assertEquals("Price confirmation mismatch. Expected: Rs. 100.00, Received: Rs. 150.00", exception.getMessage());

        verifyNoInteractions(seatRepository, reservationRepository);
    }

    @Test
    void reserveTickets_NotEnoughSeatsException() {
        ReservationInquiry request = new ReservationInquiry(10, Location.A, Location.B, new BigDecimal("500.00"));

        List<Seat> limitedSeats = testSeats.subList(0, 5);

        when(routeRepository.findByFromLocationAndToLocation(Location.A, Location.B))
                .thenReturn(Optional.of(forwardRoute));
        when(seatRepository.findByIsBookedADFalse()).thenReturn(limitedSeats);

        NoSeatsAvailableException exception = assertThrows(NoSeatsAvailableException.class,
                () -> reservationService.reserveTickets(request));
        assertEquals("Not enough seats available. Requested: 10, Available: 5", exception.getMessage());

        verifyNoInteractions(reservationRepository);
    }

}
