package com.srs.rental.util.validator;

import com.srs.common.NoContentResponse;
import com.srs.proto.dto.GrpcPrincipal;
import com.srs.rental.UpsertRateRequest;


public interface RateValidator {
    NoContentResponse validateCreateRate(UpsertRateRequest request, GrpcPrincipal principal);

    NoContentResponse validateUpdateRate(UpsertRateRequest request, GrpcPrincipal principal);
}
