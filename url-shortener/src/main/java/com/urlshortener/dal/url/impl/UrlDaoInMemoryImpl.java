package com.urlshortener.dal.url.impl;

import com.urlshortener.dal.stats.StatsDao;
import com.urlshortener.dal.url.UrlDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Profile("in-memory")
@Repository
public class UrlDaoInMemoryImpl implements UrlDao {

    private static ConcurrentMap<String, String> data = new ConcurrentHashMap<>();

    @Autowired
    private StatsDao statsDao;

    @Override
    public void registerUrlWithId(String url, String identifier, String userId) {
        data.put("id:"+identifier, url);
        data.put("url:"+url, identifier);
        data.put("owner:url:"+identifier, userId);
    }

    @Override
    public void deleteUrlByIdentifier(String identifier) {
        String url = data.get("id:" + identifier);
        statsDao.deleteUrlStats(identifier);
        data.remove("id:" + identifier);
        data.remove("url:" + url);
        data.remove("owner:url:" + identifier);
    }

    @Override
    public String fetchUrlByIdentifier(String identifier) {
        return data.get("id:"+identifier);
    }

    @Override
    public String fetchUrlOwnerByIdentifier(String identifier) {
        return data.get("owner:url:" + identifier);
    }

    public static ConcurrentMap<String, String> getData() {
        return data;
    }
}
