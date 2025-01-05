package com.azki.reservation.controller;

import com.azki.reservation.dto.ReserveRequest;
import com.azki.reservation.model.AvailableSlotsModel;
import com.azki.reservation.service.AvailableSlotsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@Tag(name = "Available Slots Rest Service v1")
@RequestMapping(value = "/api/v1/available-slots")
@RequiredArgsConstructor
public class AvailableSlotsController {
    private final AvailableSlotsService availableSlotsService;

    @PostMapping("/reserve")
    @Operation(summary = "Book the nearest available time slot")
    public ResponseEntity<AvailableSlotsModel> reserveNearestSlot(@RequestBody @Validated ReserveRequest request) {
        return ResponseEntity.ok(availableSlotsService.reserveNearestAvailableSlot(request.getUserId(), request.getExpectedTime()));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Cancel a reservation")
    public ResponseEntity<Void> cancelReservation(@PathVariable Long id, @RequestHeader UUID userId) {
        availableSlotsService.cancelReservation(id, userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get reservation details")
    public ResponseEntity<AvailableSlotsModel> getReservation(@PathVariable Long id) {
        return ResponseEntity.ok(availableSlotsService.getReservation(id));
    }
}
