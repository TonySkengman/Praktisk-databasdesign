package me.code.commands;

import me.code.models.Transaction;
import me.code.models.TransactionType;
import me.code.services.AuthService;
import me.code.services.ITransactionService;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.Scanner;
import java.util.UUID;

public class ListTransactionsCommand extends Command {

    private final Connection connection;

    public ListTransactionsCommand(ITransactionService transactionService, Connection connection) {
        super("list-transactions", "List all saved transactions", transactionService);
        this.connection = connection;
    }

    @Override
    public void execute() {
        Scanner scanner = new Scanner(System.in);
        TransactionType type = null;
        boolean showAll = false;

        while (type == null && !showAll) {
            System.out.print("Enter transaction type (income/expense/all): ");
            String input = scanner.nextLine().trim().toLowerCase();
            switch (input) {
                case "income" -> type = TransactionType.INCOME;
                case "expense" -> type = TransactionType.EXPENSE;
                case "all" -> showAll = true;
                default -> System.out.println("Invalid input.");
            }
        }

        System.out.println("Select period: (overall/yearly/monthly/weekly/daily)");
        String period = scanner.nextLine().trim().toLowerCase();

        try {
            switch (period) {
                case "overall" -> executeOverallQuery(showAll, type);
                case "yearly" -> {
                    System.out.print("Enter year: ");
                    executeYearlyQuery(Integer.parseInt(scanner.nextLine()), showAll, type);
                }
                case "monthly" -> {
                    System.out.print("Enter year: ");
                    int year = Integer.parseInt(scanner.nextLine());
                    System.out.print("Enter month (1-12): ");
                    int month = Integer.parseInt(scanner.nextLine());
                    executeMonthlyQuery(year, month, showAll, type);
                }
                case "weekly" -> {
                    System.out.print("Enter year: ");
                    executeWeeklyQuery(Integer.parseInt(scanner.nextLine()), showAll, type);
                }
                case "daily" -> {
                    System.out.print("Enter date (YYYY-MM-DD): ");
                    executeDailyQuery(LocalDate.parse(scanner.nextLine()), showAll, type);
                }
                default -> System.out.println("Unknown period.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Something went wrong.");
        }
    }

    private void executeOverallQuery(boolean showAll, TransactionType type) throws Exception {
        String sql = "SELECT * FROM transactions WHERE user_id = ?";
        if (!showAll) sql += " AND type = ?";
        sql += " ORDER BY date";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setObject(1, AuthService.getUserId());
            if (!showAll) stmt.setString(2, type.name());

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    printTransaction(rs);
                }
            }
        }
    }

    private void executeYearlyQuery(int year, boolean showAll, TransactionType type) throws Exception {
        String sql = "SELECT * FROM transactions WHERE user_id = ? AND EXTRACT(YEAR FROM date) = ?";
        if (!showAll) sql += " AND type = ?";
        sql += " ORDER BY date";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setObject(1, AuthService.getUserId());
            stmt.setInt(2, year);
            if (!showAll) stmt.setString(3, type.name());

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    printTransaction(rs);
                }
            }
        }
    }

    private void executeMonthlyQuery(int year, int month, boolean showAll, TransactionType type) throws Exception {
        String sql = "SELECT * FROM transactions WHERE user_id = ? AND EXTRACT(YEAR FROM date) = ? AND EXTRACT(MONTH FROM date) = ?";
        if (!showAll) sql += " AND type = ?";
        sql += " ORDER BY date";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setObject(1, AuthService.getUserId());
            stmt.setInt(2, year);
            stmt.setInt(3, month);
            if (!showAll) stmt.setString(4, type.name());

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    printTransaction(rs);
                }
            }
        }
    }

    private void executeWeeklyQuery(int year, boolean showAll, TransactionType type) throws Exception {
        String sql = """
            SELECT *,
                   EXTRACT(WEEK FROM date) AS week_num,
                   date_trunc('week', date)::date AS week_start
            FROM transactions
            WHERE user_id = ? AND EXTRACT(YEAR FROM date) = ?
        """;

        if (!showAll) sql += " AND type = ?";
        sql += " ORDER BY week_num, date";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setObject(1, AuthService.getUserId());
            stmt.setInt(2, year);
            if (!showAll) stmt.setString(3, type.name());

            try (ResultSet rs = stmt.executeQuery()) {
                int currentWeek = -1;

                while (rs.next()) {
                    int week = rs.getInt("week_num");
                    LocalDate start = rs.getDate("week_start").toLocalDate();
                    LocalDate end = start.plusDays(6);

                    if (week != currentWeek) {
                        System.out.printf("%nWeek %d (%s - %s)%n", week, start, end);
                        currentWeek = week;
                    }

                    printTransaction(rs);
                }
            }
        }
    }

    private void executeDailyQuery(LocalDate date, boolean showAll, TransactionType type) throws Exception {
        String sql = "SELECT * FROM transactions WHERE user_id = ? AND date = ?";
        if (!showAll) sql += " AND type = ?";
        sql += " ORDER BY date";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setObject(1, AuthService.getUserId());
            stmt.setDate(2, java.sql.Date.valueOf(date));
            if (!showAll) stmt.setString(3, type.name());

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    printTransaction(rs);
                }
            }
        }
    }

    private void printTransaction(ResultSet rs) throws Exception {
        UUID id = UUID.fromString(rs.getString("id"));
        UUID userId = UUID.fromString(rs.getString("user_id"));
        double amount = rs.getDouble("amount");
        LocalDate date = rs.getDate("date").toLocalDate();
        TransactionType type = TransactionType.valueOf(rs.getString("type"));

        System.out.println(new Transaction(id, userId, amount, date, type));
    }
}
