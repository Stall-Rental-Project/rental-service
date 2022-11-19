package com.srs.rental.repository.task;

import com.srs.rental.entity.task.ScheduledTaskEntity;
import org.springframework.data.jpa.repository.JpaRepository;


public interface ScheduledTaskRepository extends JpaRepository<ScheduledTaskEntity, String> {
}
