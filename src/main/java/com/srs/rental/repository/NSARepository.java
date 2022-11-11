package com.srs.rental.repository;

import com.srs.rental.entity.ApplicationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface NSARepository extends JpaRepository<ApplicationEntity, UUID> {
    @Query("select a from ApplicationEntity a " +
            "where a.applicationId = :id " +
            "and a.type = 0")
    Optional<ApplicationEntity> findOneById(@Param("id") UUID id);
}
