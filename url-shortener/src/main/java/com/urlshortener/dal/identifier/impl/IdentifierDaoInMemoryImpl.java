package com.urlshortener.dal.identifier.impl;

import com.urlshortener.dal.identifier.IdentifierDao;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.concurrent.ThreadLocalRandom;

@Profile("in-memory")
@Repository
public class IdentifierDaoInMemoryImpl implements IdentifierDao {
    @Override
    public long fetchAtomicLong() {
        return ThreadLocalRandom.current().nextLong();
    }
}
