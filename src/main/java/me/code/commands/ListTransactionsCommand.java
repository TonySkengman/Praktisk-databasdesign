package me.code.commands;

import me.code.models.Transaction;
import me.code.models.TransactionType;
import me.code.services.ITransactionService;

import java.time.LocalDate;
import java.time.temporal.IsoFields;
import java.time.temporal.WeekFields;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;

public class ListTransactionsCommand extends Command {

    public ListTransactionsCommand(ITransactionService transactionService) {
        super("list-transactions", "List all saved transactions", transactionService);
    }

    @Override
    public void execute() {
        List<Transaction> transactions;
        try {
            transactions = transactionService.getTransactions().toList();
        } catch (Exception exception) {
            exception.printStackTrace();
            System.out.println("Something went wrong!");
            return;
        }

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

        switch (period) {
            case "overall":
                if (transactions.isEmpty()) {
                    System.out.println("No transactions recorded yet.");
                    return;
                }

                System.out.println("\n--- Showing all transactions ---");
                for (Transaction t : transactions) {
                    if (!showAll && t.getType() != type) continue;
                    System.out.println(t);
                }
                break;

            case "yearly":
                boolean foundYear = false;
                for (Transaction t : transactions) {
                    if (!showAll && t.getType() != type) continue;
                    if (t.getDate().getYear() == year) {
                        System.out.println(t);
                        foundYear = true;
                    }
                }
                if (!foundYear)
                    System.out.println("No transactions found for " + year + ".");
                break;

            case "monthly":
                boolean foundMonth = false;
                for (Transaction t : transactions) {
                    if (!showAll && t.getType() != type) continue;
                    if (t.getDate().getYear() == year && t.getDate().getMonthValue() == month) {
                        System.out.println(t);
                        foundMonth = true;
                    }
                }
                if (!foundMonth)
                    System.out.println("No transactions found for " + year + "-" + month + ".");
                break;

            case "weekly":
                WeekFields weekFields = WeekFields.of(Locale.getDefault());
                boolean anyWeekTransaction = false;

                for (int w = 1; w <= 53; w++) {
                    boolean weekHasTransaction = false;

                    LocalDate weekStart;
                    LocalDate weekEnd;
                    try {
                        weekStart = LocalDate.of(weekYear, 1, 1)
                                .with(weekFields.weekOfYear(), w)
                                .with(weekFields.dayOfWeek(), 1);
                        weekEnd = weekStart.plusDays(6);
                    } catch (java.time.DateTimeException e) {
                        continue;
                    }

                    for (Transaction t2 : transactions) {
                        if (!showAll && t2.getType() != type) continue;

                        LocalDate tDate = t2.getDate();
                        int tWeek = tDate.get(weekFields.weekOfYear());
                        int tYear = tDate.get(IsoFields.WEEK_BASED_YEAR);

                        if (tWeek == w && tYear == weekYear) {
                            if (!weekHasTransaction) {
                                System.out.printf("\nWeek %d (%s - %s):%n", w, weekStart, weekEnd);
                                weekHasTransaction = true;
                                anyWeekTransaction = true;
                            }
                            System.out.println("  " + t2);
                        }
                    }
                }

                if (!anyWeekTransaction)
                    System.out.println("No transactions found for year " + weekYear + ".");
                break;

            case "daily":
                boolean foundDay = false;
                for (Transaction t : transactions) {
                    if (!showAll && t.getType() != type) continue;
                    if (t.getDate().equals(date)) {
                        System.out.println(t);
                        foundDay = true;
                    }
                }
                if (!foundDay)
                    System.out.println("No transactions found for " + date + ".");
                break;
        }
    }
}