package me.code.models;

public class UserTransactionStats {
    private final String username;
    private final int totalTransactions;
    private final double totalIncome;
    private final double totalExpense;
    private final double netAmount;

    public UserTransactionStats(String username, int totalTransactions, double totalIncome, double totalExpense) {
        this.username = username;
        this.totalTransactions = totalTransactions;
        this.totalIncome = totalIncome;
        this.totalExpense = totalExpense;
        this.netAmount = totalIncome + totalExpense; // expenses are negative
    }

    public String getUsername() {
        return username;
    }

    public int getTotalTransactions() {
        return totalTransactions;
    }

    public double getTotalIncome() {
        return totalIncome;
    }

    public double getTotalExpense() {
        return totalExpense;
    }

    public double getNetAmount() {
        return netAmount;
    }

    @Override
    public String toString() {
        return String.format("""
            === Transaction Statistics ===
            Username: %s
            Total Transactions: %d
            Total Income: %.2f kr
            Total Expenses: %.2f kr
            Net Amount: %.2f kr
            """, username, totalTransactions, totalIncome, Math.abs(totalExpense), netAmount);
    }
}