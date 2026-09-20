package com.aquaconnect.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.aquaconnect.backend.entity.IncidentGeoPoint;
import com.aquaconnect.backend.repository.IncidentGeoRepository;

@ExtendWith(MockitoExtension.class)
class IncidentGeoServiceTest {

    @Mock
    private IncidentGeoRepository incidentGeoRepository;

    @Test
    void findsNearbyIncidentsWithinRadius() {
        IncidentGeoPoint point = new IncidentGeoPoint();
        point = withId(point, UUID.randomUUID());
        point = withCoordinates(point, new BigDecimal("12.9716"), new BigDecimal("77.5946"));
        when(incidentGeoRepository.findNearby(new BigDecimal("12.9720"), new BigDecimal("77.5948"), 1000.0))
                .thenReturn(List.of(point));

        List<IncidentGeoPoint> nearby = new IncidentGeoService(incidentGeoRepository)
                .findNearby(new BigDecimal("12.9720"), new BigDecimal("77.5948"), 1000.0);

        assertThat(nearby).hasSize(1);
        assertThat(nearby.get(0).getLatitude()).isEqualByComparingTo(new BigDecimal("12.9716"));
    }

    private IncidentGeoPoint withId(IncidentGeoPoint point, UUID id) {
        try {
            java.lang.reflect.Field field = IncidentGeoPoint.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(point, id);
            return point;
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
    }

    private IncidentGeoPoint withCoordinates(IncidentGeoPoint point, BigDecimal latitude, BigDecimal longitude) {
        try {
            java.lang.reflect.Field latitudeField = IncidentGeoPoint.class.getDeclaredField("latitude");
            java.lang.reflect.Field longitudeField = IncidentGeoPoint.class.getDeclaredField("longitude");
            latitudeField.setAccessible(true);
            longitudeField.setAccessible(true);
            latitudeField.set(point, latitude);
            longitudeField.set(point, longitude);
            return point;
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
    }
}
