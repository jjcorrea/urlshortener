package com.urlshortener.dal.url;

public interface UrlDao {
    void registerUrlWithId(String url, String identifier, String userId);
    void deleteUrlByIdentifier(String identifier);
    String fetchUrlByIdentifier(String identifier);
    String fetchUrlOwnerByIdentifier(String identifier);
}
