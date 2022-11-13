package com.srs.rental.grpc.mapper;

import com.srs.common.util.TimestampUtil;
import com.srs.proto.dto.GrpcPrincipal;
import com.srs.rental.CreateLeaseTerminationRequest;
import com.srs.rental.LeaseTermination;
import com.srs.rental.entity.LeaseTerminationEntity;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.UUID;

import static com.srs.rental.TerminationStatus.T_PENDING_VALUE;
import static java.util.Objects.requireNonNullElse;


@Component
@RequiredArgsConstructor
public class LeaseTerminationGrpcMapper {

    public LeaseTermination entityToGrpcResponse(LeaseTerminationEntity entity) {
        return LeaseTermination.newBuilder()
                .setTerminationId(entity.getTerminationId().toString())
                .setApplicationId(entity.getApplicationId().toString())
                .setReason(requireNonNullElse(entity.getReason(), ""))
                .setAccepted(entity.isAccepted())
                .setEndDate(TimestampUtil.stringifyDatetime(entity.getEndDate(), false))
                .setCreatedBy(entity.getCreatedBy())
                .setStatusValue(entity.getStatus())
                .build();
    }

    public LeaseTerminationEntity grpcRequestToEntity(CreateLeaseTerminationRequest request,
                                                      GrpcPrincipal principal) {
        var entity = new LeaseTerminationEntity();

        entity.setApplicationId(UUID.fromString(request.getApplicationId()));
        entity.setReason(request.getReason());
        entity.setEndDate(StringUtils.isNotBlank(request.getEndDate())
                ? TimestampUtil.parseDatetimeString(request.getEndDate())
                .withOffsetSameInstant(TimestampUtil.DEFAULT_OFFSET)
                : TimestampUtil.now());
        entity.setCreatedBy(principal.getUserId().toString());
        entity.setAccepted(true);
        entity.setStatus(T_PENDING_VALUE);

        return entity;
    }
}
