package com.urlshortener.dal.user.command;

import com.urlshortener.dal.user.UserDao;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixCommandKey;
import com.netflix.hystrix.HystrixObservableCommand;
import org.apache.log4j.Logger;
import rx.Observable;

public class UserExistsCommand extends HystrixObservableCommand<Boolean> {

    private static final Logger log = Logger.getLogger(UserExistsCommand.class);
    private static final String UserExistsCommandKey = "UserExistsCommand";
    private String userId;
    private UserDao userDao;

    public UserExistsCommand(String userId, UserDao userDao) {
        super(Setter
                .withGroupKey(HystrixCommandGroupKey.Factory.asKey(UserExistsCommandKey))
                .andCommandKey(HystrixCommandKey.Factory.asKey(UserExistsCommandKey))
        );
        this.userId = userId;
        this.userDao = userDao;
    }

    @Override
    protected Observable<Boolean> construct() {
        return Observable.create(subscriber -> {
            if (!subscriber.isUnsubscribed()){
                log.debug(String.format("Checking whether user: %s exists", userId));
                subscriber.onNext(userDao.userExists(userId));
                subscriber.onCompleted();
            }
        });
    }

    @Override
    protected Observable<Boolean> resumeWithFallback() {
        String message = String.format("An Error occured while checking user: %s", userId);
        log.error(message, getFailedExecutionException());
        return Observable.error(new RuntimeException(message));
    }
}
