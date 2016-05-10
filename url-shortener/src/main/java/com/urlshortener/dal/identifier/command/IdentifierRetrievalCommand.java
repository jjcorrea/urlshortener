package com.urlshortener.dal.identifier.command;

import com.urlshortener.dal.identifier.IdentifierDao;
import com.urlshortener.exception.dal.IdentifierRetrievalException;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixCommandKey;
import com.netflix.hystrix.HystrixObservableCommand;
import org.apache.log4j.Logger;
import rx.Observable;

public class IdentifierRetrievalCommand extends HystrixObservableCommand<Long> {
    private static final Logger log = Logger.getLogger(IdentifierRetrievalCommand.class);
    private static final String urlCreationCommandKey = "AtomicLongRetrievalCommand";
    private IdentifierDao identifierDao;

    public IdentifierRetrievalCommand(IdentifierDao identifierDao) {
        super(Setter
                .withGroupKey(HystrixCommandGroupKey.Factory.asKey(urlCreationCommandKey))
                .andCommandKey(HystrixCommandKey.Factory.asKey(urlCreationCommandKey))
        );
        this.identifierDao = identifierDao;
    }

    @Override
    protected Observable<Long> construct() {
        return Observable.create(subscriber -> {
            if (!subscriber.isUnsubscribed()){
                log.debug("Generating Atomic identifier");
                subscriber.onNext(identifierDao.fetchAtomicLong());
                subscriber.onCompleted();
            }
        });
    }

    @Override
    protected Observable<Long> resumeWithFallback() {
        log.error("An Error occured while fetching identifier", getFailedExecutionException());
        return Observable.error(new IdentifierRetrievalException());
    }

}
