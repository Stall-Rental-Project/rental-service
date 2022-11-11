package com.srs.rental.grpc.service;


import com.srs.common.FindByIdRequest;
import com.srs.common.NoContentResponse;
import com.srs.proto.dto.GrpcPrincipal;
import com.srs.rental.Application;
import com.srs.rental.GetApplicationResponse;
import com.srs.rental.SubmitApplicationDocsRequest;
import com.srs.rental.SubmitApplicationRequest;

public interface NSAGrpcService {
    GetApplicationResponse submitApplication(SubmitApplicationRequest request, GrpcPrincipal principal);

    NoContentResponse submitApplicationDocs(SubmitApplicationDocsRequest request, GrpcPrincipal principal);

    GetApplicationResponse getApplication(FindByIdRequest request, GrpcPrincipal principal);

    GetApplicationResponse updateApplication(SubmitApplicationRequest request, GrpcPrincipal principal);
}
