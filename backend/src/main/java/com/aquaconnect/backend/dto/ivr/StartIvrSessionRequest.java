package com.aquaconnect.backend.dto.ivr;

import jakarta.validation.constraints.Size;

public record StartIvrSessionRequest(@Size(max = 100) String callerIdentifier) {}
