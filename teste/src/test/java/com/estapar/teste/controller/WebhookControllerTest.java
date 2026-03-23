package com.estapar.teste.controller;



import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import com.estapar.teste.entity.WebhookEvent;
import com.estapar.teste.service.ParkingService;

@DisplayName("WebhookController — testes")
class WebhookControllerTest {

    @Mock
    private ParkingService parkingService;

    @InjectMocks
    private WebhookController webhookController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    private WebhookEvent buildEvent(String type, String plate,
                                                      String entryTime, String exitTime,
                                                      Double lat, Double lng) {
        WebhookEvent event = new WebhookEvent();
        event.setEventType(type);
        event.setLicensePlate(plate);
        event.setEntryTime(entryTime);
        event.setExitTime(exitTime);
        event.setLat(lat);
        event.setLng(lng);
        return event;
    }

    // ---------------------------------------------------------------
    // ENTRY
    // ---------------------------------------------------------------

    @Test
    @DisplayName("ENTRY: deve retornar 200 com payload válido com offset Z")
    void entry_shouldReturn200WithOffsetZ() {
        var event = buildEvent("ENTRY", "ZUL0001", "2025-01-01T12:00:00.000Z", null, null, null);
        ResponseEntity<Void> response = webhookController.handleWebhook(event);
        assertEquals(200, response.getStatusCode().value());
        verify(parkingService).handleEntry(eq("ZUL0001"), any());
    }

    @Test
    @DisplayName("ENTRY: deve retornar 200 com data sem offset (formato do simulador)")
    void entry_shouldReturn200WithoutOffset() {
        var event = buildEvent("ENTRY", "ZUL0001", "2025-01-01T12:00:00", null, null, null);
        ResponseEntity<Void> response = webhookController.handleWebhook(event);
        assertEquals(200, response.getStatusCode().value());
        verify(parkingService).handleEntry(eq("ZUL0001"), any());
    }

    @Test
    @DisplayName("ENTRY: deve retornar 500 quando service lança exceção")
    void entry_shouldReturn500WhenServiceThrows() {
        doThrow(new IllegalStateException("Estacionamento lotado"))
                .when(parkingService).handleEntry(any(), any());

        var event = buildEvent("ENTRY", "ZUL0001", "2025-01-01T12:00:00.000Z", null, null, null);
        ResponseEntity<Void> response = webhookController.handleWebhook(event);
        assertEquals(500, response.getStatusCode().value());
    }

    // ---------------------------------------------------------------
    // PARKED
    // ---------------------------------------------------------------

    @Test
    @DisplayName("PARKED: deve retornar 200 com payload válido")
    void parked_shouldReturn200WithValidPayload() {
        var event = buildEvent("PARKED", "ZUL0001", null, null, -23.561684, -46.655981);
        ResponseEntity<Void> response = webhookController.handleWebhook(event);
        assertEquals(200, response.getStatusCode().value());
        verify(parkingService).handleParked("ZUL0001", -23.561684, -46.655981);
    }

    @Test
    @DisplayName("PARKED: deve retornar 500 quando service lança exceção")
    void parked_shouldReturn500WhenServiceThrows() {
        doThrow(new IllegalStateException("Vaga não encontrada"))
                .when(parkingService).handleParked(any(), any(), any());

        var event = buildEvent("PARKED", "ZUL0001", null, null, -99.0, -99.0);
        ResponseEntity<Void> response = webhookController.handleWebhook(event);
        assertEquals(500, response.getStatusCode().value());
    }

    // ---------------------------------------------------------------
    // EXIT
    // ---------------------------------------------------------------

    @Test
    @DisplayName("EXIT: deve retornar 200 com payload válido com offset Z")
    void exit_shouldReturn200WithOffsetZ() {
        var event = buildEvent("EXIT", "ZUL0001", null, "2025-01-01T14:00:00.000Z", null, null);
        ResponseEntity<Void> response = webhookController.handleWebhook(event);
        assertEquals(200, response.getStatusCode().value());
        verify(parkingService).handleExit(eq("ZUL0001"), any());
    }

    @Test
    @DisplayName("EXIT: deve retornar 200 com data sem offset (formato do simulador)")
    void exit_shouldReturn200WithoutOffset() {
        var event = buildEvent("EXIT", "ZUL0001", null, "2025-01-01T14:00:00", null, null);
        ResponseEntity<Void> response = webhookController.handleWebhook(event);
        assertEquals(200, response.getStatusCode().value());
        verify(parkingService).handleExit(eq("ZUL0001"), any());
    }

    @Test
    @DisplayName("EXIT: deve retornar 500 quando service lança exceção")
    void exit_shouldReturn500WhenServiceThrows() {
        doThrow(new IllegalStateException("Veículo não encontrado"))
                .when(parkingService).handleExit(any(), any());

        var event = buildEvent("EXIT", "ZUL9999", null, "2025-01-01T14:00:00.000Z", null, null);
        ResponseEntity<Void> response = webhookController.handleWebhook(event);
        assertEquals(500, response.getStatusCode().value());
    }

    // ---------------------------------------------------------------
    // Evento desconhecido
    // ---------------------------------------------------------------

    @Test
    @DisplayName("Deve retornar 200 e ignorar evento de tipo desconhecido")
    void shouldReturn200AndIgnoreUnknownEventType() {
        var event = buildEvent("UNKNOWN", "ZUL0001", null, null, null, null);
        ResponseEntity<Void> response = webhookController.handleWebhook(event);
        assertEquals(200, response.getStatusCode().value());
        verifyNoInteractions(parkingService);
    }
}