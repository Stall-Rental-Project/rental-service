package com.srs.rental.entity;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Getter
@Setter
@Table(name = "application")
public class ApplicationEntity {
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(
            name = "UUID",
            strategy = "org.hibernate.id.UUIDGenerator"
    )
    @Column(updatable = false, nullable = false)
    private UUID applicationId;

    @OneToOne(fetch = FetchType.EAGER) // We always need application's owner data in all related APIs
    @JoinColumn(name = "owner_id")
    private UserEntity owner;

    private String marketCode;
    private int marketType;
    private int marketClass;

    private String floorCode;

    private String stallCode;

    private int stallType;
    private int stallClass;
    private double stallArea;

    private int status;
    private String code;
    private int type;


    @OneToMany(mappedBy = "application", fetch = FetchType.LAZY)
    private Set<MemberEntity> members = new HashSet<>();

    private boolean ownedAnyStall;
    private String ownedStallInfo;
    private boolean payTaxPrevious;
    private String payTaxPreviousReason;
    private boolean forcedTerminatePrevious;
    private String forcedTerminateReason;
    private boolean exchangeRentStall;
    private String exchangeRentStallName;
    private boolean convictedViolateLaw;
    private String convictedViolateLawReason;
    private boolean administrativeCriminal;
    private String administrativeCriminalReason;

    private String capital;
    private String sourceOfCapital;


    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;


    //step 2
    private String proofOfResidency;
    private String birthCertificate;
    private String picture;
    private String identification;

    // Step 3: NSA
    private Integer paymentMethod;
    private String proofOfTransfer;

    private UUID createdBy;

    private String leaseCode;
    private int leaseStatus;

    private OffsetDateTime leaseStartDate;
    private OffsetDateTime leaseEndDate;

    private int paymentStatus;
    private double paidInitialFee; // Set right after step 3: NSA
    private double paidSecurityFee; // Set right after step 5: NSA
    private double paidTotalAmountDue; // Set right after step 5: NSA

    private OffsetDateTime approvedDate;

    private OffsetDateTime datePaid;

    private String cancelReason;

    @PreUpdate
    public void preUpdate() {
        this.setUpdatedAt(OffsetDateTime.now());
    }

    @PrePersist
    public void prePersist() {
        this.setCreatedAt(OffsetDateTime.now());
        this.setUpdatedAt(OffsetDateTime.now());
    }


}
