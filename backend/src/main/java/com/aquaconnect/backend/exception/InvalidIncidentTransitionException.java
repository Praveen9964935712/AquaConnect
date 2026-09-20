package com.aquaconnect.backend.exception;

import com.aquaconnect.backend.enums.IncidentStatus;

public class InvalidIncidentTransitionException extends RuntimeException {

    public InvalidIncidentTransitionException(IncidentStatus current, IncidentStatus target) {
        super("Invalid incident status transition from " + current + " to " + target);
    }
}
