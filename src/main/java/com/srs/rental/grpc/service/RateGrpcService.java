package com.srs.rental.grpc.service;

import com.srs.common.FindByIdRequest;
import com.srs.common.NoContentResponse;
import com.srs.common.OnlyCodeResponse;
import com.srs.common.PageResponse;
import com.srs.proto.dto.GrpcPrincipal;
import com.srs.rental.GetRateRequest;
import com.srs.rental.GetRateResponse;
import com.srs.rental.ListRatesRequest;
import com.srs.rental.UpsertRateRequest;

public interface RateGrpcService {
    PageResponse listRates(ListRatesRequest request, GrpcPrincipal principal);

    GetRateResponse getRate(GetRateRequest request, GrpcPrincipal principal);

    OnlyCodeResponse createRate(UpsertRateRequest request, GrpcPrincipal principal);

    OnlyCodeResponse updateRate(UpsertRateRequest request, GrpcPrincipal principal);
    NoContentResponse deleteRate(FindByIdRequest request, GrpcPrincipal principal);

}
