package com.urlshortener.entrypoint;

import com.urlshortener.entrypoint.input.InputUrl;
import com.urlshortener.entrypoint.input.InputUser;
import com.urlshortener.entrypoint.output.RequestFailed;
import com.urlshortener.entrypoint.output.StatsOutput;
import com.urlshortener.entrypoint.output.UrlStatsOutput;
import com.urlshortener.exception.service.UrlDoesntExistException;
import com.urlshortener.service.StatsService;
import com.urlshortener.service.UrlShorteningService;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.Json;
import io.vertx.ext.web.RoutingContext;
import org.apache.log4j.Logger;
import org.dozer.Mapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static io.netty.handler.codec.http.HttpResponseStatus.*;
import static io.vertx.core.json.Json.encodePrettily;

@Component
public class UrlShortenerEntryPoint {
    private static final Logger log = Logger.getLogger(UrlShortenerEntryPoint.class);
    private UrlShorteningService urlShorteningService;
    private StatsService statsService;
    private Mapper objectMapper;

    @Autowired
    public UrlShortenerEntryPoint(UrlShorteningService urlShorteningService, StatsService statsService, Mapper objectMapper) {
        this.urlShorteningService = urlShorteningService;
        this.statsService = statsService;
        this.objectMapper = objectMapper;
    }

    public void getUrlById(RoutingContext routingContext){
        String id = routingContext.request().getParam("id");
        urlShorteningService.getUrlById(id)
            .subscribe(
                result -> {
                    log.debug(String.format("Successfully retrieved url from id: %s", id));
                    httpServerResponse(MOVED_PERMANENTLY.code(), routingContext).putHeader("Location", result).end();
                },
                exception -> {
                    if( exception.getClass().equals(UrlDoesntExistException.class)){
                        log.error(String.format("Inexistent id provided: %s", id));
                        httpServerResponse(NOT_FOUND.code(), routingContext).end();
                    } else {
                        log.error("An error occured while retrieving url");
                        httpServerResponse(INTERNAL_SERVER_ERROR.code(), routingContext).end();
                    }
                });
    }

    public void deleteUrlById(RoutingContext routingContext){
        String id = routingContext.request().getParam("id");
        urlShorteningService.deleteUrlById(id).subscribe(
                result -> {
                    log.debug(String.format("Successfully deleted url with id: %s", id));
                    httpServerResponse(NO_CONTENT.code(), routingContext).end();
                },
                exception -> {
                    log.error("An error occured while deleting url");
                    httpServerResponse(INTERNAL_SERVER_ERROR.code(), routingContext).end();
                });
    }

    public void addUrlToUser(RoutingContext routingContext){
        String userId = routingContext.request().getParam("userId");
        InputUrl url = Json.decodeValue(routingContext.getBodyAsString(), InputUrl.class);

        urlShorteningService.addUrlToUser(userId, url.getUrl())
            .subscribe(
                    result -> {
                        log.debug(String.format("Successfully added url %s to user: %s", url, userId));
                        httpServerResponse(CREATED.code(), routingContext)
                            .end(encodePrettily(objectMapper.map(result, UrlStatsOutput.class)));
                    },
                    exception -> {
                        log.error("Unable to process request");
                        httpServerResponse(BAD_REQUEST.code(), routingContext).end(encodePrettily(new RequestFailed("Unable to create user. Try again later.")));
                    });
    }

    public void getUserStats(RoutingContext routingContext){
        String userId = routingContext.request().getParam("userId");
        statsService.getUserStats(userId).subscribe(
            result -> {
                log.debug(String.format("Successfully retrieved stats for user: %s", userId));
                httpServerResponse(OK.code(), routingContext)
                    .end(encodePrettily(objectMapper.map(result, StatsOutput.class)));
            },
            exception -> {
                log.error("Unable to retrieve user stats");
                httpServerResponse(INTERNAL_SERVER_ERROR.code(), routingContext).end(encodePrettily(new RequestFailed("Unable to fetch user stats. Try again later.")));
            });
    }

    public void createUser(RoutingContext routingContext){
        InputUser user = Json.decodeValue(routingContext.getBodyAsString(), InputUser.class);
        urlShorteningService.createUser(user.getId())
            .subscribe(
                    result -> {
                        log.debug(String.format("Successfully created user: %s", user.getId()));
                        httpServerResponse(CREATED.code(), routingContext).end();
                    },
                    exception -> {
                        log.error("Unable to process request", exception);
                        httpServerResponse(BAD_REQUEST.code(), routingContext).end(encodePrettily(new RequestFailed("Unable to create user. Try again later.")));
                    });
    }

    public void deleteUser(RoutingContext routingContext){
        String userId = routingContext.request().getParam("userId");
        urlShorteningService.deleteUser(userId).subscribe(
            result -> {
                log.debug(String.format("Successfully deleted user: %s", userId));
                httpServerResponse(NO_CONTENT.code(), routingContext).end();
            },
            exception -> {
                log.error("Unable to process request", exception);
                httpServerResponse(BAD_REQUEST.code(), routingContext).end(encodePrettily(new RequestFailed("Unable to delete user. Try again later.")));
        });
    }

    public void getUrlStats(RoutingContext routingContext){
        String urlId = routingContext.request().getParam("id");
        statsService.getUrlStats(urlId).subscribe(
            result -> {
                log.debug(String.format("Successfully retrieved stats for url: %s", urlId));
                httpServerResponse(OK.code(), routingContext)
                    .end(encodePrettily(objectMapper.map(result, UrlStatsOutput.class)));
            },
            exception -> {
                log.error("Unable to retrieve url stats");
                httpServerResponse(INTERNAL_SERVER_ERROR.code(), routingContext).end(encodePrettily(new RequestFailed("Unable to fetch url stats. Try again later.")));
            });
    }

    public void getGlobalStats(RoutingContext routingContext){
        statsService.getGlobalStats().subscribe(
                result -> {
                    log.debug("Successfully retrieved global stats");
                    httpServerResponse(OK.code(), routingContext)
                        .end(encodePrettily(objectMapper.map(result, StatsOutput.class)));
                },
                exception -> {
                    log.error("Unable to retrieve global stats");
                    httpServerResponse(INTERNAL_SERVER_ERROR.code(), routingContext).end(encodePrettily(new RequestFailed("Unable to fetch global stats. Try again later.")));
                });
    }

    private HttpServerResponse httpServerResponse(int statusCode, RoutingContext routingContext) {
        return routingContext.response()
                .setStatusCode(statusCode)
                .putHeader("content-type", "application/json; charset=utf-8");
    }

}
