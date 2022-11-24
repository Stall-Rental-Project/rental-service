package com.srs.rental.grpc.server;

import com.srs.common.NoContentResponse;
import com.srs.common.PageResponse;
import com.srs.proto.intercepter.AuthGrpcInterceptor;
import com.srs.proto.provider.GrpcPrincipalProvider;
import com.srs.proto.util.GrpcExceptionUtil;
import com.srs.rental.ApplicationServiceGrpc;
import com.srs.rental.CancelApplicationRequest;
import com.srs.rental.ListApplicationRequest;
import com.srs.rental.grpc.service.ApplicationGrpcService;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;

@GrpcService(interceptors = AuthGrpcInterceptor.class)
@RequiredArgsConstructor
public class ApplicationGrpcServer extends ApplicationServiceGrpc.ApplicationServiceImplBase {
    private final ApplicationGrpcService applicationGrpcService;

    @Override
    public void listApplications(ListApplicationRequest request, StreamObserver<PageResponse> responseObserver) {
        try {
            var user = GrpcPrincipalProvider.getGrpcPrincipal();
            var response = applicationGrpcService.listApplications(user, request);
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onNext(PageResponse.newBuilder()
                    .setSuccess(false)
                    .setError(GrpcExceptionUtil.asGrpcError(e))
                    .build());
            responseObserver.onCompleted();
            throw e;
        }
    }

    @Override
    public void cancelApplication(CancelApplicationRequest request, StreamObserver<NoContentResponse> responseObserver) {
        try {
            var principal = GrpcPrincipalProvider.getGrpcPrincipal();
            responseObserver.onNext(applicationGrpcService.cancelApplication(request, principal));
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onNext(NoContentResponse.newBuilder()
                    .setSuccess(false)
                    .setError(GrpcExceptionUtil.asGrpcError(e))
                    .build());
            responseObserver.onCompleted();
            throw e;
        }
    }
}
