package me.code.models;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public class Transaction {

    public static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    public static final String AMOUNT_FORMAT = "%.2f";

    private final UUID id;
    private double amount;
    private LocalDate date;
    private TransactionType type;

    public Transaction(double amount, LocalDate date, TransactionType type) {
        this.id = UUID.randomUUID();
        this.amount = amount;
        this.date = date;
        this.type = type;
    }

    public Transaction(UUID id, double amount, LocalDate date, TransactionType type) {
        this.id = id;
        this.amount = amount;
        this.date = date;
        this.type = type;
    }

    @Override
    public String toString() {
        return  "Amount: " + getFormattedAmount() + "kr\n    " +
                "Id: " + this.id + "\n    " +
                "Type: " + this.type.getDisplayName() + "\n    " +
                "Date: " + this.date.format(DATE_FORMAT) + "\n    ";
    }

    public String getFormattedAmount() {
        return String.format(AMOUNT_FORMAT, amount);
    }

    public double getAmount() {
        return amount;
    }

    public LocalDate getDate() {
        return date;
    }

    public TransactionType getType() {
        return type;
    }

    public void setTransactionType(TransactionType type) {
        this.type = type;
    }

    public UUID getId() {
        return id;
    }
}