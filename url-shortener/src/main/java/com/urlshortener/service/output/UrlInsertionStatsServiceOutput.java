package com.urlshortener.service.output;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UrlInsertionStatsServiceOutput {
    private String id;
    private long hits;
    private String url;
    private String shortUrl;
}
