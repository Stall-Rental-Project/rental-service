package com.srs.rental.repository;

import com.srs.rental.entity.LeaseTerminationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface LeaseTerminationRepository extends JpaRepository<LeaseTerminationEntity, UUID> {
}
