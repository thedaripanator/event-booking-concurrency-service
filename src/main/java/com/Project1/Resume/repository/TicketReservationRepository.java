package com.Project1.Resume.repository;

import com.Project1.Resume.entity.ReservationStatus;
import com.Project1.Resume.entity.TicketReservation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface TicketReservationRepository extends JpaRepository<TicketReservation,Long> {
    List<TicketReservation> findByStatusAndExpiresAtBefore(ReservationStatus status, LocalDateTime cutoff);
}
