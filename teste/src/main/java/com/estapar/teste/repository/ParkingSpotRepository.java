package com.estapar.teste.repository;

import com.estapar.teste.entity.ParkingSpot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ParkingSpotRepository extends JpaRepository<ParkingSpot, Long> {

    List<ParkingSpot> findBySector(String sector);
    List<ParkingSpot> findBySectorAndOccupied(String sector, Boolean occupied);
    Optional<ParkingSpot> findByLatAndLng(Double lat, Double lng);
    long countBySectorAndOccupied(String sector, Boolean occupied);
    long countBySector(String sector);
}
