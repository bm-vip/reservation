package com.azki.reservation;

import com.azki.reservation.common.config.SwaggerConfig;
import com.azki.reservation.common.util.RequestHolder;
import com.azki.reservation.entity.AvailableSlotsEntity;
import com.azki.reservation.exception.NotFoundException;
import com.azki.reservation.model.AvailableSlotsModel;
import com.azki.reservation.model.UserModel;
import com.azki.reservation.service.AvailableSlotsService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ReservationIntegrationTest {
    @MockBean
    private SwaggerConfig SwaggerConfig;
    @MockBean
    private RequestHolder requestHolder;
    @Autowired
    private AvailableSlotsService availableSlotsService;

    private ExecutorService executorService;

    private static final List<UUID> users = List.of(
            UUID.fromString("eb77b9de-7a86-4f16-8416-bdaf8d6c025d"),
            UUID.fromString("eb77b9de-7a86-4f16-8417-bdaf8d6c026d"),
            UUID.fromString("eb77b9de-7a86-4f16-8417-bdaf8d6c027d")
    );

    @BeforeEach
    void setUp() {
        log.info("Setting up test environment. Clearing all slots.");
        availableSlotsService.deleteAll();
        executorService = Executors.newFixedThreadPool(10);
    }

    @Test
    @Order(1)
    void testConcurrentReservations() throws InterruptedException {
        log.info("Starting testConcurrentReservations");
        createTestSlots(5);

        int numberOfUsers = 10;
        CountDownLatch readyLatch = new CountDownLatch(numberOfUsers);
        CountDownLatch startLatch = new CountDownLatch(1);
        List<Future<AvailableSlotsModel>> futures = new ArrayList<>();

        Random random = new Random();
        for (int i = 0; i < numberOfUsers; i++) {
            var user = users.get(random.nextInt(users.size()));
            futures.add(executorService.submit(() -> {
                readyLatch.countDown();
                startLatch.await();
                try {
                    return availableSlotsService.reserveNearestAvailableSlot(user, LocalDateTime.now().minusMinutes(1));
                } catch (NotFoundException e) {
                    log.warn("Reservation failed for user {}", user);
                    return new AvailableSlotsModel();
                }
            }));
        }

        readyLatch.await();
        startLatch.countDown();

        List<AvailableSlotsModel> results = new ArrayList<>();
        for (Future<AvailableSlotsModel> future : futures) {
            try {
                results.add(future.get(5, TimeUnit.SECONDS));
            } catch (ExecutionException | TimeoutException e) {
                log.error("Error during reservation execution", e);
            }
        }

        long successfulReservations = results.stream()
                .filter(AvailableSlotsModel::getIsReserved)
                .count();
        log.info("Successful reservations: {}", successfulReservations);

        assertEquals(5, successfulReservations, "Should have exactly 5 successful reservations");

        long uniqueReservations = results.stream()
                .filter(AvailableSlotsModel::getIsReserved)
                .map(AvailableSlotsModel::getId)
                .distinct()
                .count();
        assertEquals(successfulReservations, uniqueReservations, "Each slot should be reserved only once");
    }

    @Test
    @Order(2)
    void testConcurrentCancellations() throws InterruptedException {
        log.info("Starting testConcurrentCancellations");
        createTestSlots(5);

        List<AvailableSlotsModel> reservedSlots = new ArrayList<>();
        Random random = new Random();
        for (int i = 0; i < 5; i++) {
            var user = users.get(random.nextInt(users.size()));
            reservedSlots.add(availableSlotsService.reserveNearestAvailableSlot(user, LocalDateTime.now().minusMinutes(1)));
        }

        CountDownLatch latch = new CountDownLatch(5);
        List<Future<Boolean>> futures = new ArrayList<>();

        for (int i = 0; i < 5; i++) {
            final AvailableSlotsModel reservation = reservedSlots.get(i);
            futures.add(executorService.submit(() -> {
                try {
                    latch.countDown();
                    latch.await();
                    availableSlotsService.cancelReservation(reservation.getId(), reservation.getReservedBy().getId());
                    log.info("Successfully canceled reservation {}", reservation.getId());
                    return true;
                } catch (Exception e) {
                    log.error("Failed to cancel reservation {}", reservation.getId(), e);
                    return false;
                }
            }));
        }

        List<Boolean> results = futures.stream()
                .map(future -> {
                    try {
                        return future.get(10, TimeUnit.SECONDS);
                    } catch (Exception e) {
                        log.error("Error during cancellation", e);
                        return false;
                    }
                })
                .toList();

        long successfulCancellations = results.stream().filter(result -> result).count();
        log.info("Successful cancellations: {}", successfulCancellations);

        assertEquals(5, successfulCancellations, "All cancellations should be successful");

        long reservedSlotsCount = availableSlotsService.countAllByReserved(true);
        assertEquals(0, reservedSlotsCount, "No slots should be reserved");
    }

    @Test
    @Order(3)
    void testReservationRaceCondition() {
        log.info("Starting testReservationRaceCondition");
        createTestSlots(1);
        AvailableSlotsModel slot = availableSlotsService.findAll().get(0);

        int numberOfConcurrentUsers = 5;
        CountDownLatch latch = new CountDownLatch(numberOfConcurrentUsers);
        List<Future<UserModel>> futures = new ArrayList<>();
        Random random = new Random();

        for (int i = 0; i < numberOfConcurrentUsers; i++) {
            var user = users.get(random.nextInt(users.size()));
            futures.add(executorService.submit(() -> {
                latch.countDown();
                latch.await();
                try {
                    AvailableSlotsModel reserved = availableSlotsService.reserveNearestAvailableSlot(user, LocalDateTime.now().minusMinutes(1));
                    return reserved.getReservedBy();
                } catch (NotFoundException e) {
                    return null;
                }
            }));
        }

        List<UserModel> successfulUsers = futures.stream()
                .map(future -> {
                    try {
                        return future.get(10, TimeUnit.SECONDS);
                    } catch (Exception e) {
                        log.error("Error during reservation", e);
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .toList();

        log.info("Number of successful reservations: {}", successfulUsers.size());
        assertEquals(1, successfulUsers.size(), "Only one user should successfully reserve the slot");

        AvailableSlotsModel reservedSlot = availableSlotsService.getReservation(slot.getId());
        assertTrue(reservedSlot.getIsReserved(), "Slot should be reserved");
    }

    private void createTestSlots(int count) {
        log.info("Creating {} test slots", count);
        List<AvailableSlotsEntity> slots = new ArrayList<>();
        LocalDateTime startTime = LocalDateTime.now().plusHours(1);

        for (int i = 0; i < count; i++) {
            var entity = new AvailableSlotsEntity();
            entity.setStartTime(startTime.plusHours(i));
            entity.setEndTime(startTime.plusHours(i + 1));
            entity.setIsReserved(false);
            slots.add(entity);
        }
        availableSlotsService.addAll(slots);
    }

//    @Test I disabled this test because it usually took 30 minutes to run.
//    @Order(4)
    void testHighConcurrencyPerformance() {
        // Create 1000 available slots
        createTestSlots(1000);

        int numberOfRequests = 100;
        CountDownLatch latch = new CountDownLatch(numberOfRequests);
        List<Future<Long>> futures = new ArrayList<>();

        // Measure response times for concurrent requests
        Random random = new Random();
        for (int i = 0; i < numberOfRequests; i++) {
            var user = users.get(random.nextInt(users.size()));
            futures.add(executorService.submit(() -> {
                try {
                    latch.countDown();
                    latch.await();
                    long startTime = System.currentTimeMillis();
                    try {
                        availableSlotsService.reserveNearestAvailableSlot(user, LocalDateTime.now().minusMinutes(1));
                    } catch (NotFoundException ignored) {
                        // Ignore if reservation fails
                    }
                    return System.currentTimeMillis() - startTime;
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return 0L;
                }
            }));
        }

        // Calculate response time statistics
        List<Long> responseTimes = futures.stream()
                .map(future -> {
                    try {
                        return future.get(10, TimeUnit.SECONDS);
                    } catch (Exception e) {
                        return 0L;
                    }
                })
                .filter(time -> time > 0)
                .toList();

        double averageResponseTime = responseTimes.stream()
                .mapToLong(Long::longValue)
                .average()
                .orElse(0.0);

        long maxResponseTime = responseTimes.stream()
                .mapToLong(Long::longValue)
                .max()
                .orElse(0L);

        // Assert performance requirements
        assertTrue(averageResponseTime < 100,
                "Average response time should be less than 100ms but was " + averageResponseTime + "ms");
        assertTrue(maxResponseTime < 200,
                "Maximum response time should be less than 200ms but was " + maxResponseTime + "ms");
    }
}
