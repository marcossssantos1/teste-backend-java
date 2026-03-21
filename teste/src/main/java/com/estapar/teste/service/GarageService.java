package com.estapar.teste.service;

import com.estapar.teste.repository.GarageRepository;
import com.estapar.teste.repository.ParkingSpotRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
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

}
