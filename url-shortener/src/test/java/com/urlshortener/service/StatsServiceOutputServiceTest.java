package com.urlshortener.service;

import com.urlshortener.UrlShortenerApplication;
import com.urlshortener.configuration.UrlShortenerConfiguration;
import com.urlshortener.dal.stats.StatsDao;
import com.urlshortener.dal.url.UrlDao;
import com.urlshortener.dal.user.UserDao;
import com.urlshortener.service.output.StatsServiceOutput;
import com.urlshortener.service.output.StatsUrlServiceOutput;
import com.google.common.collect.Lists;
import lombok.SneakyThrows;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import rx.observers.TestSubscriber;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.Assert.assertEquals;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(UrlShortenerApplication.class)
public class StatsServiceOutputServiceTest {
    @Autowired
    private UrlShortenerConfiguration config;
    @Autowired
    private StatsService statsService;
    @Autowired
    private UserDao userDao;
    @Autowired
    private UrlDao urlDao;
    @Autowired
    private StatsDao statsDao;

    @Before
    public void init(){
        statsDao.init();
    }

    @Test
    public void getUserStatsWithSingleHitTest() {
        TestSubscriber<StatsServiceOutput> testSubscriber = new TestSubscriber<>();
        String userId = random();
        String url = random();
        String urlId = random();
        createUrlForUser(userId, url, urlId);
        hitUrl(urlId, 1);
        statsService.getUserStats(userId).subscribe(testSubscriber);

        testSubscriber.assertCompleted();
        testSubscriber.assertNoErrors();
        testSubscriber.assertValue(new StatsServiceOutput(1, 1, Lists.newArrayList(
            new StatsUrlServiceOutput(urlId, 1, url, config.shortUrl(urlId))
        )));
    }

    @Test
    public void getGlobalStatsTest() {
        TestSubscriber<StatsServiceOutput> testSubscriber = new TestSubscriber<>();
        String userId = random();
        String firstUrl = random();
        String firstUrlId = random();
        createUrlForUser(userId, firstUrl, firstUrlId);
        hitUrl(firstUrlId, 3);
        String secondUrlId = random();
        String secondUrl = random();
        createUrlForUser(random(), secondUrl, secondUrlId);
        hitUrl(secondUrlId, 1);

        statsService.getGlobalStats().subscribe(testSubscriber);
        testSubscriber.assertCompleted();
        testSubscriber.assertNoErrors();
        testSubscriber.assertValue(new StatsServiceOutput(4, 2, Lists.newArrayList(
            new StatsUrlServiceOutput(firstUrlId, 3, firstUrl, config.shortUrl(firstUrlId)),
            new StatsUrlServiceOutput(secondUrlId, 1, secondUrl, config.shortUrl(secondUrlId))
        )));
    }

    @Test
    public void userVsGlobalStatsTest() {
        TestSubscriber<StatsServiceOutput> globalStatsSubscriber = new TestSubscriber<>();
        TestSubscriber<StatsServiceOutput> firstUserStatsSubscriber = new TestSubscriber<>();
        TestSubscriber<StatsServiceOutput> secondUserStatsSubscriber = new TestSubscriber<>();
        String userId = random();
        String secondUserId = random();

        createMultipleUrlsForUser(20, userId);
        createMultipleUrlsForUser(10, secondUserId);

        statsService.getGlobalStats().subscribe(globalStatsSubscriber);
        statsService.getUserStats(userId).subscribe(firstUserStatsSubscriber);
        statsService.getUserStats(secondUserId).subscribe(secondUserStatsSubscriber);

        globalStatsSubscriber.assertCompleted();
        globalStatsSubscriber.assertNoErrors();
        firstUserStatsSubscriber.assertCompleted();
        firstUserStatsSubscriber.assertNoErrors();
        secondUserStatsSubscriber.assertCompleted();
        secondUserStatsSubscriber.assertNoErrors();
    }

