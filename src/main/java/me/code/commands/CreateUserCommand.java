package me.code.commands;

import me.code.services.ITransactionService;
import me.code.services.IUserService;

import java.util.Scanner;

public class CreateUserCommand extends Command {

    private final IUserService userService;

    public CreateUserCommand(ITransactionService transactionService, IUserService userService) {
        super("create-user", "Create a new user account", transactionService);
        this.userService = userService;
    }

    @Override
    public void execute() {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Username: ");
        String username = scanner.nextLine();
        if (username == null || username.isBlank()) {
            System.out.println("Username may not be blank.");
            return;
        }

        System.out.print("Password: ");
        String password = scanner.nextLine();
        if (password == null || password.isBlank()) {
            System.out.println("Password may not be blank.");
            return;
        }

        if (password.length() < 5) {
            System.out.println("Password must be at least 5 characters.");
            return;
        }

        try {
            userService.createUser(username, password);
            System.out.println("User created!");
        } catch (Exception exception) {
            if (exception.getMessage().contains("already exists")) {
                System.out.println("A user with that username already exists, try another one.");
            } else {
                System.out.println("An error occurred, try again later");
                exception.printStackTrace();
            }
        }
    }
}