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

public class FetchUserStatsCommand extends HystrixObservableCommand<StatsDalOutput> {
    private static final Logger log = Logger.getLogger(FetchUserStatsCommand.class);
    private static final String userStatsCommand = "FetchUserStatsCommand";
    private String userId;
    private StatsDao statsDao;

    public FetchUserStatsCommand(String userId, StatsDao statsDao, int extendedTimeoutForStats) {
        super(Setter
                .withGroupKey(HystrixCommandGroupKey.Factory.asKey(userStatsCommand))
                .andCommandKey(HystrixCommandKey.Factory.asKey(userStatsCommand))
                .andCommandPropertiesDefaults(
                    HystrixCommandProperties.Setter().withExecutionTimeoutInMilliseconds(extendedTimeoutForStats)
                )
        );
        this.statsDao = statsDao;
        this.userId = userId;
    }

    @Override
    protected Observable<StatsDalOutput> construct() {
        return Observable.create(subscriber -> {
            if (!subscriber.isUnsubscribed()){
                log.debug(String.format("Fetching stats for userId: %s", userId));
                subscriber.onNext(statsDao.fetchUserStats(userId));
                subscriber.onCompleted();
            }
        });
    }

    @Override
    protected Observable<StatsDalOutput> resumeWithFallback() {
        log.error(String.format("Unable to retrieve stats for user: %s", userId), getFailedExecutionException());
        return Observable.error(new StatsRetrievalException());
    }

}
