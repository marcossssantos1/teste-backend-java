package com.estapar.teste.repository;

import com.estapar.teste.entity.Vehicle;
import com.estapar.teste.enums.RecordStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VehicleRepository extends JpaRepository<Vehicle, Long> {

	Optional<Vehicle> findTopByLicensePlateAndStatusNotOrderByEntryTimeDesc(String licensePlate, RecordStatus status);
}
