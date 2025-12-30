package me.code.commands;

import me.code.models.UserTransactionStats;
import me.code.services.AuthService;
import me.code.services.ITransactionService;

public class ShowTransactionStatsCommand extends Command {

    public ShowTransactionStatsCommand(ITransactionService transactionService) {
        super("show-stats", "Show transaction statistics", transactionService);
    }

    @Override
    public void execute() {
        try {
            UserTransactionStats stats = transactionService.getUserTransactionStats(AuthService.getUserId());
            System.out.println(stats);

        } catch (Exception exception) {
            exception.printStackTrace();
            System.out.println("Something went wrong while fetching statistics!");
        }
    }
}