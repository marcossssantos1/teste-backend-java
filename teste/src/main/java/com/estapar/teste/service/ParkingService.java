package com.estapar.teste.service;

import com.estapar.teste.entity.Garage;
import com.estapar.teste.entity.ParkingSpot;
import com.estapar.teste.entity.RevenueEntry;
import com.estapar.teste.entity.Vehicle;
import com.estapar.teste.enums.RecordStatus;
import com.estapar.teste.repository.GarageRepository;
import com.estapar.teste.repository.ParkingSpotRepository;
import com.estapar.teste.repository.RevenueEntryRepository;
import com.estapar.teste.repository.VehicleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.TemporalAccessor;
import java.util.List;



@Service
public class ParkingService {

    private static final Logger log = LoggerFactory.getLogger(ParkingService.class);

    private static final DateTimeFormatter FLEXIBLE_FORMATTER = new DateTimeFormatterBuilder()
            .append(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            .optionalStart().appendOffsetId().optionalEnd()
            .toFormatter();

    private final GarageRepository sectorRepository;
    private final ParkingSpotRepository spotRepository;
    private final VehicleRepository vehicleRepository;
    private final RevenueEntryRepository revenueEntryRepository;
    private final PricingService pricingService;

    public ParkingService(GarageRepository sectorRepository,
                          ParkingSpotRepository spotRepository,
                          VehicleRepository vehicleRepository,
                          RevenueEntryRepository revenueEntryRepository,
                          PricingService pricingService) {
        this.sectorRepository = sectorRepository;
        this.spotRepository = spotRepository;
        this.vehicleRepository = vehicleRepository;
        this.revenueEntryRepository = revenueEntryRepository;
        this.pricingService = pricingService;
    }

    @Transactional
    public void handleEntry(String licensePlate, LocalDateTime entryTime) {
        List<Garage> sectors = sectorRepository.findAll();

        Garage availableSector = sectors.stream()
                .filter(Garage::getOpen)
                .filter(s -> spotRepository.countBySectorAndOccupied(s.getSector(), false) > 0)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Estacionamento lotado. Nenhuma vaga disponível."));

        String sector = availableSector.getSector();

        double priceFactor = pricingService.getDynamicPriceFactor(sector);
        double effectivePrice = availableSector.getBasePrice() * priceFactor;

        Vehicle record = new Vehicle();
        record.setLicensePlate(licensePlate);
        record.setEntryTime(entryTime);
        record.setSector(sector);
        record.setPriceApplied(effectivePrice);
        record.setStatus(RecordStatus.ENTERED);
        vehicleRepository.save(record);

        log.info("ENTRY: {} no setor {} com preço efetivo R$ {}", licensePlate, sector, effectivePrice);
    }

    @Transactional
    public void handleParked(String licensePlate, Double lat, Double lng) {
        Vehicle record = vehicleRepository
                .findTopByLicensePlateAndStatusNotOrderByEntryTimeDesc(
                        licensePlate, RecordStatus.EXITED)
                .orElseThrow(() -> new IllegalStateException("Registro não encontrado para: " + licensePlate));

        ParkingSpot spot = spotRepository.findByLatAndLng(lat, lng)
                .orElseThrow(() -> new IllegalStateException("Vaga não encontrada para lat=" + lat + " lng=" + lng));

        spot.setOccupied(true);
        spotRepository.save(spot);

        record.setSpotId(spot.getId());
        record.setStatus(RecordStatus.PARKED);
        vehicleRepository.save(record);

        checkAndUpdateSectorStatus(spot.getSector());

        log.info("PARKED: {} na vaga {}", licensePlate, spot.getId());
    }

    @Transactional
    public void handleExit(String licensePlate, LocalDateTime exitTime) {
        Vehicle record = vehicleRepository
            .findTopByLicensePlateAndStatusNotOrderByEntryTimeDesc(
                licensePlate, RecordStatus.EXITED)
            .orElseThrow(() -> new IllegalStateException("Registro não encontrado para: " + licensePlate));

        if (record.getSpotId() != null) {
            ParkingSpot spot = spotRepository.findById(record.getSpotId())
                .orElseThrow(() -> new IllegalStateException("Vaga não encontrada: " + record.getSpotId()));
            spot.setOccupied(false);
            spotRepository.save(spot);
        }

        // Sempre reabre o setor na saída, independente do spotId
        sectorRepository.findBySector(record.getSector()).ifPresent(s -> {
            s.setOpen(true);
            sectorRepository.save(s);
        });

        double charge = pricingService.calculateCharge(record.getEntryTime(), exitTime, record.getPriceApplied());

        record.setExitTime(exitTime);
        record.setAmountCharged(charge);
        record.setStatus(RecordStatus.EXITED);
        vehicleRepository.save(record);

        if (charge > 0) {
            RevenueEntry revenue = new RevenueEntry();
            revenue.setSector(record.getSector());
            revenue.setDate(exitTime.toLocalDate());
            revenue.setAmount(charge);
            revenueEntryRepository.save(revenue);
        }

        log.info("EXIT: {} cobrado R$ {}", licensePlate, charge);
    }
    private void checkAndUpdateSectorStatus(String sector) {
        long total = spotRepository.countBySector(sector);
        long occupied = spotRepository.countBySectorAndOccupied(sector, true);

        if (total > 0 && occupied >= total) {
            sectorRepository.findBySector(sector).ifPresent(s -> {
                s.setOpen(false);
                sectorRepository.save(s);
                log.warn("Setor {} FECHADO — lotação 100%", sector);
            });
        }
    }

    @Transactional
    public void saveGarageData(List<Garage> sectors, List<ParkingSpot> spots) {
        sectorRepository.saveAll(sectors);
        spotRepository.saveAll(spots);
        log.info("Garagem inicializada: {} setores, {} vagas", sectors.size(), spots.size());
    }


    public LocalDateTime parseDateTime(String dateTimeStr) {
        if (dateTimeStr == null) return null;
        TemporalAccessor parsed = FLEXIBLE_FORMATTER.parseBest(
                dateTimeStr,
                OffsetDateTime::from,
                LocalDateTime::from
        );
        if (parsed instanceof OffsetDateTime odt) {
            return odt.toLocalDateTime();
        }
        return (LocalDateTime) parsed;
    }
}