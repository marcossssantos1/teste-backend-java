package com.estapar.teste.dto;

import com.estapar.teste.entity.Garage;
import com.fasterxml.jackson.annotation.JsonProperty;

public record GarageDto(
        String sector,

        @JsonProperty("base_price")
        Double basePrice,

        @JsonProperty("max_capacity")
        Integer maxCapacity,

        @JsonProperty("open_hour")
        String openHour,

        @JsonProperty("close_hour")
        String closeHour,

        @JsonProperty("duration_limit_minutes")
        Integer durationLimitMinutes

) {
    public Garage toEntity() {
        Garage s = new Garage();
        s.setSector(sector);
        s.setBasePrice(basePrice);
        s.setMaxCapacity(maxCapacity);
        s.setOpenHour(openHour);
        s.setCloseHour(closeHour);
        s.setDurationLimitMinutes(durationLimitMinutes);
        s.setOpen(true);
        return s;
    }

}
