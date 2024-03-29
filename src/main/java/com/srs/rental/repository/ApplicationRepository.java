package com.srs.rental.repository;

import com.srs.rental.entity.ApplicationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ApplicationRepository extends JpaRepository<ApplicationEntity, UUID> {
    boolean existsByMarketCodeAndFloorCodeAndStallCode(String marketCode, String floorCode, String stallCode);

    boolean existsByMarketCodeAndFloorCode(String marketCode, String floorCode);

    boolean existsByMarketCode(String marketCode);

    @Query("SELECT a FROM ApplicationEntity a " +
            "left join fetch a.members " +
            "WHERE a.applicationId = :id " +
            "AND a.type in (0, 1) " +
            "AND a.status in (4)")
    Optional<ApplicationEntity> findOneLeaseById(@Param("id") UUID applicationId);
}
