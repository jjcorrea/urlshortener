package com.urlshortener.service;

import com.urlshortener.UrlShortenerApplication;
import com.urlshortener.exception.service.InvalidUrlIdentifierException;
import com.urlshortener.exception.service.UrlDoesntExistException;
import com.urlshortener.exception.service.UserDoesNotExistException;
import com.urlshortener.exception.service.UserValidationException;
import com.urlshortener.service.output.UrlInsertionStatsServiceOutput;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import rx.Observable;
import rx.observers.TestSubscriber;

import java.util.UUID;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(UrlShortenerApplication.class)
public class UrlShorteningServiceTest {
    private static final String URL = "http://google.com";
    private static final String USER_ID = "12345";

    @Autowired
    private UrlShorteningService urlShorteningService;

    @Test
    public void testCreateUser() throws Exception {
        TestSubscriber<Boolean> testSubscriber = new TestSubscriber<>();
        Observable<Boolean> userCreation = urlShorteningService.createUser(USER_ID);
        userCreation.subscribe(testSubscriber);
        testSubscriber.assertCompleted();
        testSubscriber.assertNoErrors();
    }

    @Test
    public void testCreateUserInvalidId() throws Exception {
        TestSubscriber<Boolean> testSubscriber = new TestSubscriber<>();
        Observable<Boolean> userCreation = urlShorteningService.createUser(null);
        userCreation.subscribe(testSubscriber);
        testSubscriber.assertError(UserValidationException.class);
    }

    @Test
    public void testCreateUrl() throws Exception {
        TestSubscriber<UrlInsertionStatsServiceOutput> testSubscriber = new TestSubscriber<>();
        Observable<UrlInsertionStatsServiceOutput> urlAddition = urlShorteningService.addUrlToUser(USER_ID, URL);
        urlAddition.subscribe(testSubscriber);
        testSubscriber.assertCompleted();
        testSubscriber.assertNoErrors();
    }

    @Test
    public void testCreateUrlForInvalidUser() throws Exception {
        TestSubscriber<UrlInsertionStatsServiceOutput> testSubscriber = new TestSubscriber<>();
        String invalidUserId = UUID.randomUUID().toString();
        Observable<UrlInsertionStatsServiceOutput> urlAddition = urlShorteningService.addUrlToUser(invalidUserId, URL);
        urlAddition.subscribe(testSubscriber);
        testSubscriber.assertError(UserDoesNotExistException.class);
    }

    @Test
    public void testCreateUrlForEmptyUrl() throws Exception {
        TestSubscriber<UrlInsertionStatsServiceOutput> testSubscriber = new TestSubscriber<>();
        String invalidUserId = UUID.randomUUID().toString();
        Observable<UrlInsertionStatsServiceOutput> urlAddition = urlShorteningService.addUrlToUser(invalidUserId, null);
        urlAddition.subscribe(testSubscriber);
        testSubscriber.assertError(UserValidationException.class);
    }

    @Test
    public void testCreateUrlForEmptyUser() throws Exception {
        TestSubscriber<UrlInsertionStatsServiceOutput> testSubscriber = new TestSubscriber<>();
        Observable<UrlInsertionStatsServiceOutput> urlAddition = urlShorteningService.addUrlToUser(null, URL);
        urlAddition.subscribe(testSubscriber);
        testSubscriber.assertError(UserValidationException.class);
    }

    @Test
    public void testFetchUrlFromId() throws Exception {
        TestSubscriber<String> testSubscriber = new TestSubscriber<>();
        urlShorteningService
            .createUser(USER_ID)
            .flatMap(userCreation ->urlShorteningService.addUrlToUser(USER_ID, URL))
            .flatMap(urlInsertionStats -> urlShorteningService.getUrlById(urlInsertionStats.getId()))
            .subscribe(testSubscriber);
        testSubscriber.assertCompleted();
        testSubscriber.assertNoErrors();
    }

    @Test
    public void testFetchUrlFromInvalidId() throws Exception {
        TestSubscriber<String> testSubscriber = new TestSubscriber<>();
        Observable<String> urlAddition = urlShorteningService.getUrlById(UUID.randomUUID().toString());
        urlAddition.subscribe(testSubscriber);
        testSubscriber.assertError(UrlDoesntExistException.class);
    }

    @Test
    public void testFetchUrlFromInvalidParameters() throws Exception {
        TestSubscriber<String> testSubscriber = new TestSubscriber<>();
        Observable<String> urlAddition = urlShorteningService.getUrlById(null);
        urlAddition.subscribe(testSubscriber);
        testSubscriber.assertError(InvalidUrlIdentifierException.class);
    }

}
