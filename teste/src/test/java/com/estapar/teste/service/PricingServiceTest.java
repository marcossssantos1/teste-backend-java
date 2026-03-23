package com.estapar.teste.service;

import com.estapar.teste.repository.ParkingSpotRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@DisplayName("PricingService — testes unitários")
class PricingServiceTest {

    @Mock
    private ParkingSpotRepository spotRepository;

    @InjectMocks
    private PricingService pricingService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // ---------------------------------------------------------------
    // Testes de calculateCharge
    // ---------------------------------------------------------------

    @Test
    @DisplayName("Deve ser gratuito nos primeiros 30 minutos exatos")
    void shouldReturnFreeFor30MinutesExact() {
        LocalDateTime entry = LocalDateTime.of(2025, 1, 1, 12, 0);
        LocalDateTime exit  = LocalDateTime.of(2025, 1, 1, 12, 30);
        assertEquals(0.0, pricingService.calculateCharge(entry, exit, 10.0));
    }

    @Test
    @DisplayName("Deve ser gratuito com menos de 30 minutos")
    void shouldReturnFreeForLessThan30Minutes() {
        LocalDateTime entry = LocalDateTime.of(2025, 1, 1, 12, 0);
        LocalDateTime exit  = LocalDateTime.of(2025, 1, 1, 12, 15);
        assertEquals(0.0, pricingService.calculateCharge(entry, exit, 10.0));
    }

    @Test
    @DisplayName("Deve cobrar 1 hora ao completar 31 minutos")
    void shouldChargeOneHourAt31Minutes() {
        LocalDateTime entry = LocalDateTime.of(2025, 1, 1, 12, 0);
        LocalDateTime exit  = LocalDateTime.of(2025, 1, 1, 12, 31);
        assertEquals(10.0, pricingService.calculateCharge(entry, exit, 10.0));
    }

    @Test
    @DisplayName("Deve cobrar 1 hora para exatamente 60 minutos")
    void shouldChargeOneHourFor60Minutes() {
        LocalDateTime entry = LocalDateTime.of(2025, 1, 1, 12, 0);
        LocalDateTime exit  = LocalDateTime.of(2025, 1, 1, 13, 0);
        assertEquals(10.0, pricingService.calculateCharge(entry, exit, 10.0));
    }

    @Test
    @DisplayName("Deve cobrar 2 horas ao completar 61 minutos")
    void shouldChargeTwoHoursAt61Minutes() {
        LocalDateTime entry = LocalDateTime.of(2025, 1, 1, 12, 0);
        LocalDateTime exit  = LocalDateTime.of(2025, 1, 1, 13, 1);
        assertEquals(20.0, pricingService.calculateCharge(entry, exit, 10.0));
    }

    @Test
    @DisplayName("Deve arredondar horas para cima corretamente")
    void shouldRoundUpHoursCorrectly() {
        LocalDateTime entry = LocalDateTime.of(2025, 1, 1, 12, 0);
        LocalDateTime exit  = LocalDateTime.of(2025, 1, 1, 14, 59);
        // 179 minutos → ceil(179/60) = 3 horas
        assertEquals(30.0, pricingService.calculateCharge(entry, exit, 10.0));
    }

    @Test
    @DisplayName("Deve calcular corretamente com preço fracionado")
    void shouldCalculateWithFractionalPrice() {
        LocalDateTime entry = LocalDateTime.of(2025, 1, 1, 12, 0);
        LocalDateTime exit  = LocalDateTime.of(2025, 1, 1, 13, 30);
        // 90 minutos → ceil(90/60) = 2 horas × R$ 4.10 = R$ 8.20
        assertEquals(8.20, pricingService.calculateCharge(entry, exit, 4.10));
    }

    // ---------------------------------------------------------------
    // Testes de getDynamicPriceFactor
    // ---------------------------------------------------------------

    @Test
    @DisplayName("Deve retornar fator 1.0 quando setor não tem vagas cadastradas")
    void shouldReturnNeutralFactorWhenNoSpots() {
        when(spotRepository.countBySector("A")).thenReturn(0L);
        assertEquals(1.0, pricingService.getDynamicPriceFactor("A"));
    }

    @Test
    @DisplayName("Deve aplicar desconto de 10% com lotação abaixo de 25%")
    void shouldApplyDiscountBelow25Percent() {
        when(spotRepository.countBySector("A")).thenReturn(100L);
        when(spotRepository.countBySectorAndOccupied("A", true)).thenReturn(24L);
        assertEquals(0.90, pricingService.getDynamicPriceFactor("A"));
    }

    @Test
    @DisplayName("Deve aplicar fator neutro com lotação entre 25% e 49%")
    void shouldApplyNeutralFactorBetween25And50Percent() {
        when(spotRepository.countBySector("A")).thenReturn(100L);
        when(spotRepository.countBySectorAndOccupied("A", true)).thenReturn(49L);
        assertEquals(1.00, pricingService.getDynamicPriceFactor("A"));
    }

    @Test
    @DisplayName("Deve aplicar aumento de 10% com lotação entre 50% e 74%")
    void shouldApplySurchargeBetween50And75Percent() {
        when(spotRepository.countBySector("A")).thenReturn(100L);
        when(spotRepository.countBySectorAndOccupied("A", true)).thenReturn(74L);
        assertEquals(1.10, pricingService.getDynamicPriceFactor("A"));
    }

    @Test
    @DisplayName("Deve aplicar aumento de 25% com lotação igual ou acima de 75%")
    void shouldApplyHighSurchargeAt75PercentOrAbove() {
        when(spotRepository.countBySector("A")).thenReturn(100L);
        when(spotRepository.countBySectorAndOccupied("A", true)).thenReturn(75L);
        assertEquals(1.25, pricingService.getDynamicPriceFactor("A"));
    }

    @Test
    @DisplayName("Deve aplicar aumento de 25% com lotação a 100%")
    void shouldApplyHighSurchargeAtFullCapacity() {
        when(spotRepository.countBySector("A")).thenReturn(10L);
        when(spotRepository.countBySectorAndOccupied("A", true)).thenReturn(10L);
        assertEquals(1.25, pricingService.getDynamicPriceFactor("A"));
    }
}