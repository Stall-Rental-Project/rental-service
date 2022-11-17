package com.srs.rental.grpc.service.impl;

import com.google.protobuf.Any;
import com.srs.common.FindByIdRequest;
import com.srs.common.NoContentResponse;
import com.srs.common.OnlyCodeResponse;
import com.srs.common.PageResponse;
import com.srs.common.exception.ObjectNotFoundException;
import com.srs.proto.dto.GrpcPrincipal;
import com.srs.rental.*;
import com.srs.rental.common.Constant;
import com.srs.rental.entity.ApplicationEntity;
import com.srs.rental.entity.RateEntity;
import com.srs.rental.grpc.mapper.RateGrpcMapper;
import com.srs.rental.grpc.service.RateGrpcService;
import com.srs.rental.grpc.util.PageUtil;
import com.srs.rental.repository.ApplicationRepository;
import com.srs.rental.repository.RateDslRepository;
import com.srs.rental.repository.RateRepository;
import com.srs.rental.util.RateUtil;
import com.srs.rental.util.validator.RateValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Objects;
import java.util.UUID;

@Service
@Log4j2
@RequiredArgsConstructor
public class RateGrpcServiceImpl implements RateGrpcService {
    private final RateRepository rateRepository;
    private final RateDslRepository rateDslRepository;
private final ApplicationRepository applicationRepository;
    private final RateGrpcMapper rateGrpcMapper;
    private final RateValidator rateValidator;

    private final RateUtil rateUtil;

    @Override
    public PageResponse listRates(ListRatesRequest request, GrpcPrincipal principal) {
        var pageData = rateDslRepository.findAll(request, principal);

        var rates = new ArrayList<Any>();


        for (var rate : pageData.getItems()) {
            var grpcRate = rateGrpcMapper.entityToGrpcBuilder(rate);

            rates.add(Any.pack(grpcRate.build()));
        }

        var pageRequest = PageUtil.normalizeRequest(request.getPageRequest(), Constant.RATE_SORTS);

        return PageResponse.newBuilder()
                .setSuccess(true)
                .setData(PageResponse.Data.newBuilder()
                        .setPage(pageRequest.getPage())
                        .setSize(pageRequest.getSize())
                        .setTotalElements(pageData.getTotal())
                        .setTotalPages(PageUtil.calcTotalPages(pageData.getTotal(), pageRequest.getSize()))
                        .addAllItems(rates)
                        .build())
                .build();
    }

    @Override
    public GetRateResponse getRate(GetRateRequest request, GrpcPrincipal principal) {

        var rateCode = UUID.fromString(request.getRateCode());
        var rate = rateRepository.findById(rateCode).orElseThrow(
                () -> new ObjectNotFoundException("Rate not found")
        );
        return GetRateResponse.newBuilder()
                .setSuccess(true)
                .setData(GetRateResponse.Data.newBuilder()
                        .setRate(rateGrpcMapper.entityToGrpcBuilder(rate))
                        .build())
                .build();
    }

    @Override
    @Transactional
    public OnlyCodeResponse createRate(UpsertRateRequest request, GrpcPrincipal principal) {
        var validationResult = rateValidator.validateCreateRate(request, principal);

        if (!validationResult.getSuccess()) {
            return OnlyCodeResponse.newBuilder()
                    .setSuccess(false)
                    .setError(validationResult.getError())
                    .build();
        }

        var rate = new RateEntity();
        var rateCode = rateUtil.generateCode();

        rate.setRateCode(rateCode);
        rate.setStatus(request.getStatusValue());
        rate.setType(request.getTypeValue());

        this.updateRateDetail(rate, request);

        rateRepository.save(rate);

        return OnlyCodeResponse.newBuilder()
                .setSuccess(true)
                .setData(OnlyCodeResponse.Data.newBuilder()
                        .setCode(rate.getRateCode())
                        .build())
                .build();
    }

    @Override
    public OnlyCodeResponse updateRate(UpsertRateRequest request, GrpcPrincipal principal) {
        var validationResult = rateValidator.validateUpdateRate(request, principal);

        if (!validationResult.getSuccess()) {
            return OnlyCodeResponse.newBuilder()
                    .setSuccess(false)
                    .setError(validationResult.getError())
                    .build();
        }

        var rateId = UUID.fromString(request.getRateId());
        var rate = rateRepository.findById(rateId).orElseThrow(
                () -> new ObjectNotFoundException("Rate not found")
        );

        this.updateRate(rate, request);
        rateRepository.save(rate);


        return OnlyCodeResponse.newBuilder()
                .setSuccess(true)
                .setData(OnlyCodeResponse.Data.newBuilder()
                        .setCode(rate.getRateCode())
                        .build())
                .build();
    }

