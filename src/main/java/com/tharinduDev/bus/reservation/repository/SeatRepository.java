package com.tharinduDev.bus.reservation.repository;

import com.tharinduDev.bus.reservation.entity.Seat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SeatRepository extends JpaRepository<Seat, Long> {

    List<Seat> findByIsBookedADFalse();
    List<Seat> findByIsBookedDAFalse();
}
