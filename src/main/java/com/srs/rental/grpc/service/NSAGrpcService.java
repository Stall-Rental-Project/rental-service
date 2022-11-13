package com.srs.rental.grpc.service;


import com.srs.common.BooleanResponse;
import com.srs.common.FindByIdRequest;
import com.srs.common.NoContentResponse;
import com.srs.proto.dto.GrpcPrincipal;
import com.srs.rental.*;

public interface NSAGrpcService {
    GetApplicationResponse submitApplication(SubmitApplicationRequest request, GrpcPrincipal principal);

    NoContentResponse submitApplicationDocs(SubmitApplicationDocsRequest request, GrpcPrincipal principal);

    GetApplicationResponse getApplication(FindByIdRequest request, GrpcPrincipal principal);

    GetApplicationResponse updateApplication(SubmitApplicationRequest request, GrpcPrincipal principal);

    BooleanResponse checkExistApplication(CheckExistApplicationRequest request);
    NoContentResponse submitPayment(SubmitApplicationPaymentRequest request, GrpcPrincipal principal);
    NoContentResponse confirmApplication(ConfirmApplicationRequest request, GrpcPrincipal principal);

}
