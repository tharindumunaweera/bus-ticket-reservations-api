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
@Table(name = "routes")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Route {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Location fromLocation;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Location toLocation;

    @Column(nullable = false)
    private BigDecimal price;

}
