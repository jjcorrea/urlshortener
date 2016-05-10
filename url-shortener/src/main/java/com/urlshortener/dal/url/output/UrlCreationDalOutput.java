package com.urlshortener.dal.url.output;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UrlCreationDalOutput {
    private String id;
    private long hits;
    private String url;
    private String shortUrl;
}
