package com.estapar.teste.service;

import com.estapar.teste.entity.RevenueResponse;
import com.estapar.teste.repository.RevenueEntryRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
public class RevenueService {

    private final RevenueEntryRepository revenueEntryRepository;

    public RevenueService(RevenueEntryRepository revenueEntryRepository) {
        this.revenueEntryRepository = revenueEntryRepository;
    }

    public RevenueResponse getRevenue(String sector, String date) {
        LocalDate localDate = LocalDate.parse(date);
        Double amount = revenueEntryRepository.sumAmountBySectorAndDate(sector, localDate);

        RevenueResponse response = new RevenueResponse();
        response.setAmount(amount != null ? amount : 0.0);
        response.setCurrency("BRL");
        response.setTimestamp(LocalDateTime.now().toString());
        return response;
    }
}
