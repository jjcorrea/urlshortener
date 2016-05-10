package com.urlshortener.dal.stats.impl;

import com.urlshortener.configuration.UrlShortenerConfiguration;
import com.urlshortener.dal.stats.StatsDao;
import com.urlshortener.dal.stats.output.StatsDalOutput;
import com.urlshortener.dal.stats.output.UrlStatsDalOutput;
import com.urlshortener.dal.url.impl.UrlDaoInMemoryImpl;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

import static java.lang.Integer.parseInt;
import static java.lang.Integer.valueOf;

@Profile("in-memory")
@Repository
public class StatsDaoInMemoryImpl implements StatsDao {

    @Autowired
    private UrlShortenerConfiguration config;

    private ConcurrentMap<String, String> data;
    private ConcurrentMap<String, List<Pair<Integer, String>>> orderedData;

    @PostConstruct
    public void init(){
        ConcurrentMap<String, String> urlData = UrlDaoInMemoryImpl.getData();
        urlData.clear();
        this.data = urlData;
        orderedData = new ConcurrentHashMap<>();
    }

    @Override
    public void incrementUrlCount(String userId, String urlId) {
        increment("stats:urlcount", 1);
        increment("stats:urlcount:user:" + userId, 1);
    }

    @Override
    public void initializeUrlHits(String userId, String urlId) {
        ImmutablePair<Integer, String> initialPair = new ImmutablePair<>(0, urlId);
        orderedData.getOrDefault("stats:urls", new ArrayList<>()).add(initialPair);
        orderedData.getOrDefault("stats:urls:user:"+userId, new ArrayList<>()).add(initialPair);
    }

    @Override
    public void incrementUrlHits(String urlId, String userId) {
        increment("stats:hits", 1);
        increment("stats:hits:url:" + urlId, 1);
        increment("stats:hits:user:" + userId, 1);
        incrementTopTenScores(userId, urlId);
    }

    @Override
    public void incrementTopTenScores(String userId, String urlId) {
        ArrayList<Pair<Integer, String>> defaultGlobalUrls = new ArrayList<>();
        ArrayList<Pair<Integer, String>> defaultUserUrls = new ArrayList<>();

        List<Pair<Integer, String>> globalUrlsStats = orderedData.getOrDefault("stats:urls", defaultGlobalUrls);
        List<Pair<Integer, String>> userUrlStats = orderedData.getOrDefault("stats:urls:user:" + userId, defaultUserUrls);

        if(!orderedData.containsKey("stats:urls")) orderedData.put("stats:urls", defaultGlobalUrls);
        if(!orderedData.containsKey("stats:urls:user:" + userId)) orderedData.put("stats:urls:user:" + userId, defaultUserUrls);

        increment(urlId, globalUrlsStats);
        increment(urlId, userUrlStats);
    }

    @Override
    public synchronized void deleteUrlStats(String identifier) {
        String ownerId = data.get("owner:url:" + identifier);
        int urlHits = parseInt(data.getOrDefault("stats:hits:url:" + identifier, "0"));
        deleteUrlHitsStats(identifier, ownerId, urlHits);
        deleteUrlCountStats(ownerId);
        orderedData.remove("stats:urls:user:" + ownerId);
        orderedData.getOrDefault("stats:urls", Lists.newArrayList()).removeIf(item -> item.getRight().equals(identifier));
    }

    @Override
    public void deleteUserStats(String userId) {
        List<Pair<Integer, String>> allUserUrls = orderedData.getOrDefault("stats:urls:user:" + userId, new ArrayList<>());
        allUserUrls.parallelStream().map(Pair::getRight).forEach(this::deleteUrlStats);
        data.remove("stats:urls:user:" + userId);
        data.remove("stats:urlcount:user:" + userId);
        data.remove("stats:hits:user:" + userId);
    }

    @Override
    public StatsDalOutput fetchGlobalStats() {
        return new StatsDalOutput(
                data.getOrDefault("stats:hits", "0"),
                data.getOrDefault("stats:urlcount", "0"),
                fetchGlobalUrlStats());
    }

    @Override
    public StatsDalOutput fetchUserStats(String userId) {
        return new StatsDalOutput(
                data.getOrDefault("stats:hits:user:" + userId, "0"),
                data.getOrDefault("stats:urlcount:user:" + userId, "0"),
                fetchUserUrlStats(userId));
    }

    @Override
    public UrlStatsDalOutput fetchUrlStats(String urlId) {
        return new UrlStatsDalOutput(
                urlId,
                new Long(data.getOrDefault("stats:hits:url:" + urlId, "0")),
                data.get("id:" + urlId),
                config.shortUrl(urlId));
    }

    private List<UrlStatsDalOutput> fetchUserUrlStats(String userId) {
        List<UrlStatsDalOutput> urlStats = fetchStats("stats:urls:user:" + userId);
        return urlStats.size()>10 ? urlStats.subList(0, 10) : urlStats;
    }

    private List<UrlStatsDalOutput> fetchGlobalUrlStats() {
        List<UrlStatsDalOutput> urlStats = fetchStats("stats:urls");
        return urlStats.size()>10 ? urlStats.subList(0, 10) : urlStats;
    }

    private List<UrlStatsDalOutput> fetchStats(String id) {
        return orderedData.getOrDefault(id, new ArrayList<>())
                .stream()
                .sorted((item1, item2) -> item2.getLeft().compareTo(item1.getLeft()))
                .map(pair -> pair.getValue())
                .map(this::fetchUrlStats)
                .filter(stats -> stats.getUrl()!=null)
                .collect(Collectors.toList());
    }

    private void deleteUrlHitsStats(String identifier, String ownerId, int urlHits) {
        increment("stats:hits", -urlHits);
        increment("stats:hits:user:" + ownerId, -urlHits);
        data.remove("stats:hits:url:" + identifier);
    }

    private void deleteUrlCountStats(String ownerId) {
        increment("stats:urlcount", -1);
        increment("stats:urlcount:user:" + ownerId, -1);
    }

    private String increment(String key, int value) {
        return data.put(key, Integer.toString(valueOf(data.getOrDefault(key, "0")) + value));
    }

    private void increment(String urlId, List<Pair<Integer, String>> urlStats) {
        urlStats.add(
            urlStats
                .parallelStream()
                .filter(pair -> pair.getRight().equals(urlId))
                .findFirst()
                .map(pair -> {
                    urlStats.removeIf(item -> item.getValue().equals(pair.getValue()));
                    return new ImmutablePair<>(pair.getKey() + 1, pair.getValue());
                }).orElseGet(() -> new ImmutablePair<>(1, urlId))
        );
    }

}
