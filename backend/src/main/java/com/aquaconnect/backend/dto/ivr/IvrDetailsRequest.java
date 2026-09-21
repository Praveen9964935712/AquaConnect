package com.aquaconnect.backend.dto.ivr;

import com.aquaconnect.backend.enums.IncidentCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record IvrDetailsRequest(@NotNull IncidentCategory category, @NotBlank @Size(max = 2000) String description) {}
