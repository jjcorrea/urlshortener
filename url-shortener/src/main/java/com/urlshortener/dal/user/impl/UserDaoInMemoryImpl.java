package com.urlshortener.dal.user.impl;

import com.urlshortener.dal.stats.StatsDao;
import com.urlshortener.dal.user.UserDao;
import io.vertx.core.impl.ConcurrentHashSet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

@Profile("in-memory")
@Repository
public class UserDaoInMemoryImpl implements UserDao {
    private ConcurrentHashSet<String> users = new ConcurrentHashSet<>();

    @Autowired
    private StatsDao statsDao;

    @Override
    public void createUser(String key) {
        users.add(key);
    }

    @Override
    public boolean userExists(String key) {
        return users.contains(key);
    }

    @Override
    public void deleteUser(String userId) {
        users.remove(userId);
        statsDao.deleteUserStats(userId);
    }
}
