package com.estapar.teste.controller;


import com.estapar.teste.entity.RevenueRequest;
import com.estapar.teste.entity.RevenueResponse;
import com.estapar.teste.repository.RevenueEntryRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/revenue")
public class RevenueController {

    private RevenueEntryRepository revenueEntryRepository;

    @GetMapping
    public ResponseEntity<RevenueResponse> getRevenue(@RequestBody RevenueRequest request) {
        LocalDate date = LocalDate.parse(request.getDate());
        Double amount = revenueEntryRepository.sumAmountBySectorAndDate(request.getSector(), date);

        RevenueResponse response = new RevenueResponse();
        response.setAmount(amount != null ? amount : 0.0);
        response.setCurrency("BRL");
        response.setTimestamp(LocalDateTime.now().toString());

        return ResponseEntity.ok(response);
    }


}
