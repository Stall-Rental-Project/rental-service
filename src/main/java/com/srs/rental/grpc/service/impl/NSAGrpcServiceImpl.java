package com.srs.rental.grpc.service.impl;

import com.srs.common.Error;
import com.srs.common.*;
import com.srs.common.exception.AccessDeniedException;
import com.srs.common.exception.ObjectNotFoundException;
import com.srs.common.util.PermissionUtil;
import com.srs.common.util.TimestampUtil;
import com.srs.market.MarketClass;
import com.srs.market.StallClass;
import com.srs.market.StallType;
import com.srs.proto.dto.GrpcPrincipal;
import com.srs.rental.*;
import com.srs.rental.entity.ApplicationEntity;
import com.srs.rental.grpc.mapper.ApplicationGrpcMapper;
import com.srs.rental.grpc.service.NSAGrpcService;
import com.srs.rental.kafka.producer.LeaseKafkaProducer;
import com.srs.rental.repository.ApplicationRepository;
import com.srs.rental.repository.MemberRepository;
import com.srs.rental.repository.NSARepository;
import com.srs.rental.repository.UserRepository;
import com.srs.rental.repository.sequence.CodeGeneratorRepository;
import com.srs.rental.util.LeaseUtil;
import com.srs.rental.util.RateUtil;
import com.srs.rental.util.validator.ApplicationValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static com.srs.rental.WorkflowStatus.*;

@Service
@Log4j2
@RequiredArgsConstructor
public class NSAGrpcServiceImpl implements NSAGrpcService {
    private final ApplicationRepository applicationRepository;
    private final NSARepository nsaRepository;
    private final MemberRepository memberRepository;
    private final UserRepository userRepository;
    private final ApplicationValidator applicationValidator;
    private final TransactionTemplate transactionTemplate;
    private final CodeGeneratorRepository codeGeneratorRepository;
    private final ApplicationGrpcMapper applicationGrpcMapper;
    private final LeaseUtil leaseUtil;
    private final RateUtil rateUtil;

    private final LeaseKafkaProducer leaseKafkaProducer;

    @Override
    public GetApplicationResponse submitApplication(SubmitApplicationRequest request, GrpcPrincipal principal) {
        var validateResult = applicationValidator.validateSubmitApplication(request);

        if (validateResult.hasError()) {
            return GetApplicationResponse.newBuilder()
                    .setSuccess(false)
                    .setError(validateResult.getError())
                    .build();
        }

        var applicationId = this.storeNSA(request, principal);

        var application = nsaRepository.findById(applicationId)
                .orElseThrow(() -> new IllegalStateException("Application not found"));

        var grpcApplication = applicationGrpcMapper.entityToGrpcResponse(application, false);


        return GetApplicationResponse.newBuilder()
                .setSuccess(true)
                .setData(GetApplicationResponse.Data.newBuilder()
                        .setApplication(grpcApplication)
                        .build())
                .build();
    }

    @Override
    @Transactional
    public NoContentResponse submitApplicationDocs(SubmitApplicationDocsRequest request, GrpcPrincipal principal) {
        var applicationId = UUID.fromString(request.getApplicationId());
        var application = nsaRepository.findById(applicationId)
                .orElseThrow(() -> new ObjectNotFoundException("Application not found"));

//        if (PermissionUtil.isPublicUser(principal.getRoles()) &&
//                !List.of(NEW_VALUE, IN_PROGRESS_VALUE).contains(application.getStatus())) {
//            throw new AccessDeniedException("Application is being processed");
//        }

        if (!request.getDraft()) {
            var now = TimestampUtil.now();
            application.setRemindedPaymentDate(now.getDayOfMonth() >= 20 ? now.plusMonths(1).withDayOfMonth(20) : now.withDayOfMonth(20));
        }

        this.updateApplicationDocs(application, request);

        application.setStatus(request.getDraft() ? IN_PROGRESS_VALUE : PAYMENT_INFO_REQUESTED_VALUE);

        nsaRepository.save(application);

        return NoContentResponse.newBuilder()
                .setSuccess(true)
                .build();
    }

    @Override
    public GetApplicationResponse getApplication(FindByIdRequest request, GrpcPrincipal principal) {
        var applicationId = UUID.fromString(request.getId());
        var application = nsaRepository.findOneById(applicationId)
                .orElseThrow(() -> new ObjectNotFoundException("Application not found"));

        var grpcApplication = applicationGrpcMapper.buildGrpcApplicationResponse(application);

        return GetApplicationResponse.newBuilder()
                .setSuccess(true)
                .setData(GetApplicationResponse.Data.newBuilder()
                        .setApplication(grpcApplication.build())
                        .build())
                .build();
    }

