package com.urlshortener.router;

import com.urlshortener.entrypoint.UrlShortenerEntryPoint;
import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class UrlShortenerRouter {
    private Router router;
    private UrlShortenerEntryPoint entrypoint;
    private Vertx vertx;

    @Autowired
    public UrlShortenerRouter(UrlShortenerEntryPoint entrypoint, Vertx vertx) {
        this.entrypoint = entrypoint;
        this.vertx = vertx;
    }

    @PostConstruct
    public void routes(){
        router = Router.router(vertx);
        router.route().handler(BodyHandler.create());
        urlRoutes();
        userRoutes();
        statsRoutes();
    }

    private void urlRoutes() {
        router.get("/url/:id").handler(entrypoint::getUrlById);
        router.delete("/url/:id").handler(entrypoint::deleteUrlById);
    }

    private void userRoutes() {
        router.post("/users/:userId/urls").handler(entrypoint::addUrlToUser);
        router.get("/user/:userId/stats").handler(entrypoint::getUserStats);
        router.post("/user").handler(entrypoint::createUser);
        router.delete("/user/:userId").handler(entrypoint::deleteUser);
    }

    private void statsRoutes() {
        router.get("/stats").handler(entrypoint::getGlobalStats);
        router.get("/stats/:id").handler(entrypoint::getUrlStats);
    }

    public Router getRouter() {
        return router;
    }
}
