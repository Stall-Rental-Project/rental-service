package com.srs.rental.grpc.mapper;

import com.srs.common.util.TimestampUtil;
import com.srs.market.StallType;
import com.srs.proto.dto.GrpcPrincipal;
import com.srs.rental.*;
import com.srs.rental.entity.ApplicationEntity;
import com.srs.rental.entity.MemberEntity;
import com.srs.rental.entity.UserEntity;
import com.srs.rental.repository.MemberRepository;
import com.srs.rental.util.RateUtil;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.srs.rental.WorkflowStatus.IN_PROGRESS_VALUE;
import static com.srs.rental.WorkflowStatus.NEW_VALUE;
import static java.util.Objects.requireNonNullElse;

@Component
@RequiredArgsConstructor
public class ApplicationGrpcMapper {
    private final DateTimeFormatter LEASE_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssZ");

    private final MemberRepository memberRepository;


    private final RateUtil rateUtil;

    public ApplicationEntity createNSA(SubmitApplicationRequest request, GrpcPrincipal principal) {

        var nsa = new ApplicationEntity();

        nsa.setStatus(request.getDraft() ? IN_PROGRESS_VALUE : NEW_VALUE);
        nsa.setType(ApplicationType.NEW_STALL_APP_VALUE);

        nsa.setStallCode(request.getStallCode());
        nsa.setStallType(request.getStallTypeValue());
        nsa.setStallClass(request.getStallClassValue());
        nsa.setStallArea(request.getStallArea());

        nsa.setMarketCode(request.getMarketCode());
        nsa.setMarketType(request.getMarketTypeValue());
        nsa.setMarketClass(request.getMarketClassValue());

        nsa.setFloorCode(request.getFloorCode());

        nsa.setCreatedBy(principal.getUserId());

        nsa.setOwnedAnyStall(request.getOwnedAnyStall());
        nsa.setOwnedStallInfo(request.getOwnedStallInfo());
        nsa.setPayTaxPrevious(request.getPayTaxPrevious());
        nsa.setPayTaxPreviousReason(request.getPayTaxPreviousReason());
        nsa.setForcedTerminatePrevious(request.getForcedTerminatePrevious());
        nsa.setForcedTerminateReason(request.getForcedTerminateReason());
        nsa.setExchangeRentStall(request.getExchangeRentStall());
        nsa.setExchangeRentStallName(request.getExchangeRentStallName());
        nsa.setConvictedViolateLaw(request.getConvictedViolateLaw());
        nsa.setConvictedViolateLawReason(request.getConvictedViolateLawReason());
        nsa.setAdministrativeCriminal(request.getAdministrativeCriminal());
        nsa.setAdministrativeCriminalReason(request.getAdministrativeCriminalReason());
        nsa.setCapital(request.getCapital());
        nsa.setSourceOfCapital(request.getSourceOfCapital());
        return nsa;
    }


    public List<MemberEntity> createMembers(ApplicationEntity application, List<Member> members) {
        var lstMembers = new ArrayList<MemberEntity>();

        for (var member : members) {
            var memberEntity = this.grpcRequestToEntity(member);
            memberEntity.setApplication(application);
            lstMembers.add(memberEntity);
        }

        return lstMembers;
    }


    public void updateApplication(SubmitApplicationRequest request, ApplicationEntity application) {
        application.setStallCode(StringUtils.isNotBlank(request.getStallCode()) ? request.getStallCode() : application.getStallCode());
        application.setStallType(request.getStallTypeValue());
        application.setStallClass(request.getStallClassValue());
        application.setStallArea(request.getStallArea());

        application.setMarketCode(StringUtils.isNotBlank(request.getMarketCode()) ? request.getMarketCode() : application.getMarketCode());
        application.setMarketType(request.getMarketTypeValue());
        application.setMarketClass(request.getMarketClassValue());

        application.setFloorCode(StringUtils.isNotBlank(request.getFloorCode()) ? request.getFloorCode() : application.getFloorCode());

        application.setOwnedAnyStall(request.getOwnedAnyStall());
        application.setOwnedStallInfo(request.getOwnedStallInfo());
        application.setPayTaxPrevious(request.getPayTaxPrevious());
        application.setPayTaxPreviousReason(request.getPayTaxPreviousReason());
        application.setForcedTerminateReason(request.getForcedTerminateReason());
        application.setForcedTerminatePrevious(request.getForcedTerminatePrevious());
        application.setExchangeRentStall(request.getExchangeRentStall());
        application.setExchangeRentStallName(request.getExchangeRentStallName());
        application.setConvictedViolateLaw(request.getConvictedViolateLaw());
        application.setConvictedViolateLawReason(request.getConvictedViolateLawReason());
        application.setAdministrativeCriminalReason(request.getAdministrativeCriminalReason());
        application.setAdministrativeCriminal(request.getAdministrativeCriminal());
        application.setCapital(request.getCapital());
        application.setSourceOfCapital(request.getSourceOfCapital());
    }

