package com.srs.rental.repository;

import com.srs.rental.entity.ApplicationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ApplicationRepository extends JpaRepository<ApplicationEntity, UUID> {
    boolean existsByMarketCodeAndFloorCodeAndStallCode(String marketCode, String floorCode, String stallCode);

    boolean existsByMarketCodeAndFloorCode(String marketCode, String floorCode);

    boolean existsByMarketCode(String marketCode);

}
