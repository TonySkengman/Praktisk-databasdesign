package me.code.commands;

import me.code.services.AuthService;
import me.code.services.ITransactionService;

public class ShowBalanceCommand extends Command {

    public ShowBalanceCommand(ITransactionService transactionService) {
        super("show-balance", "Display current balance.", transactionService);
    }

    @Override
    public void execute() {
        try {
            double balance = transactionService.calculateBalance();
            String username = transactionService.getCurrentUsername();

            System.out.println("=== CURRENT BALANCE ===");
            System.out.println("User: " + username);
            System.out.printf("Balance: %.2f kr%n", balance);

        } catch (Exception exception) {
            exception.printStackTrace();
            System.out.println("Something went wrong while calculating balance!");
        }
    }
}