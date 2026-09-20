package com.aquaconnect.backend.dto.workorder;

import com.aquaconnect.backend.enums.WorkOrderStatus;

import jakarta.validation.constraints.NotNull;

public record WorkOrderStatusRequest(@NotNull WorkOrderStatus status) {
}
