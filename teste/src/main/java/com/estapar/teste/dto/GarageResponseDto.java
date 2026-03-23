package com.estapar.teste.dto;

import com.estapar.teste.entity.ParkingSpot;

import java.util.List;

public record GarageResponseDto(

        List<GarageDto> garage,
        List<ParkingSpotDto>spots
) {
}