    @Override
    @Transactional
    public GetApplicationResponse updateApplication(SubmitApplicationRequest request, GrpcPrincipal principal) {
        try {
            var applicationId = UUID.fromString(request.getApplicationId());
            var application = nsaRepository.findOneById(applicationId)
                    .orElseThrow(() -> new ObjectNotFoundException("Application not found"));

            if (PermissionUtil.isPublicUser(principal.getRoles()) &&
                    !List.of(NEW_VALUE, IN_PROGRESS_VALUE).contains(application.getStatus())) {
                throw new AccessDeniedException("Application is being processed");
            }

            this.updateNSA(request, application);

            var grpcApplication = applicationGrpcMapper.buildGrpcApplicationResponse(application);

            // This function does a lot of updates in batch, so we simply cannot check if something has been changed or not,
            // Here, we proposed a heuristic that "Capturing history only when the current user is who are created this application"
            // This assumption should eliminate number of log items significantly

            return GetApplicationResponse.newBuilder()
                    .setSuccess(true)
                    .setData(GetApplicationResponse.Data.newBuilder()
                            .setApplication(grpcApplication.build())
                            .build())
                    .build();
        } catch (Exception e) {
            return GetApplicationResponse.newBuilder()
                    .setSuccess(false)
                    .setError(Error.newBuilder()
                            .setCode(ErrorCode.BAD_REQUEST)
                            .setMessage(e.getMessage())
                            .build())
                    .build();
        }
    }

    @Override
    public BooleanResponse checkExistApplication(CheckExistApplicationRequest request) {
        var builder = BooleanResponse.newBuilder();
        var validateResult = applicationValidator.validateCheckExistApplication(request);
        if (!validateResult.getSuccess()) {
            builder.setErrorResponse(ErrorResponse.newBuilder()
                    .setErrorCode(validateResult.getError().getCode())
                    .setErrorDescription(validateResult.getError().getMessage())
                    .build());
        } else {
            boolean result;
            if (StringUtils.isNotBlank(request.getStallCode())) {
                result = applicationRepository.existsByMarketCodeAndFloorCodeAndStallCode(request.getMarketCode(), request.getFloorCode(), request.getStallCode());
            } else if (StringUtils.isNotBlank(request.getFloorCode())) {
                result = applicationRepository.existsByMarketCodeAndFloorCode(request.getMarketCode(), request.getFloorCode());
            } else {
                result = applicationRepository.existsByMarketCode(request.getMarketCode());
            }
            builder.setSuccessResponse(BooleanSuccessResponse.newBuilder()
                    .setResult(result)
                    .build());
        }
        return builder.build();
    }

    @Override
    @Transactional
    public NoContentResponse submitPayment(SubmitApplicationPaymentRequest request, GrpcPrincipal principal) {
        var acceptStatus = List.of(NEW_VALUE, IN_PROGRESS_VALUE, PAYMENT_INFO_REQUESTED_VALUE);

        var nsaId = UUID.fromString(request.getApplicationId());
        var application = nsaRepository.findOneById(nsaId)
                .orElseThrow(() -> new ObjectNotFoundException("Application not found"));

        if (PermissionUtil.isPublicUser(principal.getRoles()) &&
                !acceptStatus.contains(application.getStatus())) {
            throw new AccessDeniedException("Application is being processed");
        }

        this.updateApplicationPayment(application, request);

        if (StringUtils.isBlank(application.getCreatedBy().toString()) && PermissionUtil.isPublicUser(principal.getRoles())) {
            application.setCreatedBy(principal.getUserId());
        }

        if (!request.getDraft()) {
            application.setStatus(FOR_PAYMENT_VERIFICATION_VALUE);
        } else {
            application.setStatus(IN_PROGRESS_VALUE);
        }

        var applicationType = ApplicationType.forNumber(application.getType());
        var marketClass = MarketClass.forNumber(application.getMarketClass());
        var stallClass = StallClass.forNumber(application.getStallClass());
        var stallArea = application.getStallArea();
        var stallType = StallType.forNumber(application.getStallType());

        // This code block appears here just for backward-compatible purpose.
        // When migrating from Phase 1A to 2A, some applications might have passed the step 3 without properly initialFee set
        if (application.getPaidInitialFee() == 0) {
            var initialFee = rateUtil.getInitialRate(applicationType);
            application.setPaidInitialFee(initialFee);
        }

        var monthlyFee = rateUtil.getMonthlyRate(marketClass, stallClass, stallArea);
        var securityFee = rateUtil.getSecurityRate(monthlyFee, stallType);
        var totalAmountDue = rateUtil.getTotalAmountDue(securityFee, marketClass, stallType);
        application.setPaidSecurityFee(securityFee);
        application.setPaidTotalAmountDue(totalAmountDue);
        application.setPaymentMethod(request.getPaymentMethodValue());
        application.setPaymentStatus(PaymentStatus.P_FOR_PAYMENT_VERIFICATION_VALUE);
        application.setDatePaid(TimestampUtil.now());
        applicationRepository.save(application);

        return NoContentResponse.newBuilder()
                .setSuccess(true)
                .build();
    }

