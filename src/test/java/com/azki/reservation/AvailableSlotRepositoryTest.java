package com.azki.reservation;

import com.azki.reservation.common.config.SwaggerConfig;
import com.azki.reservation.common.util.RequestHolder;
import com.azki.reservation.entity.AvailableSlotsEntity;
import com.azki.reservation.entity.UserEntity;
import com.azki.reservation.repository.AvailableSlotsRepository;
import com.azki.reservation.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@SpringBootTest
public class AvailableSlotRepositoryTest {
    @MockBean
    private SwaggerConfig SwaggerConfig;
    @MockBean
    private RequestHolder requestHolder;

    @Autowired
    private AvailableSlotsRepository repository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PlatformTransactionManager transactionManager;

    private static final UUID userId1 = UUID.fromString("eb77b9de-7a86-4f16-8416-bdaf8d6c025d");
    private static final UUID userId2 = UUID.fromString("eb77b9de-7a86-4f16-8417-bdaf8d6c026d");

    @Test
    @Transactional
    void testFindAndLockNearestAvailableSlot() {
        log.info("Starting test: testFindAndLockNearestAvailableSlot");

        createTestSlot(LocalDateTime.now().plusHours(2));
        var nearestSlot = createTestSlot(LocalDateTime.now().plusHours(1));
        createTestSlot(LocalDateTime.now().plusHours(3));

        log.debug("Finding nearest available slot after current time");
        var found = repository.findTopByIsReservedFalseAndStartTimeAfterOrderByStartTime(LocalDateTime.now());

        assertTrue(found.isPresent(), "Should find the nearest slot");
        assertEquals(nearestSlot.getId(), found.get().getId(), "Should return the chronologically nearest slot");

        log.info("testFindAndLockNearestAvailableSlot completed successfully");
    }

    @Test
    @Transactional
    void testFindAndLockNearestAvailableSlotWithReservedSlots() {
        log.info("Starting test: testFindAndLockNearestAvailableSlotWithReservedSlots");

        var reserved = createTestSlot(LocalDateTime.now().plusHours(1));
        reserved.setIsReserved(true);
        repository.save(reserved);

        var available = createTestSlot(LocalDateTime.now().plusHours(2));

        log.debug("Finding available slot, skipping reserved slots");
        var found = repository.findTopByIsReservedFalseAndStartTimeAfterOrderByStartTime(LocalDateTime.now());

        assertTrue(found.isPresent(), "Should find an available slot");
        assertEquals(available.getId(), found.get().getId(), "Should skip reserved slots and return next available");

        log.info("testFindAndLockNearestAvailableSlotWithReservedSlots completed successfully");
    }

    @Test
    void testOptimisticLocking() throws Exception {
        log.info("Starting test: testOptimisticLocking");

        var entity = createTestSlot(LocalDateTime.now());

        CompletableFuture<Void> transaction1 = CompletableFuture.runAsync(() -> {
            try {
                log.debug("Transaction 1 starting");
                updateSlot(entity.getId(), userId1);
                log.debug("Transaction 1 completed");
            } catch (InterruptedException e) {
                log.error("Transaction 1 was interrupted", e);
                throw new RuntimeException(e);
            }
        });

        CompletableFuture<Void> transaction2 = CompletableFuture.runAsync(() -> {
            try {
                log.debug("Transaction 2 starting");
                updateSlot(entity.getId(), userId2);
                log.debug("Transaction 2 completed");
            } catch (InterruptedException e) {
                log.error("Transaction 2 was interrupted", e);
                throw new RuntimeException(e);
            }
        });

        assertThrows(ExecutionException.class, () -> {
            CompletableFuture.allOf(transaction1, transaction2).get();
        });

        var updatedEntity = repository.findById(entity.getId()).orElseThrow();
//        assertEquals(userId1, updatedEntity.getReservedBy().getId());
        assertEquals(1L, updatedEntity.getVersion());

        log.info("testOptimisticLocking completed successfully");
    }

    @Test
    void testPessimisticLocking() throws Exception {
        log.info("Starting test: testPessimisticLocking");

        var slot = new AvailableSlotsEntity();
        slot.setStartTime(LocalDateTime.now().plusHours(1));
        slot.setEndTime(LocalDateTime.now().plusHours(2));
        slot.setIsReserved(false);
        repository.saveAndFlush(slot);

        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch processLatch = new CountDownLatch(1);
        AtomicReference<Exception> exception = new AtomicReference<>();

        Future<?> future1 = executor.submit(() -> {
            try {
                log.debug("First transaction starting");
                TransactionTemplate template = new TransactionTemplate(transactionManager);
                template.execute(status -> {
                    repository.findTopByIsReservedFalseAndStartTimeAfterOrderByStartTime(LocalDateTime.now().minusMinutes(3))
                            .ifPresent(a -> {
                                startLatch.countDown();
                                try {
                                    processLatch.await(10, TimeUnit.SECONDS);
                                } catch (InterruptedException e) {
                                    Thread.currentThread().interrupt();
                                }
                                a.setReservedBy(userRepository.getById(userId1));
                                a.setIsReserved(true);
                                a.setReservedAt(LocalDateTime.now());
                                repository.save(a);
                            });
                    return null;
                });
            } catch (Exception e) {
                log.error("First transaction failed", e);
                exception.set(e);
            }
        });

        Future<?> future2 = executor.submit(() -> {
            try {
                log.debug("Second transaction starting");
                startLatch.await(10, TimeUnit.SECONDS);
                TransactionTemplate template = new TransactionTemplate(transactionManager);
                template.execute(status -> {
                    repository.findTopByIsReservedFalseAndStartTimeAfterOrderByStartTime(LocalDateTime.now().minusHours(1).minusMinutes(3))
                            .ifPresent(a -> {
                                a.setReservedBy(userRepository.getById(userId2));
                                a.setIsReserved(true);
                                a.setReservedAt(LocalDateTime.now());
                                repository.save(a);
                            });
                    return null;
                });
            } catch (TransactionSystemException e) {
                if (e.getApplicationException() instanceof PessimisticLockingFailureException) {
                    exception.set((PessimisticLockingFailureException) e.getApplicationException());
                } else exception.set(e);
                processLatch.countDown(); // Ensure first transaction can complete
            } catch (InterruptedException e) {
                exception.set(e);
                processLatch.countDown();
            } finally {
                processLatch.countDown();
            }
        });



        future1.get(15, TimeUnit.SECONDS);
        future2.get(15, TimeUnit.SECONDS);

        assertThat(exception.get()).isInstanceOf(PessimisticLockingFailureException.class);

        var updatedProduct = repository.findById(slot.getId()).orElseThrow();
        assertThat(updatedProduct.getReservedBy().getId()).isEqualTo(userId1);

        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.SECONDS);

        log.info("testPessimisticLocking completed successfully");
    }

    private AvailableSlotsEntity createTestSlot(LocalDateTime startTime) {
        log.debug("Creating test slot for time: {}", startTime);
        var slot = new AvailableSlotsEntity();
        slot.setStartTime(startTime);
        slot.setEndTime(startTime.plusHours(1));
        slot.setIsReserved(false);
        return repository.save(slot);
    }

    void updateSlot(Long id, UUID userId) throws InterruptedException {
        log.debug("Updating slot with ID {} for user {}", id, userId);
        var entity = repository.findById(id).orElseThrow();
        Thread.sleep(100);
        entity.setReservedAt(LocalDateTime.now());
        entity.setIsReserved(true);
        entity.setReservedBy(new UserEntity().setId(userId));
        repository.save(entity);
        repository.flush();
    }
}
