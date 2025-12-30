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
        connection = DriverManager.getConnection(url, user, password);

        try (Statement statement = connection.createStatement()) {
            statement.execute("""
                CREATE TABLE IF NOT EXISTS transactions (
                    id UUID PRIMARY KEY,
                    user_id UUID NOT NULL,
                    amount DOUBLE PRECISION NOT NULL,
                    date DATE NOT NULL,
                    type TEXT NOT NULL
                );
            """);
        }
    }

    @Override
    public Transaction findById(UUID transactionId) throws Exception {
        String sql = "SELECT * FROM transactions WHERE id = ?";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setObject(1, transactionId);

            ResultSet set = statement.executeQuery();
            if (!set.next()) {
                return null;
            }

            UUID id = set.getObject("id", UUID.class);
            UUID userId = set.getObject("user_id", UUID.class);
            double amount = set.getDouble("amount");
            LocalDate date = set.getDate("date").toLocalDate();
            TransactionType type = TransactionType.valueOf(set.getString("type"));

            return new Transaction(id, userId, amount, date, type);
        }
    }

    @Override
    public List<Transaction> findAll() throws Exception {
        List<Transaction> transactions = new ArrayList<>();

        try (Statement statement = connection.createStatement()) {
            ResultSet set = statement.executeQuery("SELECT * FROM transactions");

            while (set.next()) {
                UUID id = set.getObject("id", UUID.class);
                UUID userId = set.getObject("user_id", UUID.class);
                double amount = set.getDouble("amount");
                LocalDate date = set.getDate("date").toLocalDate();
                TransactionType type = TransactionType.valueOf(set.getString("type"));

                transactions.add(new Transaction(id, userId, amount, date, type));
            }
        }

        return transactions;
    }

    @Override
    public List<Transaction> findByUserId(UUID userId) throws Exception {
        List<Transaction> transactions = new ArrayList<>();
        String sql = "SELECT * FROM transactions WHERE user_id = ?";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setObject(1, userId);
            ResultSet set = statement.executeQuery();

            while (set.next()) {
                UUID id = set.getObject("id", UUID.class);
                UUID txUserId = set.getObject("user_id", UUID.class);
                double amount = set.getDouble("amount");
                LocalDate date = set.getDate("date").toLocalDate();
                TransactionType type = TransactionType.valueOf(set.getString("type"));

                transactions.add(new Transaction(id, txUserId, amount, date, type));
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

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setObject(1, transaction.getId());
                statement.setObject(2, transaction.getUserId());
                statement.setDouble(3, transaction.getAmount());
                statement.setDate(4, Date.valueOf(transaction.getDate()));
                statement.setString(5, transaction.getType().name());

                statement.executeUpdate();
            }
        } else {
            String sql = """
                UPDATE transactions
                SET amount = ?, date = ?, type = ?
                WHERE id = ?
            """;

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setDouble(1, transaction.getAmount());
                statement.setDate(2, Date.valueOf(transaction.getDate()));
                statement.setString(3, transaction.getType().name());
                statement.setObject(4, transaction.getId());

                statement.executeUpdate();
            }
        }
    }

    @Override
    public void delete(UUID transactionId) throws Exception {
        String sql = "DELETE FROM transactions WHERE id = ?";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setObject(1, transactionId);
            statement.executeUpdate();
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
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String result = String.format("User: %s | Amount: %.2f | Date: %s | Type: %s",
                        rs.getString("username"),
                        rs.getDouble("amount"),
                        rs.getDate("date"),
                        rs.getString("type"));
                results.add(result);
            }
        }
        return results;
    }

    @Override
    public UserTransactionStats getUserTransactionStats(UUID userId) throws Exception {
        String sql = """
            SELECT u.username,
                   COUNT(t.id) as transaction_count,
                   COALESCE(SUM(CASE WHEN t.type = 'INCOME' THEN t.amount ELSE 0 END), 0) as total_income,
                   COALESCE(SUM(CASE WHEN t.type = 'EXPENSE' THEN t.amount ELSE 0 END), 0) as total_expense
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
        String sql = "SELECT COALESCE(SUM(amount), 0) as balance FROM transactions WHERE user_id = ?";

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
        return this.connection;
    }
}