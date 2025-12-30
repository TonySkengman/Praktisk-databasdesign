package me.code.repositories;

import me.code.models.User;

import java.util.Optional;

public interface IUserRepository {
    Optional<User> findByUsername(String username) throws Exception;
    void save(User user) throws Exception;
}