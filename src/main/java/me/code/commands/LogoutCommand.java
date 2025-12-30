package me.code.commands;

import me.code.services.AuthService;
import me.code.services.ITransactionService;

public class LogoutCommand extends Command {

    public LogoutCommand(ITransactionService transactionService) {
        super("logout", "Logout from your user", transactionService);
    }

    @Override
    public void execute() {
        if (AuthService.getUserId() == null) {
            System.out.println("You are not logged in.");
        } else {
            AuthService.setUserId(null);
            System.out.println("You have logged out.");
        }
    }
}