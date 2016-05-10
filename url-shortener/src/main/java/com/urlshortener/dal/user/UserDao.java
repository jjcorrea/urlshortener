package com.urlshortener.dal.user;

public interface UserDao {
    void createUser(String key);
    boolean userExists(String key);
    void deleteUser(String userId);
}
