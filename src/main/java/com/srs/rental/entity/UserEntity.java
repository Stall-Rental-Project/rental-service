package com.srs.rental.entity;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.io.Serializable;
import java.sql.Timestamp;
import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;

@Entity
@Getter
@Setter
@Table(name = "users")
public class UserEntity implements Serializable {
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(
            name = "UUID",
            strategy = "org.hibernate.id.UUIDGenerator"
    )
    @Column(updatable = false, nullable = false)
    private UUID userId;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String firstName;

    private String middleName;

    @Column(nullable = false)
    private String lastName;

    @Column(nullable = false)
    private Integer sex;

    @Column(nullable = false)
    private Integer maritalStatus;

    @Column(nullable = false)
    private OffsetDateTime dateOfBirth;

    @Column(nullable = false)
    private String placeOfBirth;


    @Column(nullable = false)
    private String fartherName;

    @Column(nullable = false)
    private String motherName;

    private String houseNumber;

    private String street;

    @Column(nullable = false)
    private String province;

    @Column(nullable = false)
    private String city;

    @Column(nullable = false)
    private String ward;

    @Column(nullable = false)
    private String zipcode;

    private String district;

    private String telephone;

    @Column(nullable = false)
    private Timestamp createdAt;

    private Timestamp updatedAt;

    @PreUpdate
    public void preUpdate() {
        this.setUpdatedAt(new Timestamp(System.currentTimeMillis()));
    }

    @PrePersist
    public void prePersist() {
        this.setCreatedAt(new Timestamp(System.currentTimeMillis()));
        this.setUpdatedAt(new Timestamp(System.currentTimeMillis()));
    }

    @Transient
    public String getFullName() {
        var fullName = String.format("%s %s %s",
            Objects.requireNonNullElse(firstName, ""),
            Objects.requireNonNullElse(middleName, ""),
            Objects.requireNonNullElse(lastName, ""));

        return fullName.trim();
    }
}
