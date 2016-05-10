package com.urlshortener.dal.url.command;

import com.urlshortener.dal.url.UrlDao;
import com.urlshortener.exception.dal.UrlDeletionException;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixCommandKey;
import com.netflix.hystrix.HystrixObservableCommand;
import org.apache.log4j.Logger;
import rx.Observable;

public class DeleteUrlCommand extends HystrixObservableCommand<Boolean> {
    private static final Logger log = Logger.getLogger(DeleteUrlCommand.class);
    private static final String deleteUrlCommand = "DeleteUrlCommand";
    private String identifier;
    private UrlDao urlDao;

    public DeleteUrlCommand(String identifier, UrlDao urlDao) {
        super(Setter
                .withGroupKey(HystrixCommandGroupKey.Factory.asKey(deleteUrlCommand))
                .andCommandKey(HystrixCommandKey.Factory.asKey(deleteUrlCommand))
        );
        this.identifier = identifier;
        this.urlDao = urlDao;
    }

    @Override
    protected Observable<Boolean> construct() {
        return Observable.create(subscriber -> {
            if (!subscriber.isUnsubscribed()){
                log.debug(String.format("Deleting url by identifier: %s", identifier));
                urlDao.deleteUrlByIdentifier(identifier);
                subscriber.onNext(true);
                subscriber.onCompleted();
            }
        });
    }

    @Override
    protected Observable<Boolean> resumeWithFallback() {
        log.error(String.format("An Error occured while deleting url by identifier: %s", identifier), getFailedExecutionException());
        return Observable.error(new UrlDeletionException());
    }
}
