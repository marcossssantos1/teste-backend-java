package com.estapar.teste.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.estapar.teste.entity.WebhookEvent;
import com.estapar.teste.service.WebhookService;

@RestController
@RequestMapping("/webhook")
public class WebhookController {

    private static final Logger log = LoggerFactory.getLogger(WebhookController.class);

    private final WebhookService webhookService;

    public WebhookController(WebhookService webhookService) {
        this.webhookService = webhookService;
    }

    @PostMapping
    public ResponseEntity<Void> handleWebhook(@RequestBody WebhookEvent event) {
        log.info("Webhook recebido: tipo={} placa={}", event.getEventType(), event.getLicensePlate());
        webhookService.process(event);
        return ResponseEntity.ok().build();
    }
}