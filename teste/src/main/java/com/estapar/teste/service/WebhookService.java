package com.estapar.teste.service;

import com.estapar.teste.entity.WebhookEvent;
import org.springframework.stereotype.Service;

@Service
public class WebhookService {

    private final ParkingService parkingService;

    public WebhookService(ParkingService parkingService) {
        this.parkingService = parkingService;
    }

    public void process(WebhookEvent event) {
        switch (event.getEventType()) {
            case "ENTRY" -> parkingService.handleEntry(
                    event.getLicensePlate(),
                    parkingService.parseDateTime(event.getEntryTime())
            );
            case "PARKED" -> parkingService.handleParked(
                    event.getLicensePlate(),
                    event.getLat(),
                    event.getLng()
            );
            case "EXIT" -> parkingService.handleExit(
                    event.getLicensePlate(),
                    parkingService.parseDateTime(event.getExitTime())
            );
            default -> throw new IllegalArgumentException(
                    "Tipo de evento desconhecido: " + event.getEventType()
            );
        }
    }
}
