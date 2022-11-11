package com.srs.rental.util.validator.impl;

import com.srs.common.Error;
import com.srs.common.ErrorCode;
import com.srs.common.NoContentResponse;
import com.srs.rental.Application;
import com.srs.rental.CheckExistApplicationRequest;
import com.srs.rental.SubmitApplicationRequest;
import com.srs.rental.util.validator.ApplicationValidator;
import com.srs.rental.util.validator.BaseValidator;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;

@Component
public class ApplicationValidatorImpl extends BaseValidator implements ApplicationValidator {
    @Override
    public NoContentResponse validateSubmitApplication(SubmitApplicationRequest request) {
        if (StringUtils.isBlank(request.getMarketCode())
                || StringUtils.isBlank(request.getStallCode())
                || StringUtils.isBlank(request.getFloorCode())) {
            return NoContentResponse.newBuilder()
                    .setSuccess(false)
                    .setError(Error.newBuilder()
                            .setCode(ErrorCode.BAD_REQUEST)
                            .setMessage("Market Code, Floor Code and Stall Code must be not blank")
                            .build())
                    .build();
        }

        var errors = new LinkedHashMap<String, String>();

        if (request.getMarketTypeValue() < 1) {
            errors.put("market_type", "Invalid value: " + request.getMarketTypeValue());
        }

        if (request.getMarketTypeValue() == 1 && request.getMarketClassValue() < 1) {
            errors.put("market_class", "Invalid value: " + request.getMarketClassValue());
        }

        if (request.getStallTypeValue() < 1) {
            errors.put("stall_type", "Invalid value: " + request.getStallTypeValue());
        }

        if (request.getStallClassValue() < 1) {
            errors.put("stall_class", "Invalid value: " + request.getStallClassValue());
        }

        if (errors.size() > 0) {
            return NoContentResponse.newBuilder()
                    .setSuccess(false)
                    .setError(Error.newBuilder()
                            .setCode(ErrorCode.BAD_REQUEST)
                            .setMessage("Failed to validate")
                            .putAllDetails(errors)
                            .build())
                    .build();
        }

        return NoContentResponse.newBuilder()
                .setSuccess(true)
                .build();
    }

    @Override
    public NoContentResponse validateSubmitApplication(Application request) {
        if (StringUtils.isBlank(request.getMarketCode())
                || StringUtils.isBlank(request.getStallCode())
                || StringUtils.isBlank(request.getFloorCode())) {
            return NoContentResponse.newBuilder()
                    .setSuccess(false)
                    .setError(Error.newBuilder()
                            .setCode(ErrorCode.BAD_REQUEST)
                            .setMessage("Market Code, Floor Code and Stall Code must be not blank")
                            .build())
                    .build();
        }

        var errors = new LinkedHashMap<String, String>();

        if (request.getMarketTypeValue() < 1) {
            errors.put("market_type", "Invalid value: " + request.getMarketTypeValue());
        }

        if (request.getMarketTypeValue() == 1 && request.getMarketClassValue() < 1) {
            errors.put("market_class", "Invalid value: " + request.getMarketClassValue());
        }

        if (request.getStallTypeValue() < 1) {
            errors.put("stall_type", "Invalid value: " + request.getStallTypeValue());
        }

        if (request.getStallClassValue() < 1) {
            errors.put("stall_class", "Invalid value: " + request.getStallClassValue());
        }

        if (errors.size() > 0) {
            return NoContentResponse.newBuilder()
                    .setSuccess(false)
                    .setError(Error.newBuilder()
                            .setCode(ErrorCode.BAD_REQUEST)
                            .setMessage("Failed to validate")
                            .putAllDetails(errors)
                            .build())
                    .build();
        }

        return NoContentResponse.newBuilder()
                .setSuccess(true)
                .build();
    }

    @Override
    public NoContentResponse validateCheckExistApplication(CheckExistApplicationRequest request) {
        if (StringUtils.isBlank(request.getMarketCode())) {
            return NoContentResponse.newBuilder()
                    .setSuccess(false)
                    .setError(Error.newBuilder()
                            .setCode(ErrorCode.BAD_REQUEST)
                            .setMessage("At least the Market Code must be given")
                            .build())
                    .build();
        } else {
            return NoContentResponse.newBuilder()
                    .setSuccess(true)
                    .build();
        }
    }

}
