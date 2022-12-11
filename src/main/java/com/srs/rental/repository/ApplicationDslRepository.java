package com.srs.rental.repository;

import com.banvien.emarket.rental.GetRevenueAnalyticsRequest;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.srs.common.domain.Page;
import com.srs.common.util.PermissionUtil;
import com.srs.common.util.TimestampUtil;
import com.srs.proto.dto.GrpcPrincipal;
import com.srs.rental.ListApplicationRequest;
import com.srs.rental.ListLeasesRequest;
import com.srs.rental.common.Constant;
import com.srs.rental.entity.ApplicationEntity;
import com.srs.rental.entity.QApplicationEntity;
import com.srs.rental.entity.QMemberEntity;
import com.srs.rental.entity.QUserEntity;
import com.srs.rental.grpc.util.PageUtil;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Objects;

import static com.srs.common.Status.ACTIVE_VALUE;
import static com.srs.common.Status.INACTIVE_VALUE;
import static com.srs.rental.ApplicationType.NEW_STALL_APP_VALUE;
import static com.srs.rental.ApplicationType.RENEWAL_STALL_APP_VALUE;
import static com.srs.rental.LeaseStatus.FOR_TERMINATION_VALUE;
import static com.srs.rental.LeaseStatus.TERMINATED_VALUE;
import static com.srs.rental.WorkflowStatus.APPROVED_VALUE;
import static com.srs.rental.WorkflowStatus.CANCELLED_VALUE;

@Repository
@RequiredArgsConstructor
public class ApplicationDslRepository {
    private final QApplicationEntity application = QApplicationEntity.applicationEntity;
    private final QUserEntity owner = QUserEntity.userEntity;
    private final QMemberEntity member = QMemberEntity.memberEntity;
    private final JPAQueryFactory queryFactory;


    public Page<ApplicationEntity> findAllApplications(ListApplicationRequest request, GrpcPrincipal principal) {
        JPAQuery<?> baseQuery = queryFactory.from(application)
                .innerJoin(owner).on(application.owner.userId.eq(owner.userId));

        if (StringUtils.isNotBlank(request.getCode())) {
            baseQuery.where(application.code.containsIgnoreCase(request.getCode()));
        }

        if (StringUtils.isNotBlank(request.getFirstName())) {
            baseQuery.where(owner.firstName.containsIgnoreCase(request.getFirstName()));
        }

        if (StringUtils.isNotBlank(request.getLastName())) {
            baseQuery.where(owner.lastName.containsIgnoreCase(request.getLastName()));
        }

        baseQuery.where(application.status.notIn(
                CANCELLED_VALUE));

        this.addAssignmentFilter(principal, baseQuery);

        var pageRequest = PageUtil.normalizeRequest(request.getPageRequest(), Constant.APPLICATION_SORTS);
        var sortDirection = Order.valueOf(pageRequest.getDirection());

        JPAQuery<Long> countQuery = baseQuery.clone().select(application.count());
        JPAQuery<ApplicationEntity> selectQuery = baseQuery.clone().select(application)
                .limit(pageRequest.getSize())
                .offset((long) (pageRequest.getPage() - 1) * pageRequest.getSize())
                .orderBy(new OrderSpecifier<>(sortDirection, application.code));

        return Page.from(selectQuery.fetch(), countQuery.fetchFirst());
    }

