package com.srs.rental.util;

import com.srs.rental.*;
import com.srs.rental.grpc.mapper.RateGrpcMapper;
import com.srs.rental.repository.RateDslRepository;
import com.srs.rental.repository.RateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

import java.util.UUID;


@Component
@RequiredArgsConstructor
@Log4j2
public class RateUtil {
    public static final String COMPONENT_SEP = "===";
    public static final String SUB_COMPONENT_SEP = "==";
    public static final String ELEMENT_SEP = "--";
    private final RateRepository rateRepository;

    private final RateDslRepository rateDslRepository;

    private final RateGrpcMapper rateGrpcMapper;

    public String grpcToNativeRateContent(UpsertRateRequest request) {
        switch (request.getType()) {
            case OTHER_RATES:
                return this.grpcToNativeOtherRate(request.getOtherRate());
            case STALL_RENTAL_RATE:
                return this.grpcToNativeRentalRate(request.getRentalRate());
            case STALL_RIGHTS_RATE:
                return this.grpcToNativeRightsRate(request.getRightsRate());
            case STALL_SECURITY_BOND:
                return this.grpcToNativeSecurityBond(request.getSecurityBond());
            default:
                throw new IllegalArgumentException("Invalid rate type was given with value " + request.getTypeValue());
        }
    }
    private String grpcToNativeOtherRate(OtherRate grpc) {
        return String.format("%d%s%f", grpc.getDetailValue(), COMPONENT_SEP, grpc.getAmount());
    }
    public String generateCode() {
        return UUID.randomUUID().toString();
    }

    private String grpcToNativeRentalRate(StallRentalRate grpc) {
        var classRentals = new StringBuilder();

        for (var classRental : grpc.getClassRentalAmountsList()) {
            if (classRentals.length() > 0) {
                classRentals.append(ELEMENT_SEP);
            }

            classRentals.append(String.format("%d%s%f", classRental.getClazzValue(), COMPONENT_SEP, classRental.getAmount()));
        }

        return classRentals.toString();
    }
    private String grpcToNativeRightsRate(StallRightsRate grpc) {
        var content = new StringBuilder();

        for (var classAmount : grpc.getClassRightsAmountsList()) {
            if (content.length() > 0) {
                content.append(ELEMENT_SEP);
            }

            content.append(String.format("%d%s%f", classAmount.getClazzValue(), COMPONENT_SEP, classAmount.getAmount()));
        }

        return content.toString();
    }

    private String grpcToNativeSecurityBond(StallSecurityBond grpc) {
        return String.format("%f%s%f", grpc.getRentalFee(), COMPONENT_SEP, grpc.getAmount());
    }

}
