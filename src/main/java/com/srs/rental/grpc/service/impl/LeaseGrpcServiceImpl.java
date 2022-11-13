package com.srs.rental.grpc.service.impl;

import com.google.protobuf.Any;
import com.srs.common.FindByIdRequest;
import com.srs.common.PageResponse;
import com.srs.common.exception.ObjectNotFoundException;
import com.srs.proto.dto.GrpcPrincipal;
import com.srs.rental.Application;
import com.srs.rental.GetApplicationResponse;
import com.srs.rental.ListLeasesRequest;
import com.srs.rental.common.Constant;
import com.srs.rental.grpc.mapper.ApplicationGrpcMapper;
import com.srs.rental.grpc.service.LeaseGrpcService;
import com.srs.rental.grpc.util.PageUtil;
import com.srs.rental.repository.ApplicationDslRepository;
import com.srs.rental.repository.ApplicationRepository;
import com.srs.rental.repository.LeaseTerminationDslRepository;
import com.srs.rental.repository.UserRepository;
import com.srs.rental.util.LeaseUtil;
import com.srs.rental.util.RateUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Log4j2
public class LeaseGrpcServiceImpl implements LeaseGrpcService {
    private final ApplicationRepository applicationRepository;
    private final UserRepository userRepository;

    private final ApplicationDslRepository applicationDslRepository;

    private final ApplicationGrpcMapper applicationGrpcMapper;

    private final LeaseTerminationDslRepository leaseTerminationDslRepository;

    private final RateUtil rateUtil;
    private final LeaseUtil leaseUtil;
    private ZoneOffset offset = ZoneOffset.of("+08:00");

    @Override
    public PageResponse listLeases(GrpcPrincipal principal, ListLeasesRequest request) {
        var pageResponse = applicationDslRepository.findAllLeases(request, principal);
        var pageRequest = PageUtil.normalizeRequest(request.getPageRequest(), Constant.LEASE_SORTS);

        var listApplications = new ArrayList<Application.Builder>();
        for (var application : pageResponse.getItems()) {
            var builder = applicationGrpcMapper.entityToGrpcBuilder(application, false);

            listApplications.add(builder);
        }

        return PageResponse.newBuilder()
                .setSuccess(true)
                .setData(PageResponse.Data.newBuilder()
                        .setPage(pageRequest.getPage())
                        .setSize(pageRequest.getSize())
                        .setTotalElements(pageResponse.getTotal())
                        .setTotalPages(PageUtil.calcTotalPages(pageResponse.getTotal(),
                                pageRequest.getSize()))
                        .addAllItems(listApplications.stream()
                                .map(Application.Builder::build)
                                .map(Any::pack)
                                .collect(Collectors.toList()))
                        .build())
                .build();
    }

    @Override
    public GetApplicationResponse getLease(FindByIdRequest request, GrpcPrincipal user) {

        var applicationId = UUID.fromString(request.getId());
        var application = applicationRepository.findOneLeaseById(applicationId)
                .orElseThrow(() -> new ObjectNotFoundException("Application not found"));

        var grpcApplication = applicationGrpcMapper.buildGrpcApplicationResponse(application);

        return GetApplicationResponse.newBuilder()
                .setSuccess(true)
                .setData(GetApplicationResponse.Data.newBuilder()
                        .setApplication(grpcApplication.build())
                        .build())
                .build();
    }
}
