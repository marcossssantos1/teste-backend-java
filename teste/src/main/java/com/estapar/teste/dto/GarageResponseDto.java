package com.estapar.teste.dto;

import java.util.List;

public record GarageResponseDto(

        List<GarageDto> garage,
        List<ParkingSpotDto>spots
) {
}
