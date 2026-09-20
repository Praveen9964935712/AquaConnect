package com.aquaconnect.backend.dto.workorder;

import jakarta.validation.constraints.Size;

public record WorkOrderFindingsRequest(
        @Size(max = 2000) String inspectionNotes,
        @Size(max = 1000) String observedCondition,
        @Size(max = 2000) String repairNotes) {
}