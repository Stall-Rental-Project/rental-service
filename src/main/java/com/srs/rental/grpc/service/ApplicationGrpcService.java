package com.srs.rental.grpc.service;

import com.srs.common.FindByIdRequest;
import com.srs.common.NoContentResponse;
import com.srs.common.PageResponse;
import com.srs.proto.dto.GrpcPrincipal;
import com.srs.rental.ListApplicationRequest;

public interface ApplicationGrpcService {
    PageResponse listApplications(GrpcPrincipal user, ListApplicationRequest request);

    NoContentResponse cancelApplication(FindByIdRequest request, GrpcPrincipal principal);

}
