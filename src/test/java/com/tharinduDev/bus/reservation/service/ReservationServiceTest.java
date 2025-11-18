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

    private Reservation createReservation(Long id, String resNumber, Location from, Location to, Seat... seats) {
        Reservation reservation = new Reservation();
        reservation.setId(id);
        reservation.setReservationNumber(resNumber);
        reservation.setFromLocation(from);
        reservation.setToLocation(to);
        reservation.setPassengerCount(seats.length);
        reservation.setTotalPrice(new BigDecimal("100.00"));

        List<Seat> seatList = new ArrayList<>();
        for (Seat seat : seats) {
            seat.setReservation(reservation);
            seatList.add(seat);
        }
        reservation.setSeats(seatList);

        return reservation;
    }

    // -- checkAvailability Tests --

    @Test
    void checkAvailability_ForwardTrip_Success() {

        TicketInquiry request = new TicketInquiry(2, Location.A, Location.B);
        when(routeRepository.findByFromLocationAndToLocation(Location.A, Location.B))
                .thenReturn(Optional.of(forwardRoute));
        when(seatRepository.findAll()).thenReturn(testSeats);

        AvailabilityReport response = reservationService.checkAvailability(request);

        assertNotNull(response);
        assertEquals(10, response.getAvailableSeats());
        assertEquals(new BigDecimal("50.00"), response.getPricePerSeat());
        assertEquals(new BigDecimal("100.00"), response.getTotalPrice());
        assertEquals(10, response.getAvailableSeatNumbers().size());
        assertTrue(response.getAvailableSeatNumbers().contains("1A"));
        assertTrue(response.getAvailableSeatNumbers().contains("10A"));

        verify(routeRepository).findByFromLocationAndToLocation(Location.A, Location.B);
        verify(seatRepository).findAll();
    }

    // partial availability when some seats are booked in forward direction
    @Test
    void checkAvailability_ForwardTrip_PartialAvailability() {

        // only 6 out of 10 seats available
        TicketInquiry request = new TicketInquiry(2, Location.A, Location.B);
        List<Seat> availableSeats = testSeats.subList(0, 6);

        when(routeRepository.findByFromLocationAndToLocation(Location.A, Location.B))
                .thenReturn(Optional.of(forwardRoute));
        when(seatRepository.findAll()).thenReturn(availableSeats);

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
        when(seatRepository.findAll()).thenReturn(Collections.emptyList());

        AvailabilityReport response = reservationService.checkAvailability(request);

        assertEquals(0, response.getAvailableSeats());
        assertEquals(0, response.getAvailableSeatNumbers().size());
    }

    @Test
    void checkAvailability_ReturnTrip_Success() {

        TicketInquiry request = new TicketInquiry(2, Location.B, Location.A);
        when(routeRepository.findByFromLocationAndToLocation(Location.B, Location.A))
                .thenReturn(Optional.of(returnRoute));
        when(seatRepository.findAll()).thenReturn(testSeats);

        AvailabilityReport response = reservationService.checkAvailability(request);

        assertEquals(10, response.getAvailableSeats());
        assertEquals(new BigDecimal("50.00"), response.getPricePerSeat());
        assertEquals(new BigDecimal("100.00"), response.getTotalPrice());
        assertEquals(10, response.getAvailableSeatNumbers().size());
        assertTrue(response.getAvailableSeatNumbers().contains("1A"));
        assertTrue(response.getAvailableSeatNumbers().contains("10A"));

        verify(routeRepository).findByFromLocationAndToLocation(Location.B, Location.A);
        verify(seatRepository).findAll();
    }

    // partial availability when some seats are booked in return direction
    @Test
    void testCheckAvailability_ReturnTrip_PartialAvailability() {
        // only 6 out of 10 seats available
        TicketInquiry request = new TicketInquiry(2, Location.B, Location.A);
        List<Seat> availableSeats = testSeats.subList(0, 6);

        when(routeRepository.findByFromLocationAndToLocation(Location.B, Location.A))
                .thenReturn(Optional.of(returnRoute));
        when(seatRepository.findAll()).thenReturn(availableSeats);

        AvailabilityReport response = reservationService.checkAvailability(request);

        assertEquals(6, response.getAvailableSeats());
        assertEquals(6, response.getAvailableSeatNumbers().size());
    }

    // no seats are available in return direction
    @Test
    void checkAvailability_ReturnTrip_NoSeatsAvailable() {

        TicketInquiry request = new TicketInquiry(2, Location.B, Location.A);
        when(routeRepository.findByFromLocationAndToLocation(Location.B, Location.A))
                .thenReturn(Optional.of(returnRoute));
        when(seatRepository.findAll()).thenReturn(Collections.emptyList());

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
        // only route repository should be called
        verify(routeRepository).findByFromLocationAndToLocation(Location.A, Location.B);
        verifyNoInteractions(seatRepository, reservationRepository);
    }


    // -- Reserve Tickets TESTS --

    // reserve tickets when all seats are available
    @Test
    void reserveTickets_Success_NoOverlap() {

        ReservationInquiry request = new ReservationInquiry(2, Location.A, Location.B, new BigDecimal("100.00"));

        when(routeRepository.findByFromLocationAndToLocation(Location.A, Location.B))
                .thenReturn(Optional.of(forwardRoute));
        when(seatRepository.findAll()).thenReturn(testSeats);
        when(reservationRepository.findAll()).thenReturn(Collections.emptyList());
        when(reservationRepository.save(any(Reservation.class)))
                .thenAnswer(response -> {
                    Reservation res = response.getArgument(0);
                    res.setId(1L);
                    return res;
                });

        ReservationDetails response = reservationService.reserveTickets(request);

        assertNotNull(response);
        assertNotNull(response.getReservationNumber());
        assertTrue(response.getReservationNumber().startsWith("RES-"));
        assertEquals(2, response.getSeatNumbers().size());
        assertEquals(Location.A, response.getDepartureLocation());
        assertEquals(Location.B, response.getArrivalLocation());
        assertEquals(new BigDecimal("100.00"), response.getTotalPrice());

        verify(reservationRepository).save(any(Reservation.class));

    }

    // avoiding already booked ones on overlapping route
    @Test
    void reserveTickets_Success_AvoidBookedSeats() {

        ReservationInquiry request = new ReservationInquiry(2, Location.A, Location.B, new BigDecimal("100.00"));

        Seat bookedSeat1 = testSeats.get(0); // 1A
        Seat bookedSeat2 = testSeats.get(1); // 2A
        Reservation existingReservation = createReservation(1L, "RES-001", Location.A, Location.C, bookedSeat1, bookedSeat2);

        when(routeRepository.findByFromLocationAndToLocation(Location.A, Location.B))
                .thenReturn(Optional.of(forwardRoute));
        when(seatRepository.findAll()).thenReturn(testSeats);
        when(reservationRepository.findAll()).thenReturn(List.of(existingReservation));
        when(reservationRepository.save(any(Reservation.class))).thenAnswer(invocation -> {
            Reservation res = invocation.getArgument(0);
            res.setId(2L);
            return res;
        });

        ReservationDetails response = reservationService.reserveTickets(request);

        assertEquals(2, response.getSeatNumbers().size());
        assertFalse(response.getSeatNumbers().contains("1A"));
        assertFalse(response.getSeatNumbers().contains("2A"));
        assertTrue(response.getSeatNumbers().contains("3A"));
        assertTrue(response.getSeatNumbers().contains("4A"));
    }

    @Test
    void reserveTickets_Success_NonOverlappingBookings() {

        // book A->B when B->C is already booked (no overlap)
        ReservationInquiry request = new ReservationInquiry(2, Location.A, Location.B, new BigDecimal("100.00"));

        Seat bookedSeat1 = testSeats.get(0); // 1A
        Seat bookedSeat2 = testSeats.get(1); // 2A
        Reservation existingReservation = createReservation(1L, "RES-001", Location.B, Location.C, bookedSeat1, bookedSeat2);

        when(routeRepository.findByFromLocationAndToLocation(Location.A, Location.B))
                .thenReturn(Optional.of(forwardRoute));
        when(seatRepository.findAll()).thenReturn(testSeats);
        when(reservationRepository.findAll()).thenReturn(List.of(existingReservation));
        when(reservationRepository.save(any(Reservation.class))).thenAnswer(invocation -> {
            Reservation res = invocation.getArgument(0);
            res.setId(2L);
            return res;
        });

        ReservationDetails response = reservationService.reserveTickets(request);

        assertEquals(2, response.getSeatNumbers().size());
        assertTrue(response.getSeatNumbers().contains("1A"));
        assertTrue(response.getSeatNumbers().contains("2A"));

    }

    @Test
    void reserveTickets_ReturnTrip_Success_NoOverlap() {

        ReservationInquiry request = new ReservationInquiry(2, Location.B, Location.A, new BigDecimal("100.00"));

        when(routeRepository.findByFromLocationAndToLocation(Location.B, Location.A))
                .thenReturn(Optional.of(forwardRoute));
        when(seatRepository.findAll()).thenReturn(testSeats);
        when(reservationRepository.findAll()).thenReturn(Collections.emptyList());
        when(reservationRepository.save(any(Reservation.class)))
                .thenAnswer(response -> {
                    Reservation res = response.getArgument(0);
                    res.setId(1L);
                    return res;
                });

        ReservationDetails response = reservationService.reserveTickets(request);

        assertNotNull(response);
        assertNotNull(response.getReservationNumber());
        assertTrue(response.getReservationNumber().startsWith("RES-"));
        assertEquals(2, response.getSeatNumbers().size());
        assertEquals(Location.B, response.getDepartureLocation());
        assertEquals(Location.A, response.getArrivalLocation());
        assertEquals(new BigDecimal("100.00"), response.getTotalPrice());

        verify(reservationRepository).save(any(Reservation.class));

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
        when(seatRepository.findAll()).thenReturn(limitedSeats);

        NoSeatsAvailableException exception = assertThrows(NoSeatsAvailableException.class,
                () -> reservationService.reserveTickets(request));
        assertEquals("Not enough seats available. Requested: 10, Available: 5", exception.getMessage());

        verify(reservationRepository, never()).save(any());
    }

}
