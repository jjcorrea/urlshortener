package com.urlshortener.service;

import com.urlshortener.dal.stats.StatsDao;
import com.urlshortener.dal.stats.command.*;
import com.urlshortener.dal.url.UrlDao;
import com.urlshortener.dal.url.command.FindUrlOwnerCommand;
import com.urlshortener.service.output.StatsServiceOutput;
import com.urlshortener.service.output.StatsUrlServiceOutput;
import org.apache.log4j.Logger;
import org.dozer.Mapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import rx.Observable;

import java.util.concurrent.Future;

@Service
public class StatsService {
    private static final Logger log = Logger.getLogger(StatsService.class);
    private UrlDao urlDao;
    private StatsDao statsDao;
    private Mapper beanMapper;

    @Value("${shortener.stats.maxRetries}")
    private int maxStatsRetries;

    @Value("${shortener.stats.extendedTimeout}")
    private int timeoutForStats;

    @Autowired
    public StatsService(StatsDao statsDao, UrlDao urlDao, Mapper beanMapper) {
        this.statsDao = statsDao;
        this.urlDao = urlDao;
        this.beanMapper = beanMapper;
    }

    @Async
    public Future<Boolean> registerUrlHit(String urlId) {
        log.debug(String.format("Registering url hit for: %s", urlId));
        Observable<Boolean> urlHitRegistration = new FindUrlOwnerCommand(urlId, urlDao)
                .observe()
                .flatMap(urlOwnerId -> new AddHitCommand(urlId, urlOwnerId.getUserId(), statsDao).observe())
                .doOnError((throwable) -> log.error("Unable to register url hit", throwable))
                .doOnCompleted(() -> log.debug(String.format("Successfully registered url hit for id: %s", urlId)))
                .retry(maxStatsRetries);
        return urlHitRegistration.toBlocking().toFuture();
    }

    @Async
    public Future<Boolean> registerUrlCreation(String userId, String urlId) {
        log.debug(String.format("Registering url creation for user: %s, url: %s", userId, urlId));
        Observable<Boolean> urlCreationRegistration = new IncrementUrlCountCommand(userId, urlId, statsDao)
                .observe()
                .doOnError(throwable -> log.error("Unable to increment url count", throwable))
                .doOnCompleted(() -> log.debug(String.format("Successfully incremented url count for userId: %s", userId)))
                .retry(maxStatsRetries);
        return urlCreationRegistration.toBlocking().toFuture();
    }

    public Observable<StatsServiceOutput> getUserStats(String userId){
        return new FetchUserStatsCommand(userId, statsDao, timeoutForStats)
            .observe()
            .map(userStats -> beanMapper.map(userStats, StatsServiceOutput.class));
    }

    public Observable<StatsUrlServiceOutput> getUrlStats(String urlId){
        return new FetchUrlStatsCommand(urlId, statsDao, timeoutForStats)
            .observe()
            .map(stats -> beanMapper.map(stats, StatsUrlServiceOutput.class));
    }

    public Observable<StatsServiceOutput> getGlobalStats(){
        return new FetchGlobalStatsCommand(statsDao, timeoutForStats)
                .observe()
                .map(userStats -> beanMapper.map(userStats, StatsServiceOutput.class));
    }


}
