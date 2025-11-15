package com.tharinduDev.bus.reservation.repository;

import com.tharinduDev.bus.reservation.entity.Seat;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SeatRepository extends JpaRepository<Seat, Long> {
}
