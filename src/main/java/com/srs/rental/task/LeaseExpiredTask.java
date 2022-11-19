package com.srs.rental.task;

import com.srs.common.util.TimestampUtil;
import com.srs.rental.LeaseStatus;
import com.srs.rental.entity.ApplicationEntity;
import com.srs.rental.kafka.producer.LeaseKafkaProducer;
import com.srs.rental.repository.ApplicationRepository;
import com.srs.rental.repository.LeaseDslRepository;
import com.srs.rental.repository.LeaseRepository;
import com.srs.rental.repository.task.ScheduledTaskRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.OffsetDateTime;
import java.util.stream.Collectors;

import static com.srs.rental.ScheduledTask.TASK_LEASE_EXPIRED;


/**
 * @author duynt on 3/30/22
 */
@Component
@Log4j2
@Scope("prototype")
public class LeaseExpiredTask extends BaseTask implements Runnable {
    private final LeaseRepository leaseRepository;

    private final LeaseDslRepository leaseDslRepository;

    private final LeaseKafkaProducer leaseKafkaProducer;

    protected LeaseExpiredTask(TransactionTemplate transactionTemplate,
            ScheduledTaskRepository scheduledTaskRepository,
            LeaseRepository leaseRepository,
            LeaseDslRepository leaseDslRepository, LeaseKafkaProducer leaseKafkaProducer) {
        super(transactionTemplate, scheduledTaskRepository);
        this.leaseRepository = leaseRepository;
        this.leaseDslRepository = leaseDslRepository;
        this.leaseKafkaProducer = leaseKafkaProducer;
    }

    @Override
    public void run() {
        log.info("\n=======================".repeat(3));
        log.info("Proceeding expired leases having end date before {}", TimestampUtil.now());

        var triggeredTime = TimestampUtil.now();
        final var taskName = this.getTaskName(triggeredTime);

        var taskInitResult = this.createTask(TASK_LEASE_EXPIRED, triggeredTime);

        if (!taskInitResult.isSuccess()) {
            log.warn("Ignored executing expire leases task");
            return;
        }

        var expiredLeases = leaseDslRepository.findAllToInactivate();

        if (expiredLeases.isEmpty()) {
            log.info("No expired lease having end date less than or equal {} need to be proceed", triggeredTime);
            this.saveTaskResult(TaskResult.success(taskName));
            return;
        }

        var expiredLeaseIds = expiredLeases.stream()
                .map(ApplicationEntity::getApplicationId)
                .collect(Collectors.toList());

        try {
            transactionTemplate.executeWithoutResult(status -> {
                for (var expiredLease : expiredLeases) {
                    expiredLease.setLeaseStatus(LeaseStatus.INACTIVE_VALUE);
                    leaseKafkaProducer.notifyLeaseBeingTerminated(
                            expiredLease);
                }
                leaseRepository.saveAll(expiredLeases);

                this.saveTaskResult(TaskResult.success(taskName));
                status.flush();
            });

            log.info("Proceeded {} expired leases successfully", expiredLeaseIds.size());
        } catch (Exception e) {
            log.error("Failed to proceed {} expired leases. {} - {}", expiredLeases.size(),
                    e.getClass().getSimpleName(), e.getMessage());
            this.saveTaskResult(TaskResult.failure(taskName, e));
        }

        log.info("\n=======================".repeat(3));
    }


    @Override
    protected String getTaskName(OffsetDateTime triggeredAt) {
        return TaskName.getTaskName(TASK_LEASE_EXPIRED, triggeredAt);
    }
}
