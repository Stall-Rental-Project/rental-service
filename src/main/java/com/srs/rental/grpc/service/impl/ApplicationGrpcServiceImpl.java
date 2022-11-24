package com.srs.rental.grpc.service.impl;

import com.google.common.collect.Lists;
import com.google.protobuf.Any;
import com.srs.common.FindByIdRequest;
import com.srs.common.NoContentResponse;
import com.srs.common.PageResponse;
import com.srs.common.exception.ObjectNotFoundException;
import com.srs.market.MarketClass;
import com.srs.market.StallClass;
import com.srs.proto.dto.GrpcPrincipal;
import com.srs.rental.Application;
import com.srs.rental.ListApplicationRequest;
import com.srs.rental.WorkflowStatus;
import com.srs.rental.common.Constant;
import com.srs.rental.grpc.mapper.ApplicationGrpcMapper;
import com.srs.rental.grpc.service.ApplicationGrpcService;
import com.srs.rental.grpc.util.PageUtil;
import com.srs.rental.repository.ApplicationDslRepository;
import com.srs.rental.repository.ApplicationRepository;
import com.srs.rental.util.RateUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Log4j2
public class ApplicationGrpcServiceImpl implements ApplicationGrpcService {
    private final ApplicationRepository applicationRepository;

    private final ApplicationDslRepository applicationDslRepository;

    private final TransactionTemplate transactionTemplate;

    private final ApplicationGrpcMapper applicationGrpcMapper;

    private final RateUtil rateUtil;


    @Override
    public PageResponse listApplications(GrpcPrincipal user, ListApplicationRequest request) {
        var pageResponse = applicationDslRepository.findAllApplications(request, user);
        var pageRequest = PageUtil.normalizeRequest(request.getPageRequest(), Constant.APPLICATION_SORTS);

        var mapApplications = new HashMap<String, List<Application.Builder>>();

        var listApplications = new ArrayList<Application.Builder>();
        for (var application : pageResponse.getItems()) {
            var grpcApplication = applicationGrpcMapper.entityToGrpcBuilder(application, false);

            listApplications.add(grpcApplication);

            String key = String.format("%d===%s===%s",
                    application.getMarketClass(), application.getStallClass(), application.getStallArea());
            if (mapApplications.containsKey(key)) {
                mapApplications.get(key).add(grpcApplication);
            } else {
                mapApplications.put(key, Lists.newArrayList(grpcApplication));
            }
        }

        for (var key : mapApplications.keySet()) {
            String[] components = key.split("===", 4);

            if (components.length != 4) {
                log.warn("Invalid applications found when listing. Key: {}", key);
                continue;
            }

            var marketClass = MarketClass.forNumber(Integer.parseInt(components[0]));
            var stallClass = StallClass.forNumber(Integer.parseInt(components[1]));
            var stallArea = Double.parseDouble(components[3]);

            var monthlyFee = rateUtil.getMonthlyRate(marketClass, stallClass, stallArea);

//            for (var application : mapApplications.get(key)) {
//                application.setMonthlyFee(monthlyFee);
//            }
        }

        return PageResponse.newBuilder()
                .setSuccess(true)
                .setData(PageResponse.Data.newBuilder()
                        .setPage(pageRequest.getPage())
                        .setSize(pageRequest.getSize())
                        .setTotalElements(pageResponse.getTotal())
                        .setTotalPages(PageUtil.calcTotalPages(pageResponse.getTotal(), pageRequest.getSize()))
                        .addAllItems(listApplications.stream()
                                .map(Application.Builder::build)
                                .map(Any::pack)
                                .collect(Collectors.toList()))
                        .build())
                .build();
    }

    @Override
    public NoContentResponse cancelApplication(FindByIdRequest request, GrpcPrincipal principal) {
        var applicationId = UUID.fromString(request.getId());

        var application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ObjectNotFoundException("Application not found"));

        if (List.of(WorkflowStatus.APPROVED_VALUE).contains(application.getStatus())) {
            throw new IllegalArgumentException("Cannot cancel approved application");
        }

        application.setStatus(WorkflowStatus.CANCELLED_VALUE);

        transactionTemplate.executeWithoutResult(transaction -> {
            applicationRepository.save(application);
            transaction.flush();
        });

        return NoContentResponse.newBuilder()
                .setSuccess(true)
                .build();
    }
}
