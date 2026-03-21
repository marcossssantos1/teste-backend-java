package com.estapar.teste.dto;

public record ParkingSpotDto(

         Long id,
         String sector,
         Double lat,
         Double lng,
         Boolean occupied
) {
}
