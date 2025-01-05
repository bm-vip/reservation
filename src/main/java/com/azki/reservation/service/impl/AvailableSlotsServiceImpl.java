package com.azki.reservation.service.impl;

import com.azki.reservation.entity.AvailableSlotsEntity;
import com.azki.reservation.exception.NotFoundException;
import com.azki.reservation.mapping.AvailableSlotsMapper;
import com.azki.reservation.model.AvailableSlotsModel;
import com.azki.reservation.repository.AvailableSlotsRepository;
import com.azki.reservation.repository.UserRepository;
import com.azki.reservation.service.AvailableSlotsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AvailableSlotsServiceImpl implements AvailableSlotsService {
    private final AvailableSlotsRepository availableSlotsRepository;
    private final AvailableSlotsMapper availableSlotsMapper;
    private final UserRepository userRepository;

    @Override
    @CachePut(value = "reservations")
    public void addAll(List<AvailableSlotsEntity> slots) {
        log.info("Adding {} available slots", slots.size());
        availableSlotsRepository.saveAll(slots);
        log.info("Successfully added {} available slots", slots.size());
    }

    @Override
    @Transactional
    public AvailableSlotsModel reserveNearestAvailableSlot(UUID userId, LocalDateTime expectedTime) {
        log.info("Reserving nearest available slot for userId: {} after time: {}", userId, expectedTime);
        var user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("User not found with id: {}", userId);
                    return new NotFoundException("User not found");
                });

        var entity = availableSlotsRepository.findTopByIsReservedFalseAndStartTimeAfterOrderByStartTime(expectedTime)
                .orElseThrow(() -> {
                    log.warn("No available slot found after time: {}", expectedTime);
                    return new NotFoundException("No available slot found");
                });

        if (!entity.getIsReserved()) {
            entity.setIsReserved(true);
            entity.setReservedBy(user);
            entity.setReservedAt(LocalDateTime.now());
            log.info("Slot reserved successfully for userId: {}", userId);
            return availableSlotsMapper.toModel(availableSlotsRepository.save(entity));
        }

        log.warn("Slot reservation failed for userId: {}", userId);
        return null;
    }

    @Transactional
    @CacheEvict(value = "reservations", key = "#id")
    public void cancelReservation(Long id, UUID userId) {
        log.info("Cancelling reservation with id: {} for userId: {}", id, userId);
        var user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("User not found with id: {}", userId);
                    return new NotFoundException("User not found");
                });

        var entity = availableSlotsRepository.findByIdAndReservedBy(id, user)
                .orElseThrow(() -> {
                    log.warn("Reservation not found or not owned by userId: {}", userId);
                    return new NotFoundException("Reservation not found or not owned by user");
                });

        entity.setIsReserved(false);
        entity.setReservedBy(null);
        entity.setReservedAt(null);

        availableSlotsRepository.save(entity);
        log.info("Reservation with id: {} cancelled successfully by userId: {}", id, userId);
    }

    @Cacheable(value = "reservations", key = "#id")
    public AvailableSlotsModel getReservation(Long id) {
        log.info("Fetching reservation with id: {}", id);
        return availableSlotsMapper.toModel(availableSlotsRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Reservation not found with id: {}", id);
                    return new NotFoundException("Reservation not found");
                }));
    }

    @Override
    @Cacheable(value = "reservations", key = "'countAllByReserved:' + #isReserved")
    public Long countAllByReserved(boolean isReserved) {
        log.info("Counting all slots with reserved status: {}", isReserved);
        Long count = availableSlotsRepository.countAllByIsReserved(isReserved);
        log.info("Found {} slots with reserved status: {}", count, isReserved);
        return count;
    }

    @Override
    @CacheEvict(value = "reservations", allEntries = true)
    public void deleteAll() {
        log.warn("Deleting all available slots");
        availableSlotsRepository.deleteAll();
        availableSlotsRepository.flush();
        log.info("All available slots have been deleted and flushed");
    }

    @Override
    @Cacheable(value = "reservations")
    public List<AvailableSlotsModel> findAll() {
        log.info("Fetching all available slots");
        List<AvailableSlotsModel> slots = availableSlotsMapper.toModel(availableSlotsRepository.findAll());
        log.info("Retrieved {} available slots", slots.size());
        return slots;
    }
}
