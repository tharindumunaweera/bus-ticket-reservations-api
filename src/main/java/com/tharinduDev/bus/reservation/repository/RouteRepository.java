package com.tharinduDev.bus.reservation.repository;

import com.tharinduDev.bus.reservation.entity.Route;
import com.tharinduDev.bus.reservation.enums.Location;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RouteRepository extends JpaRepository<Route, Long> {

    Optional<Route> findByFromLocationAndToLocation(Location fromLocation, Location toLocation);
}
