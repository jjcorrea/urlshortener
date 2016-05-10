package com.urlshortener.dal.stats.command;

import com.urlshortener.dal.stats.StatsDao;
import com.urlshortener.dal.stats.output.UrlStatsDalOutput;
import com.urlshortener.exception.dal.StatsRetrievalException;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixCommandKey;
import com.netflix.hystrix.HystrixCommandProperties;
import com.netflix.hystrix.HystrixObservableCommand;
import org.apache.log4j.Logger;
import rx.Observable;

public class FetchUrlStatsCommand extends HystrixObservableCommand<UrlStatsDalOutput> {
    private static final Logger log = Logger.getLogger(FetchUrlStatsCommand.class);
    private static final String urlStatsCommand = "FetchUrlStatsCommand";
    private String urlId;
    private StatsDao statsDao;

    public FetchUrlStatsCommand(String urlId, StatsDao statsDao, int extendedTimeoutForStats) {
        super(Setter
                .withGroupKey(HystrixCommandGroupKey.Factory.asKey(urlStatsCommand))
                .andCommandKey(HystrixCommandKey.Factory.asKey(urlStatsCommand))
                .andCommandPropertiesDefaults(
                    HystrixCommandProperties.Setter().withExecutionTimeoutInMilliseconds(extendedTimeoutForStats)
                )
        );
        this.statsDao = statsDao;
        this.urlId = urlId;
    }

    @Override
    protected Observable<UrlStatsDalOutput> construct() {
        return Observable.create(subscriber -> {
            if (!subscriber.isUnsubscribed()){
                log.debug(String.format("Fetching stats for urlId: %s", urlId));
                subscriber.onNext(statsDao.fetchUrlStats(urlId));
                subscriber.onCompleted();
            }
        });
    }

    @Override
    protected Observable<UrlStatsDalOutput> resumeWithFallback() {
        log.error(String.format("Unable to retrieve stats for url: %s", urlId), getFailedExecutionException());
        return Observable.error(new StatsRetrievalException());
    }

}
