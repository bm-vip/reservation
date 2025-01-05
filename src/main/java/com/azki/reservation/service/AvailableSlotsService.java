package com.azki.reservation.service;

import com.azki.reservation.entity.AvailableSlotsEntity;
import com.azki.reservation.model.AvailableSlotsModel;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface AvailableSlotsService {
    void addAll(List<AvailableSlotsEntity> model);
    void deleteAll();
    List<AvailableSlotsModel> findAll();
    AvailableSlotsModel reserveNearestAvailableSlot(UUID userId, LocalDateTime expectedTime);
    void cancelReservation(Long id, UUID userId);
    AvailableSlotsModel getReservation(Long id);
    Long countAllByReserved(boolean isReserved);
}
