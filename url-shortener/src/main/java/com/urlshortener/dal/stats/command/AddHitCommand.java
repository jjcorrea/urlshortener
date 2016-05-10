package com.urlshortener.dal.stats.command;

import com.urlshortener.dal.stats.StatsDao;
import com.urlshortener.exception.dal.StatsUpdateException;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixCommandKey;
import com.netflix.hystrix.HystrixObservableCommand;
import org.apache.log4j.Logger;
import rx.Observable;

public class AddHitCommand extends HystrixObservableCommand<Boolean> {
    private static final Logger log = Logger.getLogger(AddHitCommand.class);
    private static final String addHitCommand = "AddHitCommand";
    private StatsDao statsDao;
    private String urlId;
    private String userId;

    public AddHitCommand(String urlId, String userId, StatsDao statsDao) {
        super(Setter
            .withGroupKey(HystrixCommandGroupKey.Factory.asKey(addHitCommand))
            .andCommandKey(HystrixCommandKey.Factory.asKey(addHitCommand))
        );
        this.urlId = urlId;
        this.statsDao = statsDao;
        this.userId = userId;
    }

    @Override
    protected Observable<Boolean> construct() {
        return Observable.create(subscriber -> {
            if (!subscriber.isUnsubscribed()){
                log.debug(String.format("Adding hit to url id: %s", urlId));
                statsDao.incrementUrlHits(urlId, userId);
                subscriber.onNext(true);
                subscriber.onCompleted();
            }
        });
    }

    @Override
    protected Observable<Boolean> resumeWithFallback() {
        log.error(String.format("Unable to add hit to url id: %s", urlId), getFailedExecutionException());
        return Observable.error(new StatsUpdateException());
    }

}
