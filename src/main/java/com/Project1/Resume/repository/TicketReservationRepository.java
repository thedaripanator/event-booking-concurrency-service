package com.Project1.Resume.repository;

import com.Project1.Resume.entity.TicketReservation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TicketReservationRepository extends JpaRepository<TicketReservation,Long> {
}
