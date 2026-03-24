package com.estapar.teste.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import com.estapar.teste.entity.WebhookEvent;
import com.estapar.teste.service.WebhookService;

@DisplayName("WebhookController — testes")
public class WebhookControllerTest {

	@Mock
	private WebhookService webhookService;

	@InjectMocks
	private WebhookController webhookController;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
	}

	private WebhookEvent buildEvent(String type, String plate, String entryTime, String exitTime, Double lat,
			Double lng) {
		WebhookEvent event = new WebhookEvent();
		event.setEventType(type);
		event.setLicensePlate(plate);
		event.setEntryTime(entryTime);
		event.setExitTime(exitTime);
		event.setLat(lat);
		event.setLng(lng);
		return event;
	}

	@Test
	@DisplayName("ENTRY: deve retornar 200 com payload válido com offset Z")
	void entry_shouldReturn200WithOffsetZ() {
		var event = buildEvent("ENTRY", "ZUL0001", "2025-01-01T12:00:00.000Z", null, null, null);
		ResponseEntity<Void> response = webhookController.handleWebhook(event);
		assertEquals(200, response.getStatusCode().value());
		verify(webhookService).process(event);
	}

	@Test
	@DisplayName("ENTRY: deve retornar 200 com data sem offset (formato do simulador)")
	void entry_shouldReturn200WithoutOffset() {
		var event = buildEvent("ENTRY", "ZUL0001", "2025-01-01T12:00:00", null, null, null);
		ResponseEntity<Void> response = webhookController.handleWebhook(event);
		assertEquals(200, response.getStatusCode().value());
		verify(webhookService).process(event);
	}

	@Test
	@DisplayName("ENTRY: deve propagar exceção quando service lança")
	void entry_shouldPropagateExceptionWhenServiceThrows() {
		var event = buildEvent("ENTRY", "ZUL0001", "2025-01-01T12:00:00.000Z", null, null, null);
		doThrow(new IllegalStateException("Estacionamento lotado")).when(webhookService).process(event);

		assertThrows(IllegalStateException.class, () -> webhookController.handleWebhook(event));
	}

	@Test
	@DisplayName("PARKED: deve retornar 200 com payload válido")
	void parked_shouldReturn200WithValidPayload() {
		var event = buildEvent("PARKED", "ZUL0001", null, null, -23.561684, -46.655981);
		ResponseEntity<Void> response = webhookController.handleWebhook(event);
		assertEquals(200, response.getStatusCode().value());
		verify(webhookService).process(event);
	}

	@Test
	@DisplayName("EXIT: deve retornar 200 com payload válido com offset Z")
	void exit_shouldReturn200WithOffsetZ() {
		var event = buildEvent("EXIT", "ZUL0001", null, "2025-01-01T14:00:00.000Z", null, null);
		ResponseEntity<Void> response = webhookController.handleWebhook(event);
		assertEquals(200, response.getStatusCode().value());
		verify(webhookService).process(event);
	}

	@Test
	@DisplayName("EXIT: deve retornar 200 com data sem offset (formato do simulador)")
	void exit_shouldReturn200WithoutOffset() {
		var event = buildEvent("EXIT", "ZUL0001", null, "2025-01-01T14:00:00", null, null);
		ResponseEntity<Void> response = webhookController.handleWebhook(event);
		assertEquals(200, response.getStatusCode().value());
		verify(webhookService).process(event);
	}

	@Test
	@DisplayName("PARKED: deve propagar exceção quando service lança")
	void parked_shouldPropagateExceptionWhenServiceThrows() {
		var event = buildEvent("PARKED", "ZUL0001", null, null, -99.0, -99.0);
		doThrow(new IllegalStateException("Vaga não encontrada")).when(webhookService).process(event);

		assertThrows(IllegalStateException.class, () -> webhookController.handleWebhook(event));
	}

	@Test
	@DisplayName("EXIT: deve propagar exceção quando service lança")
	void exit_shouldPropagateExceptionWhenServiceThrows() {
		var event = buildEvent("EXIT", "ZUL9999", null, "2025-01-01T14:00:00.000Z", null, null);
		doThrow(new IllegalStateException("Veículo não encontrado")).when(webhookService).process(event);

		assertThrows(IllegalStateException.class, () -> webhookController.handleWebhook(event));
	}

	@Test
	@DisplayName("Deve propagar exceção para evento de tipo desconhecido")
	void shouldPropagateExceptionForUnknownEventType() {
		var event = buildEvent("UNKNOWN", "ZUL0001", null, null, null, null);
		doThrow(new IllegalArgumentException("Tipo de evento desconhecido: UNKNOWN")).when(webhookService)
				.process(event);

		assertThrows(IllegalArgumentException.class, () -> webhookController.handleWebhook(event));
	}

}