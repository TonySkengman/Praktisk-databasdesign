package me.code;

import me.code.commands.*;
import me.code.repositories.PostgreTransactionRepository;
import me.code.repositories.PostgresUserRepository;
import me.code.services.*;

import java.sql.SQLException;

public class Main {
    static void main(String[] args) {

        String dbUrl = System.getenv("DATABASE_URL");
        String dbUser = System.getenv("DATABASE_USER");
        String dbPassword = System.getenv("DATABASE_PASSWORD");

        boolean missingEnvVars = false;
        if (dbUrl == null || dbUrl.isBlank()) {
            System.out.println("Missing env var 'DATABASE_URL'");
            missingEnvVars = true;
        }


        if (dbUser == null || dbUser.isBlank()) {
            System.out.println("Missing env var 'DATABASE_USER'");
            missingEnvVars = true;
        }


        if (dbPassword == null || dbPassword.isBlank()) {
            System.out.println("Missing env var 'DATABASE_PASSWORD'");
            missingEnvVars = true;
        }

        if (missingEnvVars) {
            return;
        }

        PostgreTransactionRepository transactionRepository;
        PostgresUserRepository userRepository;

        try {
            transactionRepository = new PostgreTransactionRepository(dbUrl, dbUser, dbPassword);
            userRepository = new PostgresUserRepository(dbUrl, dbUser, dbPassword);
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Failed to connect to the database. Exiting.");
            return;
        }

        ITransactionService transactionService = new DefaultTransactionService(transactionRepository);
        IUserService userService = new DefaultUserService(userRepository);
        ICommandService commandService = new TerminalCommandService();

        commandService.registerCommand(new LoginCommand(transactionService, userService));
        commandService.registerCommand(new LogoutCommand(transactionService));
        commandService.registerCommand(new CreateUserCommand(transactionService, userService));
        commandService.registerCommand(new CreateTransactionCommand(transactionService));
        commandService.registerCommand(new DeleteTransactionCommand(transactionService));
        commandService.registerCommand(new ListTransactionsCommand(transactionService, transactionRepository.getConnection()));
        commandService.registerCommand(new ShowBalanceCommand(transactionService));
        commandService.registerCommand(new ShowTransactionStatsCommand(transactionService));

        if (commandService instanceof TerminalCommandService service) {
            service.start();
        }
    }
}