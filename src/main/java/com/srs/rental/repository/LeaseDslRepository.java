package com.srs.rental.repository;

import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.srs.common.util.TimestampUtil;
import com.srs.rental.entity.ApplicationEntity;
import com.srs.rental.entity.QApplicationEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import java.util.List;

import static com.srs.rental.ApplicationType.NEW_STALL_APP_VALUE;
import static com.srs.rental.LeaseStatus.INACTIVE_VALUE;
import static com.srs.rental.LeaseStatus.TERMINATED_VALUE;
import static com.srs.rental.WorkflowStatus.APPROVED_VALUE;


@Repository
@RequiredArgsConstructor
public class LeaseDslRepository {
    private final QApplicationEntity lease = QApplicationEntity.applicationEntity;

    private final JPAQueryFactory queryFactory;


    public List<ApplicationEntity> findAllToInactivate() {
        // We don't need to proceed on already terminated and/or inactive leases
        var now = TimestampUtil.now();
        var other = new QApplicationEntity("other");

        JPAQuery<ApplicationEntity> query = queryFactory.select(lease)
                .from(lease)
                .where(lease.leaseStatus.notIn(INACTIVE_VALUE, TERMINATED_VALUE))
                .where(lease.leaseEndDate.loe(now))
                .where(lease.status.in(APPROVED_VALUE))
                .where(lease.type.in(NEW_STALL_APP_VALUE));

        return query.fetch();
    }


}