    @Test
    public void veryComplexStatsScenarioTest() {
        TestSubscriber globalStatsSubscriber = new TestSubscriber<>();
        TestSubscriber firstUserStatsSubscriber = new TestSubscriber<>();
        TestSubscriber secondUserStatsSubscriber = new TestSubscriber<>();
        String firstUserId = random();
        String secondUserId = random();

        String firstUrlId = random();
        String secondUrlId = random();
        String thirdUrlId = random();
        String fourthUrlId = random();
        String fifthUrlId = random();
        String sixthUrlId = random();
        String seventhUrlId = random();
        String eighthUrlId = random();
        String ninethUrlId = random();
        String tenthUrlId = random();
        String eleventhUrlId = random();

        createUrlForUser(firstUserId, firstUrlId, firstUrlId);
        createUrlForUser(firstUserId, secondUrlId, secondUrlId);
        createUrlForUser(firstUserId, thirdUrlId, thirdUrlId);
        createUrlForUser(firstUserId, fourthUrlId, fourthUrlId);
        createUrlForUser(firstUserId, fifthUrlId, fifthUrlId);

        createUrlForUser(secondUserId, sixthUrlId, sixthUrlId);
        createUrlForUser(secondUserId, seventhUrlId, seventhUrlId);
        createUrlForUser(secondUserId, eighthUrlId, eighthUrlId);
        createUrlForUser(secondUserId, ninethUrlId, ninethUrlId);
        createUrlForUser(secondUserId, tenthUrlId, tenthUrlId);
        createUrlForUser(secondUserId, eleventhUrlId, eleventhUrlId);

        hitUrl(firstUrlId, 11);
        hitUrl(secondUrlId, 9);
        hitUrl(thirdUrlId, 7);
        hitUrl(fourthUrlId, 5);
        hitUrl(fifthUrlId, 3);

        hitUrl(sixthUrlId, 10);
        hitUrl(seventhUrlId, 8);
        hitUrl(eighthUrlId, 6);
        hitUrl(ninethUrlId, 4);
        hitUrl(tenthUrlId, 2);
        hitUrl(eleventhUrlId, 1);

        statsService.getGlobalStats().subscribe(globalStatsSubscriber);
        statsService.getUserStats(firstUserId).subscribe(firstUserStatsSubscriber);
        statsService.getUserStats(secondUserId).subscribe(secondUserStatsSubscriber);

        globalStatsSubscriber.assertCompleted();
        globalStatsSubscriber.assertNoErrors();
        globalStatsSubscriber.assertValue(new StatsServiceOutput(66, 11, Lists.newArrayList(
                new StatsUrlServiceOutput(firstUrlId, 11, firstUrlId, config.shortUrl(firstUrlId)),
                new StatsUrlServiceOutput(sixthUrlId, 10, sixthUrlId, config.shortUrl(sixthUrlId)),
                new StatsUrlServiceOutput(secondUrlId, 9, secondUrlId, config.shortUrl(secondUrlId)),
                new StatsUrlServiceOutput(seventhUrlId, 8, seventhUrlId, config.shortUrl(seventhUrlId)),
                new StatsUrlServiceOutput(thirdUrlId, 7, thirdUrlId, config.shortUrl(thirdUrlId)),
                new StatsUrlServiceOutput(eighthUrlId, 6, eighthUrlId, config.shortUrl(eighthUrlId)),
                new StatsUrlServiceOutput(fourthUrlId, 5, fourthUrlId, config.shortUrl(fourthUrlId)),
                new StatsUrlServiceOutput(ninethUrlId, 4, ninethUrlId, config.shortUrl(ninethUrlId)),
                new StatsUrlServiceOutput(fifthUrlId, 3, fifthUrlId, config.shortUrl(fifthUrlId)),
                new StatsUrlServiceOutput(tenthUrlId, 2, tenthUrlId, config.shortUrl(tenthUrlId))
        )));

        firstUserStatsSubscriber.assertCompleted();
        firstUserStatsSubscriber.assertNoErrors();
        firstUserStatsSubscriber.assertValue(new StatsServiceOutput(35, 5, Lists.newArrayList(
                new StatsUrlServiceOutput(firstUrlId, 11, firstUrlId, config.shortUrl(firstUrlId)),
                new StatsUrlServiceOutput(secondUrlId, 9, secondUrlId, config.shortUrl(secondUrlId)),
                new StatsUrlServiceOutput(thirdUrlId, 7, thirdUrlId, config.shortUrl(thirdUrlId)),
                new StatsUrlServiceOutput(fourthUrlId, 5, fourthUrlId, config.shortUrl(fourthUrlId)),
                new StatsUrlServiceOutput(fifthUrlId, 3, fifthUrlId, config.shortUrl(fifthUrlId))
        )));

        secondUserStatsSubscriber.assertCompleted();
        secondUserStatsSubscriber.assertNoErrors();
        secondUserStatsSubscriber.assertValue(new StatsServiceOutput(31, 6, Lists.newArrayList(
            new StatsUrlServiceOutput(sixthUrlId, 10, sixthUrlId, config.shortUrl(sixthUrlId)),
            new StatsUrlServiceOutput(seventhUrlId, 8, seventhUrlId, config.shortUrl(seventhUrlId)),
            new StatsUrlServiceOutput(eighthUrlId, 6, eighthUrlId, config.shortUrl(eighthUrlId)),
            new StatsUrlServiceOutput(ninethUrlId, 4, ninethUrlId, config.shortUrl(ninethUrlId)),
            new StatsUrlServiceOutput(tenthUrlId, 2, tenthUrlId, config.shortUrl(tenthUrlId)),
            new StatsUrlServiceOutput(eleventhUrlId, 1, eleventhUrlId, config.shortUrl(eleventhUrlId))
        )));
    }

    
    @Test
    public void urlStatsTest() {
        TestSubscriber statsSubscriber = new TestSubscriber<>();
        String url = random();
        String urlId = random();

        createUrlForUser(random(), url, urlId);
        hitUrl(urlId, 20);
        statsService.getUrlStats(urlId).subscribe(statsSubscriber);

        statsSubscriber.assertCompleted();
        statsSubscriber.assertNoErrors();
        statsSubscriber.assertValue(new StatsUrlServiceOutput(urlId, 20, url, config.shortUrl(urlId)));
    }

