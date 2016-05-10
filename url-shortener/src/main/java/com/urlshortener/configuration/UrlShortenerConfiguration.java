package com.urlshortener.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix="shortener")
@Data
public class UrlShortenerConfiguration {
    private String protocol;
    private String host;
    private Integer port;

    public String getBaseUrl(){
        return protocol + "://" + host + ":" + port;
    }

    public String shortUrl(String identifier){
        return getBaseUrl()+"/url/"+identifier;
    }
}
