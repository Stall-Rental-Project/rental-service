package com.srs.rental.common;

import com.srs.common.config.BaseServiceConfig;
import com.srs.rental.ScheduledTask;
import com.srs.rental.repository.config.RentalServiceConfigLoader;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;


@Component
@Log4j2
public class ServiceConfig extends BaseServiceConfig {
    private static final String LEASE_TERMINATE_CRON_EXPRESSION = "lease.terminate_cron_expression";
    private static final String LEASE_INACTIVATE_CRON_EXPRESSION = "lease.inactivate_cron_expression";

    private String leaseTerminateCronExpr;
    private String leaseInactivateCronExpr;

    protected ServiceConfig(RentalServiceConfigLoader configLoader) {
        super(configLoader, "rental_config");
    }

    @Override
    protected void parse() {
        log.info("Parsing {} service configs", configMap.size());
        log.info("=".repeat(10));
        for (var entry : this.configMap.entrySet()) {
            log.info("{}: {}", entry.getKey(), entry.getValue());
        }
        log.info("=".repeat(10));

        this.setLeaseTerminateCronExpr(StringUtils.isNotBlank(configMap.get(LEASE_TERMINATE_CRON_EXPRESSION))
                ? configMap.get(LEASE_TERMINATE_CRON_EXPRESSION)
                : "0 0 0 * * *");
        this.setLeaseInactivateCronExpr(StringUtils.isNotBlank(configMap.get(LEASE_INACTIVATE_CRON_EXPRESSION))
                ? configMap.get(LEASE_INACTIVATE_CRON_EXPRESSION)
                : "0 1 0 * * *");

    }

    public String getCronExpression(ScheduledTask task) {
        switch (task) {
            case TASK_LEASE_TERMINATE:
                return this.leaseTerminateCronExpr;
            case TASK_LEASE_EXPIRED:
                return this.leaseInactivateCronExpr;
            default:
                throw new IllegalArgumentException("Unknown scheduled task " + task);
        }
    }

    public String getLeaseTerminateCronExpr() {
        return leaseTerminateCronExpr;
    }

    public void setLeaseTerminateCronExpr(String leaseTerminateCronExpr) {
        this.leaseTerminateCronExpr = leaseTerminateCronExpr;
    }

    public String getLeaseInactivateCronExpr() {
        return leaseInactivateCronExpr;
    }

    public void setLeaseInactivateCronExpr(String leaseInactivateCronExpr) {
        this.leaseInactivateCronExpr = leaseInactivateCronExpr;
    }
}
