package com.tharinduDev.bus.reservation.entity;

import com.tharinduDev.bus.reservation.enums.Location;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String reservationNumber;

    @ManyToMany
    @JoinTable(
            name = "reservation_seats",
            joinColumns = @JoinColumn(name = "reservation_id"),
            inverseJoinColumns = @JoinColumn(name = "seat_id")
    )
    private List<Seat> seats = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Location fromLocation;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Location toLocation;

    @Column(nullable = false)
    private BigDecimal totalPrice;

}
