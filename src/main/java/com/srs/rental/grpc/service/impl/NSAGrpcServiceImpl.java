package com.srs.rental.grpc.service.impl;

import com.srs.common.Error;
import com.srs.common.ErrorCode;
import com.srs.common.FindByIdRequest;
import com.srs.common.NoContentResponse;
import com.srs.common.exception.AccessDeniedException;
import com.srs.common.exception.ObjectNotFoundException;
import com.srs.common.util.PermissionUtil;
import com.srs.proto.dto.GrpcPrincipal;
import com.srs.rental.ApplicationType;
import com.srs.rental.GetApplicationResponse;
import com.srs.rental.SubmitApplicationDocsRequest;
import com.srs.rental.SubmitApplicationRequest;
import com.srs.rental.entity.ApplicationEntity;
import com.srs.rental.grpc.mapper.ApplicationGrpcMapper;
import com.srs.rental.grpc.service.NSAGrpcService;
import com.srs.rental.repository.*;
import com.srs.rental.repository.sequence.CodeGeneratorRepository;
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

        if (PermissionUtil.isPublicUser(principal.getRoles()) &&
                !List.of(NEW_VALUE, IN_PROGRESS_VALUE, ADDITIONAL_INFO_REQUESTED_VALUE).contains(application.getStatus())) {
            throw new AccessDeniedException("Application is being processed");
        }

        this.updateApplicationDocs(application, request);

        application.setStatus(request.getDraft() ? IN_PROGRESS_VALUE : NEW_VALUE);

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
            var application = nsaRepository.findById(applicationId)
                    .orElseThrow(() -> new ObjectNotFoundException("Application not found"));

            if (PermissionUtil.isPublicUser(principal.getRoles()) &&
                    !List.of(NEW_VALUE, IN_PROGRESS_VALUE, ADDITIONAL_INFO_REQUESTED_VALUE).contains(application.getStatus())) {
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
}
