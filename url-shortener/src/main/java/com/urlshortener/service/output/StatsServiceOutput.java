package com.urlshortener.service.output;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StatsServiceOutput {
    private long hits;
    private long urlCount;
    private List<StatsUrlServiceOutput> topUrls;
}
