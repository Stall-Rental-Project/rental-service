package com.srs.rental.repository;

import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.srs.rental.entity.LeaseTerminationEntity;
import com.srs.rental.entity.QLeaseTerminationEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

import static com.srs.rental.TerminationStatus.T_CANCELLED_VALUE;
import static com.srs.rental.TerminationStatus.T_CLOSED_VALUE;

@Repository
@RequiredArgsConstructor
public class LeaseTerminationDslRepository {
    private final QLeaseTerminationEntity leaseTermination = QLeaseTerminationEntity.leaseTerminationEntity;

    private final JPAQueryFactory queryFactory;

    public Optional<LeaseTerminationEntity> findLatestTerminationByApplicationId(UUID applicationId) {
        JPAQuery<LeaseTerminationEntity> query = queryFactory.select(leaseTermination)
                .from(leaseTermination)
                .where(leaseTermination.applicationId.eq(applicationId))
                .where(leaseTermination.status.notIn(T_CLOSED_VALUE, T_CANCELLED_VALUE))
                .orderBy(new OrderSpecifier<>(Order.DESC, leaseTermination.createdAt))
                .limit(1);

        return Optional.ofNullable(query.fetchFirst());
    }
}
