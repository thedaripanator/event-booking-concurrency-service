package com.Project1.Resume.repository;

import com.Project1.Resume.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventRepository extends JpaRepository<Event,Long> {
}
