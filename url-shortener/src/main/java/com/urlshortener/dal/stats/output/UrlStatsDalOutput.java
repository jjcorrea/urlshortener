package com.urlshortener.dal.stats.output;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UrlStatsDalOutput {
    private String id;
    private long hits;
    private String url;
    private String shortUrl;
}
