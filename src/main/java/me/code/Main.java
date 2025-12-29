package me.code;

import me.code.commands.CreateTransactionCommand;
import me.code.commands.DeleteTransactionCommand;
import me.code.commands.ListTransactionsCommand;
import me.code.commands.ShowBalanceCommand;
import me.code.repositories.PostgreTransactionRepository;
import me.code.services.DefaultTransactionService;
import me.code.services.ICommandService;
import me.code.services.ITransactionService;
import me.code.services.TerminalCommandService;

import java.sql.SQLException;

public class Main {
    static void main(String[] args) {

        String dbUrl = System.getenv("DATABASE_URL");
        String dbUser = System.getenv("DATABASE_USER");
        String dbPassword = System.getenv("DATABASE_PASSWORD");

        ICommandService commandService = new TerminalCommandService();
        ITransactionService transactionService;
        PostgreTransactionRepository repository;

        try {
            repository = new PostgreTransactionRepository(dbUrl, dbUser, dbPassword);
            transactionService = new DefaultTransactionService(repository);
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Failed to connect to the database. Exiting.");
            return;
        }

        commandService.registerCommand(new CreateTransactionCommand(transactionService));
        commandService.registerCommand(new DeleteTransactionCommand(transactionService));
        commandService.registerCommand(new ListTransactionsCommand(transactionService, repository.getConnection()));
        commandService.registerCommand(new ShowBalanceCommand(transactionService));

        if (commandService instanceof TerminalCommandService service) {
            service.start();
        }
    }
}