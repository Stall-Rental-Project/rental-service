package com.srs.rental.grpc.server;

import com.srs.common.FindByIdRequest;
import com.srs.common.NoContentResponse;
import com.srs.proto.intercepter.AuthGrpcInterceptor;
import com.srs.proto.provider.GrpcPrincipalProvider;
import com.srs.proto.util.GrpcExceptionUtil;
import com.srs.rental.CreateLeaseTerminationRequest;
import com.srs.rental.GetLeaseTerminationResponse;
import com.srs.rental.LeaseTerminationServiceGrpc;
import com.srs.rental.ProceedLeaseTerminationRequest;
import com.srs.rental.grpc.service.LeaseTerminationGrpcService;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;

@GrpcService(interceptors = AuthGrpcInterceptor.class)
@RequiredArgsConstructor
public class LeaseTerminationGrpcServer extends LeaseTerminationServiceGrpc.LeaseTerminationServiceImplBase {
    private final LeaseTerminationGrpcService leaseTerminationGrpcService;

    @Override
    public void getLeaseTermination(FindByIdRequest request, StreamObserver<GetLeaseTerminationResponse> responseObserver) {
        try {
            var principal = GrpcPrincipalProvider.getGrpcPrincipal();
            responseObserver.onNext(leaseTerminationGrpcService.getLeaseTermination(request, principal));
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onNext(GetLeaseTerminationResponse.newBuilder()
                    .setSuccess(false)
                    .setError(GrpcExceptionUtil.asGrpcError(e))
                    .build());
            responseObserver.onCompleted();
            throw e;
        }
    }

    @Override
    public void createLeaseTermination(CreateLeaseTerminationRequest request, StreamObserver<NoContentResponse> responseObserver) {
        try {
            var principal = GrpcPrincipalProvider.getGrpcPrincipal();
            responseObserver.onNext(leaseTerminationGrpcService.createLeaseTermination(request, principal));
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
    public void proceedLeaseTermination(ProceedLeaseTerminationRequest request, StreamObserver<NoContentResponse> responseObserver) {
        try {
            var principal = GrpcPrincipalProvider.getGrpcPrincipal();
            responseObserver.onNext(leaseTerminationGrpcService.cancelLeaseTermination(request, principal));
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
