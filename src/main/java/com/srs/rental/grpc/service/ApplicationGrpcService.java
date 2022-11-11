package com.srs.rental.grpc.service;

import com.srs.common.PageResponse;
import com.srs.proto.dto.GrpcPrincipal;
import com.srs.rental.ListApplicationRequest;

public interface ApplicationGrpcService {
    PageResponse listApplications(GrpcPrincipal user, ListApplicationRequest request);

}
