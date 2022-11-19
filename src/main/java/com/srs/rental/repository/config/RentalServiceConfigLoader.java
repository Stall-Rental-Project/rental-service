package com.srs.rental.repository.config;

import com.srs.common.config.BaseServiceConfigLoader;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;


@Repository
public class RentalServiceConfigLoader extends BaseServiceConfigLoader {
    public RentalServiceConfigLoader(EntityManager entityManager) {
        super(entityManager);
    }
}
