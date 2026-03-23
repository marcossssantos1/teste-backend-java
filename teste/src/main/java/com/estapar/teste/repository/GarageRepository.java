package com.estapar.teste.repository;

import com.estapar.teste.entity.Garage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GarageRepository extends JpaRepository<Garage, Long> {

    Optional<Garage> findBySector(String sector);
}
