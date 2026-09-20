package com.aquaconnect.backend.dto.incident;

import java.math.BigDecimal;

import com.aquaconnect.backend.enums.IncidentCategory;
import com.aquaconnect.backend.enums.IncidentSource;
import com.aquaconnect.backend.enums.LocationSource;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record IncidentCreateRequest(
        @NotNull IncidentCategory category,
        @NotBlank @Size(max = 2000) String description,
        @DecimalMin("-90.0") @DecimalMax("90.0") BigDecimal latitude,
        @DecimalMin("-180.0") @DecimalMax("180.0") BigDecimal longitude,
        LocationSource locationSource,
        @DecimalMin("0.0") BigDecimal locationAccuracy,
        IncidentSource source) {
}
