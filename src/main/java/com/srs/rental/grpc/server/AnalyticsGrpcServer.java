package com.srs.rental.grpc.server;

import com.banvien.emarket.rental.AnalyticsServiceGrpc;
import com.banvien.emarket.rental.GetRevenueAnalyticsRequest;
import com.banvien.emarket.rental.GetRevenueAnalyticsResponse;
import com.srs.proto.intercepter.AuthGrpcInterceptor;
import com.srs.proto.provider.GrpcPrincipalProvider;
import com.srs.proto.util.GrpcExceptionUtil;
import com.srs.rental.grpc.service.AnalyticsGrpcService;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;

@GrpcService(interceptors = AuthGrpcInterceptor.class)
@RequiredArgsConstructor
public class AnalyticsGrpcServer extends AnalyticsServiceGrpc.AnalyticsServiceImplBase {
    private final AnalyticsGrpcService analyticsGrpcService;
    @Override
    public void getRevenueAnalytics(GetRevenueAnalyticsRequest request, StreamObserver<GetRevenueAnalyticsResponse> responseObserver) {
        try {
            var principal = GrpcPrincipalProvider.getGrpcPrincipal();
            responseObserver.onNext(analyticsGrpcService.getRevenueAnalytics(request));
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onNext(GetRevenueAnalyticsResponse.newBuilder()
                    .setSuccess(false)
                    .setError(GrpcExceptionUtil.asGrpcError(e))
                    .build());
            responseObserver.onCompleted();
            throw e;
        }
    }
}
