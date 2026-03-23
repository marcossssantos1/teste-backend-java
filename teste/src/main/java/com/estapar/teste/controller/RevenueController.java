package com.estapar.teste.controller;


import com.estapar.teste.entity.RevenueRequest;
import com.estapar.teste.entity.RevenueResponse;
import com.estapar.teste.repository.RevenueEntryRepository;
import com.estapar.teste.service.RevenueService;
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

    private RevenueService service;

    @GetMapping
    public ResponseEntity<RevenueResponse> getRevenue(@RequestBody RevenueRequest request) {
        return ResponseEntity.ok(service.getRevenue(request.getSector(), request.getDate()));
    }


}
