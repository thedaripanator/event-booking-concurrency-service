package com.Project1.Resume.service;

import com.Project1.Resume.entity.ReservationStatus;
import com.Project1.Resume.entity.TicketReservation;
import com.Project1.Resume.repository.EventRepository;
import com.Project1.Resume.repository.TicketReservationRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ReservationCleanupService {

    private final TicketReservationRepository reservationRepository;
    private final EventRepository eventRepository;

    public ReservationCleanupService(TicketReservationRepository reservationRepository,
                                     EventRepository eventRepository) {
        this.reservationRepository = reservationRepository;
        this.eventRepository = eventRepository;
    }

    @Scheduled(fixedDelay = 30000)
    @Transactional
    public void releaseExpiredReservations() {
        LocalDateTime now = LocalDateTime.now();
        List<TicketReservation> expiredReservations =
                reservationRepository.findByStatusAndExpiresAtBefore(ReservationStatus.PENDING, now);

        if (expiredReservations.isEmpty()) {
            return;
        }

        System.out.println("Releasing " + expiredReservations.size() + " expired PENDING reservations...");

        for (TicketReservation reservation : expiredReservations) {
            reservation.setStatus(ReservationStatus.EXPIRED);
            reservationRepository.save(reservation);

            eventRepository.findById(reservation.getEventId()).ifPresent(event -> {
                event.setAvailableSeats(event.getAvailableSeats() + 1);
                eventRepository.save(event);
                System.out.println("Restored 1 seat for event " + event.getId()
                        + " (availableSeats=" + event.getAvailableSeats() + ")");
            });
        }
    }
}