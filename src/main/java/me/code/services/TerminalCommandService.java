package me.code.services;

import me.code.commands.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;


public class TerminalCommandService implements ICommandService {

    private final List<Command> commands = new ArrayList<>();

    public void start() {
        System.out.println("=== PERSONAL FINANCE ===");
        System.out.println("Welcome! Choose from the following commands:");

        for (Command command : commands) {
            System.out.println(command);
        }

        System.out.println("exit - Exit the application");

        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.print("Enter command: ");
            String commandInput = scanner.nextLine();

            if (commandInput.equalsIgnoreCase("exit")) {
                System.out.println("You have exited the application.");
                return;
            }

            try {
                executeCommand(commandInput);
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        }
    }

    @Override
    public void registerCommand(Command command) {
        this.commands.add(command);
    }


    @Override
    public void executeCommand(String commandInput) {

        for (Command command : commands) {
            if (command.getName().equalsIgnoreCase(commandInput)) {
                command.execute();
                return;
            }
        }

        System.out.println("The command does not exist, try again!");
    }
}
