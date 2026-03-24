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

	public double getDynamicPriceFactor(String sector) {
		long total = spotRepository.countBySector(sector);
		if (total == 0)
			return 1.0;

		long occupied = spotRepository.countBySectorAndOccupied(sector, true);
		double occupancyRate = (double) occupied / total;

		if (occupancyRate < 0.25)
			return 0.90;
		if (occupancyRate < 0.50)
			return 1.00;
		if (occupancyRate < 0.75)
			return 1.10;
		return 1.25;
	}

	public double calculateCharge(LocalDateTime entryTime, LocalDateTime exitTime, double priceApplied) {
		long minutes = Duration.between(entryTime, exitTime).toMinutes();

		if (minutes <= 30)
			return 0.0;

		long hours = (long) Math.ceil(minutes / 60.0);
		return Math.round(priceApplied * hours * 100.0) / 100.0;
	}
}