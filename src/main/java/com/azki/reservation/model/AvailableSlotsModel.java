package com.azki.reservation.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
@EqualsAndHashCode(callSuper = true)
@Data
public class AvailableSlotsModel extends BaseModel<Long>{
    @NotNull
    private LocalDateTime startTime;
    @NotNull
    private LocalDateTime endTime;
    private Boolean isReserved = false;
    private UserModel reservedBy;
    private LocalDateTime reservedAt;
}
