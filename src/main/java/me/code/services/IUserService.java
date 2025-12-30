package me.code.services;

import me.code.models.User;

import java.util.Optional;

public interface IUserService {
    User createUser(String username, String password) throws Exception;
    Optional<User> checkUserLogin(String username, String password) throws Exception;
}