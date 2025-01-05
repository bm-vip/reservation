package com.azki.reservation.repository;

import com.azki.reservation.entity.AvailableSlotsEntity;
import com.azki.reservation.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.stereotype.Repository;

import javax.persistence.LockModeType;
import javax.persistence.QueryHint;
import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface AvailableSlotsRepository extends JpaRepository<AvailableSlotsEntity, Long> {
    // Find the nearest available slot and lock it for update
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints({@QueryHint(name = "javax.persistence.lock.timeout", value = "3000")})
    Optional<AvailableSlotsEntity> findTopByIsReservedFalseAndStartTimeAfterOrderByStartTime(LocalDateTime startTime);

    Optional<AvailableSlotsEntity> findByIdAndReservedBy(Long id, UserEntity reservedBy);

    Long countAllByIsReserved(boolean isReserved);
}
