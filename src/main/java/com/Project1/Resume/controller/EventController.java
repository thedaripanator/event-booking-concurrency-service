package com.Project1.Resume.controller;

import com.Project1.Resume.entity.TicketReservation;
import com.Project1.Resume.service.EventService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;

    @PostMapping("/{eventId}/book")
    public ResponseEntity<TicketReservation> bookTicket(
            @PathVariable Long eventId,
            @RequestParam String userId) {
        TicketReservation reservation = eventService.bookTicket(eventId, userId);
        return ResponseEntity.ok(reservation);
    }
}
