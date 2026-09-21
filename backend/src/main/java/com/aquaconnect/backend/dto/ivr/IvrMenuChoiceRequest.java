package com.aquaconnect.backend.dto.ivr;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record IvrMenuChoiceRequest(@NotNull @Min(1) @Max(9) Integer choice) {}
