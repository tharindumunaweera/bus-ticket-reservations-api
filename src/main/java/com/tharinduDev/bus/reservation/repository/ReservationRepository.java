package com.tharinduDev.bus.reservation.repository;

import com.tharinduDev.bus.reservation.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
}
