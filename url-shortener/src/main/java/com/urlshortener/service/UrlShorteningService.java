package com.urlshortener.service;

import com.urlshortener.configuration.UrlShortenerConfiguration;
import com.urlshortener.dal.identifier.IdentifierDao;
import com.urlshortener.dal.identifier.command.IdentifierRetrievalCommand;
import com.urlshortener.dal.url.UrlDao;
import com.urlshortener.dal.url.command.DeleteUrlCommand;
import com.urlshortener.dal.url.command.FetchUrlFromIdCommand;
import com.urlshortener.dal.url.command.UrlCreationCommand;
import com.urlshortener.dal.user.UserDao;
import com.urlshortener.dal.user.command.UserCreationCommand;
import com.urlshortener.dal.user.command.UserDeletionCommand;
import com.urlshortener.dal.user.command.UserExistsCommand;
import com.urlshortener.exception.service.InvalidUrlIdentifierException;
import com.urlshortener.exception.service.UrlDoesntExistException;
import com.urlshortener.exception.service.UserDoesNotExistException;
import com.urlshortener.exception.service.UserValidationException;
import com.urlshortener.service.output.UrlInsertionStatsServiceOutput;
import org.dozer.Mapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import rx.Observable;

import static org.apache.commons.lang.StringUtils.isBlank;
import static rx.Observable.error;

@Service
public class UrlShorteningService {
    private UserDao userDao;
    private UrlDao urlDao;
    private IdentifierDao identifierDao;
    private StatsService statsService;
    private Mapper beanMapper;
    private UrlShortenerConfiguration config;

    @Autowired
    public UrlShorteningService(UserDao userDao, UrlDao urlDao, IdentifierDao identifierDao, StatsService statsService, Mapper beanMapper, UrlShortenerConfiguration config) {
        this.userDao = userDao;
        this.urlDao = urlDao;
        this.identifierDao = identifierDao;
        this.statsService = statsService;
        this.beanMapper = beanMapper;
        this.config = config;
    }

    public Observable<String> getUrlById(String id){
        if(isBlank(id)) return error(new InvalidUrlIdentifierException());
        return new FetchUrlFromIdCommand(id, urlDao)
                .observe()
                .flatMap(url -> {
                    if (url == null) return error(new UrlDoesntExistException());
                    else {
                        statsService.registerUrlHit(id); // fire-and-forget.
                        return Observable.just(url);
                    }
                });
    }

    public Observable<Boolean> deleteUrlById(String id){
        return new DeleteUrlCommand(id, urlDao).observe();
    }

    public Observable<UrlInsertionStatsServiceOutput> addUrlToUser(String userId, String url){
        if(userId == null || url == null) return error(new UserValidationException("Invalid userId or Url provided", userId));

        return new UserExistsCommand(userId, userDao)
                .observe()
                .flatMap(exists -> {
                    if (exists) return new IdentifierRetrievalCommand(identifierDao).observe();
                    else return error(new UserDoesNotExistException());
                })
                .map(this::convertToBase36)
                .flatMap(id -> new UrlCreationCommand(id, userId, url, urlDao, config).observe())
                .map(urlCreationOutput -> {
                    statsService.registerUrlCreation(userId, urlCreationOutput.getId()); // fire-and-forget
                    return beanMapper.map(urlCreationOutput, UrlInsertionStatsServiceOutput.class);
                });
    }

    public Observable<Boolean> createUser(String userId){
        if(userId == null) return error(new UserValidationException("Invalid userId provided", userId));
        return new UserCreationCommand(userId, userDao).observe();
    }

    public Observable<Boolean> deleteUser(String userId){
        if(userId == null) return error(new UserValidationException("Invalid userId provided", userId));
        return new UserDeletionCommand(userId, userDao).observe();
    }

    private String convertToBase36(Long identifier){
        return Long.toString(identifier, 36);
    }

}
