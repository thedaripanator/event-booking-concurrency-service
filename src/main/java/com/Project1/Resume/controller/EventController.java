package com.Project1.Resume.controller;

import com.Project1.Resume.entity.TicketReservation;
import com.Project1.Resume.service.EventService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/events")
public class EventController {

    private final EventService eventService;

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    @PostMapping("/{eventId}/book")
    public ResponseEntity<TicketReservation> bookTicket(
            @PathVariable Long eventId,
            @RequestParam String userId,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey) {
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("userId must not be blank.");
        }
        return ResponseEntity.ok(eventService.bookTicket(eventId, userId, idempotencyKey));
    }

    @PostMapping("/reservations/{reservationId}/confirm")
    public ResponseEntity<TicketReservation> confirmReservation(@PathVariable Long reservationId) {
        return ResponseEntity.ok(eventService.confirmReservation(reservationId));
    }
}
