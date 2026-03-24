package com.estapar.teste.service;

import com.estapar.teste.entity.Garage;
import com.estapar.teste.entity.ParkingSpot;
import com.estapar.teste.entity.Vehicle;
import com.estapar.teste.enums.RecordStatus;
import com.estapar.teste.repository.GarageRepository;
import com.estapar.teste.repository.ParkingSpotRepository;
import com.estapar.teste.repository.RevenueEntryRepository;
import com.estapar.teste.repository.VehicleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("ParkingService — testes unitários")
class ParkingServiceTest {

	@Mock
	private GarageRepository sectorRepository;
	@Mock
	private ParkingSpotRepository spotRepository;
	@Mock
	private VehicleRepository vehicleRecordRepository;
	@Mock
	private RevenueEntryRepository revenueEntryRepository;
	@Mock
	private PricingService pricingService;

	@InjectMocks
	private ParkingService parkingService;

	private Garage sectorA;
	private ParkingSpot spot1;
	private Vehicle activeRecord;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);

		sectorA = new Garage();
		sectorA.setSector("A");
		sectorA.setBasePrice(10.0);
		sectorA.setMaxCapacity(10);
		sectorA.setOpen(true);

		spot1 = new ParkingSpot();
		spot1.setId(1L);
		spot1.setSector("A");
		spot1.setLat(-23.561684);
		spot1.setLng(-46.655981);
		spot1.setOccupied(false);

		activeRecord = new Vehicle();
		activeRecord.setId(1L);
		activeRecord.setLicensePlate("ZUL0001");
		activeRecord.setEntryTime(LocalDateTime.of(2025, 1, 1, 12, 0));
		activeRecord.setSector("A");
		activeRecord.setPriceApplied(10.0);
		activeRecord.setStatus(RecordStatus.ENTERED);
	}

	@Test
	@DisplayName("ENTRY: deve criar registro com preço dinâmico aplicado")
	void handleEntry_shouldCreateRecordWithDynamicPrice() {
		when(sectorRepository.findAll()).thenReturn(List.of(sectorA));
		when(spotRepository.countBySectorAndOccupied("A", false)).thenReturn(5L);
		when(pricingService.getDynamicPriceFactor("A")).thenReturn(1.10);
		when(vehicleRecordRepository.save(any())).thenAnswer(i -> i.getArgument(0));

		parkingService.handleEntry("ZUL0001", LocalDateTime.of(2025, 1, 1, 12, 0));

		verify(vehicleRecordRepository).save(argThat(r -> r.getLicensePlate().equals("ZUL0001")
				&& r.getSector().equals("A") && r.getPriceApplied() == 11.0 && r.getStatus() == RecordStatus.ENTERED));
	}

	@Test
	@DisplayName("ENTRY: deve lançar exceção quando estacionamento está lotado")
	void handleEntry_shouldThrowWhenFull() {
		when(sectorRepository.findAll()).thenReturn(List.of(sectorA));
		when(spotRepository.countBySectorAndOccupied("A", false)).thenReturn(0L);

		assertThrows(IllegalStateException.class, () -> parkingService.handleEntry("ZUL0001", LocalDateTime.now()));

		verify(vehicleRecordRepository, never()).save(any());
	}

	@Test
	@DisplayName("ENTRY: deve lançar exceção quando setor está fechado")
	void handleEntry_shouldThrowWhenSectorClosed() {
		sectorA.setOpen(false);
		when(sectorRepository.findAll()).thenReturn(List.of(sectorA));

		assertThrows(IllegalStateException.class, () -> parkingService.handleEntry("ZUL0001", LocalDateTime.now()));
	}

	@Test
	@DisplayName("PARKED: deve marcar vaga como ocupada e atualizar registro")
	void handleParked_shouldOccupySpotAndUpdateRecord() {
		when(vehicleRecordRepository.findTopByLicensePlateAndStatusNotOrderByEntryTimeDesc("ZUL0001",
				RecordStatus.EXITED)).thenReturn(Optional.of(activeRecord));
		when(spotRepository.findByLatAndLng(-23.561684, -46.655981)).thenReturn(Optional.of(spot1));
		when(spotRepository.countBySector("A")).thenReturn(10L);
		when(spotRepository.countBySectorAndOccupied("A", true)).thenReturn(5L);

		parkingService.handleParked("ZUL0001", -23.561684, -46.655981);

		assertTrue(spot1.getOccupied());
		verify(spotRepository).save(spot1);
		verify(vehicleRecordRepository).save(argThat(r -> r.getSpotId() == 1L && r.getStatus() == RecordStatus.PARKED));
	}

	@Test
	@DisplayName("PARKED: deve fechar setor quando lotação atingir 100%")
	void handleParked_shouldCloseSectorWhenFull() {
		when(vehicleRecordRepository.findTopByLicensePlateAndStatusNotOrderByEntryTimeDesc("ZUL0001",
				RecordStatus.EXITED)).thenReturn(Optional.of(activeRecord));
		when(spotRepository.findByLatAndLng(anyDouble(), anyDouble())).thenReturn(Optional.of(spot1));
		when(spotRepository.countBySector("A")).thenReturn(10L);
		when(spotRepository.countBySectorAndOccupied("A", true)).thenReturn(10L);
		when(sectorRepository.findBySector("A")).thenReturn(Optional.of(sectorA));

		parkingService.handleParked("ZUL0001", -23.561684, -46.655981);

		verify(sectorRepository).save(argThat(s -> !s.getOpen()));
	}

	@Test
	@DisplayName("PARKED: deve lançar exceção quando vaga GPS não encontrada")
	void handleParked_shouldThrowWhenSpotNotFound() {
		when(vehicleRecordRepository.findTopByLicensePlateAndStatusNotOrderByEntryTimeDesc("ZUL0001",
				RecordStatus.EXITED)).thenReturn(Optional.of(activeRecord));
		when(spotRepository.findByLatAndLng(anyDouble(), anyDouble())).thenReturn(Optional.empty());

		assertThrows(IllegalStateException.class, () -> parkingService.handleParked("ZUL0001", -99.0, -99.0));
	}

	@Test
	@DisplayName("EXIT: deve liberar vaga, cobrar valor e salvar receita")
	void handleExit_shouldFreeSpotAndSaveRevenue() {
		activeRecord.setSpotId(1L);
		activeRecord.setStatus(RecordStatus.PARKED);

		LocalDateTime exitTime = LocalDateTime.of(2025, 1, 1, 14, 0);

		when(vehicleRecordRepository.findTopByLicensePlateAndStatusNotOrderByEntryTimeDesc("ZUL0001",
				RecordStatus.EXITED)).thenReturn(Optional.of(activeRecord));
		when(spotRepository.findById(1L)).thenReturn(Optional.of(spot1));
		when(sectorRepository.findBySector("A")).thenReturn(Optional.of(sectorA));
		when(pricingService.calculateCharge(any(), any(), anyDouble())).thenReturn(20.0);

		parkingService.handleExit("ZUL0001", exitTime);

		assertFalse(spot1.getOccupied());
		verify(revenueEntryRepository).save(argThat(r -> r.getSector().equals("A") && r.getAmount() == 20.0));
		verify(vehicleRecordRepository)
				.save(argThat(r -> r.getStatus() == RecordStatus.EXITED && r.getAmountCharged() == 20.0));
	}

	@Test
	@DisplayName("EXIT: não deve salvar receita quando valor é zero (primeiros 30 min)")
	void handleExit_shouldNotSaveRevenueWhenFree() {
		activeRecord.setSpotId(1L);
		LocalDateTime exitTime = LocalDateTime.of(2025, 1, 1, 12, 20);

		when(vehicleRecordRepository.findTopByLicensePlateAndStatusNotOrderByEntryTimeDesc("ZUL0001",
				RecordStatus.EXITED)).thenReturn(Optional.of(activeRecord));
		when(spotRepository.findById(1L)).thenReturn(Optional.of(spot1));
		when(sectorRepository.findBySector("A")).thenReturn(Optional.of(sectorA));
		when(pricingService.calculateCharge(any(), any(), anyDouble())).thenReturn(0.0);

		parkingService.handleExit("ZUL0001", exitTime);

		verify(revenueEntryRepository, never()).save(any());
	}

	@Test
	@DisplayName("EXIT: deve reabrir setor após saída de veículo")
	void handleExit_shouldReopenSectorAfterExit() {
		sectorA.setOpen(false);
		activeRecord.setSpotId(1L);
		LocalDateTime exitTime = LocalDateTime.of(2025, 1, 1, 14, 0);

		when(vehicleRecordRepository.findTopByLicensePlateAndStatusNotOrderByEntryTimeDesc("ZUL0001",
				RecordStatus.EXITED)).thenReturn(Optional.of(activeRecord));
		when(spotRepository.findById(1L)).thenReturn(Optional.of(spot1));
		when(sectorRepository.findBySector("A")).thenReturn(Optional.of(sectorA));
		when(pricingService.calculateCharge(any(), any(), anyDouble())).thenReturn(10.0);

		parkingService.handleExit("ZUL0001", exitTime);

		verify(sectorRepository).save(argThat(Garage::getOpen));
	}

	@Test
	@DisplayName("EXIT: deve lançar exceção quando veículo não encontrado")
	void handleExit_shouldThrowWhenRecordNotFound() {
		when(vehicleRecordRepository.findTopByLicensePlateAndStatusNotOrderByEntryTimeDesc("ZUL9999",
				RecordStatus.EXITED)).thenReturn(Optional.empty());

		assertThrows(IllegalStateException.class, () -> parkingService.handleExit("ZUL9999", LocalDateTime.now()));
	}
}