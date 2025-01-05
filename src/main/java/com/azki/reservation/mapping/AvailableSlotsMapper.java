package com.azki.reservation.mapping;

import com.azki.reservation.entity.AvailableSlotsEntity;
import com.azki.reservation.model.AvailableSlotsModel;
import org.mapstruct.Mapper;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = "spring",
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public interface AvailableSlotsMapper extends BaseMapper<AvailableSlotsModel, AvailableSlotsEntity> {
}
