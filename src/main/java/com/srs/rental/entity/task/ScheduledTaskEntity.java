package com.srs.rental.entity.task;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.time.OffsetDateTime;


@Getter
@Setter
@Entity
@Table(name = "scheduled_task", schema = "scheduled")
public class ScheduledTaskEntity implements Serializable {
    @Id
    private String name;

    private int type;

    private OffsetDateTime startedAt;
    private OffsetDateTime endedAt;

    private boolean completed;
    private boolean success;

    private String stacktrace;
}