    @Override
    @Transactional
    public NoContentResponse deleteRate(FindByIdRequest request, GrpcPrincipal principal) {
        var rateId = UUID.fromString(request.getId());
        var rate = rateRepository.findById(rateId).orElseThrow(
                () -> new ObjectNotFoundException("Rate not found")
        );

        rateRepository.delete(rate);
        return NoContentResponse.newBuilder()
                .setSuccess(true)
                .build();
    }

    private void updateRateDetail(RateEntity rate, UpsertRateRequest request) {
        if (request.getType().equals(RateType.OTHER_RATES)) {
            rate.setOtherRateType(request.getOtherRate().getDetailValue());
        }
        rate.setContent(rateUtil.grpcToNativeRateContent(request));
    }

    private boolean updateRate(RateEntity entity, UpsertRateRequest request) {
        boolean hasChanged = false;

        if (!Objects.equals(entity.getStatus(), request.getStatusValue())) {
            entity.setStatus(request.getStatusValue());
            hasChanged = true;
        }

        if (!Objects.equals(entity.getType(), request.getTypeValue())) {
            entity.setType(request.getTypeValue());
            this.updateRateDetail(entity, request);
            hasChanged = true;
        } else {
            if (request.getType().equals(RateType.OTHER_RATES) && !Objects.equals(entity.getOtherRateType(), request.getOtherRate().getDetailValue())) {
                entity.setOtherRateType(request.getOtherRate().getDetailValue());
                hasChanged = true;
            }

            var rateContent = rateUtil.grpcToNativeRateContent(request);
            if (!Objects.equals(entity.getContent(), rateContent)) {
                entity.setContent(rateContent);
                hasChanged = true;
            }
        }

        return hasChanged;
    }
    @Override
    public CalculateRateResponse calculateApplicationRate(CalculateRateRequest request, GrpcPrincipal principal) {
        double initialFee = 0, securityFee = 0, monthlyRate = 0, totalAmountDue = 0;

        ApplicationEntity application = null;

        if (StringUtils.isNotBlank(request.getApplicationId())) {
            var applicationId = UUID.fromString(request.getApplicationId());
            application = applicationRepository.findById(applicationId)
                    .orElseThrow(() -> new ObjectNotFoundException("Application not found"));
        }

        if (request.getIncludesList().contains(FeeType.NSA_INITIAL_FEE)) {
            if (application != null) {
                initialFee = application.getPaidInitialFee() > 0
                        ? application.getPaidInitialFee()
                        : rateUtil.getInitialRate(ApplicationType.forNumber(application.getType()));
            } else {
                initialFee = rateUtil.getInitialRate(request.getApplicationType());
            }
        }

        if (request.getIncludesList().contains(FeeType.NSA_MONTHLY_FEE)
                || request.getIncludesList().contains(FeeType.NSA_SECURITY_FEE)
                || request.getIncludesList().contains(FeeType.NSA_TOTAL_AMOUNT_DUE)) {
            monthlyRate = rateUtil.getMonthlyRate(request.getMarketClass(), request.getStallClass(), request.getStallArea());
        }

        if (request.getIncludesList().contains(FeeType.NSA_SECURITY_FEE)
                || request.getIncludesList().contains(FeeType.NSA_TOTAL_AMOUNT_DUE)) {
            if (application != null) {
                securityFee = application.getPaidSecurityFee() > 0
                        ? application.getPaidSecurityFee()
                        : rateUtil.getSecurityRate(monthlyRate, request.getStallType());
            } else {
                securityFee = rateUtil.getSecurityRate(monthlyRate, request.getStallType());
            }
        }

        if (request.getIncludesList().contains(FeeType.NSA_TOTAL_AMOUNT_DUE)) {
            if (application != null) {
                totalAmountDue = application.getPaidTotalAmountDue() > 0
                        ? application.getPaidTotalAmountDue()
                        : rateUtil.getTotalAmountDue(securityFee, request.getMarketClass(), request.getStallType());
            } else {
                totalAmountDue = rateUtil.getTotalAmountDue(securityFee, request.getMarketClass(), request.getStallType());
            }
        }

        return CalculateRateResponse.newBuilder()
                .setSuccess(true)
                .setData(CalculateRateResponse.Data.newBuilder()
                        .setInitialFee(initialFee)
                        .setMonthlyFee(monthlyRate)
                        .setSecurityFee(securityFee)
                        .setTotalAmountDue(totalAmountDue)
                        .build())
                .build();
    }
}