    public Page<ApplicationEntity> findAllLeases(ListLeasesRequest request, GrpcPrincipal principal) {
        var pageRequest = PageUtil.normalizeRequest(request.getPageRequest(), Constant.LEASE_SORTS);

        JPAQuery<?> baseQuery = queryFactory.from(application)
                .innerJoin(owner).on(application.owner.userId.eq(owner.userId))
                .where(application.status.in(APPROVED_VALUE))
                .where(application.type.in(
                        NEW_STALL_APP_VALUE,
                        RENEWAL_STALL_APP_VALUE
                ));

        if (StringUtils.isNotBlank(request.getLeaseCode())) {
            baseQuery.where(application.leaseCode.containsIgnoreCase(request.getLeaseCode()));
        }

        if (StringUtils.isNotBlank(request.getFirstName())) {
            baseQuery.where(owner.firstName.containsIgnoreCase(request.getFirstName()));
        }

        if (StringUtils.isNotBlank(request.getLastName())) {
            baseQuery.where(owner.lastName.containsIgnoreCase(request.getLastName()));
        }

        if (request.getMarketCodesCount() > 0) {
            baseQuery.where(application.marketCode.in(request.getMarketCodesList()));
        }

        var endOfToday = TimestampUtil.endOfToday();

        if (request.getLeaseStatusCount() > 0) {
            var leaseStatusFilters = new ArrayList<BooleanExpression>();

            var inactiveCheck = new QApplicationEntity("other");

            if (request.getLeaseStatusValueList().contains(TERMINATED_VALUE)) {
                leaseStatusFilters.add(application.leaseStatus.eq(TERMINATED_VALUE));
            }

            if (request.getLeaseStatusValueList().contains(FOR_TERMINATION_VALUE)) {
                leaseStatusFilters.add(application.leaseStatus.eq(FOR_TERMINATION_VALUE));
            }

            if (request.getLeaseStatusValueList().contains(ACTIVE_VALUE)) {
                leaseStatusFilters.add(application.leaseStatus.eq(ACTIVE_VALUE));
            }

            baseQuery.where(Expressions.anyOf(leaseStatusFilters.toArray(new BooleanExpression[0])));
        } else {
            baseQuery.where(application.leaseStatus.in(
                    ACTIVE_VALUE,
                    FOR_TERMINATION_VALUE,
                    TERMINATED_VALUE,INACTIVE_VALUE));
        }

        addAssignmentFilter(principal, baseQuery);

        JPAQuery<Long> countQuery = baseQuery.clone().select(application.count());

        var direction = Order.valueOf(pageRequest.getDirection());

        JPAQuery<ApplicationEntity> selectQuery = baseQuery.clone().select(application)
                .limit(pageRequest.getSize())
                .offset((long) (pageRequest.getPage() - 1) * pageRequest.getSize())
                .orderBy(
                        pageRequest.getSort().equals("lastName")
                                ? new OrderSpecifier<>(direction, owner.lastName.lower())
                                : new OrderSpecifier<>(direction, owner.firstName.lower()),
                        new OrderSpecifier<>(direction, application.leaseCode)
                );

        return Page.from(selectQuery.fetch(), countQuery.fetchFirst());
    }

    private void addAssignmentFilter(GrpcPrincipal principal, JPAQuery<?> baseQuery) {
        // TODO: Should be modified later
        if (PermissionUtil.isPublicUser(principal.getRoles())) {
            baseQuery.where(application.createdBy.eq(principal.getUserId())
                    .or(owner.email.equalsIgnoreCase(principal.getEmail().trim())));
        }
    }

//    public double countAllForApplicationFeeRevenueAnalytics(GetRevenueAnalyticsRequest request) {
//
//        var query = queryFactory.select(application.paidInitialFee.sum())
//                .from(application)
//                .where(application.type.in(NEW_STALL_APP_VALUE));
//
//        query.where(application.approvedDate.year().lt(request.getYear()));
//
//        var applicationFee = Objects.requireNonNullElse(query.fetchFirst(), 0.0);
//
//        var securityQuery = queryFactory.select(application.paidTotalAmountDue.sum())
//                .from(application)
//                .where(application.orNumber.isNotNull())
//                .where(application.orNumber.isNotEmpty())
//                .where(application.type.in(NEW_STALL_APP_VALUE))
//                .where(application.stallType.in(StallType.STALL_TYPE_PERMANENT_VALUE));
//
//        securityQuery.where(application.securityBondAndStallRightsPaidDate.between(dateRange.getFrom(), dateRange.getTo()));
//
//        var securityFee = Objects.requireNonNullElse(securityQuery.fetchFirst(), 0.0);
//
//        return applicationFee + securityFee;
//    }

}
