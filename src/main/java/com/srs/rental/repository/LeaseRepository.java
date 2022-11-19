package com.srs.rental.repository;

import com.srs.rental.entity.ApplicationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;


public interface LeaseRepository extends JpaRepository<ApplicationEntity, UUID> {


    @Query("SELECT a FROM ApplicationEntity a " +
            "WHERE a.applicationId = :id " +
            "AND a.type in (0, 1, 2) " +
            "AND a.status in (3, 5)")
    Optional<ApplicationEntity> findOneById(@Param("id") UUID applicationId);

    @Query("select a from ApplicationEntity a " +
            "where a.applicationId = :id " +
            "and a.leaseStatus = :status " +
            "AND a.type in (0, 1, 2) " +
            "AND a.status in (3, 5)")
    Optional<ApplicationEntity> findOneByIdAndStatus(@Param("id") UUID applicationId, @Param("status") int leaseStatus);

    @Modifying
    @Query("update ApplicationEntity " +
            "set leaseStatus = :status " +
            "where applicationId in (:ids) " +
            "and status in (3, 5) " +
            "and type in (0, 1, 2)")
    void updateLeaseStatusInBatch(@Param("ids") Collection<UUID> applicationIds, @Param("status") int leaseStatus);

}
