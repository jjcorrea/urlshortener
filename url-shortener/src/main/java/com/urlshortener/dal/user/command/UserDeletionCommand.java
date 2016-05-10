package com.urlshortener.dal.user.command;

import com.urlshortener.dal.user.UserDao;
import com.urlshortener.exception.dal.UserCreationException;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixCommandKey;
import com.netflix.hystrix.HystrixObservableCommand;
import org.apache.log4j.Logger;
import rx.Observable;

public class UserDeletionCommand extends HystrixObservableCommand<Boolean> {

    private static final Logger log = Logger.getLogger(UserDeletionCommand.class);
    private static final String userDeletionCommandKey = "UserDeletionCommand";
    private String userId;
    private UserDao userDao;

    public UserDeletionCommand(String userId, UserDao userDao) {
        super(Setter
                .withGroupKey(HystrixCommandGroupKey.Factory.asKey(userDeletionCommandKey))
                .andCommandKey(HystrixCommandKey.Factory.asKey(userDeletionCommandKey))
        );
        this.userId = userId;
        this.userDao = userDao;
    }

    public UserDeletionCommand(Setter setter, String userId, UserDao userDao) {
        super(setter);
        this.userId = userId;
        this.userDao = userDao;
    }

    @Override
    protected Observable<Boolean> construct() {
        return Observable.create(subscriber -> {
            if (!subscriber.isUnsubscribed()){
                log.debug(String.format("Deleting user: %s", userId));
                userDao.deleteUser(userId);
                subscriber.onNext(true);
                subscriber.onCompleted();
            }
        });
    }

    @Override
    protected Observable<Boolean> resumeWithFallback() {
        log.error(String.format("An Error occured while creating user: %s", userId), getFailedExecutionException());
        return Observable.error(new UserCreationException(userId));
    }
}
