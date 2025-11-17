package com.tharinduDev.bus.reservation.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "seats")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Seat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String seatNumber;

    // tracks if the seat is occupied for forward journey
    @Column(nullable = false)
    private boolean isBookedAD = false;

    // tracks if the seat is occupied for return journey
    @Column(nullable = false)
    private boolean isBookedDA = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_id")
    @JsonIgnore
    private Reservation reservation;

    public Seat(String seatNumber) {
        this.seatNumber = seatNumber;
    }

    // creating seats without reservations -for testing
    public Seat(Long id, String seatNumber) {
        this.id = id;
        this.seatNumber = seatNumber;
        this.isBookedAD = false;
        this.isBookedDA = false;
        this.reservation = null;
    }
}
