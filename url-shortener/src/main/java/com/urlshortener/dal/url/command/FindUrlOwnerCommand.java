package com.urlshortener.dal.url.command;

import com.urlshortener.dal.url.UrlDao;
import com.urlshortener.dal.url.output.UrlOwnerDal;
import com.urlshortener.exception.dal.OwnerSearchingException;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixCommandKey;
import com.netflix.hystrix.HystrixObservableCommand;
import org.apache.log4j.Logger;
import rx.Observable;

public class FindUrlOwnerCommand extends HystrixObservableCommand<UrlOwnerDal> {
    private static final Logger log = Logger.getLogger(FindUrlOwnerCommand.class);
    private static final String findUrlOwnerCommandKey = "FindUrlOwnerCommand";

    private String urlId;
    private UrlDao urlDao;

    public FindUrlOwnerCommand(String urlId, UrlDao urlDao) {
        super(Setter
            .withGroupKey(HystrixCommandGroupKey.Factory.asKey(findUrlOwnerCommandKey))
            .andCommandKey(HystrixCommandKey.Factory.asKey(findUrlOwnerCommandKey))
        );
        this.urlId = urlId;
        this.urlDao = urlDao;
    }

    @Override
    protected Observable<UrlOwnerDal> construct() {
        return Observable.create(subscriber -> {
            if (!subscriber.isUnsubscribed()){
                log.debug(String.format("Finding url owner for id: %s", urlId));
                String userId = urlDao.fetchUrlOwnerByIdentifier(urlId);
                subscriber.onNext(new UrlOwnerDal(userId));
                subscriber.onCompleted();
            }
        });
    }

    @Override
    protected Observable<UrlOwnerDal> resumeWithFallback() {
        log.error(String.format("An Error occured searching url owner for id: %s", urlId), getFailedExecutionException());
        return Observable.error(new OwnerSearchingException());
    }
}
