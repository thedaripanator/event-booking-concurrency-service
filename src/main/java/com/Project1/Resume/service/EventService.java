package com.Project1.Resume.service;


import com.Project1.Resume.entity.Event;
import com.Project1.Resume.entity.ReservationStatus;
import com.Project1.Resume.entity.TicketReservation;
import com.Project1.Resume.repository.EventRepository;
import com.Project1.Resume.repository.TicketReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;
    private final TicketReservationRepository reservationRepository;

    @Transactional
    public TicketReservation bookTicket(Long eventId, String userId) {
        try {
            Event event = eventRepository.findById(eventId)
                    .orElseThrow(() -> new RuntimeException("Event not found"));

            if (event.getAvailableSeats() <= 0) {
                throw new RuntimeException("Sold out!");
            }


            event.setAvailableSeats(event.getAvailableSeats() - 1);
            eventRepository.save(event);


            TicketReservation reservation = new TicketReservation(
                    null,
                    eventId,
                    userId,
                    ReservationStatus.PENDING,
                    LocalDateTime.now().plusMinutes(5),
                    LocalDateTime.now()
            );

            return reservationRepository.save(reservation);

        } catch (ObjectOptimisticLockingFailureException e) {

            throw new RuntimeException("High traffic collision! Please try booking again.");
        }
    }
}