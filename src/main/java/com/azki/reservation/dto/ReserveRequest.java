package com.azki.reservation.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class ReserveRequest {
    @NotNull
    private UUID userId;
    @NotNull
    private LocalDateTime expectedTime;
}
