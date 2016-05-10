package com.urlshortener;

import com.urlshortener.router.UrlShortenerRouter;
import io.vertx.core.Vertx;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import io.vertx.ext.web.Router;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

@Component
public class VertxServer {
    private Vertx vertx;

    @Value("${shortener.port}")
    private Integer port;

    @Autowired
    private UrlShortenerRouter urlShortenerRouter;

    @Resource
    public void setVertx(Vertx vertx) {
        this.vertx = vertx;
    }

    @PostConstruct
    public void createServer(){
        Router router = urlShortenerRouter.getRouter();
        vertx.createHttpServer().requestHandler(router::accept).listen(port);
    }
}
