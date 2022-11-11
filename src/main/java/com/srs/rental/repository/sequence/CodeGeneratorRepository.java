package com.srs.rental.repository.sequence;

import com.srs.rental.ApplicationType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;


/**
 * @author duynt on 3/18/22
 */
@RequiredArgsConstructor
@Repository
public class CodeGeneratorRepository {

    private final EntityManager entityManager;

    public String generateApplicationCode(ApplicationType type) {
        var query = entityManager.createNativeQuery("select generate_application_code(:type)");

        query.setParameter("type", type.getNumber());

        return (String) query.getSingleResult();
    }

    public String generateLeaseCode(int marketType) {
        var query = entityManager.createNativeQuery(
                "select generate_application_lease_code(:type)");

        query.setParameter("type", marketType);

        return (String) query.getSingleResult();
    }

}
