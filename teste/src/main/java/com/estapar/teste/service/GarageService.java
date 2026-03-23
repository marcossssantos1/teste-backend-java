package com.estapar.teste.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.estapar.teste.dto.GarageDto;
import com.estapar.teste.dto.GarageResponseDto;
import com.estapar.teste.dto.ParkingSpotDto;
import com.estapar.teste.entity.Garage;
import com.estapar.teste.entity.ParkingSpot;
import com.estapar.teste.repository.GarageRepository;
import com.estapar.teste.repository.ParkingSpotRepository;

@Service
public class GarageService implements ApplicationRunner {

	private static final Logger log = LoggerFactory.getLogger(GarageService.class);

	private final GarageRepository sectorRepository;
	private final ParkingSpotRepository spotRepository;
	private final RestTemplate restTemplate;

	@Value("${simulator.base-url}")
	private String simulatorBaseUrl;

	public GarageService(GarageRepository sectorRepository, ParkingSpotRepository spotRepository,
			RestTemplate restTemplate) {
		this.sectorRepository = sectorRepository;
		this.spotRepository = spotRepository;
		this.restTemplate = restTemplate;
	}

	@Override
	public void run(ApplicationArguments args) {
		log.info("Buscando configuração da garagem em {}/garage ...", simulatorBaseUrl);

		GarageResponseDto response = restTemplate.getForObject(simulatorBaseUrl + "/garage", GarageResponseDto.class);

		if (response == null) {
			log.error("Resposta nula do simulador.");
			return;
		}

		saveSetores(response.garage());
		saveVagas(response.spots());

		log.info("Garagem inicializada: {} setores, {} vagas", sectorRepository.count(), spotRepository.count());
	}

	private void saveSetores(List<GarageDto> garagens) {
		garagens.forEach(g -> {
			Garage sector = sectorRepository.findBySector(g.sector()).map(existing -> updateSector(existing, g))
					.orElseGet(g::toEntity);
			sectorRepository.save(sector);
		});
	}

	private void saveVagas(List<ParkingSpotDto> spots) {
		spots.forEach(sp -> {
			ParkingSpot spot = spotRepository.findById(sp.id()).map(existing -> updateSpot(existing, sp))
					.orElseGet(sp::toEntity);
			spotRepository.save(spot);
		});
	}

	private Garage updateSector(Garage existing, GarageDto g) {
		existing.setBasePrice(g.basePrice());
		existing.setMaxCapacity(g.maxCapacity());
		existing.setOpenHour(g.openHour());
		existing.setCloseHour(g.closeHour());
		existing.setDurationLimitMinutes(g.durationLimitMinutes());
		existing.setOpen(true);
		return existing;
	}

	private ParkingSpot updateSpot(ParkingSpot existing, ParkingSpotDto sp) {
		existing.setSector(sp.sector());
		existing.setLat(sp.lat());
		existing.setLng(sp.lng());
		return existing;
	}

}
