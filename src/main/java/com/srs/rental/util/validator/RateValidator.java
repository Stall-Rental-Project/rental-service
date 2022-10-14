package com.srs.rental.util.validator;

import com.market.common.NoContentResponse;
import com.srs.proto.dto.GrpcPrincipal;
import com.srs.rental.UpsertRateRequest;

/**
 * @author duynt on 3/2/22
 */
public interface RateValidator {
    NoContentResponse validateCreateRate(UpsertRateRequest request, GrpcPrincipal principal);

    NoContentResponse validateUpdateRate(UpsertRateRequest request, GrpcPrincipal principal);
}
