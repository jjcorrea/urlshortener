package com.urlshortener.dal.url.command;

import com.urlshortener.dal.url.UrlDao;
import com.urlshortener.exception.dal.UrlFetchingException;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixCommandKey;
import com.netflix.hystrix.HystrixObservableCommand;
import org.apache.log4j.Logger;
import rx.Observable;

public class FetchUrlFromIdCommand extends HystrixObservableCommand<String> {
    private static final Logger log = Logger.getLogger(FetchUrlFromIdCommand.class);
    private static final String fetchUrlCommand = "FetchUrlCommand";
    private String identifier;
    private UrlDao urlDao;

    public FetchUrlFromIdCommand(String identifier, UrlDao urlDao) {
        super(Setter
                .withGroupKey(HystrixCommandGroupKey.Factory.asKey(fetchUrlCommand))
                .andCommandKey(HystrixCommandKey.Factory.asKey(fetchUrlCommand))
        );
        this.identifier = identifier;
        this.urlDao = urlDao;
    }

    @Override
    protected Observable<String> construct() {
        return Observable.create(subscriber -> {
            if (!subscriber.isUnsubscribed()){
                log.debug(String.format("Fetching url by identifier: %s", identifier));
                subscriber.onNext(urlDao.fetchUrlByIdentifier(identifier));
                subscriber.onCompleted();
            }
        });
    }

    @Override
    protected Observable<String> resumeWithFallback() {
        log.error(String.format("An Error occured while fetching identifier: %s", identifier), getFailedExecutionException());
        return Observable.error(new UrlFetchingException());
    }
}
