package com.tharinduDev.bus.reservation.entity;

import com.tharinduDev.bus.reservation.enums.Location;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
@Entity
@Table(name = "reservations")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String reservationNumber;

    @OneToMany(mappedBy = "reservation", cascade = CascadeType.ALL)
    private List<Seat> seats = new ArrayList<>();

    @Column(nullable = false)
    private Integer passengerCount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Location fromLocation;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Location toLocation;

    @Column(nullable = false)
    private BigDecimal totalPrice;

}
