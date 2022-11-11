package com.srs.rental.repository;

import com.srs.rental.entity.RateEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface RateRepository extends JpaRepository<RateEntity, UUID> {

    Optional<RateEntity> findByRateCode(String code);

    @Query("SELECT r FROM RateEntity r " +
            "WHERE r.type = :type " +
            "AND r.status = 1 ")
    Optional<RateEntity> findInUseRateByType(@Param("type") int rateType);
}
