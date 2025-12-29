package me.code.repositories;

import me.code.models.Transaction;
import me.code.models.TransactionType;

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
            double amount = set.getDouble("amount");
            LocalDate date = set.getDate("date").toLocalDate();
            TransactionType type = TransactionType.valueOf(set.getString("type"));

            return new Transaction(id, amount, date, type);
        }
    }

    @Override
    public List<Transaction> findAll() throws Exception {
        List<Transaction> transactions = new ArrayList<>();

        try (Statement statement = connection.createStatement()) {
            ResultSet set = statement.executeQuery("SELECT * FROM transactions");

            while (set.next()) {
                UUID id = set.getObject("id", UUID.class);
                double amount = set.getDouble("amount");
                LocalDate date = set.getDate("date").toLocalDate();
                TransactionType type = TransactionType.valueOf(set.getString("type"));

                transactions.add(new Transaction(id, amount, date, type));
            }
        }

        return transactions;
    }

    @Override
    public void save(Transaction transaction) throws Exception {
        Transaction existing = findById(transaction.getId());

        if (existing == null) {
            String sql = """
                INSERT INTO transactions (id, amount, date, type)
                VALUES (?, ?, ?, ?)
            """;

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setObject(1, transaction.getId());
                statement.setDouble(2, transaction.getAmount());
                statement.setDate(3, Date.valueOf(transaction.getDate()));
                statement.setString(4, transaction.getType().name());

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
}
