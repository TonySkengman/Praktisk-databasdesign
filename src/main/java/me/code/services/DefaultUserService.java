package me.code.services;

import at.favre.lib.crypto.bcrypt.BCrypt;
import me.code.models.User; // CHANGE THIS LINE
import me.code.repositories.IUserRepository;

import java.util.Optional;

public class DefaultUserService implements IUserService {

    private final IUserRepository userRepository;

    public DefaultUserService(IUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public User createUser(String username, String password) throws Exception {
        Optional<User> existing = userRepository.findByUsername(username);

        if (existing.isPresent()) {
            throw new Exception("User already exists: " + username);
        }

        String passwordHash = BCrypt
                .withDefaults()
                .hashToString(12, password.toCharArray());

        User user = new User(username, passwordHash);
        userRepository.save(user);
        return user;
    }

    @Override
    public Optional<User> checkUserLogin(String username, String password) throws Exception {
        Optional<User> optional = userRepository.findByUsername(username);

        if (optional.isEmpty()) {
            return Optional.empty();
        }

        User user = optional.get();

        BCrypt.Result result = BCrypt
                .verifyer()
                .verify(password.toCharArray(), user.getPasswordHash());

        if (!result.verified) {
            return Optional.empty();
        }

        return Optional.of(user);
    }
}