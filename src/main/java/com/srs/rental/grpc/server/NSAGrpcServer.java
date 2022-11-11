package com.srs.rental.grpc.server;

import com.srs.common.FindByIdRequest;
import com.srs.common.NoContentResponse;
import com.srs.proto.intercepter.AuthGrpcInterceptor;
import com.srs.proto.provider.GrpcPrincipalProvider;
import com.srs.proto.util.GrpcExceptionUtil;
import com.srs.rental.*;
import com.srs.rental.grpc.service.NSAGrpcService;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;

@GrpcService(interceptors = AuthGrpcInterceptor.class)
@RequiredArgsConstructor
public class NSAGrpcServer extends NSAServiceGrpc.NSAServiceImplBase {

    private final NSAGrpcService nsaGrpcService;

    @Override
    public void submitApplication(SubmitApplicationRequest request, StreamObserver<GetApplicationResponse> responseObserver) {
        try {
            var user = GrpcPrincipalProvider.getGrpcPrincipal();
            var response = nsaGrpcService.submitApplication(request, user);
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onNext(GetApplicationResponse.newBuilder()
                    .setSuccess(false)
                    .setError(GrpcExceptionUtil.asGrpcError(e))
                    .build());
            responseObserver.onCompleted();
            throw e;
        }
    }

    @Override
    public void submitApplicationDocs(SubmitApplicationDocsRequest request, StreamObserver<NoContentResponse> responseObserver) {
        try {
            var user = GrpcPrincipalProvider.getGrpcPrincipal();
            var response = nsaGrpcService.submitApplicationDocs(request, user);
            responseObserver.onNext(response);
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

    @Override
    public void getApplication(FindByIdRequest request, StreamObserver<GetApplicationResponse> responseObserver) {
        try {
            var user = GrpcPrincipalProvider.getGrpcPrincipal();
            var response = nsaGrpcService.getApplication(request, user);
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onNext(GetApplicationResponse.newBuilder()
                    .setSuccess(false)
                    .setError(GrpcExceptionUtil.asGrpcError(e))
                    .build());
            responseObserver.onCompleted();
            throw e;
        }
    }

    @Override
    public void updateApplication(SubmitApplicationRequest request, StreamObserver<GetApplicationResponse> responseObserver) {
        try {
            var user = GrpcPrincipalProvider.getGrpcPrincipal();
            var response = nsaGrpcService.updateApplication(request, user);
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onNext(GetApplicationResponse.newBuilder()
                    .setSuccess(false)
                    .setError(GrpcExceptionUtil.asGrpcError(e))
                    .build());
            responseObserver.onCompleted();
            throw e;
        }
    }
}