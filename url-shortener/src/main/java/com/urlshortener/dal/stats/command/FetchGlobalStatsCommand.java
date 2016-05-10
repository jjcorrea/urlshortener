package com.urlshortener.dal.stats.command;

import com.urlshortener.dal.stats.StatsDao;
import com.urlshortener.dal.stats.output.StatsDalOutput;
import com.urlshortener.exception.dal.StatsRetrievalException;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixCommandKey;
import com.netflix.hystrix.HystrixCommandProperties;
import com.netflix.hystrix.HystrixObservableCommand;
import org.apache.log4j.Logger;
import rx.Observable;

public class FetchGlobalStatsCommand extends HystrixObservableCommand<StatsDalOutput> {
    private static final Logger log = Logger.getLogger(FetchGlobalStatsCommand.class);
    private static final String globalStatsCommand = "FetchGlobalStatsCommand";
    private StatsDao statsDao;

    public FetchGlobalStatsCommand(StatsDao statsDao, int extendedTimeoutForStats) {
        super(Setter
                .withGroupKey(HystrixCommandGroupKey.Factory.asKey(globalStatsCommand))
                .andCommandKey(HystrixCommandKey.Factory.asKey(globalStatsCommand))
                .andCommandPropertiesDefaults(
                    HystrixCommandProperties.Setter().withExecutionTimeoutInMilliseconds(extendedTimeoutForStats)
                )
        );
        this.statsDao = statsDao;
    }

    @Override
    protected Observable<StatsDalOutput> construct() {
        return Observable.create(subscriber -> {
            if (!subscriber.isUnsubscribed()){
                log.debug("Fetching global stats");
                subscriber.onNext(statsDao.fetchGlobalStats());
                subscriber.onCompleted();
            }
        });
    }

    @Override
    protected Observable<StatsDalOutput> resumeWithFallback() {
        log.error("Unable to retrieve global stats", getFailedExecutionException());
        return Observable.error(new StatsRetrievalException());
    }

}
