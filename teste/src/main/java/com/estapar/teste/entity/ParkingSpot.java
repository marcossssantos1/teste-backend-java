package com.estapar.teste.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "parking_spot")
public class ParkingSpot {

    @Id
    private Long id;

    @Column(nullable = false)
    private String sector;

    private Double lat;
    private Double lng;

    @Column(nullable = false)
    private Boolean occupied = false;

    public ParkingSpot() {
    }

    public ParkingSpot(Long id, String sector, Double lat, Double lng, Boolean occupied) {
        this.id = id;
        this.sector = sector;
        this.lat = lat;
        this.lng = lng;
        this.occupied = occupied;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSector() {
        return sector;
    }

    public void setSector(String sector) {
        this.sector = sector;
    }

    public Double getLat() {
        return lat;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    public Double getLng() {
        return lng;
    }

    public void setLng(Double lng) {
        this.lng = lng;
    }

    public Boolean getOccupied() {
        return occupied;
    }

    public void setOccupied(Boolean occupied) {
        this.occupied = occupied;
    }
}
