package com.aquaconnect.backend.repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.aquaconnect.backend.entity.IncidentGeoPoint;

public interface IncidentGeoRepository extends JpaRepository<IncidentGeoPoint, UUID> {

    @Query(value = "SELECT * FROM incidents i " +
            "WHERE i.latitude IS NOT NULL AND i.longitude IS NOT NULL " +
            "AND ST_DWithin(ST_SetSRID(ST_MakePoint(i.longitude, i.latitude), 4326), " +
            "ST_SetSRID(ST_MakePoint(:longitude, :latitude), 4326), :radiusMeters)",
            nativeQuery = true)
    List<IncidentGeoPoint> findNearby(@Param("latitude") BigDecimal latitude,
            @Param("longitude") BigDecimal longitude,
            @Param("radiusMeters") double radiusMeters);
}
