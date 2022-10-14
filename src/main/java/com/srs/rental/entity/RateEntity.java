package com.srs.rental.entity;

import com.srs.common.util.TimestampUtil;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "rate")
public class RateEntity {
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(
            name = "UUID",
            strategy = "org.hibernate.id.UUIDGenerator"
    )
    @Column(updatable = false, nullable = false)
    private UUID rateId;

    private String rateCode;


    private int type;
    private int status;

    private int otherRateType;

    private String content;

    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = TimestampUtil.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = TimestampUtil.now();
    }
}
