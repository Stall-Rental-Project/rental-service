package com.srs.rental.repository;

import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.srs.common.domain.Page;
import com.srs.common.util.PermissionUtil;
import com.srs.proto.dto.GrpcPrincipal;
import com.srs.rental.ListApplicationRequest;
import com.srs.rental.common.Constant;
import com.srs.rental.entity.ApplicationEntity;
import com.srs.rental.entity.QApplicationEntity;
import com.srs.rental.entity.QMemberEntity;
import com.srs.rental.entity.QUserEntity;
import com.srs.rental.grpc.util.PageUtil;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

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

    private void addAssignmentFilter(GrpcPrincipal principal, JPAQuery<?> baseQuery) {
        // TODO: Should be modified later
        if (PermissionUtil.isPublicUser(principal.getRoles())) {
            baseQuery.where(application.createdBy.eq(principal.getUserId())
                    .or(owner.email.equalsIgnoreCase(principal.getEmail().trim())));
        } else {

            if (principal.getMarketCodes() != null && principal.getMarketCodes().stream().anyMatch(StringUtils::isNotBlank)) {
                baseQuery.where(application.marketCode.in(principal.getMarketCodes()));
            }
        }
    }

}
