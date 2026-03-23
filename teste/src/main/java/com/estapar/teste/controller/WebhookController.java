package com.estapar.teste.controller;

import com.estapar.teste.entity.WebhookEvent;
import com.estapar.teste.service.ParkingService;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;

@RestController
@RequestMapping("/webhook")
public class WebhookController {

    private static final Logger log = LoggerFactory.getLogger(WebhookController.class);

    private final ParkingService parkingService;

    public WebhookController(ParkingService parkingService) {
        this.parkingService = parkingService;
    }

    @PostMapping
    public ResponseEntity<Void> handleWebhook(@RequestBody WebhookEvent event) {
        log.info("Webhook recebido: tipo={} placa={}", event.getEventType(), event.getLicensePlate());

        try {
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
                default -> log.warn("Tipo de evento desconhecido: {}", event.getEventType());
            }
        } catch (Exception e) {
            log.error("Erro ao processar webhook: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }

        return ResponseEntity.ok().build();
    }
}