package com.urlshortener.entrypoint.output;

import lombok.Data;

@Data
public class UrlStatsOutput {
    private String id;
    private Long hits;
    private String url;
    private String shortUrl;
}
