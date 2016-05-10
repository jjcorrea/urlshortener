package com.urlshortener.dal.url.impl;

import com.urlshortener.dal.stats.StatsDao;
import com.urlshortener.dal.url.UrlDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;

@Profile("redis")
@Repository
public class UrlDaoRedisImpl implements UrlDao {

    @Autowired
    private StringRedisTemplate redis;

    @Autowired
    private StatsDao statsDao;
    private ValueOperations<String, String> opsForValue;

    @PostConstruct
    public void init() {
        opsForValue = redis.opsForValue();
    }

    @Override
    @Transactional
    public void registerUrlWithId(String url, String identifier, String userId){
        opsForValue.set("id:" + identifier, url);
        opsForValue.set("url:" + url, identifier);
        opsForValue.set("owner:url:" + identifier, userId);
    }

    @Override
    @Transactional
    public void deleteUrlByIdentifier(String identifier) {
        String url = opsForValue.get("id:" + identifier);
        statsDao.deleteUrlStats(identifier);
        redis.delete("id:" + identifier);
        redis.delete("url:" + url);
        redis.delete("owner:url:" + identifier);
    }

    @Override
    public String fetchUrlByIdentifier(String identifier){
        return opsForValue.get("id:" + identifier);
    }

    @Override
    public String fetchUrlOwnerByIdentifier(String identifier){
        return opsForValue.get("owner:url:" + identifier);
    }

}
