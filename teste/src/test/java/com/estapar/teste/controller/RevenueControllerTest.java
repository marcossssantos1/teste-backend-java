package com.estapar.teste.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import com.estapar.teste.entity.RevenueRequest;
import com.estapar.teste.entity.RevenueResponse;
import com.estapar.teste.service.RevenueService;

@DisplayName("RevenueController — testes")
public class RevenueControllerTest {

	@Mock
	private RevenueService revenueService;

	@InjectMocks
	private RevenueController revenueController;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
	}

	private RevenueRequest buildRequest(String date, String sector) {
		RevenueRequest request = new RevenueRequest();
		request.setDate(date);
		request.setSector(sector);
		return request;
	}

	@Test
	@DisplayName("Deve retornar receita correta para setor e data")
	void shouldReturnRevenueForSectorAndDate() {
		RevenueResponse mockResponse = new RevenueResponse();
		mockResponse.setAmount(121.50);
		mockResponse.setCurrency("BRL");
		mockResponse.setTimestamp(LocalDateTime.now().toString());

		when(revenueService.getRevenue("A", "2025-01-01")).thenReturn(mockResponse);

		ResponseEntity<RevenueResponse> response = revenueController.getRevenue(buildRequest("2025-01-01", "A"));

		assertEquals(200, response.getStatusCode().value());
		assertNotNull(response.getBody());
		assertEquals("BRL", response.getBody().getCurrency());
		assertNotNull(response.getBody().getTimestamp());
	}

	@Test
	@DisplayName("Deve retornar 0.00 quando não há receita")
	void shouldReturnZeroWhenNoRevenue() {
		RevenueResponse mockResponse = new RevenueResponse();
		mockResponse.setAmount(0.0);
		mockResponse.setCurrency("BRL");
		mockResponse.setTimestamp(LocalDateTime.now().toString());

		when(revenueService.getRevenue("B", "2025-01-01")).thenReturn(mockResponse);

		ResponseEntity<RevenueResponse> response = revenueController.getRevenue(buildRequest("2025-01-01", "B"));

		assertEquals(200, response.getStatusCode().value());
		assertNotNull(response.getBody());
		assertEquals(0, response.getBody().getAmount().intValue());
		assertEquals("BRL", response.getBody().getCurrency());
	}

	@Test
	@DisplayName("Deve retornar receita com duas casas decimais")
	void shouldReturnRevenueWithTwoDecimalPlaces() {
		RevenueResponse mockResponse = new RevenueResponse();
		mockResponse.setAmount(49.20);
		mockResponse.setCurrency("BRL");
		mockResponse.setTimestamp(LocalDateTime.now().toString());

		when(revenueService.getRevenue("B", "2025-01-01")).thenReturn(mockResponse);

		ResponseEntity<RevenueResponse> response = revenueController.getRevenue(buildRequest("2025-01-01", "B"));

		assertEquals(200, response.getStatusCode().value());
		assertNotNull(response.getBody());
		assertEquals(2, response.getBody().getAmount().scale());
	}

	@Test
	@DisplayName("Deve lançar exceção quando data está em formato inválido")
	void shouldThrowWhenDateFormatIsInvalid() {
		when(revenueService.getRevenue("A", "01/01/2025"))
				.thenThrow(new IllegalArgumentException("Formato de data inválido."));

		assertThrows(Exception.class, () -> revenueController.getRevenue(buildRequest("01/01/2025", "A")));
	}
}