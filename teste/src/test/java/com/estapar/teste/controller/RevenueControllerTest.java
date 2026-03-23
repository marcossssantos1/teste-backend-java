package com.estapar.teste.controller;


import com.estapar.teste.entity.RevenueRequest;
import com.estapar.teste.entity.RevenueResponse;
import com.estapar.teste.repository.RevenueEntryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@DisplayName("RevenueController — testes")
class RevenueControllerTest {

	@Mock
	private RevenueEntryRepository revenueEntryRepository;

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
		when(revenueEntryRepository.sumAmountBySectorAndDate("A", LocalDate.of(2025, 1, 1)))
				.thenReturn(121.50);

		ResponseEntity<RevenueResponse> response = revenueController.getRevenue(buildRequest("2025-01-01", "A"));

		assertEquals(200, response.getStatusCode().value());
		assertNotNull(response.getBody());
		assertEquals("BRL", response.getBody().getCurrency());
		assertNotNull(response.getBody().getTimestamp());
	}

	@Test
	@DisplayName("Deve retornar 0.00 quando não há receita para o setor e data")
	void shouldReturnZeroWhenNoRevenue() {
		when(revenueEntryRepository.sumAmountBySectorAndDate("B", LocalDate.of(2025, 1, 1)))
				.thenReturn(null);

		ResponseEntity<RevenueResponse> response = revenueController.getRevenue(buildRequest("2025-01-01", "B"));

		assertEquals(200, response.getStatusCode().value());
		assertNotNull(response.getBody());
		assertEquals(0, response.getBody().getAmount().intValue());
		assertEquals("BRL", response.getBody().getCurrency());
	}

	@Test
	@DisplayName("Deve retornar receita com duas casas decimais")
	void shouldReturnRevenueWithTwoDecimalPlaces() {
		when(revenueEntryRepository.sumAmountBySectorAndDate("B", LocalDate.of(2025, 1, 1)))
				.thenReturn(49.20);

		ResponseEntity<RevenueResponse> response = revenueController.getRevenue(buildRequest("2025-01-01", "B"));

		assertEquals(200, response.getStatusCode().value());
		assertNotNull(response.getBody());
		assertEquals(2, response.getBody().getAmount().scale());
	}

	@Test
	@DisplayName("Deve lançar exceção quando data está em formato inválido")
	void shouldThrowWhenDateFormatIsInvalid() {
		assertThrows(Exception.class, () ->
				revenueController.getRevenue(buildRequest("01/01/2025", "A"))
		);
	}
}