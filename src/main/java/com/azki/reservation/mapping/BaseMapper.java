package com.azki.reservation.mapping;

import org.mapstruct.BeanMapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

public interface BaseMapper<M,E> {
    M toModel(final E entity);
    List<M> toModel(final List<E> entities);

    E toEntity(final M model);
    List<E> toEntity(final List<M> models);
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    E updateEntity(M model, @MappingTarget E entity);
    List<E> updateEntity(List<M> modelList, @MappingTarget List<E> entityList);
}
