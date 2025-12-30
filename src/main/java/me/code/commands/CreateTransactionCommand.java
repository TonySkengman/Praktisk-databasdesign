package me.code.commands;

import me.code.models.Transaction;
import me.code.models.TransactionType;
import me.code.services.AuthService; // ADD THIS
import me.code.services.ITransactionService;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Scanner;

public class CreateTransactionCommand extends Command {

    public CreateTransactionCommand(ITransactionService transactionService) {
        super("create-transaction", "Create and save a new transaction", transactionService);
    }

    @Override
    public void execute() {
        Scanner scanner = new Scanner(System.in);
        TransactionType type = null;
        while (type == null) {
            System.out.print("Enter transaction type (income/expense): ");
            String input = scanner.nextLine().trim().toUpperCase();
            try {
                type = TransactionType.valueOf(input);
            } catch (IllegalArgumentException e) {
                System.out.println("Invalid type. Please enter either 'income' or 'expense'.");
            }
        }

        double amount;
        while (true) {
            System.out.print("Enter amount: ");
            String input = scanner.nextLine().trim();
            try {
                amount = Double.parseDouble(input);

                if (type == TransactionType.INCOME && amount < 0) {
                    System.out.println("Income amount cannot be negative! Try again.");
                    continue;
                }
                if (type == TransactionType.EXPENSE && amount > 0) {
                    System.out.println("Expense amount cannot be positive! Try again.");
                    continue;
                }

                break;
            } catch (NumberFormatException e) {
                System.out.println("Invalid input! Please enter a number.");
            }
        }

        LocalDate date;
        while (true) {
            System.out.print("Enter a date (YYYY-MM-DD): ");
            String dateString = scanner.nextLine();

            try {
                date = LocalDate.parse(dateString);
                break;
            } catch (DateTimeParseException ignored) {
                System.out.println("Invalid date format, try again.");
            }
        }

        Transaction transaction = new Transaction(AuthService.getUserId(), amount, date, type); // ADD AuthService.getUserId()
        try {
            transactionService.createTransaction(transaction);
            System.out.println("Transaction amount: " + transaction.getFormattedAmount() + " has been saved!");
        } catch (Exception exception) {
            System.out.println("An error occurred, message: " + exception.getMessage());
        }
    }
}