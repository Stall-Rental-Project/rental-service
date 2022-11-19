package com.srs.rental.task;

import com.srs.common.util.TimestampUtil;
import com.srs.rental.LeaseStatus;
import com.srs.rental.TerminationStatus;
import com.srs.rental.entity.LeaseTerminationEntity;
import com.srs.rental.kafka.producer.LeaseKafkaProducer;
import com.srs.rental.repository.ApplicationRepository;
import com.srs.rental.repository.LeaseRepository;
import com.srs.rental.repository.LeaseTerminationDslRepository;
import com.srs.rental.repository.LeaseTerminationRepository;
import com.srs.rental.repository.task.ScheduledTaskRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.srs.rental.ScheduledTask.TASK_LEASE_TERMINATE;

/**
 * @author duynt on 3/28/22
 */
@Component
@Log4j2
@Scope("prototype")
public class LeaseTerminatingTask extends BaseTask implements Runnable {

    private final LeaseTerminationDslRepository leaseTerminationDslRepository;
    private final ApplicationRepository applicationRepository;
    private final LeaseRepository leaseRepository;
    private final LeaseTerminationRepository leaseTerminationRepository;
    private final LeaseKafkaProducer leaseKafkaProducer;

    protected LeaseTerminatingTask(TransactionTemplate transactionTemplate,
                                   ScheduledTaskRepository scheduledTaskRepository,
                                   LeaseTerminationDslRepository leaseTerminationDslRepository,
                                   ApplicationRepository applicationRepository,
                                   LeaseRepository leaseRepository,
                                   LeaseTerminationRepository leaseTerminationRepository,
                                   LeaseKafkaProducer leaseKafkaProducer
    ) {
        super(transactionTemplate, scheduledTaskRepository);
        this.leaseTerminationDslRepository = leaseTerminationDslRepository;
        this.applicationRepository = applicationRepository;
        this.leaseRepository = leaseRepository;
        this.leaseTerminationRepository = leaseTerminationRepository;
        this.leaseKafkaProducer = leaseKafkaProducer;
    }

    @Override
    public void run() {
        log.info("\n=======================".repeat(3));
        log.info("Proceeding lease termination requests having end date before {}",
                TimestampUtil.now().toString());

        var triggeredTime = TimestampUtil.now();
        final var taskName = this.getTaskName(triggeredTime);

        var taskInitResult = this.createTask(TASK_LEASE_TERMINATE, triggeredTime);

        if (!taskInitResult.isSuccess()) {
            log.warn("Ignored executing terminating leases task");
            return;
        }

        // 1. Preparing data
        var pendingRequests = leaseTerminationDslRepository.findAllPendingTerminationRequests();

        if (pendingRequests.isEmpty()) {
            log.info("No termination need to be proceeded");
            this.saveTaskResult(TaskResult.success(taskName));
            return;
        }

        var mapPendingRequests = new HashMap<UUID /*applicationId*/, LeaseTerminationEntity>();
        for (var pendingRequest : pendingRequests) {
            pendingRequest.setStatus(TerminationStatus.T_CLOSED_VALUE);
            if (pendingRequest.getApplicationId() != null) {
                mapPendingRequests.put(pendingRequest.getApplicationId(), pendingRequest);
            }
        }
        try {
            transactionTemplate.executeWithoutResult(status -> {
                leaseTerminationRepository.saveAll(pendingRequests);
                leaseRepository.updateLeaseStatusInBatch(mapPendingRequests.keySet(),
                        LeaseStatus.TERMINATED_VALUE);
                List<UUID> applicationIds = mapPendingRequests.entrySet().stream()
                        .map(map -> map.getKey()).collect(
                                Collectors.toList());

                var listLeaseApp = leaseRepository.findAllById(applicationIds);
                if (listLeaseApp.size() > 0) {
                    for (var leaseApp : listLeaseApp) {
                        leaseKafkaProducer.notifyLeaseBeingTerminated(
                                leaseApp);
                    }
                }
                this.saveTaskResult(TaskResult.success(taskName));
                status.flush();
            });

            log.info("Proceeded {} pending termination requests successfully",
                    pendingRequests.size());
        } catch (Exception e) {
            log.error("Failed to proceed {} termination requests. {} - {}", pendingRequests.size(),
                    e.getClass().getSimpleName(), e.getMessage());
            this.saveTaskResult(TaskResult.failure(taskName, e));
        }

        log.info("\n=======================".repeat(3));
    }

    @Override
    protected String getTaskName(OffsetDateTime triggeredAt) {
        return TaskName.getTaskName(TASK_LEASE_TERMINATE, triggeredAt);
    }
}
