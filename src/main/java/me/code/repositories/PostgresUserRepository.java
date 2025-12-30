package me.code.repositories;

import me.code.models.User; // CHANGE THIS LINE

import java.sql.*;
import java.util.Optional;
import java.util.UUID;

public class PostgresUserRepository implements IUserRepository {

    private Connection connection;

    public PostgresUserRepository(String url, String user, String password) throws SQLException {
        connection = DriverManager.getConnection(url, user, password);

        try (Statement statement = connection.createStatement()) {
            statement.execute("CREATE TABLE IF NOT EXISTS users (" +
                    "id UUID PRIMARY KEY," +
                    "username TEXT," +
                    "password_hash TEXT," +
                    "created_at TIMESTAMP)");
        }
    }

    @Override
    public Optional<User> findByUsername(String username) throws Exception {
        String sql = "SELECT * FROM users WHERE username = ?";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, username);

            ResultSet set = statement.executeQuery();
            if (!set.next()) {
                return Optional.empty();
            }

            UUID id = set.getObject("id", UUID.class);
            username = set.getString("username");
            String passwordHash = set.getString("password_hash");
            java.util.Date createdAt = set.getTimestamp("created_at");

            User user = new User(id, username, passwordHash, createdAt); // REMOVE ArrayList parameter
            return Optional.of(user);
        }
    }

    @Override
    public void save(User user) throws Exception {
        String sql = "INSERT INTO users (id, username, password_hash, created_at) VALUES (?, ?, ?, ?)";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setObject(1, user.getId());
            statement.setString(2, user.getUsername());
            statement.setString(3, user.getPasswordHash());
            statement.setTimestamp(4, new Timestamp(user.getCreatedAt().getTime())); // FIX THIS

            if (statement.executeUpdate() != 1) {
                throw new SQLException("Failed to insert user");
            }
        }
    }
}