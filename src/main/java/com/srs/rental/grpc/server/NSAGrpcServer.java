package com.srs.rental.grpc.server;

import com.srs.common.*;
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

    @Override
    public void checkExistApplication(CheckExistApplicationRequest request, StreamObserver<BooleanResponse> responseObserver) {
        try {
            var response = nsaGrpcService.checkExistApplication(request);
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onNext(BooleanResponse.newBuilder()
                    .setErrorResponse(ErrorResponse.newBuilder()
                            .setErrorCode(ErrorCode.INTERNAL_SERVER_ERROR)
                            .setErrorDescription(e.getMessage())
                            .build())
                    .build());
            responseObserver.onCompleted();
            throw e;
        }
    }

    @Override
    public void submitApplicationPayment(SubmitApplicationPaymentRequest request, StreamObserver<NoContentResponse> responseObserver) {
        try {
            var user = GrpcPrincipalProvider.getGrpcPrincipal();
            var response = nsaGrpcService.submitPayment(request, user);
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onNext(NoContentResponse.newBuilder()
                    .setSuccess(false)
                    .setError(GrpcExceptionUtil.asGrpcError(e))
                    .build());
            responseObserver.onCompleted();
            throw e;
        }    }

    @Override
    public void confirmApplication(ConfirmApplicationRequest request, StreamObserver<NoContentResponse> responseObserver) {
        try {
            var user = GrpcPrincipalProvider.getGrpcPrincipal();
            var response = nsaGrpcService.confirmApplication(request, user);
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onNext(NoContentResponse.newBuilder()
                    .setSuccess(false)
                    .setError(GrpcExceptionUtil.asGrpcError(e))
                    .build());
            responseObserver.onCompleted();
            throw e;
        }      }
}