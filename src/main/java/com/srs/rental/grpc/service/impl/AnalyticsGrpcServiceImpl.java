package com.srs.rental.grpc.service.impl;

import com.banvien.emarket.rental.GetRevenueAnalyticsRequest;
import com.banvien.emarket.rental.GetRevenueAnalyticsResponse;
import com.srs.rental.grpc.service.AnalyticsGrpcService;
import com.srs.rental.repository.ApplicationDslRepository;
import com.srs.rental.repository.ApplicationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AnalyticsGrpcServiceImpl implements AnalyticsGrpcService {

    private final ApplicationDslRepository applicationDslRepository;

    @Override
    public GetRevenueAnalyticsResponse getRevenueAnalytics(GetRevenueAnalyticsRequest request) {


        return null;
    }
}
