package com.urlshortener.dal.identifier.impl;

import com.urlshortener.dal.identifier.IdentifierDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.support.atomic.RedisAtomicLong;
import org.springframework.stereotype.Repository;

@Profile("redis")
@Repository
public class IdentifierDaoRedisImpl implements IdentifierDao {

    @Autowired
    private RedisConnectionFactory connectionFactory;

    @Override
    public long fetchAtomicLong(){
        RedisAtomicLong sequential = new RedisAtomicLong("atomic-long", connectionFactory);
        return sequential.incrementAndGet();
    }
}
