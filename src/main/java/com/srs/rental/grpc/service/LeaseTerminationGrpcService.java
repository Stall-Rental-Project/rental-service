package com.srs.rental.grpc.service;

import com.srs.common.FindByIdRequest;
import com.srs.common.NoContentResponse;
import com.srs.proto.dto.GrpcPrincipal;
import com.srs.rental.CreateLeaseTerminationRequest;
import com.srs.rental.GetLeaseTerminationResponse;
import com.srs.rental.ProceedLeaseTerminationRequest;

public interface LeaseTerminationGrpcService {
    GetLeaseTerminationResponse getLeaseTermination(
            FindByIdRequest request, GrpcPrincipal principal);

    NoContentResponse createLeaseTermination(CreateLeaseTerminationRequest request,
                                              GrpcPrincipal principal);

    NoContentResponse cancelLeaseTermination(ProceedLeaseTerminationRequest request,
                                              GrpcPrincipal principal);

}
