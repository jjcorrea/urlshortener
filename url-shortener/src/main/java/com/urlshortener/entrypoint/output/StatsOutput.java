package com.urlshortener.entrypoint.output;

import lombok.Data;

import java.util.List;

@Data
public class StatsOutput {
    private Long hits;
    private Long urlCount;
    private List<UrlStatsOutput> topUrls;
}
