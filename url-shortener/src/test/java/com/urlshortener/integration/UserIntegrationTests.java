package com.urlshortener.integration;

import com.urlshortener.UrlShortenerApplication;
import com.urlshortener.configuration.UrlShortenerConfiguration;
import com.urlshortener.entrypoint.input.InputUrl;
import com.urlshortener.entrypoint.input.InputUser;
import com.urlshortener.entrypoint.output.UrlStatsOutput;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.UUID;

import static io.netty.handler.codec.http.HttpResponseStatus.*;
import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.assertEquals;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(UrlShortenerApplication.class)
public class UserIntegrationTests {
    private static TestRestTemplate restTemplate = new TestRestTemplate();

    @Autowired
    private UrlShortenerConfiguration config;

    @BeforeClass
    public static void init(){
        restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
    }

    @Test
    public void userCreationTest(){
        ResponseEntity<String> response = createUser(random());
        assertEquals(CREATED.code(), response.getStatusCode().value());
    }

    @Test
    public void invalidUserCreationTest(){
        ResponseEntity<String> response = createUser(null);
        assertEquals(BAD_REQUEST.code(), response.getStatusCode().value());
    }

    @Test
    public void urlCreationTest(){
        String userId = random();
        createUser(userId);
        ResponseEntity<UrlStatsOutput> response = createUrlForUser(userId, random());
        UrlStatsOutput body = response.getBody();

        assertEquals(CREATED.code(), response.getStatusCode().value());
        assertNotNull(body.getId());
        assertEquals(0, body.getHits().intValue());
        assertNotNull(body.getUrl());
        assertNotNull(body.getShortUrl());
    }

    @Test
    public void urlCreationInvalidUserTest(){
        String randomUser = random();
        ResponseEntity<UrlStatsOutput> response = createUrlForUser(randomUser, random());
        assertEquals(BAD_REQUEST.code(), response.getStatusCode().value());
    }

    @Test
    public void invalidUrlRetrievalTest(){
        ResponseEntity<String> response = get(config.getBaseUrl() + "/url/" + random());
        assertEquals(NOT_FOUND.code(), response.getStatusCode().value());
    }

    @Test
    public void urlDeletionTest(){
        String userId = random();
        createUser(userId);
        ResponseEntity<UrlStatsOutput> url = createUrlForUser(userId, random());
        ResponseEntity<String> deletion = delete(config.getBaseUrl() + "/url/" + url.getBody().getId());
        assertEquals(NO_CONTENT.code(), deletion.getStatusCode().value());
    }

    @Test
    public void userDeletionTest(){
        String userId = random();
        createUser(userId);
        ResponseEntity<String> deletion = delete(config.getBaseUrl() + "/user/" + userId);
        assertEquals(NO_CONTENT.code(), deletion.getStatusCode().value());
    }

    private ResponseEntity<String> createUser(String user) {
        return post(config.getBaseUrl() + "/user", new HttpEntity<>(new InputUser(user)));
    }

    private ResponseEntity<UrlStatsOutput> createUrlForUser(String userId, String url) {
        return restTemplate.postForEntity(
                config.getBaseUrl() + "/users/" + userId + "/urls",
                new HttpEntity<>(new InputUrl(url)),
                UrlStatsOutput.class);
    }

    private ResponseEntity<String> post(String resource, HttpEntity requestEntity) {
        return exchange(resource, requestEntity, HttpMethod.POST);
    }

    private ResponseEntity<String> delete(String resource) {
        return exchange(resource, null, HttpMethod.DELETE);
    }

    private ResponseEntity<String> get(String resource) {
        return exchange(resource, null, HttpMethod.GET);
    }

    private ResponseEntity<String> exchange(String resource, HttpEntity requestEntity, HttpMethod method) {
        return restTemplate.exchange(resource, method, requestEntity, String.class);
    }

    private String random() {
        return UUID.randomUUID().toString();
    }
}
