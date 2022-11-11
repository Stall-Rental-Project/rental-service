package com.srs.rental.repository;

import com.srs.rental.entity.MemberEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.List;
import java.util.UUID;

@Repository
public interface MemberRepository extends JpaRepository<MemberEntity, UUID> {

    @Transactional
    @Modifying
    @Query("delete from MemberEntity m " +
            "where m.application.applicationId = :applicationId ")
    void deleteByApplicationId(@Param("applicationId") UUID applicationId);

    @Query("SELECT m FROM MemberEntity m " +
            "WHERE m.application.applicationId = :id " +
            "ORDER BY m.name ASC ")
    List<MemberEntity> findAllByApplicationId(@Param("id") UUID applicationId);
}