package com.srs.rental.task;

import com.srs.common.util.TimestampUtil;
import com.srs.rental.ScheduledTask;
import com.srs.rental.entity.task.ScheduledTaskEntity;
import com.srs.rental.repository.task.ScheduledTaskRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

/**
 * @author duynt on 3/28/22
 */
@Log4j2
public abstract class BaseTask {
    protected static final DateTimeFormatter dtFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    protected final TransactionTemplate transactionTemplate;

    protected final ScheduledTaskRepository scheduledTaskRepository;

    protected BaseTask(TransactionTemplate transactionTemplate, ScheduledTaskRepository scheduledTaskRepository) {
        this.transactionTemplate = transactionTemplate;
        this.scheduledTaskRepository = scheduledTaskRepository;
    }

    protected TaskResult createTask(ScheduledTask type, OffsetDateTime triggeredAt) {
        var taskName = this.getTaskName(triggeredAt);

        try {
            transactionTemplate.executeWithoutResult(status -> {
                var task = new ScheduledTaskEntity();
                task.setType(type.getNumber());
                task.setStartedAt(TimestampUtil.now());
                task.setName(taskName);
                task.setCompleted(false);
                task.setSuccess(false);

                scheduledTaskRepository.save(task);
            });
            return TaskResult.success(taskName);
        } catch (Exception e) {
            log.error("Cannot create task {}. {} - {}", taskName, e.getClass().getSimpleName(), e.getMessage());
            return TaskResult.failure(taskName, e);
        }
    }

    protected void saveTaskResult(final TaskResult result) {
        try {
            transactionTemplate.executeWithoutResult(status -> {
                var task = scheduledTaskRepository.findById(result.getTaskName()).orElse(null);

                if (task != null) {
                    task.setEndedAt(TimestampUtil.now());
                    task.setCompleted(true);
                    task.setSuccess(result.isSuccess());

                    if (!result.isSuccess()) {
                        task.setStacktrace(result.getStacktrace());
                    }

                    scheduledTaskRepository.save(task);
                } else {
                    log.info("Task not found with name {}", result.getTaskName());
                }
            });
        } catch (Exception e) {
            log.error("Cannot save result of task {}. {} - {}", result.getTaskName(), e.getClass().getSimpleName(), e.getMessage());
        }
    }

    protected abstract String getTaskName(OffsetDateTime triggeredAt);
}
