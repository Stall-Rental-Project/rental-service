package com.srs.rental.grpc.service.impl;

import com.srs.common.FindByIdRequest;
import com.srs.common.NoContentResponse;
import com.srs.common.exception.AccessDeniedException;
import com.srs.common.exception.ObjectNotFoundException;
import com.srs.common.util.PermissionUtil;
import com.srs.proto.dto.GrpcPrincipal;
import com.srs.rental.CreateLeaseTerminationRequest;
import com.srs.rental.GetLeaseTerminationResponse;
import com.srs.rental.ProceedLeaseTerminationRequest;
import com.srs.rental.grpc.mapper.LeaseTerminationGrpcMapper;
import com.srs.rental.grpc.service.LeaseTerminationGrpcService;
import com.srs.rental.repository.ApplicationRepository;
import com.srs.rental.repository.LeaseTerminationDslRepository;
import com.srs.rental.repository.LeaseTerminationRepository;
import com.srs.rental.util.ApplicationUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static com.srs.rental.LeaseStatus.ACTIVE_VALUE;
import static com.srs.rental.LeaseStatus.FOR_TERMINATION_VALUE;
import static com.srs.rental.TerminationStatus.T_CANCELLED_VALUE;

@Service
@RequiredArgsConstructor
@Log4j2
public class LeaseTerminationGrpcServiceImpl implements LeaseTerminationGrpcService {
    private final LeaseTerminationRepository leaseTerminationRepository;
    private final ApplicationRepository applicationRepository;

    private final LeaseTerminationDslRepository leaseTerminationDslRepository;

    private final LeaseTerminationGrpcMapper leaseTerminationGrpcMapper;

    private final ApplicationUtil applicationUtil;

    @Override
    public GetLeaseTerminationResponse getLeaseTermination(FindByIdRequest request, GrpcPrincipal principal) {
        var applicationId = UUID.fromString(request.getId());
        var application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ObjectNotFoundException("Application no longer exist"));

        if (PermissionUtil.isPublicUser(principal.getRoles())) {
            if (applicationUtil.isNotMyApplication(application, principal)) {
                throw new AccessDeniedException("This is not your application");
            }
        }

        var leaseTermination = leaseTerminationDslRepository.findLatestTerminationByApplicationId(applicationId).orElse(null);

        if (leaseTermination == null) {
            return GetLeaseTerminationResponse.newBuilder()
                    .setSuccess(true)
                    .setData(GetLeaseTerminationResponse.Data.newBuilder()
                            .setExist(false)
                            .build())
                    .build();
        } else {
            return GetLeaseTerminationResponse.newBuilder()
                    .setSuccess(true)
                    .setData(GetLeaseTerminationResponse.Data.newBuilder()
                            .setExist(true)
                            .setTermination(leaseTerminationGrpcMapper.entityToGrpcResponse(leaseTermination))
                            .build())
                    .build();
        }
    }

    @Override
    @Transactional
    public NoContentResponse createLeaseTermination(CreateLeaseTerminationRequest request, GrpcPrincipal principal) {
        var applicationId = UUID.fromString(request.getApplicationId());
        var application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ObjectNotFoundException("Application no longer exist"));

        if (PermissionUtil.isPublicUser(principal.getRoles())) {
            if (applicationUtil.isNotMyApplication(application, principal)) {
                throw new AccessDeniedException("This is not your application");
            }

            if (application.getLeaseStatus() != ACTIVE_VALUE) {
                throw new AccessDeniedException("This application lease status must be active");
            }
        }

        if (leaseTerminationDslRepository.findLatestTerminationByApplicationId(applicationId).isPresent()) {
            throw new IllegalStateException("Application has another termination request not closed yet");
        }

        var leaseTermination = leaseTerminationGrpcMapper.grpcRequestToEntity(request, principal);

        leaseTerminationRepository.save(leaseTermination);

        application.setLeaseStatus(FOR_TERMINATION_VALUE);

        applicationRepository.save(application);


        return NoContentResponse.newBuilder()
                .setSuccess(true)
                .build();
    }

    @Override
    public NoContentResponse cancelLeaseTermination(ProceedLeaseTerminationRequest request, GrpcPrincipal principal) {
        var terminationId = UUID.fromString(request.getTerminationId());
        var leaseTermination = leaseTerminationRepository.findById(terminationId)
                .orElseThrow(() -> new ObjectNotFoundException(
                        "Lease termination request not found"));
        var application = applicationRepository.findById(leaseTermination.getApplicationId())
                .orElseThrow(() -> new ObjectNotFoundException("Lease no longer exist"));

        leaseTermination.setAccepted(false);
        leaseTermination.setStatus(T_CANCELLED_VALUE);

        leaseTerminationRepository.save(leaseTermination);

        application.setLeaseStatus(ACTIVE_VALUE);
        applicationRepository.save(application);

        return NoContentResponse.newBuilder()
                .setSuccess(true)
                .build();
    }
}
