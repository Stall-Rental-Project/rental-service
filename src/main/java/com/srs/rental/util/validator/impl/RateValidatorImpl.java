package com.srs.rental.util.validator.impl;

import com.srs.common.Error;
import com.srs.common.ErrorCode;
import com.srs.common.NoContentResponse;
import com.srs.common.Status;
import com.srs.market.MarketClass;
import com.srs.proto.dto.GrpcPrincipal;
import com.srs.rental.OtherRateDetail;
import com.srs.rental.RateType;
import com.srs.rental.UpsertRateRequest;
import com.srs.rental.repository.RateDslRepository;
import com.srs.rental.util.validator.BaseValidator;
import com.srs.rental.util.validator.RateValidator;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
public class RateValidatorImpl extends BaseValidator implements RateValidator {
    private final RateDslRepository rateDslRepository;

    @Override
    public NoContentResponse validateCreateRate(UpsertRateRequest request, GrpcPrincipal principal) {
        var error = Error.newBuilder();

        this.validateRateRequest(request, error);

        if (error.getDetailsCount() == 0 && request.getStatus().equals(Status.ACTIVE)) {
            if (rateDslRepository.existsActiveRateByType(request.getType(), request.getOtherRate().getDetail(), null)) {
                error.setCode(ErrorCode.RATE_ALREADY_EXISTS).putDetails("type", "Rate already exists");
            }
        }

        return asValidationResponse(error);
    }

    @Override
    public NoContentResponse validateUpdateRate(UpsertRateRequest request, GrpcPrincipal principal) {
        var error = Error.newBuilder();

        this.validateRateRequest(request, error);

        if (error.getDetailsCount() == 0 && request.getStatus().equals(Status.ACTIVE)) {
            if (rateDslRepository.existsActiveRateByType(request.getType(), request.getOtherRate().getDetail(), request.getRateCode())) {
                error.setCode(ErrorCode.RATE_ALREADY_EXISTS).putDetails("type", "Rate already exists");
            }
        }

        return asValidationResponse(error);
    }

    private void validateRateRequest(UpsertRateRequest request, Error.Builder error) {
        if (request.getStatus().equals(Status.UNRECOGNIZED)) {
            error.putDetails("status", "Invalid status");
        }

        if (request.getType().equals(RateType.UNRECOGNIZED)) {
            error.putDetails("type", "Invalid type");
        } else {
            switch (request.getType()) {
                case OTHER_RATES:
                    if (!request.hasOtherRate()) {
                        error.putDetails("other_rate", "Other rate is required");
                    } else if (request.getOtherRate().getDetail().equals(OtherRateDetail.UNRECOGNIZED)) {
                        error.putDetails("other_rate", "Invalid detail");
                    } else if (request.getOtherRate().getAmount() <= 0) {
                        error.putDetails("other_rate", "Invalid amount");
                    }
                    break;
                case STALL_RENTAL_RATE:
                    if (!request.hasRentalRate()) {
                        error.putDetails("rental_rate", "Rental rate is required");
                    } else if (request.getRentalRate().getClassRentalAmountsCount() == 0
                            || request.getRentalRate().getClassRentalAmountsList().stream()
                            .anyMatch(cs -> cs.getAmount() <= 0
                                    || cs.getClazz().equals(MarketClass.UNRECOGNIZED))) {
                        error.putDetails("rental_rate", "Invalid value");
                    }
                    break;
                case STALL_RIGHTS_RATE:
                    if (!request.hasRightsRate()) {
                        error.putDetails("rights_rate", "Right rate is required");
                    } else if (request.getRightsRate().getClassRightsAmountsCount() == 0
                            || request.getRightsRate().getClassRightsAmountsList().stream()
                            .anyMatch(ca -> ca.getAmount() <= 0
                                    || ca.getClazz().equals(MarketClass.UNRECOGNIZED))) {
                        error.putDetails("rights_rate", "Invalid value");
                    }
                    break;
                case STALL_SECURITY_BOND:
                    if (!request.hasSecurityBond()) {
                        error.putDetails("security_bond", "Security bond is required");
                    } else if (request.getSecurityBond().getRentalFee() <= 0
                            || request.getSecurityBond().getAmount() <= 0) {
                        error.putDetails("security_bond", "Invalid value");
                    }
                    break;
            }
        }
    }
}
