package com.srs.rental.util.validator;

import com.market.common.Error;
import com.market.common.ErrorCode;
import com.market.common.NoContentResponse;


public abstract class BaseValidator {
    protected NoContentResponse asValidationResponse(Error.Builder error) {
        var result = NoContentResponse.newBuilder();

        if (error.getDetailsCount() > 0) {
            if (error.getCode() == null || error.getCode().equals(ErrorCode.UNRECOGNIZED)) {
                error.setCode(ErrorCode.BAD_REQUEST);
            }
            error.setMessage("Failed to validate request");
            result.setSuccess(false).setError(error.build());
        } else {
            result.setSuccess(true);
        }

        return result.build();
    }
}
