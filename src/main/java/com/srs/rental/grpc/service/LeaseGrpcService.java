package com.srs.rental.grpc.service;

import com.srs.common.FindByIdRequest;
import com.srs.common.PageResponse;
import com.srs.proto.dto.GrpcPrincipal;
import com.srs.rental.GetApplicationResponse;
import com.srs.rental.ListLeasesRequest;

public interface LeaseGrpcService {
    PageResponse listLeases(GrpcPrincipal principal, ListLeasesRequest request);
    GetApplicationResponse getLease(FindByIdRequest request, GrpcPrincipal user);

}
