package me.code.commands;

import me.code.models.User;
import me.code.services.AuthService;
import me.code.services.ITransactionService;
import me.code.services.IUserService;

import java.util.Optional;
import java.util.Scanner;

public class LoginCommand extends Command {

    private final IUserService userService;

    public LoginCommand(ITransactionService transactionService, IUserService userService) {
        super("login", "Login with username and password", transactionService);
        this.userService = userService;
    }

    @Override
    public void execute() {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Username: ");
        String username = scanner.nextLine();
        System.out.print("Password: ");
        String password = scanner.nextLine();

        Optional<User> optional;
        try {
            optional = userService.checkUserLogin(username, password);
        } catch (Exception exception) {
            System.out.println("An error occurred, try again later.");
            exception.printStackTrace();
            return;
        }

        if (optional.isEmpty()) {
            System.out.println("Incorrect username or password.");
            return;
        }

        User user = optional.get();
        AuthService.setUserId(user.getId());
        System.out.println("You have logged in as '" + user.getUsername() + "'");
    }
}