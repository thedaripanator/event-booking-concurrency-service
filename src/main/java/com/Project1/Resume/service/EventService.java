package com.Project1.Resume.service;


import com.Project1.Resume.entity.Event;
import com.Project1.Resume.entity.ReservationStatus;
import com.Project1.Resume.entity.TicketReservation;
import com.Project1.Resume.repository.EventRepository;
import com.Project1.Resume.repository.TicketReservationRepository;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class EventService {
    private final EventRepository eventRepository;
    private final TicketReservationRepository reservationRepository;

    public EventService(EventRepository eventRepository,
                        TicketReservationRepository reservationRepository) {
        this.eventRepository = eventRepository;
        this.reservationRepository = reservationRepository;
    }

    @Transactional
    public TicketReservation bookTicket(Long eventId, String userId, String idempotencyKey) {
        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            Optional<TicketReservation> existing = reservationRepository.findByIdempotencyKey(idempotencyKey);
            if (existing.isPresent()) {
                return existing.get();
            }
        }

        try {
            Event event = eventRepository.findById(eventId)
                    .orElseThrow(() -> new RuntimeException("Event not found"));

            if (event.getAvailableSeats() <= 0) {
                throw new RuntimeException("Sold out!");
            }

            event.setAvailableSeats(event.getAvailableSeats() - 1);
            eventRepository.save(event);

            TicketReservation reservation = new TicketReservation();
            reservation.setEventId(eventId);
            reservation.setUserId(userId);
            reservation.setStatus(ReservationStatus.PENDING);
            reservation.setExpiresAt(LocalDateTime.now().plusSeconds(15));
            reservation.setCreatedAt(LocalDateTime.now());
            reservation.setIdempotencyKey(idempotencyKey);

            return reservationRepository.save(reservation);

        } catch (ObjectOptimisticLockingFailureException e) {
            throw new RuntimeException("High traffic collision! Please try booking again.");
        }
    }

    @Transactional
    public TicketReservation confirmReservation(Long reservationId) {
        TicketReservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("Reservation not found"));

        if (reservation.getStatus() == ReservationStatus.CONFIRMED) {
            return reservation;
        }

        if (reservation.getStatus() != ReservationStatus.PENDING || LocalDateTime.now().isAfter(reservation.getExpiresAt())) {
            throw new RuntimeException("Reservation cannot be confirmed because it is expired or invalid.");
        }

        reservation.setStatus(ReservationStatus.CONFIRMED);
        return reservationRepository.save(reservation);
    }
}