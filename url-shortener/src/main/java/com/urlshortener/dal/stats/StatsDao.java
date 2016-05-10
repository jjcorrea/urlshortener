package com.urlshortener.dal.stats;

import com.urlshortener.dal.stats.output.StatsDalOutput;
import com.urlshortener.dal.stats.output.UrlStatsDalOutput;
import org.springframework.transaction.annotation.Transactional;

public interface StatsDao {
    void init();
    void initializeUrlHits(String urlId, String userId);
    void incrementUrlCount(String userId, String urlId);
    void incrementUrlHits(String urlId, String userId);
    void incrementTopTenScores(String userId, String urlId);
    void deleteUrlStats(String identifier);
    void deleteUserStats(String userId);
    StatsDalOutput fetchGlobalStats();
    StatsDalOutput fetchUserStats(String userId);
    UrlStatsDalOutput fetchUrlStats(String urlId);
}
