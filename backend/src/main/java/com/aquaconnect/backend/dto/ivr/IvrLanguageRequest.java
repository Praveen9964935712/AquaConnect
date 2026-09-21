package com.aquaconnect.backend.dto.ivr;

import com.aquaconnect.backend.enums.IvrLanguage;
import jakarta.validation.constraints.NotNull;

public record IvrLanguageRequest(@NotNull IvrLanguage language) {}
