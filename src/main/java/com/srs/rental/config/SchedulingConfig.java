package com.srs.rental.config;

import com.srs.rental.ScheduledTask;
import com.srs.rental.common.ServiceConfig;
import com.srs.rental.task.LeaseExpiredTask;
import com.srs.rental.task.LeaseTerminatingTask;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

@Configuration
@EnableScheduling
@RequiredArgsConstructor
@Log4j2
public class SchedulingConfig implements SchedulingConfigurer {
    private final ApplicationContext context;

    private final Environment environment;

    private final ServiceConfig serviceConfig;

    @Bean
    public TaskScheduler taskScheduler() {
        var poolSize = environment.getRequiredProperty("task-scheduler.pool-size", Integer.class);

        var scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(poolSize);
        scheduler.setThreadNamePrefix("rental-task-scheduler-");

        log.info("Initialized custom task scheduler with properties {poolSize: {}}", poolSize);

        return scheduler;
    }

    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        taskRegistrar.setScheduler(taskScheduler());

        taskRegistrar.addCronTask(context.getBean(LeaseTerminatingTask.class), serviceConfig.getCronExpression(ScheduledTask.TASK_LEASE_TERMINATE));

        taskRegistrar.addCronTask(context.getBean(LeaseExpiredTask.class), serviceConfig.getCronExpression(ScheduledTask.TASK_LEASE_EXPIRED));

        log.info("Registered {} scheduled task", taskRegistrar.getCronTaskList().size());
    }
}
