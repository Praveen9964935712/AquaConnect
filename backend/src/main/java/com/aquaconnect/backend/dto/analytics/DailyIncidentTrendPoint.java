package com.aquaconnect.backend.dto.analytics;

import java.time.LocalDate;

public record DailyIncidentTrendPoint(LocalDate date, long count) {
}
