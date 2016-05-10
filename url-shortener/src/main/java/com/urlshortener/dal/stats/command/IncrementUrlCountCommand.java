package com.urlshortener.dal.stats.command;

import com.urlshortener.dal.stats.StatsDao;
import com.urlshortener.exception.dal.StatsUpdateException;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixCommandKey;
import com.netflix.hystrix.HystrixObservableCommand;
import org.apache.log4j.Logger;
import rx.Observable;

public class IncrementUrlCountCommand extends HystrixObservableCommand<Boolean> {
    private static final Logger log = Logger.getLogger(IncrementUrlCountCommand.class);
    private static final String urlCountCommand = "IncrementUrlCountCommand";
    private StatsDao statsDao;
    private String userId;
    private String urlId;

    public IncrementUrlCountCommand(String userId, String urlId, StatsDao statsDao) {
        super(Setter
            .withGroupKey(HystrixCommandGroupKey.Factory.asKey(urlCountCommand))
            .andCommandKey(HystrixCommandKey.Factory.asKey(urlCountCommand))
        );
        this.statsDao = statsDao;
        this.userId = userId;
        this.urlId = urlId;
    }

    @Override
    protected Observable<Boolean> construct() {
        return Observable.create(subscriber -> {
            if (!subscriber.isUnsubscribed()){
                log.debug(String.format("Incrementing url count for userId: %s", userId));
                statsDao.incrementUrlCount(userId, urlId);
                statsDao.initializeUrlHits(urlId,userId);
                subscriber.onNext(true);
                subscriber.onCompleted();
            }
        });
    }

    @Override
    protected Observable<Boolean> resumeWithFallback() {
        log.error(String.format("Unable to increment url count for user: %s", userId), getFailedExecutionException());
        return Observable.error(new StatsUpdateException());
    }

}
