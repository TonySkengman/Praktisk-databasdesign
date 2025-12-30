package me.code.repositories;

import me.code.models.Transaction;
import me.code.models.TransactionType;
import me.code.models.UserTransactionStats;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PostgreTransactionRepository implements ITransactionRepository {

    private final Connection connection;

    public PostgreTransactionRepository(String url, String user, String password) throws SQLException {
        this.connection = DriverManager.getConnection(url, user, password);

        try (Statement statement = connection.createStatement()) {
            statement.execute("""
                CREATE TABLE IF NOT EXISTS transactions (
                    id UUID PRIMARY KEY,
                    user_id UUID NOT NULL,
                    amount DOUBLE PRECISION NOT NULL,
                    date DATE NOT NULL,
                    type TEXT NOT NULL,
                    CONSTRAINT fk_transactions_user
                        FOREIGN KEY (user_id)
                        REFERENCES users(id)
                        ON DELETE CASCADE
                );
            """);
        }
    }

    @Override
    public Transaction findById(UUID transactionId) throws Exception {
        String sql = "SELECT * FROM transactions WHERE id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setObject(1, transactionId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }

                UUID id = rs.getObject("id", UUID.class);
                UUID userId = rs.getObject("user_id", UUID.class);
                double amount = rs.getDouble("amount");
                LocalDate date = rs.getDate("date").toLocalDate();
                TransactionType type = TransactionType.valueOf(rs.getString("type"));

                return new Transaction(id, userId, amount, date, type);
            }
        }
    }

    @Override
    public List<Transaction> findAll() throws Exception {
        List<Transaction> transactions = new ArrayList<>();

        String sql = "SELECT * FROM transactions";

        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                UUID id = rs.getObject("id", UUID.class);
                UUID userId = rs.getObject("user_id", UUID.class);
                double amount = rs.getDouble("amount");
                LocalDate date = rs.getDate("date").toLocalDate();
                TransactionType type = TransactionType.valueOf(rs.getString("type"));

                transactions.add(new Transaction(id, userId, amount, date, type));
            }
        }

        return transactions;
    }

    @Override
    public List<Transaction> findByUserId(UUID userId) throws Exception {
        List<Transaction> transactions = new ArrayList<>();

        String sql = "SELECT * FROM transactions WHERE user_id = ? ORDER BY date";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setObject(1, userId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    UUID id = rs.getObject("id", UUID.class);
                    UUID txUserId = rs.getObject("user_id", UUID.class);
                    double amount = rs.getDouble("amount");
                    LocalDate date = rs.getDate("date").toLocalDate();
                    TransactionType type = TransactionType.valueOf(rs.getString("type"));

                    transactions.add(new Transaction(id, txUserId, amount, date, type));
                }
            }
        }

        return transactions;
    }

    @Override
    public void save(Transaction transaction) throws Exception {
        Transaction existing = findById(transaction.getId());

        if (existing == null) {
            String sql = """
                INSERT INTO transactions (id, user_id, amount, date, type)
                VALUES (?, ?, ?, ?, ?)
            """;

            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setObject(1, transaction.getId());
                stmt.setObject(2, transaction.getUserId());
                stmt.setDouble(3, transaction.getAmount());
                stmt.setDate(4, Date.valueOf(transaction.getDate()));
                stmt.setString(5, transaction.getType().name());

                stmt.executeUpdate();
            }
        } else {
            String sql = """
                UPDATE transactions
                SET amount = ?, date = ?, type = ?
                WHERE id = ?
            """;

            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setDouble(1, transaction.getAmount());
                stmt.setDate(2, Date.valueOf(transaction.getDate()));
                stmt.setString(3, transaction.getType().name());
                stmt.setObject(4, transaction.getId());

                stmt.executeUpdate();
            }
        }
    }

    @Override
    public void delete(UUID transactionId) throws Exception {
        String sql = "DELETE FROM transactions WHERE id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setObject(1, transactionId);
            stmt.executeUpdate();
        }
    }

    @Override
    public List<String> getTransactionsWithUsername(UUID userId) throws Exception {
        String sql = """
            SELECT t.id, t.amount, t.date, t.type, u.username
            FROM transactions t
            JOIN users u ON t.user_id = u.id
            WHERE t.user_id = ?
            ORDER BY t.date
        """;

        List<String> results = new ArrayList<>();

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setObject(1, userId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    results.add(String.format(
                            "User: %s | Amount: %.2f | Date: %s | Type: %s",
                            rs.getString("username"),
                            rs.getDouble("amount"),
                            rs.getDate("date"),
                            rs.getString("type")
                    ));
                }
            }
        }

        return results;
    }

    @Override
    public UserTransactionStats getUserTransactionStats(UUID userId) throws Exception {
        String sql = """
            SELECT u.username,
                   COUNT(t.id) AS transaction_count,
                   COALESCE(SUM(CASE WHEN t.type = 'INCOME' THEN t.amount ELSE 0 END), 0) AS total_income,
                   COALESCE(SUM(CASE WHEN t.type = 'EXPENSE' THEN t.amount ELSE 0 END), 0) AS total_expense
            FROM users u
            LEFT JOIN transactions t ON u.id = t.user_id
            WHERE u.id = ?
            GROUP BY u.username
        """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setObject(1, userId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new UserTransactionStats(
                            rs.getString("username"),
                            rs.getInt("transaction_count"),
                            rs.getDouble("total_income"),
                            rs.getDouble("total_expense")
                    );
                }
            }
        }

        throw new Exception("User not found");
    }

    @Override
    public double getBalanceForUser(UUID userId) throws Exception {
        String sql = "SELECT COALESCE(SUM(amount), 0) AS balance FROM transactions WHERE user_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setObject(1, userId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("balance");
                }
            }
        }

        return 0.0;
    }

    @Override
    public String getUsernameById(UUID userId) throws Exception {
        String sql = "SELECT username FROM users WHERE id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setObject(1, userId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("username");
                }
            }
        }

        return "Unknown";
    }

    public Connection getConnection() {
        return connection;
    }
}
