package com.tharinduDev.bus.reservation.exception;

public class NoSeatsAvailableException extends RuntimeException {

    public NoSeatsAvailableException(String message) {
        super(message);
    }
}