    @Override
    public NoContentResponse confirmApplication(ConfirmApplicationRequest request, GrpcPrincipal principal) {
        var acceptStatus = List.of(FOR_PAYMENT_VERIFICATION_VALUE);

        var nsaId = UUID.fromString(request.getApplicationId());
        var nsa = nsaRepository.findOneById(nsaId)
                .orElseThrow(() -> new ObjectNotFoundException("Application not found"));
        if (request.getIsApproved()) {
            nsa.setStatus(APPROVED_VALUE);
            var applicationType = ApplicationType.forNumber(nsa.getType());
            assert applicationType != null;
            nsa.setLeaseCode(
                    codeGeneratorRepository.generateLeaseCode(nsa.getMarketType()));
            nsa.setLeaseStatus(LeaseStatus.ACTIVE_VALUE);

            // Note: at the time this code block is written, only NSA supported
            // In the future, when renewal application is supported, the lease start date might greater than approved date
            var now = TimestampUtil.now();
            var leaseStartDate = leaseUtil.asLeaseStartDate(now);
            var leaseEndDate = leaseUtil.calcLeaseEndDate(leaseStartDate, nsa.getStallType());

            nsa.setLeaseStartDate(leaseStartDate);
            nsa.setLeaseEndDate(leaseEndDate);
            if (nsa.getApprovedDate() == null) {
                nsa.setApprovedDate(leaseStartDate);
            }
            nsa.setPaymentStatus(PaymentStatus.P_PAID_VALUE);

            leaseKafkaProducer.notifyLeaseBeingApproved(nsa);
        } else {
            nsa.setStatus(DISAPPROVED_VALUE);
        }
        nsa.setCancelReason(request.getComment());
        applicationRepository.save(nsa);

        return NoContentResponse.newBuilder()
                .setSuccess(true)
                .build();
    }

    private UUID storeNSA(SubmitApplicationRequest request, GrpcPrincipal principal) {
        return transactionTemplate.execute(status -> {
            var nsa = applicationGrpcMapper.createNSA(request, principal);
            var nsaOwner = applicationGrpcMapper.grpcRequestToEntity(request.getOwner());
            var members = applicationGrpcMapper.createMembers(nsa, request.getMembersList());

            nsa.setOwner(nsaOwner);
            nsa.setCode(codeGeneratorRepository.generateApplicationCode(ApplicationType.NEW_STALL_APP));

            userRepository.save(nsaOwner);
            nsaRepository.save(nsa);
            memberRepository.saveAll(members);

            status.flush();

            return nsa.getApplicationId();
        });
    }

    private void updateNSA(SubmitApplicationRequest request, ApplicationEntity nsa) {
        transactionTemplate.executeWithoutResult(status -> {
            applicationGrpcMapper.updateApplication(request, nsa);
            applicationGrpcMapper.updateApplicationOwner(nsa.getOwner(), request.getOwner());

            memberRepository.deleteByApplicationId(nsa.getApplicationId());

            nsa.getMembers().clear();

            nsa.setStatus(request.getDraft() ? IN_PROGRESS_VALUE : NEW_VALUE);


            var nsaMembers = applicationGrpcMapper.createMembers(nsa, request.getMembersList());

            userRepository.save(nsa.getOwner());
            memberRepository.saveAll(nsaMembers);
            nsaRepository.save(nsa);

            status.flush();
        });
    }

    private boolean updateApplicationDocs(ApplicationEntity application, SubmitApplicationDocsRequest request) {
        boolean hasChanged = false;

        if (StringUtils.isNotBlank(request.getProofOfResidencies()) && !Objects.equals(application.getProofOfResidency(), request.getProofOfResidencies())) {
            application.setProofOfResidency(request.getProofOfResidencies());
            hasChanged = true;
        }

        if (StringUtils.isNotBlank(request.getBirthCertificate()) && !Objects.equals(application.getBirthCertificate(), request.getBirthCertificate())) {
            application.setBirthCertificate(request.getBirthCertificate());
            hasChanged = true;
        }

        if (StringUtils.isNotBlank(request.getPicture()) && !Objects.equals(application.getPicture(), request.getPicture())) {
            application.setPicture(request.getPicture());
            hasChanged = true;
        }

        if (StringUtils.isNotBlank(request.getIdentification()) && !Objects.equals(application.getIdentification(), request.getIdentification())) {
            application.setIdentification(request.getIdentification());
            hasChanged = true;
        }
        return hasChanged;
    }

    private boolean updateApplicationPayment(ApplicationEntity application, SubmitApplicationPaymentRequest request) {
        boolean hasChanged = false;

        if (!Objects.equals(application.getPaymentMethod(), request.getPaymentMethodValue())) {
            application.setPaymentMethod(request.getPaymentMethodValue());
            hasChanged = true;
        }

        if (StringUtils.isNotBlank(request.getProofOfTransfer()) && !Objects.equals(application.getProofOfTransfer(), request.getProofOfTransfer())) {
            application.setProofOfTransfer(request.getProofOfTransfer());
            hasChanged = true;
        }

        return hasChanged;
    }
}
