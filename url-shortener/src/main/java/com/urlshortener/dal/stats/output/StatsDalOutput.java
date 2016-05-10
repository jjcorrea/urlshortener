package com.urlshortener.dal.stats.output;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StatsDalOutput {
    private String hits;
    private String urlCount;
    private List<UrlStatsDalOutput> topUrls;
}
