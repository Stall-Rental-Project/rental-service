package com.srs.rental.util;

import com.fasterxml.jackson.core.io.NumberInput;
import com.srs.common.util.NumericUtil;
import com.srs.market.MarketClass;
import com.srs.market.StallClass;
import com.srs.market.StallType;
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

    public double getInitialRate(ApplicationType applicationType) {
        var rate = rateDslRepository.findRateByApplicationType(applicationType).orElse(null);

        if (rate == null) {
            log.warn("Initial rate not found. It might be inactive or unapproved");
            return 0;
        }

        var grpcRate = rateGrpcMapper.entityToGrpcResponse(rate);

        return NumericUtil.sanitize(grpcRate.getOtherRate().getAmount());
    }
    public double getSecurityRate(double monthlyRate, StallType stallType) {
        if (!StallType.STALL_TYPE_PERMANENT.equals(stallType)) {
            return 0;
        }

        var rate = rateRepository.findInUseRateByType(RateType.STALL_SECURITY_BOND_VALUE).orElse(null);

        if (rate == null) {
            log.warn("Stall security bond not found. It might be inactive or unapproved");
            return 0;
        }

        var grpcRate = rateGrpcMapper.entityToGrpcResponse(rate);

        double calculatedRate = monthlyRate * grpcRate.getSecurityBond().getRentalFee();
        double minimumRate = grpcRate.getSecurityBond().getAmount();

        return NumericUtil.sanitize(Math.max(calculatedRate, minimumRate));
    }
    public double getTotalAmountDue(double securityRate, MarketClass marketClass, StallType stallType) {
        if (!StallType.STALL_TYPE_PERMANENT.equals(stallType)) {
            return 0;
        }

        var rate = rateRepository.findInUseRateByType(RateType.STALL_RIGHTS_RATE_VALUE).orElse(null);

        if (rate == null) {
            log.warn("Stall security bond not found. It might be inactive or unapproved");
            return securityRate;
        }

        var grpcRate = rateGrpcMapper.entityToGrpcResponse(rate);

        var rightsRate = grpcRate.getRightsRate().getClassRightsAmountsList().stream()
                .filter(item -> item.getClazz().equals(marketClass))
                .map(ClassAmountRate::getAmount)
                .mapToDouble(amount -> amount)
                .sum();

        return NumericUtil.sanitize(securityRate + rightsRate);
    }

    public double getMonthlyRate(MarketClass marketClass, StallClass stallClass, double stallArea) {
        var stallRentalRate = this.getStallRentalRate(marketClass, stallClass);
        return this.getMonthlyRate(stallRentalRate, stallArea);
    }

    public double getMonthlyRate(double stallRentalRate, double stallArea) {
        return NumericUtil.sanitize(stallRentalRate * stallArea);
    }

    public double getStallRentalRate(MarketClass marketClass, StallClass stallClass) {
        var rate = rateRepository.findInUseRateByType(RateType.STALL_RENTAL_RATE_VALUE).orElse(null);

        if (rate == null) {
            log.warn("Stall rental feed not found. It might be inactive or unapproved");
            return 0;
        }

        var grpcRate = rateGrpcMapper.entityToGrpcResponse(rate);

        double stallLocationAddon;
        switch (stallClass) {
            case STALL_CLASS_FRONT:
                stallLocationAddon = 15 / 100;
                break;
            case STALL_CLASS_INSIDE_CORNER:
                stallLocationAddon = 10 / 100;
                break;
            case STALL_CLASS_FRONT_CORNER:
                stallLocationAddon = 25 / 100;
                break;
            default:
                log.warn("Stall addon not found for location " + stallClass);
                stallLocationAddon = 0;
        }

        double stallRate = grpcRate.getRentalRate().getClassRentalAmountsList().stream()
                .filter(item -> item.getClazz().equals(marketClass))
                .map(ClassAmountRate::getAmount)
                .mapToDouble(amount -> amount)
                .sum();

        return NumericUtil.sanitize(stallRate * (1 + stallLocationAddon));
    }


}
