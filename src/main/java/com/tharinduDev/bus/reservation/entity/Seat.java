package com.tharinduDev.bus.reservation.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

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

    @ManyToMany(mappedBy = "seats")
    @JsonIgnore
    private List<Reservation> reservations = new ArrayList<>();

    // Creating seats without reservations
    public Seat(Long id, String seatNumber) {
        this.id = id;
        this.seatNumber = seatNumber;
        this.reservations = new ArrayList<>();
    }
}
