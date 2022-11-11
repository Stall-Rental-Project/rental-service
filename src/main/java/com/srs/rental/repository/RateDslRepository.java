package com.srs.rental.repository;

import com.srs.common.Status;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.srs.common.domain.Page;
import com.srs.proto.dto.GrpcPrincipal;
import com.srs.rental.ApplicationType;
import com.srs.rental.ListRatesRequest;
import com.srs.rental.OtherRateDetail;
import com.srs.rental.RateType;
import com.srs.rental.common.Constant;
import com.srs.rental.entity.QRateEntity;
import com.srs.rental.entity.RateEntity;
import com.srs.rental.grpc.util.PageUtil;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Repository;

import java.util.Optional;

import static com.srs.rental.OtherRateDetail.*;
import static com.srs.rental.RateType.*;

@Repository
@RequiredArgsConstructor
public class RateDslRepository {
    private final QRateEntity rate = QRateEntity.rateEntity;
    private final JPAQueryFactory queryFactory;

    public Page<RateEntity> findAll(ListRatesRequest request, GrpcPrincipal principal) {
        var pageRequest = PageUtil.normalizeRequest(request.getPageRequest(), Constant.RATE_SORTS);

        JPAQuery<?> baseQuery = queryFactory.from(rate);

        if (request.getTypesCount() > 0) {
            baseQuery.where(rate.type.in(request.getTypesValueList()));
        }

        if (request.getOtherRateDetailsCount() > 0) {
            baseQuery.where(rate.type.eq(OTHER_RATES_VALUE))
                    .where(rate.otherRateType.in(request.getOtherRateDetailsValueList()));
        }

        var sortDirection = Order.valueOf(pageRequest.getDirection());

        JPAQuery<Long> countQuery = baseQuery.clone().select(rate.count());
        JPAQuery<RateEntity> selectQuery = baseQuery.clone().select(rate)
                .limit(pageRequest.getSize())
                .offset((long) (pageRequest.getPage() - 1) * pageRequest.getSize());

        switch (pageRequest.getSort()) {
            case "code":
                selectQuery.orderBy(new OrderSpecifier<>(sortDirection, rate.rateCode));
                break;
            case "type":
                selectQuery.orderBy(new OrderSpecifier<>(sortDirection, new CaseBuilder()
                                .when(rate.type.eq(OTHER_RATES_VALUE)).then(1)
                                .when(rate.type.eq(STALL_RENTAL_RATE_VALUE)).then(2)
                                .when(rate.type.eq(STALL_RIGHTS_RATE_VALUE)).then(3)
                                .when(rate.type.eq(STALL_SECURITY_BOND_VALUE)).then(4)
                                .otherwise(0)
                        )
                );
                break;
            case "detail":
                selectQuery.orderBy(new OrderSpecifier<>(sortDirection, new CaseBuilder()
                                .when(rate.otherRateType.eq(NEW_STALL_APPLICATION_FEE_VALUE)).then(1)
                                .when(rate.otherRateType.eq(RENEWAL_STALL_APPLICATION_FEE_VALUE)).then(2)
                                .when(rate.otherRateType.eq(REPAIR_PERMIT_FEE_VALUE)).then(3)
                                .when(rate.otherRateType.eq(TRANSFER_FEE_VALUE)).then(4)
                                .when(rate.otherRateType.eq(TRANSFER_STALL_APPLICATION_FEE_VALUE)).then(5)
                                .otherwise(6)
                        )
                );
                break;
        }

        return Page.from(selectQuery.fetch(), countQuery.fetchFirst());
    }

    public boolean existsActiveRateByType(RateType type, OtherRateDetail otherType,
                                          String excludedCode) {
        JPAQuery<Long> query = queryFactory.select(rate.count())
                .from(rate)
                .where(rate.type.eq(type.getNumber()))
                .where(rate.status.eq(Status.ACTIVE_VALUE));

        if (type.equals(OTHER_RATES)) {
            query.where(rate.otherRateType.eq(otherType.getNumber()));
        }

        if (StringUtils.isNotBlank(excludedCode)) {
            query.where(rate.rateCode.ne(excludedCode));
        }

        return query.fetchFirst() > 0;
    }

    public Optional<RateEntity> findRateByApplicationType(ApplicationType type) {
        JPAQuery<RateEntity> query = queryFactory.select(rate)
                .from(rate)
                .where(rate.status.eq(Status.ACTIVE_VALUE));
        switch (type) {
            case NEW_STALL_APP:
                query.where(rate.type.eq(OTHER_RATES_VALUE))
                        .where(rate.otherRateType.eq(NEW_STALL_APPLICATION_FEE_VALUE));
                break;
            case RENEWAL_STALL_APP:
                query.where(rate.type.eq(OTHER_RATES_VALUE))
                        .where(rate.otherRateType.eq(RENEWAL_STALL_APPLICATION_FEE_VALUE));
                break;
            default:
                throw new UnsupportedOperationException(
                        "Application of type " + type + " currently not supported");
        }

        return Optional.ofNullable(query.fetchFirst());
    }

}
