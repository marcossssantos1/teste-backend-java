package com.estapar.teste.service;

import com.estapar.teste.dto.GarageDto;
import com.estapar.teste.dto.GarageResponseDto;
import com.estapar.teste.dto.ParkingSpotDto;
import com.estapar.teste.entity.Garage;
import com.estapar.teste.repository.GarageRepository;
import com.estapar.teste.repository.ParkingSpotRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class GarageService {

    private static final Logger log = LoggerFactory.getLogger(GarageService.class);

    private final GarageRepository sectorRepository;
    private final ParkingSpotRepository spotRepository;
    private final RestTemplate restTemplate;

    @Value("${simulator.base-url}")
    private String simulatorBaseUrl;

    public GarageService(GarageRepository sectorRepository,
                         ParkingSpotRepository spotRepository,
                         RestTemplate restTemplate) {
        this.sectorRepository = sectorRepository;
        this.spotRepository = spotRepository;
        this.restTemplate = restTemplate;
    }

    @Override
    public void run(ApplicationArguments args) {
        log.info("Buscando configuração da garagem em {}/garage ...", simulatorBaseUrl);
        try {
            GarageResponseDto response = restTemplate.getForObject(
                    simulatorBaseUrl + "/garage", GarageResponseDto.class);

            if (response == null) {
                log.error("Resposta nula do simulador.");
                return;
            }

            for (GarageDto g : response.garage()) {
                Garage sector = sectorRepository.findBySector(g.sector())
                        .map(existing -> {
                            existing.setBasePrice(g.basePrice());
                            existing.setMaxCapacity(g.maxCapacity());
                            existing.setOpenHour(g.openHour());
                            existing.setCloseHour(g.closeHour());
                            existing.setDurationLimitMinutes(g.durationLimitMinutes());
                            existing.setOpen(true);
                            return existing;
                        })
                        .orElseGet(g::toEntity);
                sectorRepository.save(sector);
            }

            for (ParkingSpotDto sp : response.spots()) {
                ParkingSpotDto spot = spotRepository.findById(sp.id())
                        .map(existing -> {
                            existing.setSector(sp.sector());
                            existing.setLat(sp.lat());
                            existing.setLng(sp.lng());
                            return existing;
                        })
                        .orElseGet(sp::toEntity);
                spotRepository.save(spot);
            }

            log.info("Garagem inicializada: {} setores, {} vagas",
                    sectorRepository.count(), spotRepository.count());

        } catch (Exception e) {
            log.error("Erro ao buscar dados da garagem: {}", e.getMessage());
        }
    }

}
