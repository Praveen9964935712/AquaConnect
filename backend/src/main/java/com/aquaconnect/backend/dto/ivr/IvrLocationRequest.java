package com.aquaconnect.backend.dto.ivr;

import java.math.BigDecimal;

import com.aquaconnect.backend.enums.LocationSource;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

public record IvrLocationRequest(@DecimalMin("-90.0") @DecimalMax("90.0") BigDecimal latitude,
        @DecimalMin("-180.0") @DecimalMax("180.0") BigDecimal longitude,
        @NotNull LocationSource locationSource, @DecimalMin("0.0") BigDecimal locationAccuracy) {}
