package com.estapar.teste.dto;

import com.estapar.teste.entity.ParkingSpot;

public record ParkingSpotDto(

         Long id,
         String sector,
         Double lat,
         Double lng,
         Boolean occupied
) {

    public ParkingSpot toEntity() {
        ParkingSpot spot = new ParkingSpot();
        spot.setId(id);
        spot.setSector(sector);
        spot.setLat(lat);
        spot.setLng(lng);
        spot.setOccupied(occupied != null && occupied);
        return spot;
    }
}
