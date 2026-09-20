package com.aquaconnect.backend.exception;

import com.aquaconnect.backend.enums.WorkOrderStatus;

public class InvalidWorkOrderTransitionException extends RuntimeException {

    public InvalidWorkOrderTransitionException(WorkOrderStatus current, WorkOrderStatus target) {
        super("Invalid work-order status transition from " + current + " to " + target);
    }
}
