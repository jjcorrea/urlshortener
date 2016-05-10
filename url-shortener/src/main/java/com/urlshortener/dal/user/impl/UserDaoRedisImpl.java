package com.urlshortener.dal.user.impl;

import com.urlshortener.dal.stats.StatsDao;
import com.urlshortener.dal.user.UserDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

@Profile("redis")
@Repository
public class UserDaoRedisImpl implements UserDao {
    private static final String USERS_SET_KEY = "users";

    @Autowired
    private StatsDao statsDao;

    @Resource(name="redisTemplate")
    private SetOperations<String, String> setOps;

    @Override
    public void createUser(String key){
        setOps.add(USERS_SET_KEY, key);
    }

    @Override
    public boolean userExists(String key){
        return setOps.isMember(USERS_SET_KEY, key);
    }

    @Override
    @Transactional
    public void deleteUser(String userId) {
        statsDao.deleteUserStats(userId);
        setOps.remove(USERS_SET_KEY, userId);
    }
}
