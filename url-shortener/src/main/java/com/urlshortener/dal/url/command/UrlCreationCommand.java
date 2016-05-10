package com.urlshortener.dal.url.command;

import com.urlshortener.configuration.UrlShortenerConfiguration;
import com.urlshortener.dal.url.UrlDao;
import com.urlshortener.dal.url.output.UrlCreationDalOutput;
import com.urlshortener.exception.dal.UserCreationException;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixCommandKey;
import com.netflix.hystrix.HystrixObservableCommand;
import org.apache.log4j.Logger;
import rx.Observable;

public class UrlCreationCommand extends HystrixObservableCommand<UrlCreationDalOutput> {
    private static final Logger log = Logger.getLogger(UrlCreationCommand.class);
    private static final String urlCreationCommandKey = "UrlCreationCommand";

    private String identifier;
    private String userId;
    private String url;
    private UrlDao urlDao;
    private UrlShortenerConfiguration config;

    public UrlCreationCommand(String identifier, String userId, String url, UrlDao urlDao, UrlShortenerConfiguration config) {
        super(Setter
                .withGroupKey(HystrixCommandGroupKey.Factory.asKey(urlCreationCommandKey))
                .andCommandKey(HystrixCommandKey.Factory.asKey(urlCreationCommandKey))
        );
        this.identifier = identifier;
        this.userId = userId;
        this.url = url;
        this.urlDao = urlDao;
        this.config = config;
    }

    @Override
    protected Observable<UrlCreationDalOutput> construct() {
        return Observable.create(subscriber -> {
            if (!subscriber.isUnsubscribed()){
                log.debug(String.format("Adding url: %s to user: %s", url, userId));
                urlDao.registerUrlWithId(url, identifier, userId);
                subscriber.onNext(new UrlCreationDalOutput(identifier, 0L, url, config.shortUrl(identifier)));
                subscriber.onCompleted();
            }
        });
    }

    @Override
    protected Observable<UrlCreationDalOutput> resumeWithFallback() {
        log.error(String.format("An Error occured while adding url: %s", url), getFailedExecutionException());
        return Observable.error(new UserCreationException(userId));
    }
}