    public UserEntity grpcRequestToEntity(ApplicationOwner request) {
        var user = new UserEntity();
        user.setFirstName(request.getFirstName());
        user.setMiddleName(request.getMiddleName());
        user.setLastName(request.getLastName());
        user.setFartherName(request.getFartherName());
        user.setMotherName(request.getMotherName());
        user.setMaritalStatus(request.getMaritalStatus());
        user.setPlaceOfBirth(request.getPlaceOfBirth());
        user.setSex(request.getSex());
        user.setDateOfBirth(StringUtils.isNotBlank(request.getDateOfBirth())
                ? TimestampUtil.parseDatetimeString(request.getDateOfBirth())
                : TimestampUtil.now());
        user.setHouseNumber(request.getHouseNumber());
        user.setStreet(request.getStreet());
        user.setProvince(request.getProvince());
        user.setCity(request.getCity());
        user.setZipcode(request.getZipcode());
        user.setWard(request.getWard());
        user.setEmail(request.getEmail().trim());
        user.setDistrict(request.getDistrict());
        user.setTelephone(request.getTelephone());
        return user;
    }

    public Application.Builder entityToGrpcBuilder(ApplicationEntity entity, boolean fetchMembers) {
        var builder = Application.newBuilder()
                .setApplicationId(entity.getApplicationId().toString())
                .setCode(entity.getCode())

                .setMarketCode(entity.getMarketCode())
                .setMarketTypeValue(entity.getMarketType())
                .setMarketClassValue(entity.getMarketClass())

                .setFloorCode(entity.getFloorCode())

                .setStallCode(entity.getStallCode())
                .setStallTypeValue(entity.getStallType())
                .setStallClassValue(entity.getStallClass())
                .setStallArea(entity.getStallArea())


                .setType(ApplicationType.forNumber(entity.getType()))
                .setStatusValue(entity.getStatus())

                .setCreatedAt(TimestampUtil.stringifyDatetime(entity.getCreatedAt(), true))
                .setCreatedBy(entity.getCreatedBy().toString())

                .setOwnedAnyStall(entity.isOwnedAnyStall())
                .setOwnedStallInfo(requireNonNullElse(entity.getOwnedStallInfo(), ""))
                .setPayTaxPrevious(entity.isPayTaxPrevious())
                .setPayTaxPreviousReason(requireNonNullElse(entity.getPayTaxPreviousReason(), ""))
                .setForcedTerminatePrevious(entity.isForcedTerminatePrevious())
                .setForcedTerminateReason(requireNonNullElse(entity.getForcedTerminateReason(), ""))
                .setExchangeRentStall(entity.isExchangeRentStall())
                .setExchangeRentStallName(requireNonNullElse(entity.getExchangeRentStallName(), ""))
                .setConvictedViolateLaw(entity.isConvictedViolateLaw())
                .setConvictedViolateLawReason(requireNonNullElse(entity.getConvictedViolateLawReason(), ""))
                .setAdministrativeCriminal(entity.isAdministrativeCriminal())
                .setAdministrativeCriminalReason(requireNonNullElse(entity.getAdministrativeCriminalReason(), ""))
                .setCapital(requireNonNullElse(entity.getCapital(), ""))
                .setSourceOfCapital(requireNonNullElse(entity.getSourceOfCapital(), ""))

                .setProofOfResidencies(requireNonNullElse(entity.getProofOfResidency(), ""))
                .setBirthCertificate(requireNonNullElse(entity.getBirthCertificate(), ""))
                .setPicture(requireNonNullElse(entity.getPicture(), ""))
                .setIdentification(requireNonNullElse(entity.getIdentification(), ""))
                .setPaymentMethod(entity.getPaymentMethod() != null ? PaymentMethod.forNumber(entity.getPaymentMethod()) : PaymentMethod.INVALID_METHOD)
                .setProofOfTransfer(requireNonNullElse(entity.getProofOfTransfer(), ""))

                .setInitialFee(entity.getPaidInitialFee())

                .setLeaseCode(requireNonNullElse(entity.getLeaseCode(), ""))
                .setLeaseStatusValue(entity.getLeaseStatus())
                .setLeaseStartDate(entity.getLeaseStartDate() != null ? entity.getLeaseStartDate().format(LEASE_DATE_FORMATTER) : "")
                .setLeaseEndDate(entity.getLeaseEndDate() != null ? entity.getLeaseEndDate().format(LEASE_DATE_FORMATTER) : "")

                .setCurrentPaymentStatusValue(entity.getPaymentStatus())


                .setApprovedDate(entity.getApprovedDate() != null ? entity.getApprovedDate().format(LEASE_DATE_FORMATTER) : "")

                .setRemindedPaymentDate(TimestampUtil.stringifyDatetime(entity.getRemindedPaymentDate(), true))
                .setOwner(this.entityToGrpcResponse(entity.getOwner()));
        if (fetchMembers) {
            builder.addAllMembers(entity.getMembers().stream()
                    .map(this::entityToGrpcResponse)
                    .sorted(Comparator.comparing(Member::getName))
                    .collect(Collectors.toList()));
        }

        return builder;
    }

