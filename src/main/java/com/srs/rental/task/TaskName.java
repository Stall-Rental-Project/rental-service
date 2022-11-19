package com.srs.rental.task;

import com.srs.rental.ScheduledTask;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

public class TaskName {
    private static final DateTimeFormatter DT_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM");

    public static String getTaskName(ScheduledTask task, OffsetDateTime startDate) {
        return String.format("%s-%s", task.name(), DT_FORMATTER.format(startDate));
    }
}
