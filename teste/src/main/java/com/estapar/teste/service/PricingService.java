package com.estapar.teste.service;

import com.estapar.teste.repository.ParkingSpotRepository;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;

@Service
public class PricingService {

    private final ParkingSpotRepository spotRepository;

    public PricingService(ParkingSpotRepository spotRepository) {
        this.spotRepository = spotRepository;
    }

    /**
     * Calcula o fator de preço dinâmico baseado na lotação do setor no momento da entrada.
     * < 25%  → desconto 10%  (fator 0.90)
     * < 50%  → sem desconto  (fator 1.00)
     * < 75%  → aumento 10%  (fator 1.10)
     * <= 100% → aumento 25% (fator 1.25)
     */
    public double getDynamicPriceFactor(String sector) {
        long total = spotRepository.countBySector(sector);
        if (total == 0) return 1.0;

        long occupied = spotRepository.countBySectorAndOccupied(sector, true);
        double occupancyRate = (double) occupied / total;

        if (occupancyRate < 0.25) return 0.90;
        if (occupancyRate < 0.50) return 1.00;
        if (occupancyRate < 0.75) return 1.10;
        return 1.25;
    }

    /**
     * Calcula o valor a cobrar na saída.
     * - Primeiros 30 minutos: grátis
     * - Após 30 minutos: preço por hora cheia (arredonda para cima)
     */
    public double calculateCharge(LocalDateTime entryTime, LocalDateTime exitTime, double priceApplied) {
        long minutes = Duration.between(entryTime, exitTime).toMinutes();

        if (minutes <= 30) return 0.0;

        long hours = (long) Math.ceil(minutes / 60.0);
        return Math.round(priceApplied * hours * 100.0) / 100.0;
    }
}