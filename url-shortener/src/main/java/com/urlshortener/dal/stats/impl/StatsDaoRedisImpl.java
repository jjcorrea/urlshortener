package com.urlshortener.dal.stats.impl;

import com.urlshortener.configuration.UrlShortenerConfiguration;
import com.urlshortener.dal.stats.StatsDao;
import com.urlshortener.dal.stats.output.StatsDalOutput;
import com.urlshortener.dal.stats.output.UrlStatsDalOutput;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Profile("redis")
@Repository
public class StatsDaoRedisImpl implements StatsDao {
    private static final int STATS_URL_BEGIN = 0;
    private static final int STATS_URL_LIMIT = 9;

    @Autowired
    private UrlShortenerConfiguration config;
    private StringRedisTemplate redisTemplate;
    private ValueOperations<String, String> opsForValue;

    @Resource(name = "redisTemplate")
    private ZSetOperations<String, String> zset;

    @Autowired
    public StatsDaoRedisImpl(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @PostConstruct
    public void init() {
        opsForValue = redisTemplate.opsForValue();
    }

    @Override
    @Transactional
    public void incrementUrlCount(String userId, String urlId) {
        opsForValue.increment("stats:urlcount", 1);
        opsForValue.increment("stats:urlcount:user:" + userId, 1);
    }

    @Override
    @Transactional
    public void initializeUrlHits(String urlId, String userId) {
        zset.add("stats:urls", urlId, 0);
        zset.add("stats:urls:user:" + userId, urlId, 0);
    }

    @Override
    @Transactional
    public void incrementUrlHits(String urlId, String userId) {
        opsForValue.increment("stats:hits", 1);
        opsForValue.increment("stats:hits:url:" + urlId, 1);
        opsForValue.increment("stats:hits:user:" + userId, 1);
        incrementTopTenScores(userId, urlId);
    }

    @Override
    @Transactional
    public void incrementTopTenScores(String userId, String urlId) {
        zset.incrementScore("stats:urls", urlId, 1);
        zset.incrementScore("stats:urls:user:" + userId, urlId, 1);
    }

    @Override
    @Transactional
    public void deleteUrlStats(String identifier) {
        String ownerId = opsForValue.get("owner:url:" + identifier);
        String urlHitsStr = opsForValue.get("stats:hits:url:" + identifier);
        Long urlHits = Long.parseLong(urlHitsStr == null ? "0" : urlHitsStr);
        deleteUrlHitsStats(identifier, ownerId, urlHits);
        deleteUrlCountStats(ownerId);
        deleteUrlFromTopTenCollections(identifier, ownerId);
    }

    @Override
    @Transactional
    public void deleteUserStats(String userId) {
        Set<String> allUSerUrls = zset.range("stats:urls:user:" + userId, 0, -1);
        allUSerUrls.parallelStream().forEach(this::deleteUrlStats);
        redisTemplate.delete("stats:urls:user:" + userId);
        redisTemplate.delete("stats:urlcount:user:" + userId);
        redisTemplate.delete("stats:hits:user:" + userId);
    }

    @Override
    public StatsDalOutput fetchGlobalStats() {
        return new StatsDalOutput(
                opsForValue.get("stats:hits"),
                opsForValue.get("stats:urlcount"),
                fetchGlobalUrlStats());
    }

    @Override
    public StatsDalOutput fetchUserStats(String userId) {
        return new StatsDalOutput(
                opsForValue.get("stats:hits:user:" + userId),
                opsForValue.get("stats:urlcount:user:" + userId),
                fetchUserUrlStats(userId));
    }

    @Override
    public UrlStatsDalOutput fetchUrlStats(String urlId) {
        String urlHits = opsForValue.get("stats:hits:url:" + urlId);
        return new UrlStatsDalOutput(
                urlId,
                new Long(urlHits == null ? "0" : urlHits),
                opsForValue.get("id:" + urlId),
                config.shortUrl(urlId));
    }

    private void deleteUrlFromTopTenCollections(String identifier, String ownerId) {
        zset.remove("stats:urls", identifier);
        zset.remove("stats:urls:user:" + ownerId, identifier);
    }

    private void deleteUrlCountStats(String ownerId) {
        opsForValue.increment("stats:urlcount", -1);
        opsForValue.increment("stats:urlcount:user:" + ownerId, -1);
    }

    private void deleteUrlHitsStats(String identifier, String ownerId, Long urlHits) {
        opsForValue.increment("stats:hits", -urlHits);
        opsForValue.increment("stats:hits:user:" + ownerId, -urlHits);
        redisTemplate.delete("stats:hits:url:" + identifier);
    }

    private List<UrlStatsDalOutput> fetchUserUrlStats(String userId) {
        return zset
                .reverseRange("stats:urls:user:" + userId, STATS_URL_BEGIN, STATS_URL_LIMIT)
                .stream()
                .map(this::fetchUrlStats).collect(Collectors.toList());
    }

    private List<UrlStatsDalOutput> fetchGlobalUrlStats() {
        return zset
                .reverseRange("stats:urls", STATS_URL_BEGIN, STATS_URL_LIMIT)
                .stream()
                .map(this::fetchUrlStats).collect(Collectors.toList());
    }

}
