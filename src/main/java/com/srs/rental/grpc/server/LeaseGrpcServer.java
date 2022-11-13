package com.srs.rental.grpc.server;

import com.srs.common.FindByIdRequest;
import com.srs.common.PageResponse;
import com.srs.proto.intercepter.AuthGrpcInterceptor;
import com.srs.proto.provider.GrpcPrincipalProvider;
import com.srs.proto.util.GrpcExceptionUtil;
import com.srs.rental.GetApplicationResponse;
import com.srs.rental.LeaseServiceGrpc;
import com.srs.rental.ListLeasesRequest;
import com.srs.rental.grpc.service.LeaseGrpcService;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;

@GrpcService(interceptors = AuthGrpcInterceptor.class)
@RequiredArgsConstructor
public class LeaseGrpcServer extends LeaseServiceGrpc.LeaseServiceImplBase {
    private final LeaseGrpcService leaseGrpcService;

    @Override
    public void listLeases(ListLeasesRequest request, StreamObserver<PageResponse> responseObserver) {
        try {
            var principal = GrpcPrincipalProvider.getGrpcPrincipal();
            var response = leaseGrpcService.listLeases(principal, request);
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onNext(PageResponse.newBuilder()
                    .setSuccess(false)
                    .setError(GrpcExceptionUtil.asGrpcError(e))
                    .build());
            responseObserver.onCompleted();
            throw e;
        }    }

    @Override
    public void getLease(FindByIdRequest request, StreamObserver<GetApplicationResponse> responseObserver) {
        try {
            var user = GrpcPrincipalProvider.getGrpcPrincipal();
            var response = leaseGrpcService.getLease(request, user);
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onNext(GetApplicationResponse.newBuilder()
                    .setSuccess(false)
                    .setError(GrpcExceptionUtil.asGrpcError(e))
                    .build());
            responseObserver.onCompleted();
            throw e;
        }    }
}
