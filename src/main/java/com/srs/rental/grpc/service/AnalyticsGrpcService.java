package com.srs.rental.grpc.service;

import com.banvien.emarket.rental.GetRevenueAnalyticsRequest;
import com.banvien.emarket.rental.GetRevenueAnalyticsResponse;

public interface AnalyticsGrpcService {
    GetRevenueAnalyticsResponse getRevenueAnalytics(GetRevenueAnalyticsRequest request);
}
