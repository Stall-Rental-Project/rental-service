package com.srs.rental.entity;

import com.srs.common.util.TimestampUtil;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.UUID;


@Getter
@Setter
@Entity
@Table(name = "lease_termination")
public class LeaseTerminationEntity implements Serializable {
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(
            name = "UUID",
            strategy = "org.hibernate.id.UUIDGenerator"
    )
    @Column(updatable = false, nullable = false)
    private UUID terminationId;

    private UUID applicationId;

    private String reason;

    private OffsetDateTime endDate;

    private String createdBy;

    private boolean accepted;

    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    private int status;

    @PrePersist
    public void prePersist() {
        this.createdAt = TimestampUtil.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = TimestampUtil.now();
    }
}
