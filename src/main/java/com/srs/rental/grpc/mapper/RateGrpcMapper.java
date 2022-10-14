package com.srs.rental.grpc.mapper;

import com.srs.proto.mapper.BaseGrpcMapper;
import com.srs.rental.*;
import com.srs.rental.entity.RateEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@Component
@RequiredArgsConstructor
public class RateGrpcMapper {
    private static final String COMPONENT_SEP = "===";
    private static final String SUB_COMPONENT_SEP = "==";
    private static final String ELEMENT_SEP = "--";

    private static final String COMMA_SEP = ",";

    private static final String EQUAL_SIGN_SEP = "=";

    private OtherRate toGrpcOtherRate(String content) {
        String[] components = content.split(COMPONENT_SEP);

        int type = Integer.parseInt(components[0]);
        double amount = Double.parseDouble(components[1]);

        return OtherRate.newBuilder()
                .setDetailValue(type)
                .setAmount(amount)
                .build();
    }

    private StallSecurityBond toGrpcSecurityBond(String content) {
        String[] components = content.split(COMPONENT_SEP);

        double rentalFee = Double.parseDouble(components[0]);
        double amount = Double.parseDouble(components[1]);

        return StallSecurityBond.newBuilder()
                .setRentalFee(rentalFee)
                .setAmount(amount)
                .build();
    }

    private StallRightsRate nativeToGrpcRightsRate(String content) {
        String[] elements = content.split(ELEMENT_SEP);

        var classAmounts = new ArrayList<ClassAmountRate>();

        for (String element : elements) {
            String[] components = element.split(COMPONENT_SEP);

            int marketClass = Integer.parseInt(components[0]);
            double amount = Double.parseDouble(components[1]);

            classAmounts.add(ClassAmountRate.newBuilder()
                    .setClazzValue(marketClass)
                            .setAmount(amount)
                    .build());
        }

        return StallRightsRate.newBuilder()
                .addAllClassRightsAmounts(classAmounts)
                .build();
    }

    private StallRentalRate nativeToGrpcRentalRate(String content) {
        String[] elements = content.split(ELEMENT_SEP);

        var classAmounts = new ArrayList<ClassAmountRate>();

        for (String element : elements) {
            String[] components = element.split(COMPONENT_SEP);

            int marketClass = Integer.parseInt(components[0]);
            double amount = Double.parseDouble(components[1]);

            classAmounts.add(ClassAmountRate.newBuilder()
                    .setClazzValue(marketClass)
                    .setAmount(amount)
                    .build());
        }

        return StallRentalRate.newBuilder()
                .addAllClassRentalAmounts(classAmounts)
                .build();
    }

    public Rate.Builder entityToGrpcBuilder(RateEntity entity) {
        var builder = Rate.newBuilder()
                .setRateId(entity.getRateId().toString())
                .setRateCode(entity.getRateCode())
                .setStatusValue(entity.getStatus())
                .setTypeValue(entity.getType());

        if (entity.getType() == RateType.STALL_RENTAL_RATE_VALUE) {
            builder.setRentalRate(this.nativeToGrpcRentalRate(entity.getContent()));
        } else if (entity.getType() == RateType.STALL_RIGHTS_RATE_VALUE) {
            builder.setRightsRate(this.nativeToGrpcRightsRate(entity.getContent()));
        } else if (entity.getType() == RateType.STALL_SECURITY_BOND_VALUE) {
            builder.setSecurityBond(this.toGrpcSecurityBond(entity.getContent()));
        } else if (entity.getType() == RateType.OTHER_RATES_VALUE) {
            builder.setOtherRate(this.toGrpcOtherRate(entity.getContent()));
        } else {
            throw new IllegalStateException(
                    "Invalid rate type was given with value " + entity.getType());
        }

        return builder;
    }
}