    public Application entityToGrpcResponse(ApplicationEntity entity) {
        return entityToGrpcBuilder(entity, false).build();
    }

    public Application entityToGrpcResponse(ApplicationEntity entity, boolean fetchMembers) {
        return entityToGrpcBuilder(entity, fetchMembers).build();
    }

    public ApplicationOwner entityToGrpcResponse(UserEntity entity) {
        if (entity == null) {
            return ApplicationOwner.getDefaultInstance();
        }
        var dateS = "";
        var age = 0;
        if (entity.getDateOfBirth() != null) {
            dateS = TimestampUtil.stringifyDatetime(entity.getDateOfBirth(), true);
            age = Period.between(LocalDate.from(entity.getDateOfBirth()), LocalDate.now()).getYears();
        }
        return ApplicationOwner.newBuilder()
                .setUserId(entity.getUserId().toString())
                .setFirstName(entity.getFirstName())
                .setLastName(entity.getLastName())
                .setMiddleName(entity.getMiddleName())
                .setDateOfBirth(dateS)
                .setAge(age)
                .setPlaceOfBirth(entity.getPlaceOfBirth())
                .setFartherName(entity.getFartherName())
                .setMotherName(entity.getMotherName())
                .setMaritalStatus(entity.getMaritalStatus())
                .setSex(entity.getSex())
                .setHouseNumber(requireNonNullElse(entity.getHouseNumber(), ""))
                .setStreet(requireNonNullElse(entity.getStreet(), ""))
                .setProvince(entity.getProvince())
                .setCity(entity.getCity())
                .setWard(entity.getWard())
                .setZipcode(requireNonNullElse(entity.getZipcode(), ""))
                .setEmail(entity.getEmail())
                .setDistrict(requireNonNullElse(entity.getDistrict(), ""))
                .setFullName(requireNonNullElse(entity.getFullName(), ""))
                .setTelephone(requireNonNullElse(entity.getTelephone(),""))
                .build();
    }

    public Member entityToGrpcResponse(MemberEntity entity) {
        return Member.newBuilder()
                .setMemberId(entity.getMemberId().toString())
                .setName(entity.getName())
                .setAge(entity.getAge())
                .build();
    }

    public void updateApplicationOwner(UserEntity user, ApplicationOwner response) {
        user.setFirstName(response.getFirstName());
        user.setLastName(response.getLastName());
        user.setMiddleName(response.getMiddleName());
        user.setCity(response.getCity());
        user.setWard(response.getWard());
        user.setFartherName(response.getFartherName());
        user.setMotherName(response.getMotherName());
        user.setDateOfBirth(StringUtils.isNotBlank(response.getDateOfBirth())
                ? TimestampUtil.parseDatetimeString(response.getDateOfBirth())
                : TimestampUtil.now());
        user.setProvince(response.getProvince());
        user.setZipcode(response.getZipcode());
        user.setStreet(response.getStreet());
        user.setMaritalStatus(response.getMaritalStatus());
        user.setPlaceOfBirth(response.getPlaceOfBirth());
        user.setSex(response.getSex());
        user.setHouseNumber(response.getHouseNumber());
        user.setEmail(response.getEmail());
        user.setTelephone(response.getTelephone());
    }

    public MemberEntity grpcRequestToEntity(Member request) {
        var member = new MemberEntity();
        member.setName(request.getName());
        member.setAge(request.getAge());
        return member;
    }


    public Application.Builder buildGrpcApplicationResponse(ApplicationEntity application) {
        var grpcApplication = this.entityToGrpcBuilder(application, true);

        // application fee is not apply for this application

        if (grpcApplication.getInitialFee() == 0) {
            grpcApplication.setInitialFee(rateUtil.getInitialRate(grpcApplication.getType()));
        }

        if (StallType.STALL_TYPE_TEMPORARY.equals(grpcApplication.getStallType())) {
            grpcApplication.setSecurityFee(0)
                    .setTotalAmountDue(0);
        } else {
            if (grpcApplication.getSecurityFee() == 0) {
                var monthlyFee = rateUtil.getMonthlyRate(grpcApplication.getMarketClass(), grpcApplication.getStallClass(), grpcApplication.getStallArea());
                var securityFee = rateUtil.getSecurityRate(monthlyFee, grpcApplication.getStallType());

                grpcApplication.setSecurityFee(securityFee);

                if (grpcApplication.getTotalAmountDue() == 0) {
                    var totalAmountDue = rateUtil.getTotalAmountDue(securityFee, grpcApplication.getMarketClass(), grpcApplication.getStallType());
                    grpcApplication.setTotalAmountDue(totalAmountDue);
                }
            } // else, it already set in entityToGrpcBuilder()
        }

        // We must re-fetch these entities here due to application's code is generated via SQL trigger instead of directly in business code
//        var members = memberRepository.findAllByApplicationId(application.getApplicationId());
//
//        grpcApplication.addAllMembers(members.stream()
//                .map(this::entityToGrpcResponse)
//                .collect(Collectors.toList()));

        return grpcApplication;
    }
}
