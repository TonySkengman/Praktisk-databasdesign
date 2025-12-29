package me.code.commands;

import me.code.models.Transaction;
import me.code.models.TransactionType;
import me.code.services.ITransactionService;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.temporal.WeekFields;
import java.util.Locale;
import java.util.Scanner;
import java.util.UUID;

public class ListTransactionsCommand extends Command {
    private Connection connection;

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
                case "income":
                    type = TransactionType.INCOME;
                    break;
                case "expense":
                    type = TransactionType.EXPENSE;
                    break;
                case "all":
                    showAll = true;
                    break;
                default:
                    System.out.println("Invalid input. Please enter 'income', 'expense', or 'all'.");
            }
        }

        System.out.println("Select period: (overall/yearly/monthly/weekly/daily).");
        String period = scanner.nextLine().trim().toLowerCase();

        int year = 0, month = 0;
        LocalDate date = null;
        int weekYear = 0;

        switch (period) {
            case "yearly":
                while (true) {
                    System.out.print("Enter year: ");
                    try {
                        year = Integer.parseInt(scanner.nextLine().trim());
                        break;
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid year. Please enter a valid number.");
                    }
                }
                break;
            case "monthly":
                while (true) {
                    System.out.print("Enter year: ");
                    try {
                        year = Integer.parseInt(scanner.nextLine().trim());
                        break;
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid year. Please enter a valid number.");
                    }
                }
                while (true) {
                    System.out.print("Enter month (1-12): ");
                    try {
                        month = Integer.parseInt(scanner.nextLine().trim());
                        if (month < 1 || month > 12) {
                            System.out.println("Month must be between 1 and 12.");
                            continue;
                        }
                        break;
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid month. Please enter a number between 1 and 12.");
                    }
                }
                break;
            case "weekly":
                while (true) {
                    System.out.print("Enter year: ");
                    try {
                        weekYear = Integer.parseInt(scanner.nextLine().trim());
                        break;
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid year. Please enter a valid number.");
                    }
                }
                break;
            case "daily":
                while (true) {
                    System.out.print("Enter date (YYYY-MM-DD): ");
                    try {
                        date = LocalDate.parse(scanner.nextLine().trim());
                        break;
                    } catch (java.time.format.DateTimeParseException e) {
                        System.out.println("Invalid date format. Please use YYYY-MM-DD format.");
                    }
                }
                break;
            case "overall":
                break;
            default:
                System.out.println("Unknown period. Exiting.");
                return;
        }

        try {
            switch (period) {
                case "overall":
                    executeOverallQuery(showAll, type);
                    break;
                case "yearly":
                    executeYearlyQuery(year, showAll, type);
                    break;
                case "monthly":
                    executeMonthlyQuery(year, month, showAll, type);
                    break;
                case "weekly":
                    executeWeeklyQuery(weekYear, showAll, type);
                    break;
                case "daily":
                    executeDailyQuery(date, showAll, type);
                    break;
            }
        } catch (Exception exception) {
            exception.printStackTrace();
            System.out.println("Something went wrong!");
        }
    }

    private void executeOverallQuery(boolean showAll, TransactionType type) throws Exception {
        String sql = "SELECT * FROM transactions";
        if (!showAll) {
            sql += " WHERE type = ?";
        }
        sql += " ORDER BY date";

        PreparedStatement stmt = connection.prepareStatement(sql);
        if (!showAll) {
            stmt.setString(1, type.toString());
        }

        ResultSet rs = stmt.executeQuery();
        boolean hasResults = false;

        System.out.println("\n--- Showing all transactions ---");
        while (rs.next()) {
            hasResults = true;
            printTransaction(rs);
        }

        if (!hasResults) {
            System.out.println("No transactions recorded yet.");
        }

        rs.close();
        stmt.close();
    }

    private void executeYearlyQuery(int year, boolean showAll, TransactionType type) throws Exception {
        String sql = "SELECT * FROM transactions WHERE EXTRACT(YEAR FROM date) = ?";
        if (!showAll) {
            sql += " AND type = ?";
        }
        sql += " ORDER BY date";

        PreparedStatement stmt = connection.prepareStatement(sql);
        stmt.setInt(1, year);
        if (!showAll) {
            stmt.setString(2, type.toString());
        }

        ResultSet rs = stmt.executeQuery();
        boolean hasResults = false;

        while (rs.next()) {
            hasResults = true;
            printTransaction(rs);
        }

        if (!hasResults) {
            System.out.println("No transactions found for " + year + ".");
        }

        rs.close();
        stmt.close();
    }

    private void executeMonthlyQuery(int year, int month, boolean showAll, TransactionType type) throws Exception {
        String sql = "SELECT * FROM transactions WHERE EXTRACT(YEAR FROM date) = ? AND EXTRACT(MONTH FROM date) = ?";
        if (!showAll) {
            sql += " AND type = ?";
        }
        sql += " ORDER BY date";

        PreparedStatement stmt = connection.prepareStatement(sql);
        stmt.setInt(1, year);
        stmt.setInt(2, month);
        if (!showAll) {
            stmt.setString(3, type.toString());
        }

        ResultSet rs = stmt.executeQuery();
        boolean hasResults = false;

        while (rs.next()) {
            hasResults = true;
            printTransaction(rs);
        }

        if (!hasResults) {
            System.out.println("No transactions found for " + year + "-" + month + ".");
        }

        rs.close();
        stmt.close();
    }

    private void executeWeeklyQuery(int weekYear, boolean showAll, TransactionType type) throws Exception {
        String sql = "SELECT *, EXTRACT(WEEK FROM date) as week_num, " +
                "date_trunc('week', date)::date as week_start " +
                "FROM transactions " +
                "WHERE EXTRACT(YEAR FROM date) = ?";
        if (!showAll) {
            sql += " AND type = ?";
        }
        sql += " ORDER BY week_num, date";

        PreparedStatement stmt = connection.prepareStatement(sql);
        stmt.setInt(1, weekYear);
        if (!showAll) {
            stmt.setString(2, type.toString());
        }

        ResultSet rs = stmt.executeQuery();
        boolean anyWeekTransaction = false;
        int currentWeek = -1;

        while (rs.next()) {
            int weekNum = rs.getInt("week_num");
            LocalDate weekStart = rs.getDate("week_start").toLocalDate();
            LocalDate weekEnd = weekStart.plusDays(6);

            if (weekNum != currentWeek) {
                System.out.printf("\nWeek %d (%s - %s):%n", weekNum, weekStart, weekEnd);
                currentWeek = weekNum;
                anyWeekTransaction = true;
            }

            System.out.print("  ");
            printTransaction(rs);
        }

        if (!anyWeekTransaction) {
            System.out.println("No transactions found for year " + weekYear + ".");
        }

        rs.close();
        stmt.close();
    }

    private void executeDailyQuery(LocalDate date, boolean showAll, TransactionType type) throws Exception {
        String sql = "SELECT * FROM transactions WHERE date = ?";
        if (!showAll) {
            sql += " AND type = ?";
        }
        sql += " ORDER BY date";

        PreparedStatement stmt = connection.prepareStatement(sql);
        stmt.setDate(1, java.sql.Date.valueOf(date));
        if (!showAll) {
            stmt.setString(2, type.toString());
        }

        ResultSet rs = stmt.executeQuery();
        boolean hasResults = false;

        while (rs.next()) {
            hasResults = true;
            printTransaction(rs);
        }

        if (!hasResults) {
            System.out.println("No transactions found for " + date + ".");
        }

        rs.close();
        stmt.close();
    }

    private void printTransaction(ResultSet rs) throws Exception {
        UUID id = UUID.fromString(rs.getString("id"));
        String typeStr = rs.getString("type");
        TransactionType type = TransactionType.valueOf(typeStr);
        double amount = rs.getDouble("amount");
        LocalDate date = rs.getDate("date").toLocalDate();

        Transaction transaction = new Transaction(id, amount, date, type);
        System.out.println(transaction);
    }
}