    @Test
    public void statsAfterUrlDeletionTest() throws Exception {
        String userid = random();
        String firstUrlId = random();
        String secondUrlId = random();

        createUrlForUser(userid, firstUrlId, firstUrlId);
        createUrlForUser(userid, secondUrlId, secondUrlId);
        hitUrl(firstUrlId, 20);
        hitUrl(secondUrlId, 10);

        StatsServiceOutput globalStats = statsService.getGlobalStats().toBlocking().toFuture().get();
        assertEquals(new StatsServiceOutput(30, 2, Lists.newArrayList(
                new StatsUrlServiceOutput(firstUrlId, 20, firstUrlId, config.shortUrl(firstUrlId)),
                new StatsUrlServiceOutput(secondUrlId, 10, secondUrlId, config.shortUrl(secondUrlId))
        )), globalStats);

        urlDao.deleteUrlByIdentifier(firstUrlId);
        StatsServiceOutput updatedGlobalStats = statsService.getGlobalStats().toBlocking().toFuture().get();
        assertEquals(new StatsServiceOutput(10, 1, Lists.newArrayList(
                new StatsUrlServiceOutput(secondUrlId, 10, secondUrlId, config.shortUrl(secondUrlId))
        )), updatedGlobalStats);
    }

    @Test
    public void statsAfterUserDeletionTest() throws Exception {
        String firstUserId = random();
        String secondUserId = random();
        String firstUrlId = random();
        String secondUrlId = random();
        String thirdUrlId = random();

        createUrlForUser(firstUserId, firstUrlId, firstUrlId);
        createUrlForUser(firstUserId, secondUrlId, secondUrlId);
        createUrlForUser(secondUserId, thirdUrlId, thirdUrlId);

        hitUrl(firstUrlId, 20);
        hitUrl(secondUrlId, 10);
        hitUrl(thirdUrlId, 5);

        StatsServiceOutput globalStats = statsService.getGlobalStats().toBlocking().toFuture().get();
        assertEquals(new StatsServiceOutput(35, 3, Lists.newArrayList(
                new StatsUrlServiceOutput(firstUrlId, 20, firstUrlId, config.shortUrl(firstUrlId)),
                new StatsUrlServiceOutput(secondUrlId, 10, secondUrlId, config.shortUrl(secondUrlId)),
                new StatsUrlServiceOutput(thirdUrlId, 5, thirdUrlId, config.shortUrl(thirdUrlId))
        )), globalStats);

        userDao.deleteUser(firstUserId);
        StatsServiceOutput updatedGlobalStats = statsService.getGlobalStats().toBlocking().toFuture().get();
        assertEquals(new StatsServiceOutput(5, 1, Lists.newArrayList(
            new StatsUrlServiceOutput(thirdUrlId, 5, thirdUrlId, config.shortUrl(thirdUrlId))
        )), updatedGlobalStats);
    }

    @SneakyThrows
    private List<Pair<String,String>> createMultipleUrlsForUser(int amount, String userId){
        return IntStream.rangeClosed(1, amount).mapToObj(i->{
            String id = random();
            String url = random();
            createUrlForUser(userId, url, id);
            return new ImmutablePair<>(id, url);
        }).collect(Collectors.toList());
    }

    @SneakyThrows
    private void createUrlForUser(String userId, String url, String urlId){
        userDao.createUser(userId);
        urlDao.registerUrlWithId(url, urlId, userId);
        statsService.registerUrlCreation(userId, urlId).get();
    }

    @SneakyThrows
    private void hitUrl(String urlId, int times){
        for(int i=1;i<=times;i++) statsService.registerUrlHit(urlId).get();
    }

    private String random() {
        return UUID.randomUUID().toString();
    }

}