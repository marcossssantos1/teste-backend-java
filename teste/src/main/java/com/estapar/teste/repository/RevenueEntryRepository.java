package com.estapar.teste.repository;

import com.estapar.teste.entity.RevenueEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;


public interface RevenueEntryRepository extends JpaRepository<RevenueEntry, Long> {

    @Query("SELECT COALESCE(SUM(r.amount), 0) FROM RevenueEntry r WHERE r.sector = :sector AND r.date = :date")
    Double sumAmountBySectorAndDate(String sector, LocalDate date);
}
