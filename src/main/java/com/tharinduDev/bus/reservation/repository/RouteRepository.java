package com.tharinduDev.bus.reservation.repository;

import com.tharinduDev.bus.reservation.entity.Route;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RouteRepository extends JpaRepository<Route, Long> {
}
